package com.codeforcesvisualizer

import com.codeforcesvisualizer.core.ratingalerts.RATING_CHECK_TASK_ID
import com.codeforcesvisualizer.core.ratingalerts.RatingChangeAlerts
import com.codeforcesvisualizer.core.ratingalerts.RatingCheckResult
import com.codeforcesvisualizer.core.ratingalerts.submitRatingCheck
import com.codeforcesvisualizer.core.widget.WidgetUpdater
import com.codeforcesvisualizer.inject.initKoin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform
import platform.BackgroundTasks.BGAppRefreshTask
import platform.BackgroundTasks.BGTaskScheduler

/**
 * Registers background task handlers. Called from the Swift app's `init`, because iOS only accepts
 * registrations made before the app finishes launching.
 */
fun registerBackgroundTasks() {
    initKoin()
    BGTaskScheduler.sharedScheduler.registerForTaskWithIdentifier(RATING_CHECK_TASK_ID, usingQueue = null) { task ->
        val refreshTask = task as? BGAppRefreshTask ?: return@registerForTaskWithIdentifier
        // Background refresh requests run once, so ask for the next check straight away.
        submitRatingCheck()

        val job = CoroutineScope(Dispatchers.Default).launch {
            val koin = KoinPlatform.getKoin()
            val result = koin.get<RatingChangeAlerts>().check()
            if (result == RatingCheckResult.Skipped) {
                BGTaskScheduler.sharedScheduler.cancelTaskRequestWithIdentifier(RATING_CHECK_TASK_ID)
            }
            // The check may have saved a newer rating, which the widget shows.
            if (result != RatingCheckResult.Failed) koin.get<WidgetUpdater>().update()
            refreshTask.setTaskCompletedWithSuccess(result != RatingCheckResult.Failed)
        }
        refreshTask.expirationHandler = {
            job.cancel()
            refreshTask.setTaskCompletedWithSuccess(false)
        }
    }
}
