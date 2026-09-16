package com.codeforcesvisualizer.shared.domain.stats

import com.codeforcesvisualizer.shared.domain.entity.ParticipantType
import com.codeforcesvisualizer.shared.domain.entity.Problem
import com.codeforcesvisualizer.shared.domain.entity.UserRating
import com.codeforcesvisualizer.shared.domain.entity.UserStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UpsolveQueueTest {

    private val ratings = listOf(rated(2264), rated(2250))

    @Test
    fun triedProblemsComeFirstWithTheirWrongAttempts() {
        val submissions = listOf(
            submission(problem(2264, "C", 1600), "WRONG_ANSWER"),
            submission(problem(2264, "C", 1600), "TIME_LIMIT_EXCEEDED"),
            submission(problem(2264, "C", 1600), "COMPILATION_ERROR"),
            submission(problem(2264, "A", 800), "OK"),
        )
        val problemset = listOf(problem(2264, "A", 800), problem(2264, "B", 1200), problem(2264, "C", 1600))

        val queue = upsolveQueue(ratings, submissions, problemset)

        assertEquals(listOf("2264-C" to UpsolveReason.TRIED, "2264-B" to UpsolveReason.NOT_OPENED), queue.map { it.problem.key to it.reason })
        assertEquals(2, queue.first().wrongAttempts)
        assertEquals("Codeforces Round 2264", queue.first().contestName)
    }

    @Test
    fun problemSolvedLaterInPracticeIsLeftOut() {
        val submissions = listOf(
            submission(problem(2264, "D", 2000), "WRONG_ANSWER", ParticipantType.CONTESTANT),
            submission(problem(2264, "D", 2000), "OK", ParticipantType.PRACTICE),
        )

        assertTrue(upsolveQueue(ratings, submissions, listOf(problem(2264, "D", 2000))).isEmpty())
    }

    @Test
    fun onlyProblemsFromRatedRoundsAreListed() {
        val submissions = listOf(submission(problem(1999, "B", 1300), "WRONG_ANSWER"))
        val problemset = listOf(problem(1999, "A", 800), problem(2250, "A", 900))

        val queue = upsolveQueue(ratings, submissions, problemset)

        assertEquals(listOf("2250-A"), queue.map { it.problem.key })
    }

    @Test
    fun eachGroupRunsFromTheLowestRatingWithUnratedLast() {
        val problemset = listOf(
            problem(2250, "E", null),
            problem(2250, "C", 1700),
            problem(2264, "C", 1700),
            problem(2264, "A", 800),
        )

        val queue = upsolveQueue(ratings, emptyList(), problemset)

        assertEquals(listOf("2264-A", "2264-C", "2250-C", "2250-E"), queue.map { it.problem.key })
    }

    private fun rated(contestId: Int) = UserRating(
        contestId = contestId,
        contestName = "Codeforces Round $contestId",
        rank = 100,
        ratingUpdateTimeSeconds = 0,
        oldRating = 1500,
        newRating = 1520,
    )

    private fun problem(contestId: Int, index: String, rating: Int?) = Problem(
        contestId = contestId,
        problemsetName = null,
        index = index,
        name = "Problem $index",
        rating = rating,
        tags = listOf("math"),
    )

    private fun submission(
        problem: Problem,
        verdict: String,
        participantType: ParticipantType = ParticipantType.CONTESTANT,
    ) = UserStatus(
        id = 0,
        creationTimeSeconds = 0,
        participantType = participantType,
        programmingLanguage = "C++20",
        verdict = verdict,
        problem = problem,
    )
}
