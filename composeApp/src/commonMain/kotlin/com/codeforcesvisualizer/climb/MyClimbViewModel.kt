package com.codeforcesvisualizer.climb

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codeforcesvisualizer.core.data.UserSettingsRepository
import com.codeforcesvisualizer.shared.core.AppError
import com.codeforcesvisualizer.shared.core.Either
import com.codeforcesvisualizer.shared.domain.usecase.ObserveUserProfileUseCase
import com.codeforcesvisualizer.shared.domain.usecase.RefreshUserProfileUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone

sealed interface MyClimbUiState {
    /** The saved handle hasn't been read yet. */
    data object Checking : MyClimbUiState
    data object NoHandle : MyClimbUiState
    data class Loading(val handle: String) : MyClimbUiState
    data class Ready(val summary: ClimbSummary) : MyClimbUiState
    /** Nothing is saved for the handle and loading it failed. */
    data class Failed(val handle: String, val message: String) : MyClimbUiState
}

/**
 * Summary of the saved handle for the home screen. Shows what is saved at once and refreshes
 * it once in the background, so opening the app doesn't wait on the network.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MyClimbViewModel(
    userSettings: UserSettingsRepository,
    private val observeUserProfile: ObserveUserProfileUseCase,
    private val refreshUserProfile: RefreshUserProfileUseCase,
    private val clock: Clock = Clock.System,
    private val timeZone: TimeZone = TimeZone.currentSystemDefault(),
) : ViewModel() {
    private val handle = userSettings.username.distinctUntilChanged()
    private val refresh = MutableStateFlow(RefreshStatus())
    private var refreshJob: Job? = null

    val uiState: StateFlow<MyClimbUiState> = handle.flatMapLatest { savedHandle ->
        if (savedHandle.isBlank()) {
            flowOf(MyClimbUiState.NoHandle)
        } else {
            combine(observeUserProfile(savedHandle), refresh) { profile, status ->
                when {
                    profile.ratings != null || profile.user != null -> MyClimbUiState.Ready(
                        buildClimbSummary(savedHandle, profile.ratings, profile.submissions, clock.now(), timeZone)
                    )
                    status.error.isNotEmpty() -> MyClimbUiState.Failed(savedHandle, status.error)
                    else -> MyClimbUiState.Loading(savedHandle)
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, MyClimbUiState.Checking)

    init {
        viewModelScope.launch {
            handle.collectLatest { savedHandle -> if (savedHandle.isNotBlank()) refresh(savedHandle) }
        }
    }

    fun retry() {
        val current = (uiState.value as? MyClimbUiState.Failed)?.handle ?: return
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch { refresh(current) }
    }

    private suspend fun refresh(handle: String) {
        refresh.value = RefreshStatus(inProgress = true)
        // Requests go through the API throttle one by one; the small ones first, so rating and
        // tier can show before the submission history arrives.
        val error = listOf(
            refreshUserProfile.user(handle),
            refreshUserProfile.ratings(handle),
            refreshUserProfile.submissions(handle),
        ).firstNotNullOfOrNull { it.errorMessage() }
        refresh.value = RefreshStatus(inProgress = false, error = error.orEmpty())
    }
}

private data class RefreshStatus(val inProgress: Boolean = false, val error: String = "")

private fun Either<AppError, Unit>.errorMessage(): String? = when (this) {
    is Either.Left -> data.message
    is Either.Right -> null
}
