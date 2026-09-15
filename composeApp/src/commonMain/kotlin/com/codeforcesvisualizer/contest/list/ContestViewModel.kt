package com.codeforcesvisualizer.contest.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codeforcesvisualizer.shared.core.Either
import com.codeforcesvisualizer.shared.domain.usecase.ObserveContestListUseCase
import com.codeforcesvisualizer.shared.domain.usecase.RefreshContestListUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.seconds

class ContestViewModel(
    observeContestListUseCase: ObserveContestListUseCase,
    private val refreshContestListUseCase: RefreshContestListUseCase,
    /** Current time in epoch seconds; ticks so rounds move from upcoming to live to past on screen. */
    now: Flow<Long> = ticker(),
) : ViewModel() {
    private val refreshState = MutableStateFlow(RefreshState())

    /**
     * Shows cached contests right away. A refresh error only replaces the list when nothing is
     * cached; otherwise the saved list stays on screen with a retry banner.
     */
    val uiState: StateFlow<ContestListUiState> = combine(
        observeContestListUseCase(),
        refreshState,
        now
    ) { contests, refresh, nowSeconds ->
        ContestListUiState(
            refreshing = refresh.inProgress && contests.isNotEmpty(),
            loading = refresh.inProgress && contests.isEmpty(),
            contestList = contests,
            groups = groupContests(contests, nowSeconds),
            nowEpochSeconds = nowSeconds,
            userMessage = if (contests.isEmpty()) refresh.error else "",
            refreshError = if (contests.isNotEmpty()) refresh.error else "",
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ContestListUiState(loading = true))

    init {
        refreshContestList()
    }

    fun refreshContestList() {
        if (refreshState.value.inProgress) return
        refreshState.value = RefreshState(inProgress = true)
        viewModelScope.launch {
            val error = when (val result = refreshContestListUseCase()) {
                is Either.Left -> result.data.message
                is Either.Right -> ""
            }
            refreshState.value = RefreshState(inProgress = false, error = error)
        }
    }
}

private fun ticker(clock: Clock = Clock.System): Flow<Long> = flow {
    while (true) {
        emit(clock.now().epochSeconds)
        delay(30.seconds)
    }
}

private data class RefreshState(
    val inProgress: Boolean = false,
    val error: String = "",
)
