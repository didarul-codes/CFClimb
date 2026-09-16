package com.codeforcesvisualizer.core.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.codeforcesvisualizer.core.theme.CFThemeColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import com.codeforcesvisualizer.core.theme.CFText
import androidx.compose.ui.unit.sp

@Immutable
data class RadarSeries(
    val values: List<Int>,
    val color: Color,
)

@Composable
fun TagRadarChart(
    series: List<RadarSeries>,
    axes: List<String>,
    modifier: Modifier = Modifier,
    size: Dp = 280.dp,
) {
    val colors = CFThemeColors.current
    val textMeasurer = rememberTextMeasurer()

    val axisCount = axes.size
    val maxValue = remember(series) {
        series.flatMap { it.values }.maxOrNull() ?: 1
    }

    val labelStyle = CFText.micro.copy(color = colors.dim, textAlign = TextAlign.Center)

    if (axisCount < 3) return

    Canvas(
        modifier = modifier.size(size)
    ) {
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        val labelPadding = 24.dp.toPx()
        val radius = (this.size.width / 2f) - labelPadding
        val angleStep = (2.0 * PI / axisCount).toFloat()

        fun vertexOffset(axisIndex: Int, fraction: Float): Offset {
            val angle = -PI.toFloat() / 2f + axisIndex * angleStep
            return Offset(
                x = center.x + radius * fraction * cos(angle),
                y = center.y + radius * fraction * sin(angle),
            )
        }

        // Draw concentric polygon rings at 25%, 50%, 75%, 100%
        val ringLevels = listOf(0.25f, 0.5f, 0.75f, 1f)
        for (level in ringLevels) {
            val ringPath = Path().apply {
                for (i in 0 until axisCount) {
                    val vertex = vertexOffset(i, level)
                    if (i == 0) moveTo(vertex.x, vertex.y) else lineTo(vertex.x, vertex.y)
                }
                close()
            }
            drawPath(
                path = ringPath,
                color = colors.border,
                style = Stroke(width = 1f),
            )
        }

        // Draw axis lines from center to each vertex
        for (i in 0 until axisCount) {
            val vertex = vertexOffset(i, 1f)
            drawLine(
                color = colors.border,
                start = center,
                end = vertex,
                strokeWidth = 1f,
            )
        }

        // Draw each series
        for (s in series) {
            if (s.values.size < axisCount) continue

            val seriesPath = Path().apply {
                for (i in 0 until axisCount) {
                    val fraction = s.values[i].toFloat() / maxValue.coerceAtLeast(1)
                    val vertex = vertexOffset(i, fraction.coerceIn(0f, 1f))
                    if (i == 0) moveTo(vertex.x, vertex.y) else lineTo(vertex.x, vertex.y)
                }
                close()
            }

            // Filled polygon
            drawPath(
                path = seriesPath,
                color = s.color.copy(alpha = 0.18f),
                style = Fill,
            )

            // Stroked outline
            drawPath(
                path = seriesPath,
                color = s.color,
                style = Stroke(width = 1.5.dp.toPx()),
            )

            // Dots at vertices
            for (i in 0 until axisCount) {
                val fraction = s.values[i].toFloat() / maxValue.coerceAtLeast(1)
                val vertex = vertexOffset(i, fraction.coerceIn(0f, 1f))
                drawCircle(
                    color = s.color,
                    radius = 2.5.dp.toPx(),
                    center = vertex,
                )
            }
        }

        // Labels at each axis vertex
        for (i in 0 until axisCount) {
            val angle = -PI.toFloat() / 2f + i * angleStep
            val labelRadius = radius + 14.dp.toPx()
            val labelCenter = Offset(
                x = center.x + labelRadius * cos(angle),
                y = center.y + labelRadius * sin(angle),
            )

            val measured = textMeasurer.measure(
                AnnotatedString(axes[i]),
                style = labelStyle,
            )
            drawText(
                textLayoutResult = measured,
                topLeft = Offset(
                    x = labelCenter.x - measured.size.width / 2f,
                    y = labelCenter.y - measured.size.height / 2f,
                ),
            )
        }
    }
}
