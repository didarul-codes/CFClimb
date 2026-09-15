package com.codeforcesvisualizer.compare

import com.codeforcesvisualizer.shared.domain.entity.UserStatus

data class UserStatusUiState(
    val loading: Boolean = false,
    /** Error message per handle that failed to load. */
    val errors: Map<String, String> = emptyMap(),
    val userStatus1: List<UserStatus>? = null,
    val userStatus2: List<UserStatus>? = null
)
