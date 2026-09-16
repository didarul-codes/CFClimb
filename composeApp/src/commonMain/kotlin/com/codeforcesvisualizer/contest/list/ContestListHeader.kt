package com.codeforcesvisualizer.contest.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.codeforcesvisualizer.core.theme.CFThemeColors
import com.codeforcesvisualizer.core.theme.CFText
import androidx.compose.ui.unit.sp
import com.codeforcesvisualizer.core.theme.CFSpace

/**
 * Kept for backward compatibility with search results. The main contest list
 * now uses ScreenHeader from core components.
 */
@Composable
internal fun Header(modifier: Modifier = Modifier, text: String) {
    val colors = CFThemeColors.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surface)
            .padding(horizontal = CFSpace.gutter, vertical = 8.dp),
    ) {
        Text(
            text = "// ${text.uppercase()}",
            style = CFText.caption.copy(color = colors.dim),
        )
    }
}
