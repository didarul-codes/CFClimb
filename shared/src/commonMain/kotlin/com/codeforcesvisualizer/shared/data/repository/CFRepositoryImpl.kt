package com.codeforcesvisualizer.shared.data.repository

import com.codeforcesvisualizer.shared.core.AppError
import com.codeforcesvisualizer.shared.core.DataNotFoundError
import com.codeforcesvisualizer.shared.core.Either
import com.codeforcesvisualizer.shared.core.MatchingDataNotFoundError
import com.codeforcesvisualizer.shared.data.datasource.CFRemoteDataSource
import com.codeforcesvisualizer.shared.data.local.ContestDao
import com.codeforcesvisualizer.shared.data.local.FetchTimeEntity
import com.codeforcesvisualizer.shared.data.local.PROBLEMSET_FETCH_KEY
import com.codeforcesvisualizer.shared.data.local.ProblemDao
import com.codeforcesvisualizer.shared.data.local.ProfileDao
import com.codeforcesvisualizer.shared.data.local.handleKey
import com.codeforcesvisualizer.shared.data.local.ratingsFetchKey
import com.codeforcesvisualizer.shared.data.local.submissionsFetchKey
import com.codeforcesvisualizer.shared.data.local.toDomain
import com.codeforcesvisualizer.shared.data.local.toRow
import com.codeforcesvisualizer.shared.domain.entity.Contest
import com.codeforcesvisualizer.shared.domain.entity.Problem
import com.codeforcesvisualizer.shared.domain.entity.User
import com.codeforcesvisualizer.shared.domain.entity.UserRating
import com.codeforcesvisualizer.shared.domain.entity.UserStatus
import com.codeforcesvisualizer.shared.domain.repository.CFRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlin.time.Duration

class CFRepositoryImpl(
    private val cfRemoteDataSource: CFRemoteDataSource,
    private val contestDao: ContestDao,
    private val profileDao: ProfileDao,
    private val problemDao: ProblemDao,
    private val clock: Clock = Clock.System
) : CFRepository {

    override fun observeContestList(): Flow<List<Contest>> {
        return contestDao.observeAll().map { rows -> rows.map { it.toDomain() } }
    }

    override fun observeContest(id: Int): Flow<Contest?> {
        return contestDao.observeById(id).map { it?.toDomain() }
    }

    override suspend fun refreshContestList(): Either<AppError, Unit> {
        return when (val data = cfRemoteDataSource.getContestList()) {
            is Either.Left -> Either.Left(data.data)
            is Either.Right -> {
                contestDao.replaceAll(data.data.toEntity().map { it.toRow() })
                Either.Right(Unit)
            }
        }
    }

    override suspend fun filterContestList(key: String): Either<AppError, List<Contest>> {
        if (key.isBlank()) return Either.Left(data = AppError(""))

        val matches = contestDao.search(key.trim()).map { it.toDomain() }
        return if (matches.isNotEmpty()) Either.Right(data = matches)
        else Either.Left(data = MatchingDataNotFoundError())
    }

    override fun observeUser(handle: String): Flow<User?> {
        return profileDao.observeUser(handleKey(handle)).map { it?.toDomain() }
    }

    override fun observeUserRatings(handle: String): Flow<List<UserRating>?> {
        val key = handleKey(handle)
        return combine(
            profileDao.observeRatingChanges(key),
            profileDao.observeFetchTime(ratingsFetchKey(key))
        ) { rows, fetchTime ->
            if (fetchTime == null) null else rows.map { it.toDomain() }
        }
    }

    override fun observeUserSubmissions(handle: String): Flow<List<UserStatus>?> {
        val key = handleKey(handle)
        return combine(
            profileDao.observeSubmissions(key),
            profileDao.observeFetchTime(submissionsFetchKey(key))
        ) { rows, fetchTime ->
            if (fetchTime == null) null else rows.map { it.toDomain() }
        }
    }

    override suspend fun refreshUser(handle: String): Either<AppError, Unit> {
        return when (val data = cfRemoteDataSource.getUserInfoByHandle(handle.trim())) {
            is Either.Left -> Either.Left(data.data)
            is Either.Right -> {
                profileDao.upsertUser(data.data.toEntity().first().toRow())
                Either.Right(Unit)
            }
        }
    }

    override suspend fun refreshUserRatings(handle: String): Either<AppError, Unit> {
        return when (val data = cfRemoteDataSource.getUserRatingByHandle(handle.trim())) {
            is Either.Left -> Either.Left(data.data)
            is Either.Right -> {
                val key = handleKey(handle)
                profileDao.replaceRatingChanges(
                    handleKey = key,
                    changes = data.data.toEntity().map { it.toRow(key) },
                    fetchTime = FetchTimeEntity(ratingsFetchKey(key), clock.now().epochSeconds)
                )
                Either.Right(Unit)
            }
        }
    }

    override suspend fun refreshUserSubmissions(handle: String): Either<AppError, Unit> {
        return when (val data = cfRemoteDataSource.getUserStatusByHandle(handle.trim())) {
            is Either.Left -> Either.Left(data.data)
            is Either.Right -> {
                val key = handleKey(handle)
                profileDao.replaceSubmissions(
                    handleKey = key,
                    submissions = data.data.toEntity().map { it.toRow(key) },
                    fetchTime = FetchTimeEntity(submissionsFetchKey(key), clock.now().epochSeconds)
                )
                Either.Right(Unit)
            }
        }
    }

    override suspend fun getUserInfoByHandle(handle: String): Either<AppError, User> {
        return refreshThenRead(refreshUser(handle)) { observeUser(handle).first() }
    }

    override suspend fun getUserStatusByHandle(handle: String): Either<AppError, List<UserStatus>> {
        return refreshThenRead(refreshUserSubmissions(handle)) { observeUserSubmissions(handle).first() }
    }

    override suspend fun getUserRatingByHandle(handle: String): Either<AppError, List<UserRating>> {
        return refreshThenRead(refreshUserRatings(handle)) { observeUserRatings(handle).first() }
    }

    override fun observeProblemset(): Flow<List<Problem>?> {
        return combine(
            problemDao.observeAll(),
            problemDao.observeFetchTime(PROBLEMSET_FETCH_KEY)
        ) { rows, fetchTime ->
            if (fetchTime == null) null else rows.map { it.toDomain() }
        }
    }

    override suspend fun refreshProblemset(maxAge: Duration): Either<AppError, Unit> {
        val now = clock.now().epochSeconds
        val lastFetch = problemDao.fetchTime(PROBLEMSET_FETCH_KEY)
        if (lastFetch != null && now - lastFetch.fetchedAtEpochSeconds < maxAge.inWholeSeconds) {
            return Either.Right(Unit)
        }
        return when (val data = cfRemoteDataSource.getProblemset()) {
            is Either.Left -> Either.Left(data.data)
            is Either.Right -> {
                problemDao.replaceAll(
                    problems = data.data.toEntity().mapNotNull { it.toRow() },
                    fetchTime = FetchTimeEntity(PROBLEMSET_FETCH_KEY, now)
                )
                Either.Right(Unit)
            }
        }
    }

    private suspend fun <T : Any> refreshThenRead(
        refresh: Either<AppError, Unit>,
        readCache: suspend () -> T?
    ): Either<AppError, T> {
        val cached = readCache()
        return when {
            cached != null -> Either.Right(cached)
            refresh is Either.Left -> Either.Left(refresh.data)
            else -> Either.Left(DataNotFoundError())
        }
    }
}
