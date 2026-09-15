package com.codeforcesvisualizer.shared.domain.repository

import com.codeforcesvisualizer.shared.core.AppError
import com.codeforcesvisualizer.shared.core.Either
import com.codeforcesvisualizer.shared.domain.entity.Contest
import com.codeforcesvisualizer.shared.domain.entity.User
import com.codeforcesvisualizer.shared.domain.entity.UserRating
import com.codeforcesvisualizer.shared.domain.entity.UserStatus
import kotlinx.coroutines.flow.Flow

/**
 * The local database is the source of truth: screens observe cached data, and refresh calls
 * update the cache from the Codeforces API.
 */
interface CFRepository {
    /** Cached contests, newest first; empty until the first successful refresh. */
    fun observeContestList(): Flow<List<Contest>>

    fun observeContest(id: Int): Flow<Contest?>

    suspend fun refreshContestList(): Either<AppError, Unit>

    suspend fun filterContestList(key: String): Either<AppError, List<Contest>>

    fun observeUser(handle: String): Flow<User?>

    /** Null until ratings for [handle] have been fetched once. */
    fun observeUserRatings(handle: String): Flow<List<UserRating>?>

    /** Null until submissions for [handle] have been fetched once. */
    fun observeUserSubmissions(handle: String): Flow<List<UserStatus>?>

    suspend fun refreshUser(handle: String): Either<AppError, Unit>

    suspend fun refreshUserRatings(handle: String): Either<AppError, Unit>

    suspend fun refreshUserSubmissions(handle: String): Either<AppError, Unit>

    /** Fetches fresh data; when the request fails, returns cached data if there is any. */
    suspend fun getUserInfoByHandle(handle: String): Either<AppError, User>

    /** Fetches fresh data; when the request fails, returns cached data if there is any. */
    suspend fun getUserStatusByHandle(handle: String): Either<AppError, List<UserStatus>>

    /** Fetches fresh data; when the request fails, returns cached data if there is any. */
    suspend fun getUserRatingByHandle(handle: String): Either<AppError, List<UserRating>>
}
