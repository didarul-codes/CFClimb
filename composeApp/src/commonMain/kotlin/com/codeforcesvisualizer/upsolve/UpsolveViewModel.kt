package com.codeforcesvisualizer.upsolve

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codeforcesvisualizer.core.data.UserSettingsRepository
import com.codeforcesvisualizer.shared.core.AppError
import com.codeforcesvisualizer.shared.core.Either
import com.codeforcesvisualizer.shared.domain.stats.UpsolveProblem
import com.codeforcesvisualizer.shared.domain.stats.UpsolveReason
import com.codeforcesvisualizer.shared.domain.stats.upsolveQueue
import com.codeforcesvisualizer.shared.domain.usecase.ObserveProblemsetUseCase
import com.codeforcesvisualizer.shared.domain.usecase.ObserveUserProfileUseCase
import com.codeforcesvisualizer.shared.domain.usecase.RefreshProblemsetUseCase
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

enum class UpsolveFilter { ALL, TRIED, NOT_OPENED }

sealed interface UpsolveUiState {
    /** The saved handle hasn't been read yet. */
    data object Checking : UpsolveUiState
    data object NoHandle : UpsolveUiState
    data class Loading(val handle: String) : UpsolveUiState
    /** Nothing is saved for the handle and loading it failed. */
    data class Failed(val handle: String, val message: String) : UpsolveUiState
    data class Ready(
        val handle: String,
        /** The queue after [filter]. */
        val items: List<UpsolveProblem>,
        val filter: UpsolveFilter,
        val triedCount: Int,
        val notOpenedCount: Int,
        val hasRatedRounds: Boolean,
        /** False until the problemset is saved; until then only tried problems are listed. */
        val problemsetLoaded: Boolean,
        val refreshing: Boolean,
        /** A refresh failed while saved data is on screen. */
        val refreshError: String,
    ) : UpsolveUiState
}

/** Upsolve queue for the saved handle, built from saved data and refreshed in the background. */
@OptIn(ExperimentalCoroutinesApi::class)
class UpsolveViewModel(
    userSettings: UserSettingsRepository,
    private val observeUserProfile: ObserveUserProfileUseCase,
    private val refreshUserProfile: RefreshUserProfileUseCase,
    private val observeProblemset: ObserveProblemsetUseCase,
    private val refreshProblemset: RefreshProblemsetUseCase,
) : ViewModel() {
    private val handle = userSettings.username.distinctUntilChanged()
    private val filter = MutableStateFlow(UpsolveFilter.ALL)
    private val refresh = MutableStateFlow(RefreshStatus())
    private var refreshJob: Job? = null
    private var currentHandle = ""

    val uiState: StateFlow<UpsolveUiState> = handle.flatMapLatest { savedHandle ->
        if (savedHandle.isBlank()) {
            flowOf(UpsolveUiState.NoHandle)
        } else {
            combine(observeUserProfile(savedHandle), observeProblemset(), filter, refresh) { profile, problemset, selected, status ->
                val ratings = profile.ratings
                val submissions = profile.submissions
                when {
                    ratings != null && submissions != null -> {
                        val queue = upsolveQueue(ratings, submissions, problemset.orEmpty())
                        UpsolveUiState.Ready(
                            handle = savedHandle,
                            items = queue.filter { selected.matches(it) },
                            filter = selected,
                            triedCount = queue.count { it.reason == UpsolveReason.TRIED },
                            notOpenedCount = queue.count { it.reason == UpsolveReason.NOT_OPENED },
                            hasRatedRounds = ratings.isNotEmpty(),
                            problemsetLoaded = problemset != null,
                            refreshing = status.inProgress,
                            refreshError = status.error,
                        )
                    }
                    status.error.isNotEmpty() -> UpsolveUiState.Failed(savedHandle, status.error)
                    else -> UpsolveUiState.Loading(savedHandle)
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, UpsolveUiState.Checking)

    init {
        viewModelScope.launch {
            handle.collectLatest { savedHandle ->
                currentHandle = savedHandle
                if (savedHandle.isNotBlank()) load(savedHandle)
            }
        }
    }

    fun setFilter(selected: UpsolveFilter) {
        filter.value = selected
    }

    /** Pull-to-refresh and retry. */
    fun reload() {
        val savedHandle = currentHandle.ifBlank { return }
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch { load(savedHandle) }
    }

    private suspend fun load(handle: String) {
        refresh.value = RefreshStatus(inProgress = true)
        // One at a time through the API throttle: the handle's own data first, then the large
        // problemset, which is skipped when it was fetched today.
        val error = listOf(
            refreshUserProfile.ratings(handle),
            refreshUserProfile.submissions(handle),
            refreshProblemset(),
        ).firstNotNullOfOrNull { it.errorMessage() }
        refresh.value = RefreshStatus(inProgress = false, error = error.orEmpty())
    }
}

private fun UpsolveFilter.matches(item: UpsolveProblem): Boolean = when (this) {
    UpsolveFilter.ALL -> true
    UpsolveFilter.TRIED -> item.reason == UpsolveReason.TRIED
    UpsolveFilter.NOT_OPENED -> item.reason == UpsolveReason.NOT_OPENED
}

private data class RefreshStatus(val inProgress: Boolean = false, val error: String = "")

private fun Either<AppError, Unit>.errorMessage(): String? = when (this) {
    is Either.Left -> data.message
    is Either.Right -> null
}
