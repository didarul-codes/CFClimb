package com.codeforcesvisualizer.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.codeforcesvisualizer.core.theme.CFShapes
import com.codeforcesvisualizer.core.theme.CFText
import com.codeforcesvisualizer.core.theme.CFThemeColors
import com.codeforcesvisualizer.core.theme.bold

/**
 * One row of tabs that splits a screen's content, such as upcoming and past contests.
 * Labels get the `$ ` prompt here, so callers pass plain text.
 */
@Composable
fun SegmentedTabs(
    labels: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = CFThemeColors.current
    val containerShape = CFShapes.control

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(containerShape)
            .background(colors.surface)
            .border(1.dp, colors.border, containerShape)
            .padding(3.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        labels.forEachIndexed { index, label ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(CFShapes.control)
                    .then(if (isSelected) Modifier.background(colors.surface2) else Modifier)
                    .clickable(role = Role.Tab) { onSelect(index) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "$ $label",
                    style = (if (isSelected) CFText.label.bold() else CFText.label)
                        .copy(color = if (isSelected) colors.fg else colors.dim),
                )
            }
        }
    }
}
