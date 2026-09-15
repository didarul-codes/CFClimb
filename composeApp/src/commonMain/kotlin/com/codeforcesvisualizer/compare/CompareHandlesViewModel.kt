package com.codeforcesvisualizer.compare

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codeforcesvisualizer.core.data.RecentSearchRepository
import com.codeforcesvisualizer.shared.core.AppError
import com.codeforcesvisualizer.shared.core.Either
import com.codeforcesvisualizer.shared.domain.usecase.GetUserRatingsByHandleUseCase
import com.codeforcesvisualizer.shared.domain.usecase.GetUserStatusByHandleUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CompareHandlesViewModel(
    private val getUserRatingsByHandleUseCase: GetUserRatingsByHandleUseCase,
    private val getUserStatusByHandleUseCase: GetUserStatusByHandleUseCase,
    private val recentSearchRepository: RecentSearchRepository
) : ViewModel() {
    private val _handle1State = MutableStateFlow("")
    val handle1State: StateFlow<String> = _handle1State

    private val _handle2State = MutableStateFlow("")
    val handle2State: StateFlow<String> = _handle2State

    private val _userRatingState = MutableStateFlow(UserRatingUiState())
    val userRatingState: StateFlow<UserRatingUiState> = _userRatingState

    private val _userStatusState = MutableStateFlow(UserStatusUiState())
    val userStatusState: StateFlow<UserStatusUiState> = _userStatusState

    val recentSearches: StateFlow<List<String>> = recentSearchRepository.recentSearches
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private var compareJob: Job? = null

    fun onHandle1Change(handle1: String){
        _handle1State.value = handle1
    }

    fun onHandle2Change(handle2: String){
        _handle2State.value = handle2
    }

    /** Starting a new comparison cancels requests still queued for the previous one. */
    fun compare(handle1: String, handle2: String) {
        compareJob?.cancel()
        viewModelScope.launch {
            recentSearchRepository.addSearch(handle1)
            recentSearchRepository.addSearch(handle2)
        }

        _userRatingState.value = _userRatingState.value.copy(loading = true)
        _userStatusState.value = _userStatusState.value.copy(loading = true)

        // Calls go through the shared API throttle, which spaces them to respect the rate limit.
        compareJob = viewModelScope.launch {
            val ratings1 = getUserRatingsByHandleUseCase(handle1)
            val ratings2 = getUserRatingsByHandleUseCase(handle2)
            _userRatingState.value = UserRatingUiState(
                loading = false,
                errors = errorsByHandle(handle1 to ratings1, handle2 to ratings2),
                userRatings1 = ratings1.valueOrNull(),
                userRatings2 = ratings2.valueOrNull()
            )

            val status1 = getUserStatusByHandleUseCase(handle1)
            val status2 = getUserStatusByHandleUseCase(handle2)
            _userStatusState.value = UserStatusUiState(
                loading = false,
                errors = errorsByHandle(handle1 to status1, handle2 to status2),
                userStatus1 = status1.valueOrNull(),
                userStatus2 = status2.valueOrNull()
            )
        }
    }

    fun clearRecentSearches() {
        viewModelScope.launch { recentSearchRepository.clearAll() }
    }
}

/** Error message per handle that failed to load, so an error for one user isn't hidden by the other. */
private fun errorsByHandle(vararg results: Pair<String, Either<AppError, *>>): Map<String, String> {
    return results.mapNotNull { (handle, result) ->
        when (result) {
            is Either.Left -> handle.trim() to result.data.message
            is Either.Right -> null
        }
    }.toMap()
}

private fun <V> Either<AppError, V>.valueOrNull(): V? {
    return when (this) {
        is Either.Left -> null
        is Either.Right -> data
    }
}
