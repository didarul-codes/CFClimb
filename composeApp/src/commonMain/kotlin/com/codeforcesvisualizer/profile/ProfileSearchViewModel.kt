package com.codeforcesvisualizer.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codeforcesvisualizer.core.data.RecentSearchRepository
import com.codeforcesvisualizer.shared.core.Either
import com.codeforcesvisualizer.shared.domain.usecase.GetUserInfoByHandleUseCase
import com.codeforcesvisualizer.shared.domain.usecase.GetUserRatingsByHandleUseCase
import com.codeforcesvisualizer.shared.domain.usecase.GetUserStatusByHandleUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileSearchViewModel(
    private val getUserInfoByHandleUseCase: GetUserInfoByHandleUseCase,
    private val getUserStatusByHandleUseCase: GetUserStatusByHandleUseCase,
    private val getUserRatingByHandleUseCase: GetUserRatingsByHandleUseCase,
    private val recentSearchRepository: RecentSearchRepository
) : ViewModel() {
    private val _searchTextState = MutableStateFlow("")
    val searchTextState: StateFlow<String> = _searchTextState

    private val _userInfoState = MutableStateFlow(UserInfoUiState())
    val userInfoState: StateFlow<UserInfoUiState> = _userInfoState

    private val _userStatusState = MutableStateFlow(UserStatusUiState())
    val userStatusState: StateFlow<UserStatusUiState> = _userStatusState

    private val _userRatingState = MutableStateFlow(UserRatingUiState())
    val userRatingState: StateFlow<UserRatingUiState> = _userRatingState

    val recentSearches: StateFlow<List<String>> = recentSearchRepository.recentSearches
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private var searchJob: Job? = null

    fun onSearchTextChanged(text: String) {
        _searchTextState.value = text
    }

    /**
     * Loads the profile for [handle]. Starting a new search cancels requests still queued for the
     * previous one, so a slow earlier response can't replace newer results.
     */
    fun search(handle: String) {
        if (handle.isBlank()) return
        searchJob?.cancel()
        viewModelScope.launch { recentSearchRepository.addSearch(handle) }

        _userInfoState.value = _userInfoState.value.copy(loading = true)
        _userRatingState.value = _userRatingState.value.copy(loading = true)
        _userStatusState.value = _userStatusState.value.copy(loading = true)

        // Requests run one after another through the API throttle; the small responses go first
        // so the header and rating appear before the submission history arrives.
        searchJob = viewModelScope.launch {
            loadUserInfo(handle)
            loadUserRating(handle)
            loadUserStatus(handle)
        }
    }

    fun clearRecentSearches() {
        viewModelScope.launch { recentSearchRepository.clearAll() }
    }

    private suspend fun loadUserInfo(handle: String) {
        _userInfoState.value = when (val data = getUserInfoByHandleUseCase(handle)) {
            is Either.Left -> UserInfoUiState(loading = false, userMessage = data.data.message, user = null)
            is Either.Right -> UserInfoUiState(loading = false, userMessage = "", user = data.data)
        }
    }

    private suspend fun loadUserStatus(handle: String) {
        _userStatusState.value = when (val data = getUserStatusByHandleUseCase(handle)) {
            is Either.Left -> UserStatusUiState(loading = false, userMessage = data.data.message, userStatus = null)
            is Either.Right -> UserStatusUiState(loading = false, userMessage = "", userStatus = data.data)
        }
    }

    private suspend fun loadUserRating(handle: String) {
        _userRatingState.value = when (val data = getUserRatingByHandleUseCase(handle)) {
            is Either.Left -> UserRatingUiState(loading = false, userMessage = data.data.message, userRatings = null)
            is Either.Right -> UserRatingUiState(loading = false, userMessage = "", userRatings = data.data)
        }
    }
}
