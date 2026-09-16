package com.codeforcesvisualizer.shared.domain.usecase

import com.codeforcesvisualizer.shared.core.AppError
import com.codeforcesvisualizer.shared.core.Either
import com.codeforcesvisualizer.shared.domain.entity.Problem
import com.codeforcesvisualizer.shared.domain.repository.CFRepository
import kotlinx.coroutines.flow.Flow

class ObserveProblemsetUseCase(
    private val cfRepository: CFRepository
) {
    operator fun invoke(): Flow<List<Problem>?> = cfRepository.observeProblemset()
}

/** Refreshes the saved problemset at most once a day. */
class RefreshProblemsetUseCase(
    private val cfRepository: CFRepository
) {
    suspend operator fun invoke(): Either<AppError, Unit> = cfRepository.refreshProblemset()
}
