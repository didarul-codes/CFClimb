package com.codeforcesvisualizer.core.reminders

import com.codeforcesvisualizer.shared.domain.usecase.ObserveContestListUseCase
import com.codeforcesvisualizer.testing.FakeCFRepository
import com.codeforcesvisualizer.testing.InMemoryPreferencesDataStore
import com.codeforcesvisualizer.testing.contest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val NOW = 1_789_500_000L
private const val HOUR = 3_600L

class ReminderPlannerTest {

    @Test
    fun plansOneReminderPerLeadTimeEarliestFirst() {
        val start = NOW + 3 * 24 * HOUR
        val upcoming = contest(id = 5, scheduled = true, startTimeSeconds = start.toInt())

        val plan = planReminders(upcoming, ReminderLeadTime.entries.toSet(), NOW)

        assertEquals(
            listOf(start - 24 * HOUR, start - HOUR, start - 600),
            plan.map { it.triggerAtEpochSeconds }
        )
        assertEquals("contest-5-OneDay", plan.first().id)
    }

    @Test
    fun leavesOutRemindersWhoseTimeHasPassed() {
        val startsIn30Minutes = contest(id = 5, scheduled = true, startTimeSeconds = (NOW + 1800).toInt())

        val plan = planReminders(startsIn30Minutes, ReminderLeadTime.entries.toSet(), NOW)

        assertEquals(listOf(ReminderLeadTime.TenMinutes), plan.map { it.leadTime })
    }

    @Test
    fun noRemindersForContestsThatAreNotScheduled() {
        val running = contest(id = 5, scheduled = false, startTimeSeconds = (NOW + 7200).toInt())

        assertTrue(planReminders(running, ReminderLeadTime.Default, NOW).isEmpty())
    }

    @Test
    fun messageUsesTheUnitAPersonWouldSay() {
        assertEquals("Starts in 10 min", reminderMessage(NOW + 600, NOW))
        assertEquals("Starts in 10 min", reminderMessage(NOW + 570, NOW))
        assertEquals("Starts in 1 hour", reminderMessage(NOW + HOUR, NOW))
        assertEquals("Starts in 2 hours", reminderMessage(NOW + 2 * HOUR, NOW))
        assertEquals("Starts in 1 day", reminderMessage(NOW + 24 * HOUR, NOW))
        assertEquals("Starting now", reminderMessage(NOW, NOW))
    }
}

class ContestRemindersTest {

    private val repository = FakeCFRepository()
    private val settings = ReminderSettingsRepository(InMemoryPreferencesDataStore())
    private val scheduler = RecordingReminderScheduler()
    private val reminders = ContestReminders(
        observeContestListUseCase = ObserveContestListUseCase(repository),
        settings = settings,
        scheduler = scheduler,
        clock = FixedClock
    )

    @Test
    fun turningOnAReminderSchedulesTheDefaultLeadTimes() = runTest {
        givenUpcomingContest(id = 5)

        reminders.setReminder(5, enabled = true)

        assertEquals(
            listOf(ReminderLeadTime.OneHour, ReminderLeadTime.TenMinutes),
            scheduler.pending[5]?.map { it.leadTime }
        )
        assertEquals(setOf(5), settings.remindedContestIds.first())
    }

    @Test
    fun turningOffAReminderCancelsIt() = runTest {
        givenUpcomingContest(id = 5)
        reminders.setReminder(5, enabled = true)

        reminders.setReminder(5, enabled = false)

        assertEquals(emptyList(), scheduler.pending[5])
        assertEquals(emptySet(), settings.remindedContestIds.first())
    }

    @Test
    fun changingLeadTimesReschedulesSavedReminders() = runTest {
        // Two days out, so the one-day reminder is still in the future.
        givenUpcomingContest(id = 5, startTimeSeconds = NOW + 48 * HOUR)
        reminders.setReminder(5, enabled = true)

        reminders.setLeadTime(ReminderLeadTime.OneDay, enabled = true)

        assertEquals(3, scheduler.pending[5]?.size)
    }

    @Test
    fun rescheduledContestMovesItsReminders() = runTest {
        givenUpcomingContest(id = 5, startTimeSeconds = NOW + 3 * HOUR)
        reminders.setReminder(5, enabled = true)

        givenUpcomingContest(id = 5, startTimeSeconds = NOW + 5 * HOUR)
        reminders.sync()

        assertEquals(NOW + 4 * HOUR, scheduler.pending[5]?.first()?.triggerAtEpochSeconds)
    }

    @Test
    fun remindersForStartedContestsAreDropped() = runTest {
        givenUpcomingContest(id = 5)
        reminders.setReminder(5, enabled = true)

        repository.cachedContests.value = listOf(contest(id = 5, scheduled = false, startTimeSeconds = (NOW - 60).toInt()))
        reminders.sync()

        assertEquals(emptyList(), scheduler.pending[5])
        assertEquals(emptySet(), settings.remindedContestIds.first())
    }

    @Test
    fun reminderIsKeptWhileTheContestListIsNotLoaded() = runTest {
        settings.setReminder(5, enabled = true)

        reminders.sync()

        assertEquals(setOf(5), settings.remindedContestIds.first())
        assertTrue(scheduler.pending.isEmpty())
    }

    private fun givenUpcomingContest(id: Int, startTimeSeconds: Long = NOW + 24 * HOUR) {
        repository.cachedContests.value = listOf(
            contest(id = id, scheduled = true, startTimeSeconds = startTimeSeconds.toInt())
        )
    }
}

private object FixedClock : Clock {
    override fun now(): Instant = Instant.fromEpochSeconds(NOW)
}

private class RecordingReminderScheduler : ReminderScheduler {
    val pending = mutableMapOf<Int, List<PlannedReminder>>()

    override fun replace(contestId: Int, reminders: List<PlannedReminder>) {
        pending[contestId] = reminders
    }
}
