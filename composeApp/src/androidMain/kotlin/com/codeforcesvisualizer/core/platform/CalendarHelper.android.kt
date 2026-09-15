package com.codeforcesvisualizer.core.platform

import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.CalendarContract
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberCalendarLauncher(onResult: (CalendarResult) -> Unit): (CalendarEvent) -> Unit {
    val context = LocalContext.current
    val currentOnResult by rememberUpdatedState(onResult)
    return remember(context) {
        { event ->
            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.Events.TITLE, event.title)
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, event.startTimeMillis)
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, event.startTimeMillis + event.durationMillis)
                putExtra(CalendarContract.Events.DESCRIPTION, event.description)
            }
            val result = try {
                context.startActivity(intent)
                CalendarResult.OpenedCalendarApp
            } catch (_: ActivityNotFoundException) {
                CalendarResult.NoCalendarApp
            }
            currentOnResult(result)
        }
    }
}
