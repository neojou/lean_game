package com.neojou.leangame.ui.pane

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.neojou.leangame.level.HiddenHint
import com.neojou.leangame.level.LevelContent

@Composable
fun LeftPane(
    level: LevelContent,
    completed: Boolean,
    revealedHints: List<HiddenHint>,
    noMoreHintsMessage: String?,
    onShowMoreHints: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            "${level.worldName} · 關卡 ${level.levelNumber}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            level.titleZh,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            level.introduction,
            style = MaterialTheme.typography.bodyMedium,
        )
        if (revealedHints.isNotEmpty()) {
            Text("提示", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
            revealedHints.forEach { hint ->
                Text("• ${hint.text}", style = MaterialTheme.typography.bodyMedium)
            }
        }
        OutlinedButton(onClick = onShowMoreHints, enabled = !completed) {
            Text("顯示更多提示！")
        }
        noMoreHintsMessage?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (completed) {
            Text(
                "過關",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
            Text(level.conclusion, style = MaterialTheme.typography.bodyMedium)
            Text(
                "定理：${level.theoremName}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}
