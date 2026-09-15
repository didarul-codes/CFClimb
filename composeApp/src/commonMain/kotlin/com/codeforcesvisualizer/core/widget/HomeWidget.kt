package com.codeforcesvisualizer.core.widget

import androidx.compose.runtime.Composable
import com.codeforcesvisualizer.core.data.UserSettingsRepository
import com.codeforcesvisualizer.shared.domain.entity.Contest
import com.codeforcesvisualizer.shared.domain.entity.UserRating
import com.codeforcesvisualizer.shared.domain.usecase.ObserveContestListUseCase
import com.codeforcesvisualizer.shared.domain.usecase.ObserveUserProfileUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

/** What the home screen widget shows. Countdown text is worked out when the widget draws. */
data class WidgetSnapshot(
    val nextRound: NextRound?,
    val handle: String?,
    val rating: Int?,
)

data class NextRound(
    val contestId: Int,
    val name: String,
    val startTimeEpochSeconds: Long,
)

/** The soonest scheduled round that hasn't started, and the latest rating of the saved handle. */
fun buildWidgetSnapshot(
    contests: List<Contest>,
    handle: String,
    ratings: List<UserRating>?,
    nowEpochSeconds: Long,
): WidgetSnapshot {
    val next = contests
        .filter { it.scheduled && it.startTimeSeconds > nowEpochSeconds }
        .minByOrNull { it.startTimeSeconds }
    return WidgetSnapshot(
        nextRound = next?.let { NextRound(it.id, it.name, it.startTimeSeconds.toLong()) },
        handle = handle.ifBlank { null },
        rating = ratings?.maxByOrNull { it.ratingUpdateTimeSeconds }?.newRating,
    )
}

/**
 * Reads widget data from the local database only; the app and background jobs keep it fresh,
 * so drawing a widget never waits on the network.
 */
class WidgetDataSource(
    private val observeContestList: ObserveContestListUseCase,
    private val observeUserProfile: ObserveUserProfileUseCase,
    private val userSettings: UserSettingsRepository,
    private val clock: Clock = Clock.System,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observe(): Flow<WidgetSnapshot> = userSettings.username.flatMapLatest { handle ->
        val ratings = if (handle.isBlank()) flowOf(null) else observeUserProfile(handle).map { it.ratings }
        combine(observeContestList(), ratings) { contests, handleRatings ->
            buildWidgetSnapshot(contests, handle, handleRatings, clock.now().epochSeconds)
        }
    }

    suspend fun snapshot(): WidgetSnapshot = observe().first()
}

/** Redraws placed home screen widgets. */
interface WidgetUpdater {
    suspend fun update()
}

/** Redraws widgets whenever what they show changes, for as long as it is collected. */
class HomeWidgets(
    private val dataSource: WidgetDataSource,
    private val updater: WidgetUpdater,
) {
    suspend fun keepUpdated() {
        dataSource.observe().distinctUntilChanged().collect { updater.update() }
    }
}

/** Asks the launcher to add the widget, or null where that isn't supported. */
@Composable
expect fun rememberAddWidgetAction(): (() -> Unit)?
