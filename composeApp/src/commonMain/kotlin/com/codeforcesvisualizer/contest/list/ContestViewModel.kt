package com.codeforcesvisualizer.contest.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codeforcesvisualizer.shared.core.Either
import com.codeforcesvisualizer.shared.domain.usecase.ObserveContestListUseCase
import com.codeforcesvisualizer.shared.domain.usecase.RefreshContestListUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ContestViewModel(
    observeContestListUseCase: ObserveContestListUseCase,
    private val refreshContestListUseCase: RefreshContestListUseCase
) : ViewModel() {
    private val refreshState = MutableStateFlow(RefreshState())

    /**
     * Shows cached contests right away. A refresh error only replaces the list when nothing is
     * cached; otherwise the saved list stays on screen.
     */
    val uiState: StateFlow<ContestListUiState> = combine(
        observeContestListUseCase(),
        refreshState
    ) { contests, refresh ->
        ContestListUiState(
            refreshing = refresh.inProgress && contests.isNotEmpty(),
            loading = refresh.inProgress && contests.isEmpty(),
            contestList = contests,
            userMessage = if (contests.isEmpty()) refresh.error else "",
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

private data class RefreshState(
    val inProgress: Boolean = false,
    val error: String = "",
)
