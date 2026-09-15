package com.codeforcesvisualizer.core.reminders

import android.Manifest
import android.app.AlarmManager
import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlarmManager

// SDK 34 keeps Robolectric on Java 17+; SDK 35 and up need Java 21.
@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class AndroidRemindersTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val scheduler = AndroidReminderScheduler(context)
    private val alarms get() = shadowOf(context.getSystemService(AlarmManager::class.java)).scheduledAlarms
    private val notifications get() = shadowOf(context.getSystemService(NotificationManager::class.java)).allNotifications

    @Test
    fun withExactAccessAlarmsFireAtTheReminderTimeEvenWhenIdle() {
        ShadowAlarmManager.setCanScheduleExactAlarms(true)

        scheduler.replace(5, reminders(5, ReminderLeadTime.OneHour, ReminderLeadTime.TenMinutes))

        assertEquals(2, alarms.size)
        assertEquals(TRIGGER_AT * 1000, alarms.minOf { it.triggerAtMs })
        assertTrue(alarms.all { it.isAllowWhileIdle })
    }

    @Test
    fun withoutExactAccessTheDeliveryWindowEndsAtTheReminderTime() {
        ShadowAlarmManager.setCanScheduleExactAlarms(false)

        scheduler.replace(5, reminders(5, ReminderLeadTime.TenMinutes))

        val alarm = alarms.single()
        assertEquals(FALLBACK_WINDOW_MILLIS, alarm.windowLengthMs)
        assertEquals(TRIGGER_AT * 1000, alarm.triggerAtMs + alarm.windowLengthMs)
    }

    @Test
    fun reminderArrivingAfterTheStartIsDropped() {
        shadowOf(context as Application).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)

        ContestReminderReceiver().onReceive(
            context,
            Intent(ACTION_CONTEST_REMINDER)
                .putExtra(EXTRA_CONTEST_ID, 5)
                .putExtra(EXTRA_CONTEST_NAME, "Codeforces Round (Div. 2)")
                .putExtra(EXTRA_START_TIME, System.currentTimeMillis() / 1000 - 60)
        )

        assertEquals(0, notifications.size)
    }

    @Test
    fun replacingRemindersCancelsTheOldAlarms() {
        scheduler.replace(5, reminders(5, ReminderLeadTime.OneDay, ReminderLeadTime.OneHour))

        scheduler.replace(5, reminders(5, ReminderLeadTime.TenMinutes))

        assertEquals(1, alarms.size)
    }

    @Test
    fun emptyReplacementCancelsOnlyThatContest() {
        scheduler.replace(5, reminders(5, ReminderLeadTime.OneHour))
        scheduler.replace(6, reminders(6, ReminderLeadTime.OneHour))

        scheduler.replace(5, emptyList())

        assertEquals(1, alarms.size)
    }

    @Test
    fun firedReminderPostsANotification() {
        shadowOf(context as Application).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)

        ContestReminderReceiver().onReceive(
            context,
            Intent(ACTION_CONTEST_REMINDER)
                .putExtra(EXTRA_CONTEST_ID, 5)
                .putExtra(EXTRA_CONTEST_NAME, "Codeforces Round (Div. 2)")
                .putExtra(EXTRA_START_TIME, System.currentTimeMillis() / 1000 + 600)
        )

        val notification = notifications.single()
        assertEquals("Codeforces Round (Div. 2)", shadowOf(notification).contentTitle)
        assertEquals("Starts in 10 min", shadowOf(notification).contentText)
    }

    @Test
    fun noNotificationWhenNotificationsAreBlocked() {
        // On Android 13+ a missing POST_NOTIFICATIONS grant also reports notifications as disabled.
        shadowOf(context.getSystemService(NotificationManager::class.java)).setNotificationsEnabled(false)

        ContestReminderReceiver().onReceive(
            context,
            Intent(ACTION_CONTEST_REMINDER)
                .putExtra(EXTRA_CONTEST_ID, 5)
                .putExtra(EXTRA_CONTEST_NAME, "Codeforces Round (Div. 2)")
                .putExtra(EXTRA_START_TIME, System.currentTimeMillis() / 1000 + 600)
        )

        assertEquals(0, notifications.size)
    }

    private fun reminders(contestId: Int, vararg leadTimes: ReminderLeadTime) = leadTimes.mapIndexed { index, leadTime ->
        PlannedReminder(
            id = reminderId(contestId, leadTime),
            contestId = contestId,
            contestName = "Codeforces Round $contestId",
            startTimeEpochSeconds = TRIGGER_AT + 86_400,
            triggerAtEpochSeconds = TRIGGER_AT + index * 60L,
            leadTime = leadTime
        )
    }

    private companion object {
        const val TRIGGER_AT = 4_102_444_800L // 2100-01-01
    }
}
