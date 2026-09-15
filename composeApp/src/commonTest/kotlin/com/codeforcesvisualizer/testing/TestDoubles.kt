package com.codeforcesvisualizer.testing

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.codeforcesvisualizer.shared.core.AppError
import com.codeforcesvisualizer.shared.core.DataNotFoundError
import com.codeforcesvisualizer.shared.core.Either
import com.codeforcesvisualizer.shared.core.MatchingDataNotFoundError
import com.codeforcesvisualizer.shared.domain.entity.Contest
import com.codeforcesvisualizer.shared.domain.entity.ParticipantType
import com.codeforcesvisualizer.shared.domain.entity.Problem
import com.codeforcesvisualizer.shared.domain.entity.User
import com.codeforcesvisualizer.shared.domain.entity.UserRating
import com.codeforcesvisualizer.shared.domain.entity.UserStatus
import com.codeforcesvisualizer.shared.domain.repository.CFRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * Behaves like the offline-first repository: refreshes copy "remote" responses into an in-memory
 * cache that observers see. Unknown handles fail with [DataNotFoundError].
 */
class FakeCFRepository : CFRepository {
    /** What the next contest refresh returns. */
    var remoteContests: Either<AppError, List<Contest>> = Either.Right(emptyList())
    val cachedContests = MutableStateFlow<List<Contest>>(emptyList())

    /** Remote responses per handle. */
    val users = mutableMapOf<String, Either<AppError, User>>()
    val ratings = mutableMapOf<String, Either<AppError, List<UserRating>>>()
    val submissions = mutableMapOf<String, Either<AppError, List<UserStatus>>>()

    /** Refreshes for a handle listed here suspend until the deferred completes. */
    val gates = mutableMapOf<String, CompletableDeferred<Unit>>()

    private val cachedUsers = MutableStateFlow<Map<String, User>>(emptyMap())
    private val cachedRatings = MutableStateFlow<Map<String, List<UserRating>>>(emptyMap())
    private val cachedSubmissions = MutableStateFlow<Map<String, List<UserStatus>>>(emptyMap())

    override fun observeContestList(): Flow<List<Contest>> = cachedContests

    override fun observeContest(id: Int): Flow<Contest?> = cachedContests.map { list -> list.find { it.id == id } }

    override suspend fun refreshContestList(): Either<AppError, Unit> = when (val result = remoteContests) {
        is Either.Left -> Either.Left(result.data)
        is Either.Right -> {
            cachedContests.value = result.data
            Either.Right(Unit)
        }
    }

    override suspend fun filterContestList(key: String): Either<AppError, List<Contest>> {
        val matches = cachedContests.value.filter { it.name.contains(key, ignoreCase = true) }
        return if (matches.isEmpty()) Either.Left(MatchingDataNotFoundError()) else Either.Right(matches)
    }

    override fun observeUser(handle: String): Flow<User?> = cachedUsers.map { it[handle] }

    override fun observeUserRatings(handle: String): Flow<List<UserRating>?> = cachedRatings.map { it[handle] }

    override fun observeUserSubmissions(handle: String): Flow<List<UserStatus>?> = cachedSubmissions.map { it[handle] }

    override suspend fun refreshUser(handle: String) = refresh(handle, users, cachedUsers)

    override suspend fun refreshUserRatings(handle: String) = refresh(handle, ratings, cachedRatings)

    override suspend fun refreshUserSubmissions(handle: String) = refresh(handle, submissions, cachedSubmissions)

    override suspend fun getUserInfoByHandle(handle: String) =
        refreshThenRead(refreshUser(handle), observeUser(handle).first())

    override suspend fun getUserStatusByHandle(handle: String) =
        refreshThenRead(refreshUserSubmissions(handle), observeUserSubmissions(handle).first())

    override suspend fun getUserRatingByHandle(handle: String) =
        refreshThenRead(refreshUserRatings(handle), observeUserRatings(handle).first())

    private suspend fun <T> refresh(
        handle: String,
        remote: Map<String, Either<AppError, T>>,
        cache: MutableStateFlow<Map<String, T>>
    ): Either<AppError, Unit> {
        gates[handle]?.await()
        return when (val result = remote[handle] ?: Either.Left(DataNotFoundError())) {
            is Either.Left -> Either.Left(result.data)
            is Either.Right -> {
                cache.update { it + (handle to result.data) }
                Either.Right(Unit)
            }
        }
    }

    private fun <T> refreshThenRead(refresh: Either<AppError, Unit>, cached: T?): Either<AppError, T> = when {
        cached != null -> Either.Right(cached)
        refresh is Either.Left -> Either.Left(refresh.data)
        else -> Either.Left(DataNotFoundError())
    }
}

class InMemoryPreferencesDataStore : DataStore<Preferences> {
    private val state = MutableStateFlow(emptyPreferences())

    override val data: Flow<Preferences> = state

    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
        val updated = transform(state.value)
        state.value = updated
        return updated
    }
}

fun user(handle: String, rating: Int = 1500) = User(
    handle = handle,
    email = "",
    firstName = "",
    lastName = "",
    country = "",
    city = "",
    organization = "",
    contribution = 0,
    rank = "specialist",
    rating = rating,
    maxRank = "specialist",
    maxRating = rating,
    lastOnlineTimeSeconds = 0,
    registrationTimeSeconds = 0,
    friendOfCount = 0,
    avatar = "",
    titlePhoto = ""
)

fun ratingChange(oldRating: Int, newRating: Int) = UserRating(
    contestId = 1,
    contestName = "Codeforces Round",
    rank = 100,
    ratingUpdateTimeSeconds = 0,
    oldRating = oldRating,
    newRating = newRating
)

fun acceptedSubmission(index: String) = UserStatus(
    id = 0,
    creationTimeSeconds = 0,
    participantType = ParticipantType.PRACTICE,
    programmingLanguage = "C++20",
    verdict = UserStatus.VERDICT_OK,
    problem = Problem(
        contestId = 1,
        problemsetName = null,
        index = index,
        name = "Problem $index",
        rating = 800,
        tags = emptyList()
    )
)

fun contest(
    id: Int,
    name: String = "Codeforces Round $id",
    scheduled: Boolean = false,
    // A start time in the past keeps the details countdown from running in tests.
    startTimeSeconds: Int = 0,
) = Contest(
    id = id,
    name = name,
    type = "CF",
    phase = if (scheduled) "Scheduled" else "Finished",
    frozen = false,
    scheduled = scheduled,
    durationSeconds = 7200,
    startTimeSeconds = startTimeSeconds,
    relativeTimeSeconds = 0,
    preparedBy = null,
    websiteUrl = null,
    description = null,
    difficulty = null,
    kind = null,
    icpcRegion = null,
    country = null,
    season = null
)
