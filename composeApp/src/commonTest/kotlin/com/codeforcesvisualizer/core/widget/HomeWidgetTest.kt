package com.codeforcesvisualizer.core.widget

import com.codeforcesvisualizer.core.data.UserSettingsRepository
import com.codeforcesvisualizer.core.theme.rankTierFor
import com.codeforcesvisualizer.core.utils.formatStartShort
import com.codeforcesvisualizer.core.utils.formatTimeUntil
import com.codeforcesvisualizer.shared.core.Either
import com.codeforcesvisualizer.shared.domain.usecase.ObserveContestListUseCase
import com.codeforcesvisualizer.shared.domain.usecase.ObserveUserProfileUseCase
import com.codeforcesvisualizer.testing.FakeCFRepository
import com.codeforcesvisualizer.testing.InMemoryPreferencesDataStore
import com.codeforcesvisualizer.testing.contest
import com.codeforcesvisualizer.testing.ratingChange
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private const val NOW = 1_789_500_000L
private const val HOUR = 3_600L

class HomeWidgetTest {

    @Test
    fun nextRoundIsTheSoonestScheduledOneThatHasNotStarted() {
        val contests = listOf(
            contest(id = 1, scheduled = true, startTimeSeconds = (NOW + 48 * HOUR).toInt()),
            contest(id = 2, scheduled = true, startTimeSeconds = (NOW + 5 * HOUR).toInt()),
            contest(id = 3, scheduled = true, startTimeSeconds = (NOW - HOUR).toInt()),
            contest(id = 4, scheduled = false, startTimeSeconds = (NOW + HOUR).toInt()),
        )

        val snapshot = buildWidgetSnapshot(contests, handle = "", ratings = null, nowEpochSeconds = NOW)

        assertEquals(NextRound(2, "Codeforces Round 2", NOW + 5 * HOUR), snapshot.nextRound)
    }

    @Test
    fun noUpcomingRoundAndNoHandleLeaveTheWidgetEmpty() {
        val snapshot = buildWidgetSnapshot(emptyList(), handle = " ", ratings = null, nowEpochSeconds = NOW)

        assertEquals(WidgetSnapshot(nextRound = null, handle = null, rating = null), snapshot)
    }

    @Test
    fun ratingComesFromTheLatestChange() {
        val ratings = listOf(
            ratingChange(3700, 3750).copy(contestId = 2, ratingUpdateTimeSeconds = 200),
            ratingChange(1500, 3700).copy(contestId = 1, ratingUpdateTimeSeconds = 100),
        )

        val snapshot = buildWidgetSnapshot(emptyList(), "tourist", ratings, NOW)

        assertEquals("tourist", snapshot.handle)
        assertEquals(3750, snapshot.rating)
    }

    @Test
    fun labelsUseShortUnits() {
        assertEquals("6d 4h", formatTimeUntil(NOW + 6 * 24 * HOUR + 4 * HOUR + 59, NOW))
        assertEquals("3h 5m", formatTimeUntil(NOW + 3 * HOUR + 300, NOW))
        assertEquals("42m", formatTimeUntil(NOW + 42 * 60, NOW))
        assertEquals("now", formatTimeUntil(NOW, NOW))
        assertEquals(
            "Mon 2:35 PM",
            formatStartShort(Instant.parse("2026-09-21T14:35:00Z").epochSeconds, TimeZone.UTC)
        )
    }

    @Test
    fun tierMatchesCodeforcesRanges() {
        assertEquals("legendary grandmaster", rankTierFor(3301).name)
        assertEquals("specialist", rankTierFor(1500).name)
        assertEquals("newbie", rankTierFor(0).name)
    }

    @Test
    fun dataSourceFollowsTheSavedHandleAndCachedData() = runTest {
        val repository = FakeCFRepository()
        val userSettings = UserSettingsRepository(InMemoryPreferencesDataStore())
        val dataSource = WidgetDataSource(
            observeContestList = ObserveContestListUseCase(repository),
            observeUserProfile = ObserveUserProfileUseCase(repository),
            userSettings = userSettings,
            clock = object : Clock {
                override fun now(): Instant = Instant.fromEpochSeconds(NOW)
            },
        )
        repository.cachedContests.value = listOf(contest(id = 9, scheduled = true, startTimeSeconds = (NOW + HOUR).toInt()))
        repository.ratings["tourist"] = Either.Right(listOf(ratingChange(3700, 3750)))
        repository.refreshUserRatings("tourist")

        assertNull(dataSource.snapshot().rating)

        userSettings.setUsername("tourist")

        val snapshot = dataSource.snapshot()
        assertEquals(9, snapshot.nextRound?.contestId)
        assertEquals(3750, snapshot.rating)
    }
}
