package com.codeforcesvisualizer.shared.domain.stats

import com.codeforcesvisualizer.shared.domain.entity.Problem
import com.codeforcesvisualizer.shared.domain.entity.UserRating
import com.codeforcesvisualizer.shared.domain.entity.UserStatus

enum class UpsolveReason {
    /** Submitted at least once, never accepted. */
    TRIED,

    /** Never submitted. */
    NOT_OPENED,
}

data class UpsolveProblem(
    val problem: Problem,
    val reason: UpsolveReason,
    /** Rejected submissions; compilation errors and submissions still being judged don't count. */
    val wrongAttempts: Int,
    val contestName: String,
)

private val NotAnAttempt = setOf(UserStatus.VERDICT_TESTING, "COMPILATION_ERROR")

/**
 * Unsolved problems from the user's rated rounds: problems they tried first, then problems they
 * never opened. Each group runs from the lowest rating up, unrated problems last, newer rounds
 * first on a tie. A problem accepted at any time, including later in practice, is left out.
 *
 * @param ratings the user's rating changes, one per rated round.
 * @param problemset problems to look for unopened ones in; empty lists only tried problems.
 */
fun upsolveQueue(
    ratings: List<UserRating>,
    submissions: List<UserStatus>,
    problemset: List<Problem>,
): List<UpsolveProblem> {
    val ratedRounds = ratings.associate { it.contestId to it.contestName }
    val solved = submissions.filter { it.isAccepted }.mapTo(mutableSetOf()) { it.problem.key }
    val submitted = submissions.mapTo(mutableSetOf()) { it.problem.key }

    val tried = submissions
        .filter { it.problem.roundName(ratedRounds) != null && it.problem.key !in solved }
        .groupBy { it.problem.key }
        .map { (_, attempts) ->
            val problem = attempts.first().problem
            UpsolveProblem(
                problem = problem,
                reason = UpsolveReason.TRIED,
                wrongAttempts = attempts.count { it.verdict !in NotAnAttempt },
                contestName = problem.roundName(ratedRounds).orEmpty(),
            )
        }

    val notOpened = problemset
        .filter { it.roundName(ratedRounds) != null && it.key !in submitted }
        .distinctBy { it.key }
        .map { problem ->
            UpsolveProblem(
                problem = problem,
                reason = UpsolveReason.NOT_OPENED,
                wrongAttempts = 0,
                contestName = problem.roundName(ratedRounds).orEmpty(),
            )
        }

    return tried.sortedWith(UpsolveOrder) + notOpened.sortedWith(UpsolveOrder)
}

private val UpsolveOrder: Comparator<UpsolveProblem> =
    compareBy<UpsolveProblem, Int?>(nullsLast()) { it.problem.rating }
        .thenByDescending { it.problem.contestId }
        .thenBy { it.problem.index }

private fun Problem.roundName(ratedRounds: Map<Int, String>): String? = contestId?.let { ratedRounds[it] }
