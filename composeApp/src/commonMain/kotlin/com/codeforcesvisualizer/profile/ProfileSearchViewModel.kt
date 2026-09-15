package com.codeforcesvisualizer.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codeforcesvisualizer.core.data.RecentSearchRepository
import com.codeforcesvisualizer.shared.core.AppError
import com.codeforcesvisualizer.shared.core.Either
import com.codeforcesvisualizer.shared.domain.entity.UserProfile
import com.codeforcesvisualizer.shared.domain.usecase.ObserveUserProfileUseCase
import com.codeforcesvisualizer.shared.domain.usecase.RefreshUserProfileUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileSearchViewModel(
    private val observeUserProfileUseCase: ObserveUserProfileUseCase,
    private val refreshUserProfileUseCase: RefreshUserProfileUseCase,
    private val recentSearchRepository: RecentSearchRepository
) : ViewModel() {
    private val _searchTextState = MutableStateFlow("")
    val searchTextState: StateFlow<String> = _searchTextState

    private val activeHandle = MutableStateFlow<String?>(null)
    private val refreshStatus = MutableStateFlow(ProfileRefreshStatus())

    /** Cached data for the searched handle; updates as each refresh writes to the database. */
    private val profile = activeHandle.flatMapLatest { handle ->
        if (handle == null) flowOf(EmptyProfile) else observeUserProfileUseCase(handle)
    }

    // A refresh error only shows when nothing is cached for that part of the profile.
    val userInfoState: StateFlow<UserInfoUiState> = combine(profile, refreshStatus) { profile, status ->
        UserInfoUiState(
            loading = status.user.loading,
            userMessage = if (profile.user == null) status.user.error else "",
            user = profile.user
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, UserInfoUiState())

    val userRatingState: StateFlow<UserRatingUiState> = combine(profile, refreshStatus) { profile, status ->
        UserRatingUiState(
            loading = status.ratings.loading,
            userMessage = if (profile.ratings == null) status.ratings.error else "",
            userRatings = profile.ratings
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, UserRatingUiState())

    val userStatusState: StateFlow<UserStatusUiState> = combine(profile, refreshStatus) { profile, status ->
        UserStatusUiState(
            loading = status.submissions.loading,
            userMessage = if (profile.submissions == null) status.submissions.error else "",
            userStatus = profile.submissions
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, UserStatusUiState())

    /** True when saved data is on screen because refreshing it failed, for example offline. */
    val showingSavedData: StateFlow<Boolean> = combine(profile, refreshStatus) { profile, status ->
        profile.user != null && status.user.error.isNotEmpty()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val recentSearches: StateFlow<List<String>> = recentSearchRepository.recentSearches
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private var searchJob: Job? = null

    fun onSearchTextChanged(text: String) {
        _searchTextState.value = text
    }

    /**
     * Shows the saved profile for [handle] at once, then refreshes it. Starting a new search
     * cancels requests still queued for the previous one.
     */
    fun search(handle: String) {
        if (handle.isBlank()) return
        searchJob?.cancel()
        viewModelScope.launch { recentSearchRepository.addSearch(handle) }

        activeHandle.value = handle.trim()
        refreshStatus.value = ProfileRefreshStatus(
            user = PartStatus(loading = true),
            ratings = PartStatus(loading = true),
            submissions = PartStatus(loading = true)
        )

        // Requests run one after another through the API throttle; the small responses go first
        // so the header and rating appear before the submission history arrives.
        searchJob = viewModelScope.launch {
            val user = refreshUserProfileUseCase.user(handle).toPartStatus()
            refreshStatus.update { it.copy(user = user) }
            val ratings = refreshUserProfileUseCase.ratings(handle).toPartStatus()
            refreshStatus.update { it.copy(ratings = ratings) }
            val submissions = refreshUserProfileUseCase.submissions(handle).toPartStatus()
            refreshStatus.update { it.copy(submissions = submissions) }
        }
    }

    fun clearRecentSearches() {
        viewModelScope.launch { recentSearchRepository.clearAll() }
    }
}

private val EmptyProfile = UserProfile(user = null, ratings = null, submissions = null)

private data class PartStatus(
    val loading: Boolean = false,
    val error: String = "",
)

private data class ProfileRefreshStatus(
    val user: PartStatus = PartStatus(),
    val ratings: PartStatus = PartStatus(),
    val submissions: PartStatus = PartStatus(),
)

private fun Either<AppError, Unit>.toPartStatus(): PartStatus = when (this) {
    is Either.Left -> PartStatus(error = data.message)
    is Either.Right -> PartStatus()
}
