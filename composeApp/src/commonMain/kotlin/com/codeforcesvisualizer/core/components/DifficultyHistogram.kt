package com.codeforcesvisualizer.core.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.codeforcesvisualizer.core.theme.CFThemeColors
import com.codeforcesvisualizer.core.theme.rankColorFor
import com.codeforcesvisualizer.core.theme.CFText
import androidx.compose.ui.unit.sp

@Immutable
data class DifficultyBucket(
    val range: String,
    val count: Int,
)

@Composable
fun DifficultyHistogram(
    buckets: List<DifficultyBucket>,
    modifier: Modifier = Modifier,
) {
    val colors = CFThemeColors.current
    val textMeasurer = rememberTextMeasurer()

    val maxCount = remember(buckets) {
        buckets.maxOfOrNull { it.count } ?: 1
    }

    val labelStyle = CFText.nano.copy(color = colors.dim, textAlign = TextAlign.Center)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        if (buckets.isEmpty()) return@Canvas

        val bottomPadding = 20.dp.toPx()
        val topPadding = 8.dp.toPx()
        val plotHeight = size.height - bottomPadding - topPadding
        val barGap = 2.dp.toPx()
        val bucketCount = buckets.size
        val barWidth = (size.width - barGap * (bucketCount - 1)) / bucketCount

        // Up to 28 buckets (800–3500) don't leave room for every label, so label every n-th bar.
        val labels = buckets.map { textMeasurer.measure(AnnotatedString(it.range), style = labelStyle) }
        val widestLabel = labels.maxOf { it.size.width }
        val labelEvery = kotlin.math.ceil((widestLabel + 4.dp.toPx()) / (barWidth + barGap))
            .toInt()
            .coerceAtLeast(1)

        buckets.forEachIndexed { index, bucket ->
            val ratio = bucket.count.toFloat() / maxCount.coerceAtLeast(1)
            val barHeight = ratio * plotHeight
            val x = index * (barWidth + barGap)
            val y = topPadding + plotHeight - barHeight

            // Parse range to get rating for color
            val ratingValue = bucket.range.filter { it.isDigit() }.toIntOrNull() ?: 0
            val barColor = rankColorFor(ratingValue).copy(alpha = 0.85f)

            drawRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
            )

            // X-axis label below the bar
            if (index % labelEvery == 0) {
                val labelMeasured = labels[index]
                drawText(
                    textLayoutResult = labelMeasured,
                    topLeft = Offset(
                        x = (x + (barWidth - labelMeasured.size.width) / 2f)
                            .coerceIn(0f, size.width - labelMeasured.size.width),
                        y = topPadding + plotHeight + 4.dp.toPx(),
                    ),
                )
            }
        }
    }
}
