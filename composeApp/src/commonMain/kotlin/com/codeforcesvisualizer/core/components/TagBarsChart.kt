package com.codeforcesvisualizer.core.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.dp
import com.codeforcesvisualizer.core.theme.CFThemeColors
import com.codeforcesvisualizer.core.theme.CFText
import androidx.compose.ui.unit.sp

@Immutable
data class TagData(
    val tag: String,
    val count: Int,
)

@Composable
fun TagBarsChart(
    tags: List<TagData>,
    modifier: Modifier = Modifier,
    maxItems: Int = 8,
    barColor: Color = CFThemeColors.current.violet,
) {
    val colors = CFThemeColors.current
    val textMeasurer = rememberTextMeasurer()

    val displayTags = remember(tags, maxItems) {
        tags.sortedByDescending { it.count }.take(maxItems)
    }
    val maxCount = remember(displayTags) {
        displayTags.maxOfOrNull { it.count } ?: 1
    }

    val labelStyle = CFText.label.copy(color = colors.fg)
    val countStyle = CFText.label.copy(color = colors.dim)

    val rowHeight = 32.dp
    val barHeight = 4.dp
    val labelAreaHeight = 16.dp

    Column(modifier = modifier.fillMaxWidth()) {
        for (tagData in displayTags) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(rowHeight)
            ) {
                val barHeightPx = barHeight.toPx()
                val labelAreaPx = labelAreaHeight.toPx()

                // Label row: tag name left, count right
                val tagMeasured = textMeasurer.measure(AnnotatedString(tagData.tag), style = labelStyle)
                drawText(
                    textLayoutResult = tagMeasured,
                    topLeft = Offset(0f, (labelAreaPx - tagMeasured.size.height) / 2f),
                )

                val countText = tagData.count.toString()
                val countMeasured = textMeasurer.measure(AnnotatedString(countText), style = countStyle)
                drawText(
                    textLayoutResult = countMeasured,
                    topLeft = Offset(
                        x = size.width - countMeasured.size.width,
                        y = (labelAreaPx - countMeasured.size.height) / 2f,
                    ),
                )

                // Bar background
                val barY = labelAreaPx + 4.dp.toPx()
                val barCorner = CornerRadius(2.dp.toPx())
                drawRoundRect(
                    color = colors.surface2,
                    topLeft = Offset(0f, barY),
                    size = Size(size.width, barHeightPx),
                    cornerRadius = barCorner,
                )

                // Bar fill
                val fillRatio = tagData.count.toFloat() / maxCount.coerceAtLeast(1)
                val fillWidth = size.width * fillRatio
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(0f, barY),
                    size = Size(fillWidth, barHeightPx),
                    cornerRadius = barCorner,
                )

                // Glow shadow effect (subtle glow line below the bar)
                drawRoundRect(
                    color = barColor.copy(alpha = 0.25f),
                    topLeft = Offset(0f, barY + barHeightPx),
                    size = Size(fillWidth, 1.5.dp.toPx()),
                    cornerRadius = barCorner,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
