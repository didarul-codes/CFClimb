package com.codeforcesvisualizer.core.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codeforcesvisualizer.core.theme.CFThemeColors
import com.codeforcesvisualizer.core.theme.RankTiers

@Immutable
data class RatingPoint(
    val index: Int,
    val rating: Int,
)

@Immutable
data class RatingSeriesData(
    val data: List<RatingPoint>,
    val color: Color,
    val label: String,
)

@Composable
fun RatingLineChart(
    series: List<RatingSeriesData>,
    modifier: Modifier = Modifier,
    height: Dp = 220.dp,
) {
    val colors = CFThemeColors.current
    val textMeasurer = rememberTextMeasurer()

    val allRatings = remember(series) {
        series.flatMap { s -> s.data.map { it.rating } }
    }
    val minRating = remember(allRatings) { (allRatings.minOrNull() ?: 0) }
    val maxRating = remember(allRatings) { (allRatings.maxOrNull() ?: 3000) }
    val allIndices = remember(series) {
        series.flatMap { s -> s.data.map { it.index } }
    }
    val minIndex = remember(allIndices) { allIndices.minOrNull() ?: 0 }
    val maxIndex = remember(allIndices) { allIndices.maxOrNull() ?: 1 }

    val labelStyle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 8.5.sp,
        color = colors.dim,
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val padTop = 12.dp.toPx()
        val padRight = 8.dp.toPx()
        val padBottom = 18.dp.toPx()
        val padLeft = 30.dp.toPx()

        val plotWidth = size.width - padLeft - padRight
        val plotHeight = size.height - padTop - padBottom

        val ratingRange = (maxRating - minRating).coerceAtLeast(1)

        fun ratingToY(rating: Int): Float {
            val ratio = (rating - minRating).toFloat() / ratingRange
            return padTop + plotHeight - (ratio * plotHeight)
        }

        fun indexToX(index: Int): Float {
            val indexRange = (maxIndex - minIndex).coerceAtLeast(1)
            val ratio = (index - minIndex).toFloat() / indexRange
            return padLeft + ratio * plotWidth
        }

        // Tier bands
        for (tier in RankTiers) {
            val bandTop = tier.max.coerceAtMost(maxRating)
            val bandBottom = tier.min.coerceAtLeast(minRating)
            if (bandBottom > maxRating || bandTop < minRating) continue

            val y1 = ratingToY(bandTop)
            val y2 = ratingToY(bandBottom)
            drawRect(
                color = tier.color.copy(alpha = 0.06f),
                topLeft = Offset(padLeft, y1),
                size = androidx.compose.ui.geometry.Size(plotWidth, y2 - y1),
            )
        }

        // Y-axis grid: 3 ticks (min, mid, max)
        val midRating = (minRating + maxRating) / 2
        val ticks = listOf(minRating, midRating, maxRating)
        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx()))

        for (tick in ticks) {
            val y = ratingToY(tick)
            drawLine(
                color = colors.dim.copy(alpha = 0.3f),
                start = Offset(padLeft, y),
                end = Offset(padLeft + plotWidth, y),
                strokeWidth = 1f,
                pathEffect = dashEffect,
            )
            val label = tick.toString()
            val measured = textMeasurer.measure(AnnotatedString(label), style = labelStyle)
            drawText(
                textLayoutResult = measured,
                topLeft = Offset(
                    x = padLeft - measured.size.width - 4.dp.toPx(),
                    y = y - measured.size.height / 2f,
                ),
            )
        }

        // Draw lines for each series
        series.forEachIndexed { seriesIdx, s ->
            if (s.data.size < 2) return@forEachIndexed
            val sorted = s.data.sortedBy { it.index }

            val path = Path().apply {
                sorted.forEachIndexed { i, point ->
                    val x = indexToX(point.index)
                    val y = ratingToY(point.rating)
                    if (i == 0) moveTo(x, y) else lineTo(x, y)
                }
            }

            drawPath(
                path = path,
                color = s.color,
                style = Stroke(
                    width = 1.8.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                ),
            )

            // End dot
            val last = sorted.last()
            val ex = indexToX(last.index)
            val ey = ratingToY(last.rating)
            drawCircle(
                color = s.color.copy(alpha = 0.4f),
                radius = 5.dp.toPx(),
                center = Offset(ex, ey),
            )
            drawCircle(
                color = s.color,
                radius = 3.dp.toPx(),
                center = Offset(ex, ey),
            )
        }
    }
}
