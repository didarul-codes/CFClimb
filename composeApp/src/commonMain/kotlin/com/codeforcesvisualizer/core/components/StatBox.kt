package com.codeforcesvisualizer.core.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.codeforcesvisualizer.core.theme.CFThemeColors
import com.codeforcesvisualizer.core.theme.CFText
import androidx.compose.ui.unit.sp

@Composable
fun StatBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    delta: String? = null,
    color: Color? = null,
) {
    val colors = CFThemeColors.current
    val valueColor = color ?: colors.fg

    Column(modifier = modifier) {
        Text(
            text = label.uppercase(),
            style = CFText.micro.copy(color = colors.dim),
        )

        HeightSpacer(height = 2.dp)

        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = CFText.heading.copy(color = valueColor),
            )

            if (delta != null) {
                WidthSpacer(width = 4.dp)
                val deltaColor = if (delta.startsWith("+")) colors.green else colors.red
                Text(
                    text = delta,
                    style = CFText.caption.copy(color = deltaColor),
                )
            }
        }
    }
}
