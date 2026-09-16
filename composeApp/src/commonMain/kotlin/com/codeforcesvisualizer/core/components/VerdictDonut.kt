package com.codeforcesvisualizer.core.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.dp
import com.codeforcesvisualizer.core.theme.CFThemeColors
import kotlin.math.min
import com.codeforcesvisualizer.core.theme.CFText
import androidx.compose.ui.unit.sp

@Immutable
data class VerdictDonutData(
    val verdicts: Map<String, Int>,
)

private val VerdictColorMap = mapOf(
    "AC" to Color(0xFF36D399),
    "WA" to Color(0xFFF87171),
    "TLE" to Color(0xFFFBBF24),
    "RE" to Color(0xFFF472B6),
    "MLE" to Color(0xFFA78BFA),
    "CE" to Color(0xFF60A5FA),
)

private fun verdictColor(verdict: String): Color {
    return VerdictColorMap[verdict] ?: Color(0xFF7D8597)
}

@Composable
fun VerdictDonut(
    data: Map<String, Int>,
    modifier: Modifier = Modifier,
) {
    val colors = CFThemeColors.current
    val textMeasurer = rememberTextMeasurer()

    val total = remember(data) { data.values.sum() }
    val acCount = remember(data) { (data["AC"] ?: 0) }
    val acPercent = remember(total, acCount) {
        if (total > 0) (acCount * 100) / total else 0
    }

    val entries = remember(data) {
        data.entries.sortedByDescending { it.value }
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Donut chart
        val donutSize = 140.dp
        Canvas(
            modifier = Modifier.size(donutSize)
        ) {
            val diameter = min(size.width, size.height)
            val radius = diameter / 2f
            val innerRadius = radius * 0.62f
            val center = Offset(size.width / 2f, size.height / 2f)

            if (total == 0) return@Canvas

            // Draw pie segments
            var startAngle = -90f
            for (entry in entries) {
                val sweep = (entry.value.toFloat() / total) * 360f
                drawArc(
                    color = verdictColor(entry.key),
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = true,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(diameter, diameter),
                    style = Fill,
                )
                startAngle += sweep
            }

            // Cut out inner circle
            drawCircle(
                color = colors.surface,
                radius = innerRadius,
                center = center,
            )

            // Center text: AC percentage
            val percentText = "$acPercent%"
            val percentStyle = CFText.heading.copy(color = colors.fg)
            val percentMeasured = textMeasurer.measure(AnnotatedString(percentText), style = percentStyle)
            drawText(
                textLayoutResult = percentMeasured,
                topLeft = Offset(
                    x = center.x - percentMeasured.size.width / 2f,
                    y = center.y - percentMeasured.size.height - 1.dp.toPx(),
                ),
            )

            val labelText = "ACCEPTED"
            val labelStyle = CFText.nano.copy(color = colors.dim)
            val labelMeasured = textMeasurer.measure(AnnotatedString(labelText), style = labelStyle)
            drawText(
                textLayoutResult = labelMeasured,
                topLeft = Offset(
                    x = center.x - labelMeasured.size.width / 2f,
                    y = center.y + 2.dp.toPx(),
                ),
            )
        }

        Spacer(modifier = Modifier.width(20.dp))

        // Legend column
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            for (entry in entries) {
                LegendRow(
                    color = verdictColor(entry.key),
                    verdict = entry.key,
                    count = entry.value,
                    percentage = if (total > 0) (entry.value * 100) / total else 0,
                )
            }
        }
    }
}

@Composable
private fun LegendRow(
    color: Color,
    verdict: String,
    count: Int,
    percentage: Int,
) {
    val colors = CFThemeColors.current
    val textMeasurer = rememberTextMeasurer()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        // Colored square with 2dp radius
        Canvas(modifier = Modifier.size(8.dp)) {
            drawRoundRect(
                color = color,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()),
                size = Size(size.width, size.height),
            )
        }

        val textStyle = CFText.micro.copy(color = colors.fg)
        val dimStyle = CFText.micro.copy(color = colors.dim)

        Canvas(modifier = Modifier.width(100.dp).height(14.dp)) {
            val verdictMeasured = textMeasurer.measure(AnnotatedString(verdict), style = textStyle)
            drawText(
                textLayoutResult = verdictMeasured,
                topLeft = Offset(0f, (size.height - verdictMeasured.size.height) / 2f),
            )

            val infoText = "$count ($percentage%)"
            val infoMeasured = textMeasurer.measure(AnnotatedString(infoText), style = dimStyle)
            drawText(
                textLayoutResult = infoMeasured,
                topLeft = Offset(
                    x = verdictMeasured.size.width + 8.dp.toPx(),
                    y = (size.height - infoMeasured.size.height) / 2f,
                ),
            )
        }
    }
}
