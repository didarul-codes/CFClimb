package com.codeforcesvisualizer.shared.data.model

import com.codeforcesvisualizer.shared.domain.entity.UserRating
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserRatingModel(
    @SerialName("contestId")
    val contestId: Int,

    @SerialName("contestName")
    val contestName: String = "",

    @SerialName("rank")
    val rank: Int,

    @SerialName("ratingUpdateTimeSeconds")
    val ratingUpdateTimeSeconds: Long = 0,

    @SerialName("oldRating")
    val oldRating: Int,

    @SerialName("newRating")
    val newRating: Int
) {
    fun toEntity(): UserRating {
        return UserRating(
            contestId = contestId,
            contestName = contestName,
            rank = rank,
            ratingUpdateTimeSeconds = ratingUpdateTimeSeconds,
            oldRating = oldRating,
            newRating = newRating
        )
    }
}
