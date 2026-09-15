package com.codeforcesvisualizer.shared.domain.stats

import com.codeforcesvisualizer.shared.domain.entity.UserStatus
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SubmissionStatsTest {

    @Test
    fun solvedProblemsCountsEachProblemOnce() {
        val submissions = listOf(
            submission(contestId = 1, index = "A"),
            submission(contestId = 1, index = "A"),
            submission(contestId = 1, index = "B", verdict = "WRONG_ANSWER"),
            submission(contestId = 2, index = "A"),
        )

        assertEquals(2, submissions.solvedProblems().size)
    }

    @Test
    fun problemsetProblemsWithoutContestAreDistinctFromContestProblems() {
        val submissions = listOf(
            submission(contestId = null, problemsetName = "acmsguru", index = "100"),
            submission(contestId = 100, index = "100"),
        )

        assertEquals(2, submissions.solvedProblems().size)
    }

    @Test
    fun acceptanceRateIgnoresSubmissionsStillInQueue() {
        val submissions = listOf(
            submission(),
            submission(verdict = "WRONG_ANSWER"),
            submission(verdict = UserStatus.VERDICT_TESTING),
        )

        assertEquals(50, submissions.acceptanceRatePercent())
    }

    @Test
    fun acceptanceRateIsZeroWithoutJudgedSubmissions() {
        assertEquals(0, emptyList<UserStatus>().acceptanceRatePercent())
        assertEquals(0, listOf(submission(verdict = UserStatus.VERDICT_TESTING)).acceptanceRatePercent())
    }

    @Test
    fun tagCountsUseDistinctSolvedProblemsOnly() {
        val submissions = listOf(
            submission(index = "A", tags = listOf("dp", "math")),
            submission(index = "A", tags = listOf("dp", "math")),
            submission(index = "B", tags = listOf("math")),
            submission(index = "C", tags = listOf("graphs"), verdict = "WRONG_ANSWER"),
        )

        assertEquals(
            listOf(TagCount("math", 2), TagCount("dp", 1)),
            submissions.solvedTagCounts()
        )
    }

    @Test
    fun difficultyFillsGapsFrom800AndIgnoresUnrated() {
        val submissions = listOf(
            submission(index = "A", rating = 800),
            submission(index = "B", rating = 1000),
            submission(index = "C", rating = 1000),
            submission(index = "D", rating = null),
            submission(index = "E", rating = 1200, verdict = "WRONG_ANSWER"),
        )

        assertEquals(
            listOf(DifficultyCount(800, 1), DifficultyCount(900, 0), DifficultyCount(1000, 2)),
            submissions.solvedByDifficulty()
        )
    }

    @Test
    fun difficultyIsEmptyWithoutRatedSolves() {
        assertTrue(listOf(submission(rating = null)).solvedByDifficulty().isEmpty())
    }

    @Test
    fun submissionsAreGroupedByLocalDay() {
        // 2026-09-15T23:30Z and 2026-09-16T00:30Z
        val submissions = listOf(
            submission(creationTimeSeconds = 1_789_515_000),
            submission(creationTimeSeconds = 1_789_518_600),
        )

        assertEquals(
            mapOf(LocalDate(2026, 9, 15) to 1, LocalDate(2026, 9, 16) to 1),
            submissions.submissionsPerDay(TimeZone.UTC)
        )
    }

    @Test
    fun currentStreakSurvivesUntilTheDayEnds() {
        val today = LocalDate(2026, 9, 15)
        val days = setOf(LocalDate(2026, 9, 12), LocalDate(2026, 9, 13), LocalDate(2026, 9, 14))

        assertEquals(3, currentStreak(days, today))
        assertEquals(4, currentStreak(days + today, today))
    }

    @Test
    fun currentStreakIsZeroAfterAMissedDay() {
        val today = LocalDate(2026, 9, 15)

        assertEquals(0, currentStreak(setOf(LocalDate(2026, 9, 13)), today))
    }

    @Test
    fun longestStreakFindsTheLongestRun() {
        val days = setOf(
            LocalDate(2026, 8, 30),
            LocalDate(2026, 8, 31),
            LocalDate(2026, 9, 1),
            LocalDate(2026, 9, 10),
        )

        assertEquals(3, longestStreak(days))
        assertEquals(0, longestStreak(emptySet()))
    }

    @Test
    fun heatmapEndsOnTodayAndStartsOnMonday() {
        val today = LocalDate(2026, 9, 15) // Tuesday
        val perDay = mapOf(LocalDate(2026, 9, 7) to 4, today to 2)

        val grid = heatmapGrid(perDay, today, weeks = 2)

        assertEquals(9, grid.size)
        assertEquals(HeatmapDay(week = 0, dayOfWeek = 0, date = LocalDate(2026, 9, 7), count = 4), grid.first())
        assertEquals(HeatmapDay(week = 1, dayOfWeek = 1, date = today, count = 2), grid.last())
    }
}
