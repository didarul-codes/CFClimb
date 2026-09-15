package com.codeforcesvisualizer.shared.domain.usecase

import com.codeforcesvisualizer.shared.core.AppError
import com.codeforcesvisualizer.shared.core.Either
import com.codeforcesvisualizer.shared.domain.entity.Contest
import com.codeforcesvisualizer.shared.domain.repository.CFRepository
import kotlinx.coroutines.flow.Flow

class ObserveContestListUseCase(
    private val cfRepository: CFRepository
) {
    operator fun invoke(): Flow<List<Contest>> = cfRepository.observeContestList()
}

class ObserveContestUseCase(
    private val cfRepository: CFRepository
) {
    operator fun invoke(id: Int): Flow<Contest?> = cfRepository.observeContest(id)
}

class RefreshContestListUseCase(
    private val cfRepository: CFRepository
) {
    suspend operator fun invoke(): Either<AppError, Unit> = cfRepository.refreshContestList()
}
