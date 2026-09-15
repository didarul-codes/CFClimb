package com.codeforcesvisualizer.shared.domain.stats

import com.codeforcesvisualizer.shared.domain.entity.ParticipantType
import com.codeforcesvisualizer.shared.domain.entity.Problem
import com.codeforcesvisualizer.shared.domain.entity.UserRating
import com.codeforcesvisualizer.shared.domain.entity.UserStatus

internal fun submission(
    contestId: Int? = 1,
    index: String = "A",
    verdict: String = UserStatus.VERDICT_OK,
    rating: Int? = null,
    tags: List<String> = emptyList(),
    creationTimeSeconds: Long = 0,
    problemsetName: String? = null,
) = UserStatus(
    id = 0,
    creationTimeSeconds = creationTimeSeconds,
    participantType = ParticipantType.PRACTICE,
    programmingLanguage = "C++20",
    verdict = verdict,
    problem = Problem(
        contestId = contestId,
        problemsetName = problemsetName,
        index = index,
        name = "Problem $index",
        rating = rating,
        tags = tags
    )
)

internal fun ratingChange(oldRating: Int, newRating: Int, rank: Int = 100) = UserRating(
    contestId = 1,
    contestName = "Codeforces Round",
    rank = rank,
    ratingUpdateTimeSeconds = 0,
    oldRating = oldRating,
    newRating = newRating
)
