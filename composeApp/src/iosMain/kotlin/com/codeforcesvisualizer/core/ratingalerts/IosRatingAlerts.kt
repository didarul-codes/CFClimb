@file:OptIn(ExperimentalForeignApi::class)

package com.codeforcesvisualizer.core.ratingalerts

import kotlinx.cinterop.ExperimentalForeignApi
import platform.BackgroundTasks.BGAppRefreshTaskRequest
import platform.BackgroundTasks.BGTaskRequest
import platform.BackgroundTasks.BGTaskScheduler
import platform.Foundation.NSDate
import platform.Foundation.dateWithTimeIntervalSinceNow
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter

/** Must match `BGTaskSchedulerPermittedIdentifiers` in Info.plist. */
const val RATING_CHECK_TASK_ID = "com.codeforcesvisualizer.rating-check"

/** iOS decides when background refresh actually runs; the interval is only the earliest time. */
class IosRatingCheckScheduler : RatingCheckScheduler {
    override fun setEnabled(enabled: Boolean) {
        if (enabled) {
            // This runs on every app start. Resubmitting would move the earliest start time back
            // each time, so only submit when no check is pending.
            BGTaskScheduler.sharedScheduler.getPendingTaskRequestsWithCompletionHandler { requests ->
                val pending = requests.orEmpty().any { (it as? BGTaskRequest)?.identifier == RATING_CHECK_TASK_ID }
                if (!pending) submitRatingCheck()
            }
        } else {
            BGTaskScheduler.sharedScheduler.cancelTaskRequestWithIdentifier(RATING_CHECK_TASK_ID)
        }
    }
}

/** Requests the next background check; a new request replaces the pending one. */
internal fun submitRatingCheck() {
    val request = BGAppRefreshTaskRequest(identifier = RATING_CHECK_TASK_ID)
    request.earliestBeginDate = NSDate.dateWithTimeIntervalSinceNow(RATING_CHECK_INTERVAL_HOURS * 3600.0)
    BGTaskScheduler.sharedScheduler.submitTaskRequest(request, error = null)
}

class IosRatingChangeNotifier : RatingChangeNotifier {
    override fun show(alert: RatingChangeAlert) {
        // Kotlin/Native exposes these as read-only properties plus setter methods on the mutable subclass.
        val content = UNMutableNotificationContent().apply {
            setTitle(alert.title)
            setBody(alert.message)
            setSound(UNNotificationSound.defaultSound())
        }
        UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(
            UNNotificationRequest.requestWithIdentifier(
                "rating-${alert.handle}-${alert.contestId}",
                content,
                trigger = null
            ),
            withCompletionHandler = null
        )
    }
}
