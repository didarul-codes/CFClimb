package com.codeforcesvisualizer.core.reminders

import com.codeforcesvisualizer.shared.domain.entity.Contest

enum class ReminderLeadTime(val minutes: Int, val label: String) {
    OneDay(24 * 60, "1 day"),
    OneHour(60, "1 hour"),
    TenMinutes(10, "10 min");

    companion object {
        val Default: Set<ReminderLeadTime> = setOf(OneHour, TenMinutes)
    }
}

data class PlannedReminder(
    val id: String,
    val contestId: Int,
    val contestName: String,
    val startTimeEpochSeconds: Long,
    val triggerAtEpochSeconds: Long,
    val leadTime: ReminderLeadTime,
)

/** Delivers reminders through the platform: alarms and notifications on Android, local notifications on iOS. */
interface ReminderScheduler {
    /** Replaces every pending reminder for [contestId] with [reminders]; an empty list cancels them. */
    fun replace(contestId: Int, reminders: List<PlannedReminder>)
}

fun reminderId(contestId: Int, leadTime: ReminderLeadTime): String = "contest-$contestId-${leadTime.name}"

/** One reminder per lead time, earliest first. Times that already passed are left out. */
fun planReminders(
    contest: Contest,
    leadTimes: Set<ReminderLeadTime>,
    nowEpochSeconds: Long,
): List<PlannedReminder> {
    if (!contest.scheduled) return emptyList()
    val start = contest.startTimeSeconds.toLong()
    return leadTimes
        .map { leadTime ->
            PlannedReminder(
                id = reminderId(contest.id, leadTime),
                contestId = contest.id,
                contestName = contest.name,
                startTimeEpochSeconds = start,
                triggerAtEpochSeconds = start - leadTime.minutes * 60L,
                leadTime = leadTime,
            )
        }
        .filter { it.triggerAtEpochSeconds > nowEpochSeconds }
        .sortedBy { it.triggerAtEpochSeconds }
}

/** Notification text such as "Starts in 10 min", rounded to the unit a person would say. */
fun reminderMessage(startTimeEpochSeconds: Long, nowEpochSeconds: Long): String {
    val minutes = (startTimeEpochSeconds - nowEpochSeconds + 59) / 60
    return when {
        minutes <= 0 -> "Starting now"
        minutes < 60 -> "Starts in $minutes min"
        minutes < 24 * 60 -> plural((minutes + 30) / 60, "hour")
        else -> plural((minutes + 12 * 60) / (24 * 60), "day")
    }
}

private fun plural(count: Long, unit: String): String =
    "Starts in $count $unit" + if (count == 1L) "" else "s"
