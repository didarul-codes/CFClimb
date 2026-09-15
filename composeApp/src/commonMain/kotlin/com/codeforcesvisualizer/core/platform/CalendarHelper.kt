package com.codeforcesvisualizer.core.platform

import androidx.compose.runtime.Composable
import com.codeforcesvisualizer.shared.data.config.BASE_URL
import com.codeforcesvisualizer.shared.domain.entity.Contest

data class CalendarEvent(
    val title: String,
    val startTimeMillis: Long,
    val durationMillis: Long,
    val description: String = "",
)

enum class CalendarResult {
    /** The event was saved to the user's calendar. */
    Added,

    /** A calendar app opened to let the user confirm the event; the outcome is unknown. */
    OpenedCalendarApp,
    PermissionDenied,
    NoCalendarApp,
    Failed,
}

fun Contest.toCalendarEvent() = CalendarEvent(
    title = name,
    startTimeMillis = startTimeSeconds.toLong() * 1000,
    durationMillis = durationSeconds.toLong() * 1000,
    description = "Codeforces contest: $BASE_URL/contests/$id",
)

/**
 * Returns a launcher that adds an event to the system calendar. [onResult] is called on the
 * main thread once the platform reports what happened.
 */
@Composable
expect fun rememberCalendarLauncher(onResult: (CalendarResult) -> Unit): (CalendarEvent) -> Unit
