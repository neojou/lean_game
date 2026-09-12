package com.neojou.leangame.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.neojou.leangame.level.InventoryItem
import com.neojou.leangame.level.LevelContent

private enum class InventoryTab { Tactics, Theorems, Definitions }

@Composable
fun InventoryPanel(
    level: LevelContent,
    onInsert: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var tab by remember { mutableStateOf(InventoryTab.Tactics) }
    val items = when (tab) {
        InventoryTab.Tactics -> level.tactics
        InventoryTab.Theorems -> level.theorems
        InventoryTab.Definitions -> level.definitions
    }
    Column(modifier = modifier.fillMaxSize()) {
        PrimaryTabRow(selectedTabIndex = tab.ordinal) {
            Tab(
                selected = tab == InventoryTab.Tactics,
                onClick = { tab = InventoryTab.Tactics },
                text = { Text("Tactics") },
            )
            Tab(
                selected = tab == InventoryTab.Theorems,
                onClick = { tab = InventoryTab.Theorems },
                text = { Text("Theorems") },
            )
            Tab(
                selected = tab == InventoryTab.Definitions,
                onClick = { tab = InventoryTab.Definitions },
                text = { Text("Definitions") },
            )
        }
        Text(
            "精簡背包：點名稱插入輸入框，不會立刻送出。旁的 ? 看繁中說明。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            items.forEach { item ->
                InventoryRow(item = item, onInsert = onInsert)
            }
        }
    }
}

@Composable
private fun InventoryRow(
    item: InventoryItem,
    onInsert: (String) -> Unit,
) {
    var expanded by remember(item.name) { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(onClick = { onInsert(item.insertTemplate) }) {
                Text(item.name, fontFamily = FontFamily.Monospace)
            }
            TextButton(onClick = { expanded = !expanded }) {
                Text(if (expanded) "收起" else "?")
            }
        }
        if (expanded) {
            Text(
                text = item.descriptionZh,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 8.dp, bottom = 4.dp),
            )
            item.englishNote?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp, bottom = 4.dp),
                )
            }
        }
    }
}
