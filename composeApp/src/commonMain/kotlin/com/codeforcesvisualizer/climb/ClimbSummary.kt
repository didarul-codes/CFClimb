package com.codeforcesvisualizer.climb

import com.codeforcesvisualizer.core.theme.RankTier
import com.codeforcesvisualizer.core.theme.RankTiers
import com.codeforcesvisualizer.core.theme.rankTierFor
import com.codeforcesvisualizer.shared.domain.entity.UserRating
import com.codeforcesvisualizer.shared.domain.entity.UserStatus
import com.codeforcesvisualizer.shared.domain.stats.currentStreak
import com.codeforcesvisualizer.shared.domain.stats.solveDays
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private const val WEEK_SECONDS = 7 * 86_400L

/** Where a rating sits within its tier. */
data class TierProgress(
    val tier: RankTier,
    /** Null at the top tier. */
    val nextTier: RankTier?,
    val pointsToNext: Int,
    /** 0 at the bottom of the tier, 1 at the next tier's threshold or at the top tier. */
    val fraction: Float,
)

fun tierProgress(rating: Int): TierProgress {
    val tier = rankTierFor(rating)
    val next = RankTiers.firstOrNull { it.min > rating }
        ?: return TierProgress(tier, nextTier = null, pointsToNext = 0, fraction = 1f)
    val fraction = (rating - tier.min).toFloat() / (next.min - tier.min)
    return TierProgress(tier, next, pointsToNext = next.min - rating, fraction = fraction.coerceIn(0f, 1f))
}

/** Distinct problems whose first accepted submission came at or after [sinceEpochSeconds]. */
fun List<UserStatus>.problemsFirstSolvedSince(sinceEpochSeconds: Long): Int =
    filter { it.isAccepted }
        .groupBy { it.problem.key }
        .count { (_, accepted) -> accepted.minOf { it.creationTimeSeconds } >= sinceEpochSeconds }

/** The saved handle at a glance: where it stands and how the current week is going. */
data class ClimbSummary(
    val handle: String,
    /** Null for a handle without rated rounds. */
    val rating: Int?,
    /** Rating change in the latest rated round. */
    val lastChange: Int?,
    val tierProgress: TierProgress?,
    /** Consecutive days with an accepted submission, ending today or yesterday. */
    val streakDays: Int,
    /** Problems solved for the first time in the last 7 days; re-solves don't count. */
    val solvedThisWeek: Int,
    val ratedContests: Int,
)

/** [ratings] are in chronological order, as returned by `user.rating`. */
fun buildClimbSummary(
    handle: String,
    ratings: List<UserRating>?,
    submissions: List<UserStatus>?,
    now: Instant,
    timeZone: TimeZone,
): ClimbSummary {
    val latest = ratings?.lastOrNull()
    val rating = latest?.newRating
    val today = now.toLocalDateTime(timeZone).date
    return ClimbSummary(
        handle = handle,
        rating = rating,
        lastChange = latest?.let { it.newRating - it.oldRating },
        tierProgress = rating?.let(::tierProgress),
        streakDays = submissions?.let { currentStreak(it.solveDays(timeZone), today) } ?: 0,
        solvedThisWeek = submissions?.problemsFirstSolvedSince(now.epochSeconds - WEEK_SECONDS) ?: 0,
        ratedContests = ratings?.size ?: 0,
    )
}
