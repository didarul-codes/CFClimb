package com.codeforcesvisualizer.core.components

import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codeforcesvisualizer.core.theme.CFThemeColors

@Composable
fun Chip(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = CFThemeColors.current.violet,
    subtle: Boolean = false,
    icon: ImageVector? = null,
    onClick: (() -> Unit)? = null,
) {
    val bgAlpha = if (subtle) 0x14 else 0x22
    val backgroundColor = color.copy(alpha = bgAlpha / 255f)
    val borderColor = color.copy(alpha = 0x33 / 255f)
    val shape = RoundedCornerShape(4.dp)

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
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 9.5.sp,
                letterSpacing = 0.04.sp,
                color = color,
            ),
        )
    }
}
