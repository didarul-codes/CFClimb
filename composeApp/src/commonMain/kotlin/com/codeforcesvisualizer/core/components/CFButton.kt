package com.codeforcesvisualizer.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.codeforcesvisualizer.core.theme.CFAlpha
import com.codeforcesvisualizer.core.theme.CFShapes
import com.codeforcesvisualizer.core.theme.CFSpace
import com.codeforcesvisualizer.core.theme.CFText
import com.codeforcesvisualizer.core.theme.CFThemeColors
import com.codeforcesvisualizer.core.theme.bold

/** How much weight a button carries on its screen. */
enum class CFButtonStyle {
    /** The action the screen is about: tinted background and a matching border. */
    Filled,

    /** A second choice next to a filled one: border only. */
    Outline,

    /** Neutral, for actions that leave the app or undo something. */
    Plain,
}

enum class CFButtonSize {
    /** Inside a card or a row of two. */
    Medium,

    /** The main action of a screen. */
    Large,
}

/**
 * The app's one button. The `$ ` prompt is part of the look, so callers pass the bare
 * label ("open", "retry") and never write the prefix themselves.
 */
@Composable
fun CFButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = CFThemeColors.current.violet,
    style: CFButtonStyle = CFButtonStyle.Filled,
    size: CFButtonSize = CFButtonSize.Medium,
    contentPadding: Boolean = true,
) {
    val colors = CFThemeColors.current
    val shape = CFShapes.control
    val background = when (style) {
        CFButtonStyle.Filled -> color.copy(alpha = CFAlpha.FILL)
        CFButtonStyle.Outline -> Color.Transparent
        CFButtonStyle.Plain -> colors.surface
    }
    val borderColor = when (style) {
        CFButtonStyle.Plain -> colors.border
        else -> color.copy(alpha = CFAlpha.BORDER)
    }
    val labelColor = if (style == CFButtonStyle.Plain) colors.fg else color
    val labelStyle = when (size) {
        CFButtonSize.Medium -> CFText.label.bold()
        CFButtonSize.Large -> CFText.subtitle
    }
    val verticalPadding = when (size) {
        CFButtonSize.Medium -> 10.dp
        CFButtonSize.Large -> CFSpace.card
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(background)
            .border(width = 1.dp, color = borderColor, shape = shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(
                horizontal = if (contentPadding) CFSpace.gutter else 0.dp,
                vertical = verticalPadding,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "$ $text", style = labelStyle.copy(color = labelColor))
    }
}
