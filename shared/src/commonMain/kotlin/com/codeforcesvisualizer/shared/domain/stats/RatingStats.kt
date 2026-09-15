package com.codeforcesvisualizer.shared.domain.stats

import com.codeforcesvisualizer.shared.domain.entity.UserRating
import com.codeforcesvisualizer.shared.domain.entity.UserStatus

// Rating lists are in chronological order, as returned by user.rating.

/** Rating after the latest rated round; null when the user never competed. */
fun List<UserRating>.currentRating(): Int? = lastOrNull()?.newRating

fun List<UserRating>.maxRating(): Int? = maxOfOrNull { it.newRating }

fun List<UserRating>.bestRank(): Int? = minOfOrNull { it.rank }

/** Rated rounds in a row, ending with the latest, that raised the rating. */
fun List<UserRating>.currentGainStreak(): Int {
    return asReversed().takeWhile { it.newRating > it.oldRating }.size
}

fun List<UserRating>.longestGainStreak(): Int {
    var longest = 0
    var current = 0
    for (change in this) {
        current = if (change.newRating > change.oldRating) current + 1 else 0
        longest = maxOf(longest, current)
    }
    return longest
}

data class ProfileSummary(
    val rating: Int?,
    val maxRating: Int?,
    val solvedProblems: Int,
    val ratedContests: Int,
    val acceptanceRatePercent: Int,
    val bestRank: Int?,
    val longestGainStreak: Int
)

fun profileSummary(ratings: List<UserRating>, submissions: List<UserStatus>): ProfileSummary {
    return ProfileSummary(
        rating = ratings.currentRating(),
        maxRating = ratings.maxRating(),
        solvedProblems = submissions.solvedProblems().size,
        ratedContests = ratings.size,
        acceptanceRatePercent = submissions.acceptanceRatePercent(),
        bestRank = ratings.bestRank(),
        longestGainStreak = ratings.longestGainStreak()
    )
}
