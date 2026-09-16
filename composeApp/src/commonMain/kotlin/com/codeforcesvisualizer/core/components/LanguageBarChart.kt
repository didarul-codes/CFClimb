package com.codeforcesvisualizer.core.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import com.codeforcesvisualizer.core.theme.CFThemeColors
import com.codeforcesvisualizer.core.theme.CFText
import com.codeforcesvisualizer.core.theme.CFShapes
import androidx.compose.ui.unit.sp

@Immutable
data class LanguageData(
    val name: String,
    val percentage: Float,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LanguageBarChart(
    languages: List<LanguageData>,
    modifier: Modifier = Modifier,
) {
    val colors = CFThemeColors.current

    val palette = remember {
        listOf(colors.violet, colors.green, colors.amber, colors.blue, colors.rose)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Stacked horizontal bar with rounded corners via clip
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(CFShapes.bar)
        ) {
            val barHeight = size.height
            val totalWidth = size.width

            // Background
            drawRect(
                color = colors.surface2,
                topLeft = Offset.Zero,
                size = Size(totalWidth, barHeight),
            )

            // Draw colored segments from left to right
            var offsetX = 0f
            languages.forEachIndexed { index, lang ->
                val segColor = palette[index % palette.size]
                val segWidth = (lang.percentage / 100f) * totalWidth

                if (segWidth > 0f) {
                    drawRect(
                        color = segColor,
                        topLeft = Offset(offsetX, 0f),
                        size = Size(segWidth, barHeight),
                    )
                    offsetX += segWidth
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Legend: wrap row of items
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            languages.forEachIndexed { index, lang ->
                val segColor = palette[index % palette.size]
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Canvas(modifier = Modifier.size(8.dp)) {
                        drawCircle(
                            color = segColor,
                            radius = size.width / 2f,
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${lang.name} ${lang.percentage.toInt()}%",
                        style = CFText.micro.copy(color = colors.dim),
                    )
                }
            }
        }
    }
}
