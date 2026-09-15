package com.codeforcesvisualizer.testing

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.codeforcesvisualizer.shared.core.AppError
import com.codeforcesvisualizer.shared.core.DataNotFoundError
import com.codeforcesvisualizer.shared.core.Either
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

/** Answers from in-memory maps; unknown handles fail with [DataNotFoundError]. */
class FakeCFRepository : CFRepository {
    val users = mutableMapOf<String, Either<AppError, User>>()
    val ratings = mutableMapOf<String, Either<AppError, List<UserRating>>>()
    val submissions = mutableMapOf<String, Either<AppError, List<UserStatus>>>()

    /** Calls for a handle listed here suspend until the deferred completes. */
    val gates = mutableMapOf<String, CompletableDeferred<Unit>>()

    override suspend fun getUserInfoByHandle(handle: String): Either<AppError, User> {
        gates[handle]?.await()
        return users[handle] ?: Either.Left(DataNotFoundError())
    }

    override suspend fun getUserRatingByHandle(handle: String): Either<AppError, List<UserRating>> {
        gates[handle]?.await()
        return ratings[handle] ?: Either.Left(DataNotFoundError())
    }

    override suspend fun getUserStatusByHandle(handle: String): Either<AppError, List<UserStatus>> {
        gates[handle]?.await()
        return submissions[handle] ?: Either.Left(DataNotFoundError())
    }

    override suspend fun getContestList(refresh: Boolean): Either<AppError, List<Contest>> =
        Either.Left(DataNotFoundError())

    override suspend fun getContestById(id: Int): Either<AppError, Contest> =
        Either.Left(DataNotFoundError())

    override suspend fun filterContestList(key: String): Either<AppError, List<Contest>> =
        Either.Left(DataNotFoundError())
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
