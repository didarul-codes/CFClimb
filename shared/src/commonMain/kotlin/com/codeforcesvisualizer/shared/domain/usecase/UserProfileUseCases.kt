package com.codeforcesvisualizer.shared.domain.usecase

import com.codeforcesvisualizer.shared.core.AppError
import com.codeforcesvisualizer.shared.core.Either
import com.codeforcesvisualizer.shared.domain.entity.UserProfile
import com.codeforcesvisualizer.shared.domain.repository.CFRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ObserveUserProfileUseCase(
    private val cfRepository: CFRepository
) {
    /** Emits whatever is cached for [handle] and every later update. */
    operator fun invoke(handle: String): Flow<UserProfile> = combine(
        cfRepository.observeUser(handle),
        cfRepository.observeUserRatings(handle),
        cfRepository.observeUserSubmissions(handle),
        ::UserProfile
    )
}

/** Refreshes each part of a cached profile; parts can fail independently. */
class RefreshUserProfileUseCase(
    private val cfRepository: CFRepository
) {
    suspend fun user(handle: String): Either<AppError, Unit> = cfRepository.refreshUser(handle)

    suspend fun ratings(handle: String): Either<AppError, Unit> = cfRepository.refreshUserRatings(handle)

    suspend fun submissions(handle: String): Either<AppError, Unit> = cfRepository.refreshUserSubmissions(handle)
}
