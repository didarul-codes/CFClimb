package com.codeforcesvisualizer.climb

import com.codeforcesvisualizer.shared.domain.entity.UserStatus
import com.codeforcesvisualizer.testing.acceptedSubmission
import com.codeforcesvisualizer.testing.ratingChange
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private val NOW = Instant.parse("2026-09-15T12:00:00Z")
private const val DAY = 86_400L

class ClimbSummaryTest {

    @Test
    fun progressWithinATier() {
        val progress = tierProgress(1500)

        assertEquals("specialist", progress.tier.name)
        assertEquals("expert", progress.nextTier?.name)
        assertEquals(100, progress.pointsToNext)
        assertEquals(0.5f, progress.fraction)
    }

    @Test
    fun progressStartsAtZeroOnATierThreshold() {
        val progress = tierProgress(1600)

        assertEquals("expert", progress.tier.name)
        assertEquals(300, progress.pointsToNext)
        assertEquals(0f, progress.fraction)
    }

    @Test
    fun topTierHasNothingLeftToClimb() {
        val progress = tierProgress(3301)

        assertEquals("legendary grandmaster", progress.tier.name)
        assertNull(progress.nextTier)
        assertEquals(0, progress.pointsToNext)
        assertEquals(1f, progress.fraction)
    }

    @Test
    fun weekCountsProblemsSolvedForTheFirstTimeOnly() {
        val submissions = listOf(
            // Solved long ago and again yesterday: a re-solve, not new progress.
            solved("A", NOW.epochSeconds - 10 * DAY),
            solved("A", NOW.epochSeconds - DAY),
            // Two accepted submissions for one problem count once.
            solved("B", NOW.epochSeconds - DAY),
            solved("B", NOW.epochSeconds - DAY + 60),
            // Rejected attempts don't count.
            solved("C", NOW.epochSeconds - DAY).copy(verdict = "WRONG_ANSWER"),
            solved("D", NOW.epochSeconds - 3_600),
        )
        val ratings = listOf(ratingChange(0, 1500), ratingChange(1500, 1547))

        val summary = buildClimbSummary("climber", ratings, submissions, NOW, TimeZone.UTC)

        assertEquals(2, summary.solvedThisWeek)
        assertEquals(2, summary.streakDays)
        assertEquals(1547, summary.rating)
        assertEquals(47, summary.lastChange)
        assertEquals(2, summary.ratedContests)
        assertEquals("specialist", summary.tierProgress?.tier?.name)
    }

    @Test
    fun unratedHandleHasNoTierYet() {
        val summary = buildClimbSummary("newcomer", emptyList(), emptyList(), NOW, TimeZone.UTC)

        assertNull(summary.rating)
        assertNull(summary.lastChange)
        assertNull(summary.tierProgress)
        assertEquals(0, summary.streakDays)
    }

    private fun solved(index: String, atEpochSeconds: Long): UserStatus =
        acceptedSubmission(index).copy(creationTimeSeconds = atEpochSeconds)
}
