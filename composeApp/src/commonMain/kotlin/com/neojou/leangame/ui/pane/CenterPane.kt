package com.neojou.leangame.ui.pane

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.neojou.leangame.level.LevelContent
import com.neojou.leangame.proof.ProofState
import com.neojou.leangame.ui.GoalView
import com.neojou.leangame.ui.TypewriterBar

@Composable
fun CenterPane(
    level: LevelContent,
    proof: ProofState,
    draft: String,
    onDraftChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onRestartFrom: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().padding(12.dp)) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("題目陳述", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
            Text(level.statementZh, style = MaterialTheme.typography.bodyLarge)
            Text(
                "le_total (x y : ℕ) : x ≤ y ∨ y ≤ x",
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            HorizontalDivider()
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("目前 goals", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                if (proof.busy) {
                    CircularProgressIndicator(modifier = Modifier.padding(2.dp), strokeWidth = 2.dp)
                    Text("Lean 思考中…", style = MaterialTheme.typography.bodySmall)
                }
            }
            GoalView(proof.currentGoals)
            if (proof.acceptedCommands.isNotEmpty()) {
                HorizontalDivider()
                Text("已送出的指令", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                proof.steps.drop(1).forEachIndexed { index, step ->
                    val stepNumber = index + 1
                    Text(
                        text = "$stepNumber. ${step.command}",
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !proof.busy) {
                                onRestartFrom(stepNumber)
                            }
                            .padding(vertical = 2.dp),
                    )
                }
                Text(
                    "點某一列可從那一步之後重來。",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        TypewriterBar(
            draft = draft,
            onDraftChange = onDraftChange,
            enabled = proof.canSubmit,
            error = proof.lastSubmitError,
            onSubmit = onSubmit,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}
