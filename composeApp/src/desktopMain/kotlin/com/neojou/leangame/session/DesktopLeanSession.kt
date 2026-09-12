package com.neojou.leangame.session

import com.neojou.leangame.proof.PlayFile
import com.neojou.leangame.proof.ProofState
import com.neojou.leangame.proof.ProofStep
import com.neojou.leangame.proof.diagnosticIsUnsolvedGoals
import com.neojou.leangame.proof.normalizeTacticLine
import com.neojou.leangame.proof.parsePlainGoals
import com.neojou.leangame.proof.tacticBlockedReason
import com.neojou.tools.LogLevel
import com.neojou.tools.MyLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import java.nio.file.Path
import kotlin.io.path.absolutePathString

private const val TAG = "LeanSession"

class DesktopLeanSession(
    private val scope: CoroutineScope,
) : LeanSession {
    private val _proof = MutableStateFlow(ProofState.connecting())
    private val _connection = MutableStateFlow(ConnectionInfo.connecting())
    override val proof: StateFlow<ProofState> = _proof.asStateFlow()
    override val connection: StateFlow<ConnectionInfo> = _connection.asStateFlow()

    private val mutex = Mutex()
    private var process: Process? = null
    private var client: LspClient? = null
    private var uri: String? = null
    private var docVersion: Int = 1
    private var commands: List<String> = emptyList()
    @Volatile private var diagnostics: List<LspDiagnostic> = emptyList()
    @Volatile private var diagnosticsVersion: Int = 0
    @Volatile private var fileProgressIdle: Boolean = true
    private var stderrJob: Job? = null

    override fun start() {
        scope.launch {
            mutex.withLock { boot() }
        }
    }

    override fun submit(command: String) {
        val blocked = tacticBlockedReason(command)
        if (blocked != null) {
            _proof.update { it.copy(lastSubmitError = blocked) }
            return
        }
        val current = _proof.value
        if (!current.canSubmit) return
        val normalized = normalizeTacticLine(command)
        if (normalized.isEmpty()) return
        scope.launch {
            mutex.withLock {
                _proof.update { it.copy(busy = true, lastSubmitError = null) }
                try {
                    val attempted = commands + normalized
                    val result = pushAndQuery(attempted)
                    if (result.tacticFailed) {
                        pushAndQuery(commands)
                        _proof.update {
                            it.copy(
                                busy = false,
                                lastSubmitError = result.errorText ?: "這一步 Lean 不接受。",
                            )
                        }
                    } else {
                        commands = attempted
                        _proof.value = result.toState(busy = false)
                    }
                } catch (e: Exception) {
                    MyLog.add(TAG, "submit failed: ${e.message}", LogLevel.ERROR)
                    _proof.update {
                        it.copy(busy = false, lastSubmitError = e.message ?: "送出失敗")
                    }
                    _connection.update { it.copy(lastLspError = e.message) }
                }
            }
        }
    }

    override fun undo() {
        if (commands.isEmpty() || _proof.value.busy) return
        scope.launch {
            mutex.withLock {
                _proof.update { it.copy(busy = true, lastSubmitError = null) }
                try {
                    commands = commands.dropLast(1)
                    _proof.value = pushAndQuery(commands).toState(busy = false)
                } catch (e: Exception) {
                    _proof.update { it.copy(busy = false, lastSubmitError = e.message) }
                }
            }
        }
    }

    override fun restart() {
        restartFrom(0)
    }

    override fun restartFrom(stepIndex: Int) {
        scope.launch {
            mutex.withLock {
                _proof.update { it.copy(busy = true, lastSubmitError = null) }
                try {
                    commands = commands.take(stepIndex.coerceIn(0, commands.size))
                    _proof.value = pushAndQuery(commands).toState(busy = false)
                } catch (e: Exception) {
                    _proof.update { it.copy(busy = false, lastSubmitError = e.message) }
                }
            }
        }
    }

    override fun close() {
        runCatching { client?.notify("exit", JsonNull) }
        process?.destroy()
        stderrJob?.cancel()
    }

    private suspend fun boot() {
        _connection.value = ConnectionInfo.connecting()
        _proof.value = ProofState.connecting()
        val lake = LeanPaths.findExecutable("lake")
        val lean = LeanPaths.findExecutable("lean")
        val project = LeanPaths.findNngProject()
        val expected = project?.let { LeanPaths.readToolchain(it) } ?: LeanPaths.EXPECTED_TOOLCHAIN
        val version = lean?.let { LeanPaths.runVersion(it) }
        if (project == null) {
            failMissing(
                lake, lean, version, expected,
                "找不到 lean/nng_level8。請在 repo 根目錄執行 Gradle（user.dir 要能往上找到 lakefile.toml）。",
            )
            return
        }
        if (lake == null && lean == null) {
            failMissing(
                null, null, version, expected,
                "找不到 `lake` 或 `lean`。請安裝 elan（https://lean-lang.org/install/），確認 ~/.elan/bin 在 PATH，然後重新啟動。",
            )
            return
        }
        val mismatch = version != null && "4.33.1" !in version
        try {
            startProcess(project, lake, lean)
            val lsp = client ?: error("LSP client 未建立")
            val rootUri = project.toUriString()
            uri = project.resolve(PlayFile.FILE_NAME).toUriString()
            val init = lsp.request(
                "initialize",
                buildJsonObject {
                    put("processId", ProcessHandle.current().pid().toInt())
                    put("rootUri", rootUri)
                    putJsonObject("capabilities") {
                        putJsonObject("textDocument") {
                            putJsonObject("publishDiagnostics") { put("relatedInformation", true) }
                            putJsonObject("synchronization") { put("didSave", true) }
                        }
                    }
                    putJsonArray("workspaceFolders") {
                        add(
                            buildJsonObject {
                                put("uri", rootUri)
                                put("name", "nng_level8")
                            },
                        )
                    }
                },
                timeoutMs = 45_000,
            )
            MyLog.add(TAG, "initialize: ${init.toString().take(200)}", LogLevel.DEBUG)
            lsp.notify("initialized", buildJsonObject {})
            docVersion = 1
            commands = emptyList()
            val text = PlayFile.render(commands)
            lsp.notify(
                "textDocument/didOpen",
                buildJsonObject {
                    putJsonObject("textDocument") {
                        put("uri", uri!!)
                        put("languageId", "lean4")
                        put("version", docVersion)
                        put("text", text)
                    }
                },
            )
            val snapshot = pushAndQuery(emptyList(), alreadyPushed = true)
            val detail = buildString {
                append("已連上")
                append(if (lake != null) " lake serve" else " lean --server")
                if (mismatch) {
                    append("。注意：此關卡 toolchain 是 $expected，本機是 $version。若編譯失敗請執行 elan toolchain install $expected")
                }
            }
            _connection.value = ConnectionInfo(
                status = ConnectionStatus.Ready,
                detailZh = detail,
                leanVersion = version,
                toolchainExpected = expected,
                toolchainActual = version,
                lakePath = lake?.absolutePathString(),
                leanPath = lean?.absolutePathString(),
                mode = if (lake != null) "lake serve" else "lean --server",
                projectDir = project.absolutePathString(),
            )
            _proof.value = snapshot.toState(busy = false)
        } catch (e: Exception) {
            MyLog.add(TAG, "boot failed: ${e.message}", LogLevel.ERROR)
            _connection.value = ConnectionInfo(
                status = ConnectionStatus.Error,
                detailZh = "Lean 連線失敗：${e.message ?: "未知錯誤"}",
                leanVersion = version,
                toolchainExpected = expected,
                toolchainActual = version,
                lakePath = lake?.absolutePathString(),
                leanPath = lean?.absolutePathString(),
                lastLspError = e.message,
                projectDir = project.absolutePathString(),
            )
            _proof.update {
                it.copy(
                    busy = false,
                    serverError = e.message ?: "Lean 連線失敗",
                )
            }
        }
    }

    private fun failMissing(
        lake: Path?,
        lean: Path?,
        version: String?,
        expected: String,
        message: String,
    ) {
        _connection.value = ConnectionInfo(
            status = ConnectionStatus.MissingToolchain,
            detailZh = message,
            leanVersion = version,
            toolchainExpected = expected,
            toolchainActual = version,
            lakePath = lake?.absolutePathString(),
            leanPath = lean?.absolutePathString(),
        )
        _proof.value = ProofState(
            steps = emptyList(),
            completed = false,
            busy = false,
            serverError = message,
        )
    }

    private fun startProcess(project: Path, lake: Path?, lean: Path?) {
        val envPath = LeanPaths.augmentedPath()
        val command: List<String> = when {
            lake != null -> listOf(lake.absolutePathString(), "serve")
            lean != null -> listOf(lean.absolutePathString(), "--server")
            else -> error("no lean")
        }
        val pb = ProcessBuilder(command).directory(project.toFile())
        pb.environment()["PATH"] = envPath
        pb.redirectErrorStream(false)
        val proc = pb.start()
        process = proc
        stderrJob = scope.launch(Dispatchers.IO) {
            proc.errorStream.bufferedReader().useLines { lines ->
                lines.forEach { MyLog.add(TAG, "stderr $it", LogLevel.DEBUG) }
            }
        }
        val lsp = LspClient(
            input = proc.inputStream,
            output = proc.outputStream,
            onDiagnostics = { incomingUri, version, diags ->
                if (incomingUri == uri || uri == null) {
                    diagnostics = diags
                    if (version != null) diagnosticsVersion = version
                }
            },
            onFileProgressIdle = { fileProgressIdle = it },
            onServerError = { msg ->
                _connection.update { it.copy(lastLspError = msg) }
            },
        )
        client = lsp
        lsp.startReader(scope)
    }

    private suspend fun pushAndQuery(
        nextCommands: List<String>,
        alreadyPushed: Boolean = false,
    ): QueryResult {
        val lsp = client ?: error("尚未連上 Lean")
        val fileUri = uri ?: error("尚未打開 Play.lean")
        val text = PlayFile.render(nextCommands)
        if (!alreadyPushed || nextCommands.isNotEmpty()) {
            docVersion += 1
            lsp.notify(
                "textDocument/didChange",
                buildJsonObject {
                    putJsonObject("textDocument") {
                        put("uri", fileUri)
                        put("version", docVersion)
                    }
                    putJsonArray("contentChanges") {
                        add(buildJsonObject { put("text", text) })
                    }
                },
            )
        }
        waitUntilIdle(fileUri)
        val (line, col) = PlayFile.cursorAtEnd(nextCommands)
        val plain = runCatching {
            lsp.request(
                "$/lean/plainGoal",
                buildJsonObject {
                    putJsonObject("textDocument") { put("uri", fileUri) }
                    putJsonObject("position") {
                        put("line", line)
                        put("character", col)
                    }
                },
                timeoutMs = 20_000,
            )
        }.getOrNull()
        val goalStrings = extractGoalStrings(plain)
        val goals = parsePlainGoals(goalStrings)
        val diags = diagnostics
        val realErrors = diags.filter { it.severity <= 1 && !diagnosticIsUnsolvedGoals(it.message) }
        val tacticFailed = nextCommands.isNotEmpty() && realErrors.isNotEmpty()
        val hasUnsolved = diags.any { diagnosticIsUnsolvedGoals(it.message) }
        val completed = nextCommands.isNotEmpty() &&
            realErrors.isEmpty() &&
            goals.isEmpty() &&
            !hasUnsolved &&
            !tacticFailed
        val errorText = realErrors.joinToString("\n") { it.message }.ifBlank { null }
        return QueryResult(
            commands = nextCommands,
            goals = goals,
            diagnostics = diags.map { it.message },
            playFileText = text,
            completed = completed,
            tacticFailed = tacticFailed,
            errorText = errorText,
        )
    }

    private suspend fun waitUntilIdle(fileUri: String) {
        val lsp = client ?: return
        withContext(Dispatchers.IO) {
            runCatching {
                lsp.request(
                    "textDocument/waitForDiagnostics",
                    buildJsonObject {
                        put("uri", fileUri)
                        put("version", docVersion)
                    },
                    timeoutMs = 60_000,
                )
            }
        }
        withTimeoutOrNull(15_000) {
            while (!fileProgressIdle || diagnosticsVersion < docVersion) delay(40)
        }
        delay(40)
    }

    private fun extractGoalStrings(result: kotlinx.serialization.json.JsonElement?): List<String> {
        if (result == null || result is JsonNull) return emptyList()
        val obj = result as? JsonObject ?: return emptyList()
        val arr = obj["goals"] as? JsonArray ?: return emptyList()
        return arr.mapNotNull { it.jsonPrimitive.contentOrNull }
    }

    private data class QueryResult(
        val commands: List<String>,
        val goals: List<com.neojou.leangame.proof.Goal>,
        val diagnostics: List<String>,
        val playFileText: String,
        val completed: Boolean,
        val tacticFailed: Boolean,
        val errorText: String?,
    ) {
        fun toState(busy: Boolean): ProofState {
            val initial = ProofStep(command = "", goals = if (commands.isEmpty()) goals else emptyList(), diagnostics = emptyList())
            val rest = if (commands.isEmpty()) {
                emptyList()
            } else {
                commands.dropLast(1).map { ProofStep(it, emptyList(), emptyList()) } +
                    ProofStep(commands.last(), goals, diagnostics)
            }
            val steps = listOf(initial) + rest
            return ProofState(
                steps = steps,
                completed = completed,
                busy = busy,
                serverError = null,
                lastSubmitError = null,
                playFileText = playFileText,
            )
        }
    }
}

private fun Path.toUriString(): String = toUri().toString()
