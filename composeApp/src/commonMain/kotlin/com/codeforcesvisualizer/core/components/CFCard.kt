package com.codeforcesvisualizer.core.components

import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.codeforcesvisualizer.core.theme.CFThemeColors
import com.codeforcesvisualizer.core.theme.CFText
import com.codeforcesvisualizer.core.theme.CFShapes
import androidx.compose.ui.unit.sp

@Composable
fun CFCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    titleRight: @Composable (() -> Unit)? = null,
    noBorder: Boolean = false,
    contentPadding: Dp = 14.dp,
    content: @Composable () -> Unit,
) {
    val colors = CFThemeColors.current
    val shape = CFShapes.card

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surface)
            .then(
                if (noBorder) Modifier
                else Modifier.border(width = 1.dp, color = colors.border, shape = shape)
            ),
    ) {
        if (title != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 14.dp, top = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = colors.violet)) {
                            append("// ")
                        }
                        withStyle(SpanStyle(color = colors.dim)) {
                            append(title.uppercase())
                        }
                    },
                    style = CFText.caption,
                    modifier = Modifier.weight(1f),
                )
                if (titleRight != null) {
                    titleRight()
                }
            }
            HeightSpacer(height = 10.dp)
        }

        Column(
            modifier = Modifier
                .padding(horizontal = contentPadding)
                .padding(
                    top = if (title != null) 0.dp else contentPadding,
                    bottom = contentPadding.coerceAtLeast(6.dp),
                )
        ) {
            content()
        }
    }
}
