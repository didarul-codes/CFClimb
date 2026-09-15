package com.codeforcesvisualizer.core.reminders

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.codeforcesvisualizer.HomeActivity
import com.codeforcesvisualizer.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext

internal const val ACTION_CONTEST_REMINDER = "com.codeforcesvisualizer.action.CONTEST_REMINDER"
internal const val EXTRA_CONTEST_ID = "contest_id"
internal const val EXTRA_CONTEST_NAME = "contest_name"
internal const val EXTRA_START_TIME = "start_time_epoch_seconds"

class AndroidReminderScheduler(private val context: Context) : ReminderScheduler {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    override fun replace(contestId: Int, reminders: List<PlannedReminder>) {
        ReminderLeadTime.entries.forEach { leadTime ->
            alarmManager.cancel(reminderIntent(contestId, leadTime, reminder = null))
        }
        reminders.forEach { reminder ->
            // Inexact on purpose: exact alarms need SCHEDULE_EXACT_ALARM or USE_EXACT_ALARM, which
            // Google Play reserves for alarm clock and calendar apps. Allow-while-idle still fires
            // during Doze, typically within a few minutes of the requested time.
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminder.triggerAtEpochSeconds * 1000,
                reminderIntent(reminder.contestId, reminder.leadTime, reminder)
            )
        }
    }

    private fun reminderIntent(contestId: Int, leadTime: ReminderLeadTime, reminder: PlannedReminder?): PendingIntent {
        val intent = Intent(context, ContestReminderReceiver::class.java).apply {
            action = ACTION_CONTEST_REMINDER
            // The data URI makes each contest and lead time a distinct alarm; extras don't count.
            data = Uri.parse("cfclimb://reminders/$contestId/${leadTime.name}")
            if (reminder != null) {
                putExtra(EXTRA_CONTEST_ID, reminder.contestId)
                putExtra(EXTRA_CONTEST_NAME, reminder.contestName)
                putExtra(EXTRA_START_TIME, reminder.startTimeEpochSeconds)
            }
        }
        return PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}

/** Shows the notification when a reminder alarm fires. */
class ContestReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_CONTEST_REMINDER) return
        ReminderNotifications.show(
            context = context,
            contestId = intent.getIntExtra(EXTRA_CONTEST_ID, 0),
            contestName = intent.getStringExtra(EXTRA_CONTEST_NAME) ?: return,
            startTimeEpochSeconds = intent.getLongExtra(EXTRA_START_TIME, 0)
        )
    }
}

/** Alarms don't survive a reboot or an app update, and time changes shift them: schedule them again. */
class RescheduleRemindersReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            try {
                GlobalContext.get().get<ContestReminders>().sync()
            } finally {
                pendingResult.finish()
            }
        }
    }
}

internal object ReminderNotifications {
    private const val CHANNEL_ID = "contest_reminders"

    // Notifications are checked with areNotificationsEnabled(), which covers POST_NOTIFICATIONS.
    @SuppressLint("MissingPermission")
    fun show(context: Context, contestId: Int, contestName: String, startTimeEpochSeconds: Long) {
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return

        manager.createNotificationChannel(
            NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_HIGH)
                .setName("Contest reminders")
                .setDescription("Alerts before contests you asked to be reminded about")
                .build()
        )

        val openApp = PendingIntent.getActivity(
            context,
            contestId,
            Intent(context, HomeActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_reminder)
            .setContentTitle(contestName)
            .setContentText(reminderMessage(startTimeEpochSeconds, System.currentTimeMillis() / 1000))
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()

        manager.notify(contestId, notification)
    }
}
