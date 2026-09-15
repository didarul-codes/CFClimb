package com.codeforcesvisualizer.core.reminders

import com.codeforcesvisualizer.shared.domain.usecase.ObserveContestListUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock

/**
 * Turns reminder choices into scheduled platform reminders, using start times from the saved
 * contest list so a rescheduled round moves its reminders too.
 */
class ContestReminders(
    private val observeContestListUseCase: ObserveContestListUseCase,
    private val settings: ReminderSettingsRepository,
    private val scheduler: ReminderScheduler,
    private val clock: Clock = Clock.System,
) {
    private val mutex = Mutex()

    val remindedContestIds: Flow<Set<Int>> = settings.remindedContestIds
    val leadTimes: Flow<Set<ReminderLeadTime>> = settings.leadTimes

    suspend fun setReminder(contestId: Int, enabled: Boolean) {
        settings.setReminder(contestId, enabled)
        if (enabled) sync() else mutex.withLock { scheduler.replace(contestId, emptyList()) }
    }

    suspend fun setLeadTime(leadTime: ReminderLeadTime, enabled: Boolean) {
        settings.setLeadTime(leadTime, enabled)
        sync()
    }

    /** Re-plans reminders whenever contests or reminder settings change, for as long as it is collected. */
    suspend fun keepScheduled() {
        combine(
            observeContestListUseCase(),
            settings.remindedContestIds,
            settings.leadTimes
        ) { _, _, _ -> }
            .collectLatest { sync() }
    }

    /**
     * Re-plans every reminder from saved data. Reminders for contests that already started are
     * dropped; a contest missing from the cache keeps its reminder until the list loads.
     */
    suspend fun sync() = mutex.withLock {
        val contests = observeContestListUseCase().first().associateBy { it.id }
        val leadTimes = settings.leadTimes.first()
        val now = clock.now().epochSeconds
        val finished = mutableSetOf<Int>()

        for (contestId in settings.remindedContestIds.first()) {
            val contest = contests[contestId] ?: continue
            scheduler.replace(contestId, planReminders(contest, leadTimes, now))
            if (!contest.scheduled || contest.startTimeSeconds <= now) finished += contestId
        }

        if (finished.isNotEmpty()) settings.removeReminders(finished)
    }
}
