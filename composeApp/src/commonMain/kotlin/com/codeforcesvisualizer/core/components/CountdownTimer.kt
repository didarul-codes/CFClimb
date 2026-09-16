package com.codeforcesvisualizer.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.codeforcesvisualizer.core.theme.CFThemeColors
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import com.codeforcesvisualizer.core.theme.CFText
import com.codeforcesvisualizer.core.theme.CFShapes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.sp

@Composable
fun CountdownTimer(
    targetEpochSeconds: Long,
    modifier: Modifier = Modifier,
) {
    val colors = CFThemeColors.current

    var nowEpochSeconds by remember {
        mutableStateOf(Clock.System.now().epochSeconds)
    }

    LaunchedEffect(targetEpochSeconds) {
        while (true) {
            nowEpochSeconds = Clock.System.now().epochSeconds
            delay(1000L)
        }
    }

    val remaining = (targetEpochSeconds - nowEpochSeconds).coerceAtLeast(0)
    val days = remaining / 86400
    val hours = (remaining % 86400) / 3600
    val minutes = (remaining % 3600) / 60
    val seconds = remaining % 60

    val segments = listOf(
        "DAYS" to days,
        "HRS" to hours,
        "MIN" to minutes,
        "SEC" to seconds,
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        segments.forEach { (label, value) ->
            CountdownSegment(
                label = label,
                value = value,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun CountdownSegment(
    label: String,
    value: Long,
    modifier: Modifier = Modifier,
) {
    val colors = CFThemeColors.current
    val shape = CFShapes.control

    Column(
        modifier = modifier
            .background(color = colors.surface2, shape = shape)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value.toString().padStart(2, '0'),
            style = CFText.heading.copy(color = colors.fg, textAlign = TextAlign.Center),
        )
        HeightSpacer(height = 2.dp)
        Text(
            text = label,
            style = CFText.nano.copy(color = colors.dim, textAlign = TextAlign.Center),
        )
    }
}
