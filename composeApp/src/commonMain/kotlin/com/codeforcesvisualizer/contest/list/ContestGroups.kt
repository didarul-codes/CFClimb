package com.codeforcesvisualizer.contest.list

import com.codeforcesvisualizer.core.utils.formatTimeUntil
import com.codeforcesvisualizer.shared.domain.entity.Contest

/** Contests split by what is happening now: running, still to come, and over. */
data class ContestGroups(
    /** Running or in system testing, ending soonest first. */
    val live: List<Contest> = emptyList(),
    /** Not started yet, soonest first. */
    val upcoming: List<Contest> = emptyList(),
    /** Newest first. */
    val past: List<Contest> = emptyList(),
)

// Results usually come out within a few hours; after that a saved "system test" phase is stale.
private const val SYSTEM_TEST_GRACE_SECONDS = 6 * 3_600L

/**
 * Groups by start and end time rather than the saved phase, which can be hours old when the
 * list was last refreshed before a round began.
 */
fun groupContests(contests: List<Contest>, nowEpochSeconds: Long): ContestGroups {
    val live = mutableListOf<Contest>()
    val upcoming = mutableListOf<Contest>()
    val past = mutableListOf<Contest>()
    for (contest in contests) {
        when {
            contest.startTimeSeconds > nowEpochSeconds -> upcoming += contest
            nowEpochSeconds < contest.endTimeSeconds || contest.isSystemTesting(nowEpochSeconds) -> live += contest
            else -> past += contest
        }
    }
    return ContestGroups(
        live = live.sortedBy { it.endTimeSeconds },
        upcoming = upcoming.sortedBy { it.startTimeSeconds },
        past = past.sortedByDescending { it.startTimeSeconds },
    )
}

/** For example "ends in 1h 20m", or "system testing" once coding is over. */
fun liveStatusLabel(contest: Contest, nowEpochSeconds: Long): String =
    if (nowEpochSeconds < contest.endTimeSeconds) {
        "ends in ${formatTimeUntil(contest.endTimeSeconds, nowEpochSeconds)}"
    } else {
        "system testing"
    }

private val Contest.endTimeSeconds: Long get() = startTimeSeconds.toLong() + durationSeconds

private fun Contest.isSystemTesting(nowEpochSeconds: Long): Boolean =
    phase.contains("test", ignoreCase = true) && nowEpochSeconds < endTimeSeconds + SYSTEM_TEST_GRACE_SECONDS
