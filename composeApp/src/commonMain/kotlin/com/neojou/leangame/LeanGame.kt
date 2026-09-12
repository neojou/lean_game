package com.neojou.leangame

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.neojou.leangame.level.Level8
import com.neojou.leangame.level.matchingHiddenHints
import com.neojou.leangame.session.createLeanSession
import com.neojou.leangame.session.requestAppExit
import com.neojou.leangame.ui.AboutDialog
import com.neojou.leangame.ui.DiagnosticDialog
import com.neojou.leangame.ui.PlayFileDialog
import com.neojou.leangame.ui.StatusBar
import com.neojou.leangame.ui.pane.CenterPane
import com.neojou.leangame.ui.pane.LeftPane
import com.neojou.leangame.ui.pane.RightPane
import com.neojou.tools.LogLevel
import com.neojou.tools.MyLog
import com.neojou.tools.ui.menu.MyTopMenuBar
import com.neojou.tools.ui.menu.MyTopMenuItem

private const val TAG = "LEANGame"

private enum class OpenDialog { None, About, Diagnostic, PlayFile }

@Composable
fun LeanGame() {
    val scope = rememberCoroutineScope()
    val session = remember { createLeanSession(scope) }
    val proof by session.proof.collectAsState()
    val connection by session.connection.collectAsState()

    var draft by remember { mutableStateOf("") }
    var dialog by remember { mutableStateOf(OpenDialog.None) }
    var revealedIds by remember { mutableStateOf(setOf<String>()) }
    var noMoreHints by remember { mutableStateOf<String?>(null) }

    DisposableEffect(session) {
        session.start()
        onDispose { session.close() }
    }

    LaunchedEffect(Unit) {
        MyLog.add(TAG, "Enter level 8", LogLevel.DEBUG)
    }

    val revealedHints = Level8.hiddenHints.filter { it.id in revealedIds }
    val canSubmit = proof.canSubmit

    val topMenus = listOf(
        MyTopMenuItem(
            id = "game",
            label = "遊戲",
            children = listOf(
                MyTopMenuItem(
                    id = "restart",
                    label = "重新開始本關",
                    enabled = !proof.busy,
                    onClick = {
                        session.restart()
                        draft = ""
                        revealedIds = emptySet()
                        noMoreHints = null
                    },
                ),
                MyTopMenuItem(
                    id = "quit",
                    label = "結束",
                    onClick = { requestAppExit() },
                ),
            ),
        ),
        MyTopMenuItem(
            id = "proof",
            label = "證明",
            children = listOf(
                MyTopMenuItem(
                    id = "undo",
                    label = "復原一步",
                    enabled = !proof.busy && proof.acceptedCommands.isNotEmpty(),
                    onClick = { session.undo() },
                ),
                MyTopMenuItem(
                    id = "show-file",
                    label = "顯示目前 Lean 檔",
                    onClick = { dialog = OpenDialog.PlayFile },
                ),
            ),
        ),
        MyTopMenuItem(
            id = "help",
            label = "說明",
            children = listOf(
                MyTopMenuItem(
                    id = "about",
                    label = "關於",
                    onClick = { dialog = OpenDialog.About },
                ),
                MyTopMenuItem(
                    id = "diag",
                    label = "Lean 連線診斷",
                    onClick = { dialog = OpenDialog.Diagnostic },
                ),
            ),
        ),
    )

    Scaffold(
        topBar = { MyTopMenuBar(items = topMenus) },
        bottomBar = { StatusBar(connection = connection, busy = proof.busy) },
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            LeftPane(
                level = Level8,
                completed = proof.completed,
                revealedHints = revealedHints,
                noMoreHintsMessage = noMoreHints,
                onShowMoreHints = {
                    val matching = matchingHiddenHints(proof.currentGoals, Level8.hiddenHints)
                        .filter { it.id !in revealedIds }
                    if (matching.isEmpty()) {
                        noMoreHints = "目前沒有更多提示。"
                    } else {
                        revealedIds = revealedIds + matching.map { it.id }
                        noMoreHints = null
                    }
                },
                modifier = Modifier.weight(0.25f).fillMaxHeight(),
            )
            VerticalDivider()
            CenterPane(
                level = Level8,
                proof = proof,
                draft = draft,
                onDraftChange = { draft = it },
                onSubmit = submit@{
                    if (!canSubmit) return@submit
                    session.submit(draft)
                },
                onRestartFrom = { stepIndex ->
                    session.restartFrom(stepIndex)
                    draft = ""
                },
                modifier = Modifier.weight(0.50f).fillMaxHeight(),
            )
            VerticalDivider()
            RightPane(
                level = Level8,
                onInsert = { template ->
                    draft = if (draft.isBlank()) template else draft + template
                },
                modifier = Modifier.weight(0.25f).fillMaxHeight(),
            )
        }
    }

    when (dialog) {
        OpenDialog.About -> AboutDialog(onDismiss = { dialog = OpenDialog.None })
        OpenDialog.Diagnostic -> DiagnosticDialog(connection, onDismiss = { dialog = OpenDialog.None })
        OpenDialog.PlayFile -> PlayFileDialog(proof.playFileText, onDismiss = { dialog = OpenDialog.None })
        OpenDialog.None -> Unit
    }

    LaunchedEffect(proof.steps.size, proof.lastSubmitError, proof.busy) {
        if (!proof.busy && proof.lastSubmitError == null && proof.acceptedCommands.isNotEmpty()) {
            // Successful submit: last command matches what we sent; clear if draft equals it.
            val last = proof.acceptedCommands.last()
            if (draft.trim().trimEnd(',') == last) {
                draft = ""
            }
        }
        if (proof.completed) {
            noMoreHints = null
        }
    }
}
