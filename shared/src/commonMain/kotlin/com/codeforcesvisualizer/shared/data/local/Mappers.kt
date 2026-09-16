package com.codeforcesvisualizer.shared.data.local

import com.codeforcesvisualizer.shared.domain.entity.Contest
import com.codeforcesvisualizer.shared.domain.entity.ParticipantType
import com.codeforcesvisualizer.shared.domain.entity.Problem
import com.codeforcesvisualizer.shared.domain.entity.User
import com.codeforcesvisualizer.shared.domain.entity.UserRating
import com.codeforcesvisualizer.shared.domain.entity.UserStatus

/** Cache key for a handle; Codeforces handles are case-insensitive. */
internal fun handleKey(handle: String): String = handle.trim().lowercase()

internal fun ratingsFetchKey(handleKey: String) = "ratings:$handleKey"

internal fun submissionsFetchKey(handleKey: String) = "submissions:$handleKey"

internal const val PROBLEMSET_FETCH_KEY = "problemset"

internal fun Contest.toRow() = ContestEntity(
    id = id,
    name = name,
    type = type,
    phase = phase,
    frozen = frozen,
    scheduled = scheduled,
    durationSeconds = durationSeconds,
    startTimeSeconds = startTimeSeconds,
    relativeTimeSeconds = relativeTimeSeconds,
    preparedBy = preparedBy,
    websiteUrl = websiteUrl,
    description = description,
    difficulty = difficulty,
    kind = kind,
    icpcRegion = icpcRegion,
    country = country,
    season = season,
)

internal fun ContestEntity.toDomain() = Contest(
    id = id,
    name = name,
    type = type,
    phase = phase,
    frozen = frozen,
    scheduled = scheduled,
    durationSeconds = durationSeconds,
    startTimeSeconds = startTimeSeconds,
    relativeTimeSeconds = relativeTimeSeconds,
    preparedBy = preparedBy,
    websiteUrl = websiteUrl,
    description = description,
    difficulty = difficulty,
    kind = kind,
    icpcRegion = icpcRegion,
    country = country,
    season = season,
)

internal fun User.toRow() = UserEntity(
    handleKey = handleKey(handle),
    handle = handle,
    email = email,
    firstName = firstName,
    lastName = lastName,
    country = country,
    city = city,
    organization = organization,
    contribution = contribution,
    rank = rank,
    rating = rating,
    maxRank = maxRank,
    maxRating = maxRating,
    lastOnlineTimeSeconds = lastOnlineTimeSeconds,
    registrationTimeSeconds = registrationTimeSeconds,
    friendOfCount = friendOfCount,
    avatar = avatar,
    titlePhoto = titlePhoto,
)

internal fun UserEntity.toDomain() = User(
    handle = handle,
    email = email,
    firstName = firstName,
    lastName = lastName,
    country = country,
    city = city,
    organization = organization,
    contribution = contribution,
    rank = rank,
    rating = rating,
    maxRank = maxRank,
    maxRating = maxRating,
    lastOnlineTimeSeconds = lastOnlineTimeSeconds,
    registrationTimeSeconds = registrationTimeSeconds,
    friendOfCount = friendOfCount,
    avatar = avatar,
    titlePhoto = titlePhoto,
)

internal fun UserRating.toRow(handleKey: String) = RatingChangeEntity(
    handleKey = handleKey,
    contestId = contestId,
    contestName = contestName,
    rank = rank,
    ratingUpdateTimeSeconds = ratingUpdateTimeSeconds,
    oldRating = oldRating,
    newRating = newRating,
)

internal fun RatingChangeEntity.toDomain() = UserRating(
    contestId = contestId,
    contestName = contestName,
    rank = rank,
    ratingUpdateTimeSeconds = ratingUpdateTimeSeconds,
    oldRating = oldRating,
    newRating = newRating,
)

internal fun UserStatus.toRow(handleKey: String) = SubmissionEntity(
    handleKey = handleKey,
    id = id,
    creationTimeSeconds = creationTimeSeconds,
    participantType = participantType.name,
    programmingLanguage = programmingLanguage,
    verdict = verdict,
    problemContestId = problem.contestId,
    problemsetName = problem.problemsetName,
    problemIndex = problem.index,
    problemName = problem.name,
    problemRating = problem.rating,
    problemTags = problem.tags,
)

internal fun SubmissionEntity.toDomain() = UserStatus(
    id = id,
    creationTimeSeconds = creationTimeSeconds,
    participantType = ParticipantType.entries.firstOrNull { it.name == participantType } ?: ParticipantType.UNKNOWN,
    programmingLanguage = programmingLanguage,
    verdict = verdict,
    problem = Problem(
        contestId = problemContestId,
        problemsetName = problemsetName,
        index = problemIndex,
        name = problemName,
        rating = problemRating,
        tags = problemTags,
    ),
)

/** Null for problems outside a contest, such as acmsguru, which the app doesn't list. */
internal fun Problem.toRow(): ProblemEntity? = contestId?.let { id ->
    ProblemEntity(contestId = id, index = index, name = name, rating = rating, tags = tags)
}

internal fun ProblemEntity.toDomain() = Problem(
    contestId = contestId,
    problemsetName = null,
    index = index,
    name = name,
    rating = rating,
    tags = tags,
)
