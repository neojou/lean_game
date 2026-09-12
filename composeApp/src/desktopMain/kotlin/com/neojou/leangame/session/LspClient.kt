package com.neojou.leangame.session

import com.neojou.tools.LogLevel
import com.neojou.tools.MyLog
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

private const val TAG = "LspClient"

data class LspDiagnostic(
    val message: String,
    val severity: Int,
    val line: Int,
)

class LspClient(
    private val input: InputStream,
    private val output: OutputStream,
    private val onDiagnostics: (uri: String, version: Int?, diags: List<LspDiagnostic>) -> Unit,
    private val onFileProgressIdle: (Boolean) -> Unit,
    private val onServerError: (String) -> Unit,
) {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    private val nextId = AtomicInteger(1)
    private val pending = ConcurrentHashMap<Int, CompletableDeferred<JsonElement?>>()
    private val writeLock = Any()
    private val headerBuf = ArrayList<Byte>()

    fun startReader(scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            try {
                while (isActive) {
                    val body = readMessage() ?: break
                    handleMessage(body)
                }
            } catch (e: Exception) {
                MyLog.add(TAG, "reader stopped: ${e.message}", LogLevel.ERROR)
                onServerError(e.message ?: "LSP 讀取中斷")
            }
        }
    }

    suspend fun request(method: String, params: JsonElement, timeoutMs: Long = 30_000): JsonElement? {
        val id = nextId.getAndIncrement()
        val deferred = CompletableDeferred<JsonElement?>()
        pending[id] = deferred
        send(
            buildJsonObject {
                put("jsonrpc", "2.0")
                put("id", id)
                put("method", method)
                put("params", params)
            }.toString(),
        )
        return try {
            withTimeout(timeoutMs) { deferred.await() }
        } catch (e: Exception) {
            pending.remove(id)
            throw e
        }
    }

    fun notify(method: String, params: JsonElement) {
        send(
            buildJsonObject {
                put("jsonrpc", "2.0")
                put("method", method)
                put("params", params)
            }.toString(),
        )
    }

    private fun send(body: String) {
        val bytes = body.toByteArray(StandardCharsets.UTF_8)
        val header = "Content-Length: ${bytes.size}\r\n\r\n"
        synchronized(writeLock) {
            output.write(header.toByteArray(StandardCharsets.US_ASCII))
            output.write(bytes)
            output.flush()
        }
        MyLog.add(TAG, "→ ${body.take(180)}", LogLevel.DEBUG)
    }

    private fun readMessage(): String? {
        headerBuf.clear()
        var last4 = 0
        while (true) {
            val b = input.read()
            if (b < 0) return null
            headerBuf.add(b.toByte())
            last4 = ((last4 shl 8) or (b and 0xFF))
            if (last4 == 0x0D0A0D0A) break
        }
        val header = String(headerBuf.toByteArray(), StandardCharsets.US_ASCII)
        val length = HEADER_LENGTH.find(header)?.groupValues?.get(1)?.toIntOrNull()
            ?: throw IllegalStateException("LSP 訊息缺少 Content-Length")
        val body = ByteArray(length)
        var off = 0
        while (off < length) {
            val n = input.read(body, off, length - off)
            if (n < 0) return null
            off += n
        }
        return String(body, StandardCharsets.UTF_8)
    }

    private fun handleMessage(body: String) {
        MyLog.add(TAG, "← ${body.take(180)}", LogLevel.DEBUG)
        val root = json.parseToJsonElement(body).jsonObject
        val idEl = root["id"]
        val method = root["method"]?.jsonPrimitive?.contentOrNull
        when {
            method != null && idEl != null -> respondToServer(idEl, method, root["params"])
            method != null -> handleNotification(method, root["params"])
            idEl != null -> {
                val id = idEl.jsonPrimitive.intOrNull ?: return
                val deferred = pending.remove(id) ?: return
                val error = root["error"]
                if (error != null) {
                    val msg = error.jsonObject["message"]?.jsonPrimitive?.contentOrNull ?: error.toString()
                    deferred.completeExceptionally(IllegalStateException(msg))
                } else {
                    deferred.complete(root["result"])
                }
            }
        }
    }

    private fun handleNotification(method: String, params: JsonElement?) {
        when (method) {
            "textDocument/publishDiagnostics" -> {
                val obj = params as? JsonObject ?: return
                val uri = obj["uri"]?.jsonPrimitive?.contentOrNull ?: return
                val version = obj["version"]?.jsonPrimitive?.intOrNull
                val diags = obj["diagnostics"]?.jsonArray.orEmpty().mapNotNull { parseDiagnostic(it) }
                onDiagnostics(uri, version, diags)
            }
            "$/lean/fileProgress" -> {
                val processing = (params as? JsonObject)
                    ?.get("processing") as? JsonArray
                onFileProgressIdle(processing == null || processing.isEmpty())
            }
            "window/showMessage" -> {
                val msg = (params as? JsonObject)?.get("message")?.jsonPrimitive?.contentOrNull
                if (msg != null) MyLog.add(TAG, msg, LogLevel.INFO)
            }
            else -> Unit
        }
    }

    private fun parseDiagnostic(el: JsonElement): LspDiagnostic? {
        val obj = el as? JsonObject ?: return null
        val message = obj["message"]?.jsonPrimitive?.contentOrNull ?: return null
        val severity = obj["severity"]?.jsonPrimitive?.intOrNull ?: 1
        val line = obj["range"]?.jsonObject
            ?.get("start")?.jsonObject
            ?.get("line")?.jsonPrimitive?.intOrNull ?: 0
        return LspDiagnostic(message, severity, line)
    }

    private fun respondToServer(idEl: JsonElement, method: String, params: JsonElement?) {
        val result: JsonElement = when (method) {
            "workspace/configuration" -> JsonArray(emptyList())
            "client/registerCapability" -> JsonNull
            "window/workDoneProgress/create" -> JsonNull
            else -> JsonNull
        }
        send(
            buildJsonObject {
                put("jsonrpc", "2.0")
                put("id", idEl)
                put("result", result)
            }.toString(),
        )
        MyLog.add(TAG, "server request $method", LogLevel.DEBUG)
        if (params != null && method == "window/showMessageRequest") {
            onServerError(params.toString())
        }
    }

    companion object {
        private val HEADER_LENGTH = Regex("""Content-Length:\s*(\d+)""", RegexOption.IGNORE_CASE)
    }
}
