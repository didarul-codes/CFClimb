package com.codeforcesvisualizer.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.codeforcesvisualizer.core.platform.CalendarResult
import com.codeforcesvisualizer.core.theme.CFThemeColors
import com.codeforcesvisualizer.core.theme.CFText
import com.codeforcesvisualizer.core.theme.bold
import com.codeforcesvisualizer.core.theme.CFAlpha
import com.codeforcesvisualizer.core.theme.CFShapes
import androidx.compose.ui.unit.sp
import com.codeforcesvisualizer.core.theme.CFSpace

@Composable
fun CalendarToast(result: CalendarResult, onDismiss: () -> Unit) {
    val colors = CFThemeColors.current
    val shape = CFShapes.card

    val (symbol, message, accent) = when (result) {
        CalendarResult.Added -> Triple("✓", "Added to your calendar", colors.green)
        CalendarResult.PermissionDenied -> Triple("!", "Calendar access is off. Allow it in Settings.", colors.amber)
        CalendarResult.NoCalendarApp -> Triple("!", "No calendar app found", colors.amber)
        CalendarResult.Failed -> Triple("✕", "Couldn't add the event. Try again.", colors.red)
        // The calendar app is already on screen and shows its own confirmation.
        CalendarResult.OpenedCalendarApp -> return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onDismiss() },
        contentAlignment = Alignment.BottomCenter,
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = CFSpace.wideGutter, vertical = 32.dp)
                .clip(shape)
                .background(colors.surface2)
                .border(1.dp, accent.copy(alpha = CFAlpha.BORDER), shape)
                .padding(horizontal = 20.dp, vertical = 14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = symbol,
                    style = CFText.subtitle.bold().copy(color = accent),
                )
                Text(
                    text = "  $message",
                    style = CFText.body.copy(color = colors.fg),
                )
            }
        }
    }
}
