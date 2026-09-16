package com.codeforcesvisualizer.core.components

import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.codeforcesvisualizer.core.theme.CFThemeColors
import com.codeforcesvisualizer.core.theme.CFText
import com.codeforcesvisualizer.core.theme.CFAlpha
import com.codeforcesvisualizer.core.theme.CFShapes
import androidx.compose.ui.unit.sp

@Composable
fun Chip(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = CFThemeColors.current.violet,
    subtle: Boolean = false,
    icon: ImageVector? = null,
    onClick: (() -> Unit)? = null,
) {
    val backgroundColor = color.copy(alpha = if (subtle) CFAlpha.FILL_SUBTLE else CFAlpha.FILL)
    val borderColor = color.copy(alpha = CFAlpha.BORDER)
    val shape = CFShapes.chip

    Row(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .border(width = 1.dp, color = borderColor, shape = shape)
            .then(
                if (onClick != null) Modifier.clickable { onClick() }
                else Modifier
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(
                modifier = Modifier.size(12.dp),
                imageVector = icon,
                contentDescription = null,
                tint = color,
            )
            WidthSpacer(width = 4.dp)
        }
        Text(
            text = text.lowercase(),
            style = CFText.micro.copy(color = color),
        )
    }
}

/**
 * A chip that can be switched on and off. Selected chips take the accent colour and a
 * tick, so "on" reads the same everywhere: filters, reminder lead times, alert toggles.
 */
@Composable
fun SelectableChip(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    color: Color = CFThemeColors.current.violet,
    onClick: (() -> Unit)?,
) {
    val colors = CFThemeColors.current
    Chip(
        text = if (selected) "\u2713 $text" else text,
        modifier = modifier,
        color = if (selected && onClick != null) color else colors.dim,
        subtle = !selected || onClick == null,
        onClick = onClick,
    )
}
