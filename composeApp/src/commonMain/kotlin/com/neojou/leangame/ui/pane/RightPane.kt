package com.neojou.leangame.ui.pane

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.neojou.leangame.level.LevelContent
import com.neojou.leangame.ui.InventoryPanel

@Composable
fun RightPane(
    level: LevelContent,
    onInsert: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    InventoryPanel(level = level, onInsert = onInsert, modifier = modifier)
}
