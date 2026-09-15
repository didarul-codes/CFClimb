package com.codeforcesvisualizer.shared.domain.entity

data class UserRating(
    val contestId: Int,
    val contestName: String,
    val rank: Int,
    val ratingUpdateTimeSeconds: Long,
    val oldRating: Int,
    val newRating: Int
)
