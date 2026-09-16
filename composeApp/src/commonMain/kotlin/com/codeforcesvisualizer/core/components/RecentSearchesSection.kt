package com.codeforcesvisualizer.core.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.codeforcesvisualizer.core.theme.CFThemeColors
import com.codeforcesvisualizer.core.theme.CFText
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecentSearchesSection(
    searches: List<String>,
    onSearchClick: (String) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (searches.isEmpty()) return

    val colors = CFThemeColors.current

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "// RECENT",
                style = CFText.caption.copy(color = colors.dim),
            )
            Text(
                text = "clear",
                style = CFText.caption.copy(color = colors.violet),
                modifier = Modifier.clickable { onClearAll() },
            )
        }

        HeightSpacer(height = 10.dp)

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            searches.forEach { handle ->
                Chip(
                    text = "@$handle",
                    color = colors.violet,
                    subtle = true,
                    onClick = { onSearchClick(handle) },
                )
            }
        }
    }
}
