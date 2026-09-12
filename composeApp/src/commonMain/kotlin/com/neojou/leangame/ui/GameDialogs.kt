package com.neojou.leangame.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.neojou.leangame.AppVersion
import com.neojou.leangame.session.ConnectionInfo

@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    GameDialog(title = "關於", onDismiss = onDismiss) {
        Text(AppVersion.APP_NAME, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
        Text(AppVersion.APP_NAME_EN, style = MaterialTheme.typography.bodyMedium)
        Text("版本 ${AppVersion.DISPLAY}", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        Text(AppVersion.SUMMARY, style = MaterialTheme.typography.bodySmall)
        Text(
            "NNG4 ≤ 世界第 8 關 le_total，Typewriter 模式，本機 lake serve。右欄點按鈕看編譯進來的指令說明。",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
fun DiagnosticDialog(info: ConnectionInfo, onDismiss: () -> Unit) {
    GameDialog(title = "Lean 連線診斷", onDismiss = onDismiss) {
        Text("狀態：${info.detailZh}")
        Text("預期 toolchain：${info.toolchainExpected ?: "（未知）"}")
        Text("本機 Lean：${info.leanVersion ?: "找不到"}")
        Text("lake：${info.lakePath ?: "找不到"}")
        Text("lean：${info.leanPath ?: "找不到"}")
        Text("模式：${info.mode ?: "—"}")
        Text("專案目錄：${info.projectDir ?: "—"}")
        info.lastLspError?.let {
            Text("最後 LSP 錯誤：", fontWeight = FontWeight.Medium)
            Text(it, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall)
        }
        Text(
            "若缺少命令：安裝 elan（https://lean-lang.org/install/），確認 ~/.elan/bin 在 PATH，於 lean/nng_level8 執行 lake build。",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
fun PlayFileDialog(text: String, onDismiss: () -> Unit) {
    GameDialog(title = "目前 Lean 檔", onDismiss = onDismiss) {
        Text(
            text = text.ifBlank { "（尚無內容）" },
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun GameDialog(
    title: String,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .widthIn(min = 360.dp, max = 640.dp)
                .fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(20.dp).heightIn(max = 520.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Column(
                    modifier = Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    content()
                }
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("關閉")
                }
            }
        }
    }
}
