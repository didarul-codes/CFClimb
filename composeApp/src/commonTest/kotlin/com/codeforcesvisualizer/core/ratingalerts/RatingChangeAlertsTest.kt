package com.codeforcesvisualizer.core.ratingalerts

import com.codeforcesvisualizer.core.data.UserSettingsRepository
import com.codeforcesvisualizer.shared.core.Either
import com.codeforcesvisualizer.shared.domain.entity.UserRating
import com.codeforcesvisualizer.shared.domain.usecase.ObserveUserProfileUseCase
import com.codeforcesvisualizer.shared.domain.usecase.RefreshUserProfileUseCase
import com.codeforcesvisualizer.testing.FakeCFRepository
import com.codeforcesvisualizer.testing.InMemoryPreferencesDataStore
import com.codeforcesvisualizer.testing.ratingChange
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RatingChangeAlertsTest {

    private val repository = FakeCFRepository()
    private val dataStore = InMemoryPreferencesDataStore()
    private val userSettings = UserSettingsRepository(dataStore)
    private val settings = RatingAlertSettingsRepository(dataStore)
    private val notifier = RecordingNotifier()
    private val scheduler = RecordingScheduler()
    private val alerts = RatingChangeAlerts(
        userSettings = userSettings,
        settings = settings,
        refreshUserProfile = RefreshUserProfileUseCase(repository),
        observeUserProfile = ObserveUserProfileUseCase(repository),
        notifier = notifier,
        scheduler = scheduler,
    )

    @Test
    fun turningOnSchedulesChecksAndRecordsTheCurrentRatingWithoutAnAlert() = runTest {
        givenHandle("tourist", rated(contestId = 1, old = 3700, new = 3750, time = 100))

        alerts.setEnabled(true)

        assertEquals(true, scheduler.enabled)
        assertTrue(notifier.shown.isEmpty())
        assertEquals(SeenRating("tourist", 1), settings.lastSeen.first())
    }

    @Test
    fun newRatedContestShowsOneAlert() = runTest {
        givenHandle("tourist", rated(contestId = 1, old = 3700, new = 3750, time = 100))
        alerts.setEnabled(true)

        // The API lists oldest first, but the latest change is picked by update time.
        givenHandle(
            "tourist",
            rated(contestId = 2, old = 3750, new = 3707, time = 200),
            rated(contestId = 1, old = 3700, new = 3750, time = 100),
        )

        assertEquals(RatingCheckResult.Notified, alerts.check())
        assertEquals(RatingCheckResult.NoChange, alerts.check())
        val alert = notifier.shown.single()
        assertEquals(2, alert.contestId)
        assertEquals(-43, alert.delta)
    }

    @Test
    fun switchingHandlesStartsFromANewBaseline() = runTest {
        givenHandle("tourist", rated(contestId = 1, old = 3700, new = 3750, time = 100))
        alerts.setEnabled(true)

        givenHandle("jiangly", rated(contestId = 7, old = 3600, new = 3650, time = 300))

        assertEquals(RatingCheckResult.Baseline, alerts.check())
        assertTrue(notifier.shown.isEmpty())
    }

    @Test
    fun firstRatedContestOfANewAccountAlerts() = runTest {
        givenHandle("newbie")
        alerts.setEnabled(true)

        givenHandle("newbie", rated(contestId = 3, old = 0, new = 400, time = 100))

        assertEquals(RatingCheckResult.Notified, alerts.check())
    }

    @Test
    fun failedRefreshIsReportedForRetryAndKeepsTheLastSeenContest() = runTest {
        givenHandle("tourist", rated(contestId = 1, old = 3700, new = 3750, time = 100))
        alerts.setEnabled(true)

        repository.ratings.remove("tourist")

        assertEquals(RatingCheckResult.Failed, alerts.check())
        assertEquals(SeenRating("tourist", 1), settings.lastSeen.first())
    }

    @Test
    fun checksAreSkippedWithoutAHandleOrWhenAlertsAreOff() = runTest {
        settings.setEnabled(true)
        assertEquals(RatingCheckResult.Skipped, alerts.check())

        givenHandle("tourist", rated(contestId = 1, old = 3700, new = 3750, time = 100))
        alerts.setEnabled(false)

        assertEquals(RatingCheckResult.Skipped, alerts.check())
        assertEquals(false, scheduler.enabled)
    }

    @Test
    fun alertTextShowsTheSignedChange() {
        val alert = RatingChangeAlert(
            handle = "tourist",
            contestId = 2,
            contestName = "Codeforces Round 2",
            oldRating = 3750,
            newRating = 3707,
            rank = 12,
        )

        assertEquals("tourist: -43", alert.title)
        assertEquals("Codeforces Round 2: 3750 → 3707, rank 12", alert.message)
        assertEquals("tourist: +43", alert.copy(oldRating = 3707, newRating = 3750).title)
    }

    private suspend fun givenHandle(handle: String, vararg ratings: UserRating) {
        userSettings.setUsername(handle)
        repository.ratings[handle] = Either.Right(ratings.toList())
    }

    private fun rated(contestId: Int, old: Int, new: Int, time: Long) = ratingChange(old, new).copy(
        contestId = contestId,
        contestName = "Codeforces Round $contestId",
        ratingUpdateTimeSeconds = time,
        rank = 12,
    )
}

private class RecordingNotifier : RatingChangeNotifier {
    val shown = mutableListOf<RatingChangeAlert>()

    override fun show(alert: RatingChangeAlert) {
        shown += alert
    }
}

private class RecordingScheduler : RatingCheckScheduler {
    var enabled: Boolean? = null

    override fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }
}
