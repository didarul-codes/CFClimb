package com.codeforcesvisualizer.contest.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codeforcesvisualizer.shared.core.DataNotFoundError
import com.codeforcesvisualizer.shared.core.Either
import com.codeforcesvisualizer.shared.domain.usecase.ObserveContestUseCase
import com.codeforcesvisualizer.shared.domain.usecase.RefreshContestListUseCase
import kotlinx.datetime.Clock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ContestDetailsViewModel(
    private val observeContestUseCase: ObserveContestUseCase,
    private val refreshContestListUseCase: RefreshContestListUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(ContestDetailsUiState(loading = true))
    val uiState: StateFlow<ContestDetailsUiState> = _uiState

    private val _remainingTimeFlow = MutableStateFlow(0L)
    val remainingTimeFlow: StateFlow<Long> = _remainingTimeFlow

    private var observeJob: Job? = null
    private var countdownJob: Job? = null

    /**
     * Reads the contest from the database, so the screen works after process death and offline.
     * If it isn't cached yet, the contest list is fetched once.
     */
    fun getContestById(id: Int) {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            var refreshed = false
            observeContestUseCase(id).collect { contest ->
                when {
                    contest != null -> {
                        if (_uiState.value.contest?.id != contest.id) {
                            startCountdown(contest.startTimeSeconds.toLong() - Clock.System.now().epochSeconds)
                        }
                        _uiState.value = ContestDetailsUiState(contest = contest)
                    }

                    !refreshed -> {
                        refreshed = true
                        _uiState.value = ContestDetailsUiState(loading = true)
                        val result = refreshContestListUseCase()
                        if (result is Either.Left) {
                            _uiState.value = ContestDetailsUiState(userMessage = result.data.message)
                        }
                    }

                    else -> _uiState.value = ContestDetailsUiState(userMessage = DataNotFoundError().message)
                }
            }
        }
    }

    private fun startCountdown(duration: Long) {
        countdownJob?.cancel()
        val initialDuration = duration.coerceAtLeast(0L)
        if (initialDuration == 0L) {
            _remainingTimeFlow.value = 0
            return
        }

        countdownJob = viewModelScope.launch(Dispatchers.Default) {
            var remainingSeconds = initialDuration
            while (remainingSeconds >= 0 && isActive) {
                _remainingTimeFlow.value = remainingSeconds
                if (remainingSeconds == 0L) break
                delay(1000)
                remainingSeconds--
            }
        }
    }

    override fun onCleared() {
        countdownJob?.cancel()
        super.onCleared()
    }

}
