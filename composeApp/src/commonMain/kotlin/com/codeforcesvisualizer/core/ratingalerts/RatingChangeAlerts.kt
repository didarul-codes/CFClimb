package com.codeforcesvisualizer.core.ratingalerts

import com.codeforcesvisualizer.core.data.UserSettingsRepository
import com.codeforcesvisualizer.shared.core.Either
import com.codeforcesvisualizer.shared.domain.usecase.ObserveUserProfileUseCase
import com.codeforcesvisualizer.shared.domain.usecase.RefreshUserProfileUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Tells the user when the rating of their saved handle changes. Uses `user.rating` for that one
 * handle, which is a small response, instead of the full `contest.ratingChanges` of every round.
 */
class RatingChangeAlerts(
    private val userSettings: UserSettingsRepository,
    private val settings: RatingAlertSettingsRepository,
    private val refreshUserProfile: RefreshUserProfileUseCase,
    private val observeUserProfile: ObserveUserProfileUseCase,
    private val notifier: RatingChangeNotifier,
    private val scheduler: RatingCheckScheduler,
) {
    private val mutex = Mutex()

    val enabled: Flow<Boolean> = settings.enabled

    suspend fun setEnabled(enabled: Boolean) {
        settings.setEnabled(enabled)
        scheduler.setEnabled(enabled)
        // Records the current rating now, so the first background check can already alert.
        if (enabled) check()
    }

    /** Keeps the background check registered to match the setting, for as long as it is collected. */
    suspend fun keepScheduled() {
        settings.enabled.distinctUntilChanged().collect { scheduler.setEnabled(it) }
    }

    /**
     * Loads the latest ratings and alerts about a rated contest newer than the last one seen. The
     * first check for a handle only records where it stands, so turning alerts on or switching
     * handles never brings up an old contest.
     */
    suspend fun check(): RatingCheckResult = mutex.withLock {
        val handle = userSettings.username.first()
        if (handle.isBlank() || !settings.enabled.first()) return RatingCheckResult.Skipped

        if (refreshUserProfile.ratings(handle) is Either.Left) return RatingCheckResult.Failed
        val ratings = observeUserProfile(handle).first().ratings ?: return RatingCheckResult.Failed

        val latest = ratings.maxByOrNull { it.ratingUpdateTimeSeconds }
        val lastSeen = settings.lastSeen.first()?.takeIf { it.handle.equals(handle, ignoreCase = true) }
        val latestContestId = latest?.contestId ?: NO_RATED_CONTEST

        val result = when {
            lastSeen == null -> RatingCheckResult.Baseline
            latest == null || latest.contestId == lastSeen.contestId -> RatingCheckResult.NoChange
            else -> {
                notifier.show(
                    RatingChangeAlert(
                        handle = handle,
                        contestId = latest.contestId,
                        contestName = latest.contestName,
                        oldRating = latest.oldRating,
                        newRating = latest.newRating,
                        rank = latest.rank,
                    )
                )
                RatingCheckResult.Notified
            }
        }
        if (lastSeen?.contestId != latestContestId) {
            settings.setLastSeen(SeenRating(handle, latestContestId))
        }
        result
    }

    private companion object {
        // Recorded for handles without rated contests, so their first one still alerts.
        const val NO_RATED_CONTEST = 0
    }
}
