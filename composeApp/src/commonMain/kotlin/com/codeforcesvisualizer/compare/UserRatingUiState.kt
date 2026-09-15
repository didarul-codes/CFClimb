package com.codeforcesvisualizer.compare

import com.codeforcesvisualizer.shared.domain.entity.UserRating

data class UserRatingUiState(
    val loading: Boolean = false,
    /** Error message per handle that failed to load. */
    val errors: Map<String, String> = emptyMap(),
    val userRatings1: List<UserRating>? = null,
    val userRatings2: List<UserRating>? = null
)
