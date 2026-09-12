package com.neojou.leangame.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.neojou.leangame.proof.Goal

@Composable
fun GoalView(
    goals: List<Goal>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (goals.isEmpty()) {
            Text(
                "沒有剩餘目標。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return
        }
        goals.forEachIndexed { index, goal ->
            Surface(
                tonalElevation = 2.dp,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    if (goals.size > 1) {
                        Text(
                            "目標 ${index + 1} / ${goals.size}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    goal.userName?.let { name ->
                        Text(
                            "case $name",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = FontFamily.Monospace,
                        )
                    }
                    goal.hyps.forEach { hyp ->
                        Text(
                            text = "${hyp.names.joinToString(" ")} : ${hyp.typePretty}",
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    Text(
                        text = "⊢ ${goal.targetPretty}",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}
