package com.codeforcesvisualizer.contest

import com.codeforcesvisualizer.contest.list.groupContests
import com.codeforcesvisualizer.contest.list.liveStatusLabel
import com.codeforcesvisualizer.testing.contest
import kotlin.test.Test
import kotlin.test.assertEquals

private const val NOW = 1_789_500_000L
private const val HOUR = 3_600

class ContestGroupsTest {

    @Test
    fun runningRoundIsLiveEvenWhenTheSavedPhaseStillSaysScheduled() {
        // Saved an hour before the start and not refreshed since.
        val running = contest(id = 1, scheduled = true, startTimeSeconds = (NOW - 20 * 60).toInt())

        val groups = groupContests(listOf(running), NOW)

        assertEquals(listOf(1), groups.live.map { it.id })
        assertEquals("ends in 1h 40m", liveStatusLabel(running, NOW))
    }

    @Test
    fun systemTestingStaysLiveForAFewHoursOnly() {
        val justEnded = contest(id = 1, startTimeSeconds = (NOW - 3 * HOUR).toInt()).copy(phase = "System Test")
        val longAgo = contest(id = 2, startTimeSeconds = (NOW - 20 * HOUR).toInt()).copy(phase = "System Test")
        val finished = contest(id = 3, startTimeSeconds = (NOW - 3 * HOUR).toInt())

        val groups = groupContests(listOf(justEnded, longAgo, finished), NOW)

        assertEquals(listOf(1), groups.live.map { it.id })
        assertEquals("system testing", liveStatusLabel(justEnded, NOW))
        assertEquals(setOf(2, 3), groups.past.map { it.id }.toSet())
    }

    @Test
    fun groupsAreOrderedForReading() {
        val contests = listOf(
            contest(id = 1, scheduled = true, startTimeSeconds = (NOW + 48 * HOUR).toInt()),
            contest(id = 2, scheduled = true, startTimeSeconds = (NOW + 2 * HOUR).toInt()),
            contest(id = 3, startTimeSeconds = (NOW - 72 * HOUR).toInt()),
            contest(id = 4, startTimeSeconds = (NOW - 24 * HOUR).toInt()),
            contest(id = 5, startTimeSeconds = (NOW - HOUR).toInt()).copy(durationSeconds = 3 * HOUR),
            contest(id = 6, startTimeSeconds = (NOW - HOUR).toInt()).copy(durationSeconds = 2 * HOUR),
        )

        val groups = groupContests(contests, NOW)

        assertEquals(listOf(6, 5), groups.live.map { it.id })
        assertEquals(listOf(2, 1), groups.upcoming.map { it.id })
        assertEquals(listOf(4, 3), groups.past.map { it.id })
    }
}
