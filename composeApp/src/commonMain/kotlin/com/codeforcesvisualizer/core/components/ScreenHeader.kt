package com.codeforcesvisualizer.core.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.codeforcesvisualizer.core.theme.CFThemeColors
import com.codeforcesvisualizer.core.theme.CFText
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.sp
import com.codeforcesvisualizer.core.theme.CFSpace
import androidx.compose.foundation.clickable
import androidx.compose.ui.semantics.Role

@Composable
fun ScreenHeader(
    prompt: String,
    title: String,
    modifier: Modifier = Modifier,
    /** Set on any screen the user can leave; draws the "cd .." row above the prompt. */
    onNavigateBack: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    val colors = CFThemeColors.current

    val infiniteTransition = rememberInfiniteTransition(label = "cursor_blink")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1050
                1f at 0
                1f at 524
                0f at 525
                0f at 1050
            },
            repeatMode = RepeatMode.Restart,
        ),
        label = "cursor_alpha",
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = CFSpace.gutter)
            .padding(top = 8.dp, bottom = 14.dp),
    ) {
        if (onNavigateBack != null) {
            BackRow(onNavigateBack)
            HeightSpacer(height = 6.dp)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = colors.violet)) {
                        append("cf://")
                    }
                    withStyle(SpanStyle(color = colors.dim)) {
                        append(prompt)
                    }
                },
                style = CFText.label,
            )
            WidthSpacer(width = 4.dp)
            Box(
                modifier = Modifier
                    .size(width = 6.dp, height = 9.dp)
                    .alpha(cursorAlpha)
                    .background(colors.violet),
            )
        }

        HeightSpacer(height = 4.dp)

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = CFText.heading.copy(color = colors.fg),
                modifier = Modifier.weight(1f),
            )
            if (trailing != null) {
                trailing()
            }
        }
    }
}

/** Going back is a shell command like everything else in this app, never a chevron. */
@Composable
private fun BackRow(onNavigateBack: () -> Unit) {
    val colors = CFThemeColors.current
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(color = colors.violet)) { append("\u2190 ") }
            withStyle(SpanStyle(color = colors.fg)) { append("cd ..") }
        },
        style = CFText.subtitle,
        modifier = Modifier
            .clickable(role = Role.Button, onClickLabel = "Back", onClick = onNavigateBack)
            .padding(vertical = 4.dp),
    )
}
