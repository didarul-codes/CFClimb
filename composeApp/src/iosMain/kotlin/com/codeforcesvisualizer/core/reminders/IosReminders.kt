package com.codeforcesvisualizer.core.reminders

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.datetime.Clock
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

/** Schedules local notifications; iOS keeps pending ones across restarts, so no rescheduling is needed. */
class IosReminderScheduler : ReminderScheduler {
    private val center = UNUserNotificationCenter.currentNotificationCenter()

    override fun replace(contestId: Int, reminders: List<PlannedReminder>) {
        center.removePendingNotificationRequestsWithIdentifiers(
            ReminderLeadTime.entries.map { reminderId(contestId, it) }
        )
        val now = Clock.System.now().epochSeconds
        reminders.forEach { reminder ->
            val secondsUntilTrigger = reminder.triggerAtEpochSeconds - now
            if (secondsUntilTrigger <= 0) return@forEach

            // Kotlin/Native exposes these as read-only properties plus setter methods on the mutable subclass.
            val content = UNMutableNotificationContent().apply {
                setTitle(reminder.contestName)
                setBody(reminderMessage(reminder.startTimeEpochSeconds, reminder.triggerAtEpochSeconds))
                setSound(UNNotificationSound.defaultSound())
            }
            val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(
                timeInterval = secondsUntilTrigger.toDouble(),
                repeats = false
            )
            center.addNotificationRequest(
                UNNotificationRequest.requestWithIdentifier(reminder.id, content, trigger),
                withCompletionHandler = null
            )
        }
    }
}

@Composable
actual fun rememberNotificationPermissionRequester(): (onResult: (granted: Boolean) -> Unit) -> Unit = remember {
    { onResult ->
        UNUserNotificationCenter.currentNotificationCenter().requestAuthorizationWithOptions(
            UNAuthorizationOptionAlert or UNAuthorizationOptionSound
        ) { granted, _ ->
            dispatch_async(dispatch_get_main_queue()) { onResult(granted) }
        }
    }
}
