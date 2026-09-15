@file:OptIn(ExperimentalForeignApi::class)

package com.codeforcesvisualizer.core.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.EventKit.EKEvent
import platform.EventKit.EKEventStore
import platform.EventKit.EKSpan
import platform.Foundation.NSDate
import platform.Foundation.NSError
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

@Composable
actual fun rememberCalendarLauncher(onResult: (CalendarResult) -> Unit): (CalendarEvent) -> Unit {
    val currentOnResult by rememberUpdatedState(onResult)
    val store = remember { EKEventStore() }
    return remember(store) {
        { event ->
            // Write-only access is enough to add events and never exposes the user's calendar.
            store.requestWriteOnlyAccessToEventsWithCompletion { granted, _ ->
                val result = if (granted) store.save(event) else CalendarResult.PermissionDenied
                dispatch_async(dispatch_get_main_queue()) { currentOnResult(result) }
            }
        }
    }
}

private fun EKEventStore.save(event: CalendarEvent): CalendarResult = memScoped {
    val defaultCalendar = defaultCalendarForNewEvents ?: return CalendarResult.Failed
    val ekEvent = EKEvent.eventWithEventStore(this@save).apply {
        title = event.title
        startDate = NSDate.dateWithTimeIntervalSince1970(event.startTimeMillis / 1000.0)
        endDate = NSDate.dateWithTimeIntervalSince1970((event.startTimeMillis + event.durationMillis) / 1000.0)
        notes = event.description
        calendar = defaultCalendar
    }
    val error = alloc<ObjCObjectVar<NSError?>>()
    if (saveEvent(ekEvent, EKSpan.EKSpanThisEvent, error.ptr)) CalendarResult.Added else CalendarResult.Failed
}
