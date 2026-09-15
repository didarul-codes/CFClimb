package com.codeforcesvisualizer.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codeforcesvisualizer.core.platform.CalendarResult
import com.codeforcesvisualizer.core.theme.CFThemeColors

@Composable
fun CalendarToast(result: CalendarResult, onDismiss: () -> Unit) {
    val colors = CFThemeColors.current
    val shape = RoundedCornerShape(10.dp)

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
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .clip(shape)
                .background(colors.surface2)
                .border(1.dp, accent.copy(alpha = 0.3f), shape)
                .padding(horizontal = 20.dp, vertical = 14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = symbol,
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = accent,
                        fontWeight = FontWeight.Bold,
                    ),
                )
                Text(
                    text = "  $message",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = colors.fg,
                    ),
                )
            }
        }
    }
}
