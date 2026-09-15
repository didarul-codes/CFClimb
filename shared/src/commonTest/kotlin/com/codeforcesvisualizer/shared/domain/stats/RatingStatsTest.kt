package com.codeforcesvisualizer.shared.domain.stats

import com.codeforcesvisualizer.shared.domain.entity.UserRating
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RatingStatsTest {

    private val history = listOf(
        ratingChange(oldRating = 0, newRating = 1400, rank = 900),
        ratingChange(oldRating = 1400, newRating = 1350, rank = 1200),
        ratingChange(oldRating = 1350, newRating = 1420, rank = 400),
        ratingChange(oldRating = 1420, newRating = 1510, rank = 250),
    )

    @Test
    fun summarizesRatingHistory() {
        assertEquals(1510, history.currentRating())
        assertEquals(1510, history.maxRating())
        assertEquals(250, history.bestRank())
    }

    @Test
    fun emptyHistoryHasNoRatingOrRank() {
        val empty = emptyList<UserRating>()

        assertNull(empty.currentRating())
        assertNull(empty.maxRating())
        assertNull(empty.bestRank())
        assertEquals(0, empty.currentGainStreak())
    }

    @Test
    fun gainStreaks() {
        assertEquals(2, history.currentGainStreak())
        assertEquals(2, history.longestGainStreak())
        assertEquals(0, (history + ratingChange(1510, 1480)).currentGainStreak())
    }

    @Test
    fun profileSummaryCombinesRatingsAndSubmissions() {
        val submissions = listOf(
            submission(index = "A"),
            submission(index = "A"),
            submission(index = "B", verdict = "WRONG_ANSWER"),
        )

        val summary = profileSummary(history, submissions)

        assertEquals(
            ProfileSummary(
                rating = 1510,
                maxRating = 1510,
                solvedProblems = 1,
                ratedContests = 4,
                acceptanceRatePercent = 66,
                bestRank = 250,
                longestGainStreak = 2
            ),
            summary
        )
    }
}
