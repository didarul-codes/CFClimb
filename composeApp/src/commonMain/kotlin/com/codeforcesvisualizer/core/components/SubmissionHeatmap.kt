package com.codeforcesvisualizer.core.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.codeforcesvisualizer.core.theme.CFThemeColors
import kotlin.math.min
import com.codeforcesvisualizer.core.theme.CFText
import androidx.compose.ui.unit.sp

@Immutable
data class HeatmapCell(
    val week: Int,
    val day: Int,
    val count: Int,
)

@Composable
fun SubmissionHeatmap(
    cells: List<HeatmapCell>,
    modifier: Modifier = Modifier,
    weeks: Int = 26,
    cellSize: Dp = 9.dp,
    gap: Dp = 2.dp,
    accent: Color = CFThemeColors.current.violet,
) {
    val colors = CFThemeColors.current
    val textMeasurer = rememberTextMeasurer()

    val cellMap = remember(cells) {
        cells.associateBy { it.week to it.day }
    }

    val totalWidth = remember(weeks, cellSize, gap) {
        cellSize * weeks + gap * (weeks - 1)
    }
    val totalHeight = remember(cellSize, gap) {
        cellSize * 7 + gap * 6
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
    ) {
        // Heatmap grid
        Canvas(
            modifier = Modifier
                .width(totalWidth)
                .height(totalHeight)
        ) {
            val cellSizePx = cellSize.toPx()
            val gapPx = gap.toPx()
            val cornerRadius = CornerRadius(1.5.dp.toPx())

            for (w in 0 until weeks) {
                for (d in 0 until 7) {
                    val x = w * (cellSizePx + gapPx)
                    val y = d * (cellSizePx + gapPx)
                    val cell = cellMap[w to d]
                    val count = cell?.count ?: 0

                    val cellColor = if (count == 0) {
                        colors.surface2
                    } else {
                        val intensity = 0.18f + 0.82f * min(1f, count / 6f)
                        accent.copy(alpha = intensity)
                    }

                    drawRoundRect(
                        color = cellColor,
                        topLeft = Offset(x, y),
                        size = Size(cellSizePx, cellSizePx),
                        cornerRadius = cornerRadius,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Legend: "less" + 5 swatches + "more"
        val legendCellSize = 9.dp
        val legendGap = 3.dp
        val legendStyle = CFText.nano.copy(color = colors.dim)

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Canvas(
                modifier = Modifier
                    .width(180.dp)
                    .height(14.dp)
            ) {
                val lessMeasured = textMeasurer.measure(AnnotatedString("less"), style = legendStyle)
                drawText(
                    textLayoutResult = lessMeasured,
                    topLeft = Offset(0f, (size.height - lessMeasured.size.height) / 2f),
                )

                var offsetX = lessMeasured.size.width + 6.dp.toPx()
                val swatchSize = legendCellSize.toPx()
                val swatchGap = legendGap.toPx()
                val swatchRadius = CornerRadius(1.5.dp.toPx())

                val intensities = listOf(0f, 0.18f, 0.38f, 0.62f, 1f)
                for (intensity in intensities) {
                    val swatchColor = if (intensity == 0f) {
                        colors.surface2
                    } else {
                        accent.copy(alpha = intensity)
                    }
                    drawRoundRect(
                        color = swatchColor,
                        topLeft = Offset(offsetX, (size.height - swatchSize) / 2f),
                        size = Size(swatchSize, swatchSize),
                        cornerRadius = swatchRadius,
                    )
                    offsetX += swatchSize + swatchGap
                }

                offsetX += 3.dp.toPx()
                val moreMeasured = textMeasurer.measure(AnnotatedString("more"), style = legendStyle)
                drawText(
                    textLayoutResult = moreMeasured,
                    topLeft = Offset(offsetX, (size.height - moreMeasured.size.height) / 2f),
                )
            }
        }
    }
}
