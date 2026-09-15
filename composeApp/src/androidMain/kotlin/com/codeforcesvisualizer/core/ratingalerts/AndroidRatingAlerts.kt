package com.codeforcesvisualizer.core.ratingalerts

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.codeforcesvisualizer.HomeActivity
import com.codeforcesvisualizer.R
import com.codeforcesvisualizer.core.widget.WidgetUpdater
import org.koin.core.context.GlobalContext
import java.util.concurrent.TimeUnit

internal const val RATING_CHECK_WORK = "rating-check"
internal const val RATING_NOTIFICATION_TAG = "rating-change"
private const val CHANNEL_ID = "rating_changes"

class AndroidRatingCheckScheduler(private val context: Context) : RatingCheckScheduler {
    override fun setEnabled(enabled: Boolean) {
        val workManager = WorkManager.getInstance(context)
        if (!enabled) {
            workManager.cancelUniqueWork(RATING_CHECK_WORK)
            return
        }
        val request = PeriodicWorkRequestBuilder<RatingCheckWorker>(RATING_CHECK_INTERVAL_HOURS.toLong(), TimeUnit.HOURS)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES)
            .build()
        // KEEP, because this runs on every app start and must not push the next check back.
        workManager.enqueueUniquePeriodicWork(RATING_CHECK_WORK, ExistingPeriodicWorkPolicy.KEEP, request)
    }
}

class RatingCheckWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val koin = GlobalContext.get()
        return when (koin.get<RatingChangeAlerts>().check()) {
            RatingCheckResult.Failed -> Result.retry()
            else -> {
                // The check may have saved a newer rating, which the widget shows.
                koin.get<WidgetUpdater>().update()
                Result.success()
            }
        }
    }
}

class AndroidRatingChangeNotifier(private val context: Context) : RatingChangeNotifier {

    // Notifications are checked with areNotificationsEnabled(), which covers POST_NOTIFICATIONS.
    @SuppressLint("MissingPermission")
    override fun show(alert: RatingChangeAlert) {
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return

        manager.createNotificationChannel(
            NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_DEFAULT)
                .setName("Rating changes")
                .setDescription("Your new rating after a rated contest")
                .build()
        )

        val openApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, HomeActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_reminder)
            .setContentTitle(alert.title)
            .setContentText(alert.message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(alert.message))
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()

        // The tag keeps contest ids from replacing contest reminder notifications with the same id.
        manager.notify(RATING_NOTIFICATION_TAG, alert.contestId, notification)
    }
}
