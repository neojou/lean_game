package com.neojou.leangame.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.neojou.leangame.docs.LeanCommandDocs
import com.neojou.leangame.level.InventoryItem
import com.neojou.leangame.level.LevelContent
import com.neojou.leangame.level.TheoremTab
import com.neojou.leangame.ui.markdown.SimpleMarkdown

private enum class InventoryTab(val label: String) {
    Theorems("定理"),
    Tactics("策略"),
    Definitions("定義"),
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InventoryPanel(
    level: LevelContent,
    modifier: Modifier = Modifier,
) {
    var tab by remember { mutableStateOf(InventoryTab.Tactics) }
    var theoremTab by remember { mutableStateOf(TheoremTab.Plus) }
    var selected by remember { mutableStateOf<InventoryItem?>(null) }

    val items = when (tab) {
        InventoryTab.Tactics -> level.tactics
        InventoryTab.Definitions -> level.definitions
        InventoryTab.Theorems -> level.theorems.filter { it.theoremTab == theoremTab }
    }

    Column(modifier = modifier.fillMaxSize()) {
        PrimaryTabRow(selectedTabIndex = tab.ordinal) {
            InventoryTab.entries.forEach { t ->
                Tab(
                    selected = tab == t,
                    onClick = {
                        tab = t
                        selected = null
                    },
                    text = { Text(t.label) },
                )
            }
        }
        if (tab == InventoryTab.Theorems) {
            PrimaryScrollableTabRow(
                selectedTabIndex = TheoremTab.entries.indexOf(theoremTab),
                edgePadding = 8.dp,
            ) {
                TheoremTab.entries.forEach { g ->
                    Tab(
                        selected = theoremTab == g,
                        onClick = {
                            theoremTab = g
                            selected = null
                        },
                        text = { Text(g.label, fontFamily = FontFamily.Monospace) },
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
        ) {
            if (items.isEmpty()) {
                Text(
                    if (tab == InventoryTab.Theorems) "此分類在本關沒有定理。" else "沒有項目。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items.forEach { item ->
                        val isOn = selected?.docId == item.docId
                        OutlinedButton(
                            onClick = { selected = item },
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isOn) {
                                    MaterialTheme.colorScheme.secondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surface
                                },
                            ),
                        ) {
                            Text(item.name, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }
        HorizontalDivider()
        val current = selected
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(12.dp),
        ) {
            if (current == null) {
                Text(
                    "點選上方按鈕查看說明（不會插入輸入框）。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                SimpleMarkdown(LeanCommandDocs.text(current.docId))
            }
        }
    }
}
