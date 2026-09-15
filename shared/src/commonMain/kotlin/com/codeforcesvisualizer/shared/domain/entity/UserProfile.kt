package com.codeforcesvisualizer.shared.domain.entity

/** Cached profile data for one handle. Each part is null until it has been fetched once. */
data class UserProfile(
    val user: User?,
    val ratings: List<UserRating>?,
    val submissions: List<UserStatus>?
)
