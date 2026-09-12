package com.neojou.leangame.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.neojou.leangame.session.ConnectionInfo
import com.neojou.leangame.session.ConnectionStatus

@Composable
fun StatusBar(
    connection: ConnectionInfo,
    busy: Boolean,
    modifier: Modifier = Modifier,
) {
    val color = when (connection.status) {
        ConnectionStatus.Ready -> MaterialTheme.colorScheme.surfaceContainer
        ConnectionStatus.Connecting -> MaterialTheme.colorScheme.surfaceContainerHigh
        ConnectionStatus.Error, ConnectionStatus.MissingToolchain ->
            MaterialTheme.colorScheme.errorContainer
    }
    Surface(color = color, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = connection.leanVersion ?: "Lean 未偵測",
                style = MaterialTheme.typography.labelMedium,
            )
            Text(
                text = connection.mode ?: "未連線",
                style = MaterialTheme.typography.labelMedium,
            )
            Text(
                text = if (busy) "忙碌" else connection.detailZh,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
