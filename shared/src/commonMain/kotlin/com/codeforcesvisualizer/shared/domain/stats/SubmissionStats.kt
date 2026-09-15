package com.codeforcesvisualizer.shared.domain.stats

import com.codeforcesvisualizer.shared.domain.entity.Problem
import com.codeforcesvisualizer.shared.domain.entity.UserStatus
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

const val MIN_PROBLEM_RATING = 800
const val PROBLEM_RATING_STEP = 100

data class TagCount(val tag: String, val count: Int)

data class DifficultyCount(val rating: Int, val count: Int)

data class HeatmapDay(
    /** 0 is the oldest week. */
    val week: Int,
    /** 0 is Monday. */
    val dayOfWeek: Int,
    val date: LocalDate,
    val count: Int
)

/** Distinct problems with at least one accepted submission, most recently solved first. */
fun List<UserStatus>.solvedProblems(): List<Problem> {
    return filter { it.isAccepted }
        .distinctBy { it.problem.key }
        .map { it.problem }
}

/** Accepted submissions as a whole percentage of judged submissions; 0 when nothing is judged. */
fun List<UserStatus>.acceptanceRatePercent(): Int {
    val judged = count { it.verdict != UserStatus.VERDICT_TESTING }
    if (judged == 0) return 0
    return count { it.isAccepted } * 100 / judged
}

/** Number of distinct solved problems carrying each tag, highest count first. */
fun List<UserStatus>.solvedTagCounts(): List<TagCount> {
    return solvedProblems()
        .flatMap { it.tags }
        .groupingBy { it }
        .eachCount()
        .map { (tag, count) -> TagCount(tag, count) }
        .sortedWith(compareByDescending<TagCount> { it.count }.thenBy { it.tag })
}

/**
 * Distinct solved problems per difficulty rating, from 800 up to the hardest solved rating.
 * Unrated problems are ignored; empty when no rated problem is solved.
 */
fun List<UserStatus>.solvedByDifficulty(): List<DifficultyCount> {
    val counts = solvedProblems()
        .mapNotNull { it.rating }
        .groupingBy { it }
        .eachCount()
    val hardest = counts.keys.maxOrNull() ?: return emptyList()
    return (MIN_PROBLEM_RATING..hardest step PROBLEM_RATING_STEP).map { rating ->
        DifficultyCount(rating = rating, count = counts[rating] ?: 0)
    }
}

/** Submissions per calendar day in [timeZone]. */
fun List<UserStatus>.submissionsPerDay(timeZone: TimeZone): Map<LocalDate, Int> {
    return groupingBy { it.localDate(timeZone) }.eachCount()
}

/** Calendar days in [timeZone] with at least one accepted submission. */
fun List<UserStatus>.solveDays(timeZone: TimeZone): Set<LocalDate> {
    return filter { it.isAccepted }.mapTo(mutableSetOf()) { it.localDate(timeZone) }
}

/**
 * Consecutive solve days ending today. A streak that ended yesterday still counts, because the
 * user has the rest of today to extend it.
 */
fun currentStreak(solveDays: Set<LocalDate>, today: LocalDate): Int {
    var day = if (today in solveDays) today else today.minus(1, DateTimeUnit.DAY)
    var streak = 0
    while (day in solveDays) {
        streak++
        day = day.minus(1, DateTimeUnit.DAY)
    }
    return streak
}

fun longestStreak(solveDays: Set<LocalDate>): Int {
    var longest = 0
    for (day in solveDays) {
        // Only measure from the first day of each run.
        if (day.minus(1, DateTimeUnit.DAY) in solveDays) continue
        var length = 1
        var next = day.plus(1, DateTimeUnit.DAY)
        while (next in solveDays) {
            length++
            next = next.plus(1, DateTimeUnit.DAY)
        }
        longest = maxOf(longest, length)
    }
    return longest
}

/**
 * Lays out the last [weeks] weeks as a Monday-first grid whose final column contains [today].
 * Days after today are left out.
 */
fun heatmapGrid(perDay: Map<LocalDate, Int>, today: LocalDate, weeks: Int): List<HeatmapDay> {
    val daysIntoWeek = today.dayOfWeek.isoDayNumber - 1
    val firstMonday = today.minus(daysIntoWeek + (weeks - 1) * 7, DateTimeUnit.DAY)
    return buildList {
        for (week in 0 until weeks) {
            for (dayOfWeek in 0..6) {
                val date = firstMonday.plus(week * 7 + dayOfWeek, DateTimeUnit.DAY)
                if (date > today) break
                add(HeatmapDay(week, dayOfWeek, date, perDay[date] ?: 0))
            }
        }
    }
}

private fun UserStatus.localDate(timeZone: TimeZone): LocalDate {
    return Instant.fromEpochSeconds(creationTimeSeconds).toLocalDateTime(timeZone).date
}
