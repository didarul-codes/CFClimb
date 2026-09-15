package com.codeforcesvisualizer.contest

import com.codeforcesvisualizer.contest.details.ContestDetailsViewModel
import com.codeforcesvisualizer.contest.list.ContestViewModel
import com.codeforcesvisualizer.shared.core.Either
import com.codeforcesvisualizer.shared.core.ServerConnectionResponseError
import com.codeforcesvisualizer.shared.domain.usecase.ObserveContestListUseCase
import com.codeforcesvisualizer.shared.domain.usecase.ObserveContestUseCase
import com.codeforcesvisualizer.shared.domain.usecase.RefreshContestListUseCase
import com.codeforcesvisualizer.testing.FakeCFRepository
import com.codeforcesvisualizer.testing.contest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@OptIn(ExperimentalCoroutinesApi::class)
class ContestViewModelsTest {

    private val dispatcher = StandardTestDispatcher()
    private val repository = FakeCFRepository()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun listViewModel() = ContestViewModel(
        observeContestListUseCase = ObserveContestListUseCase(repository),
        refreshContestListUseCase = RefreshContestListUseCase(repository)
    )

    private fun detailsViewModel() = ContestDetailsViewModel(
        observeContestUseCase = ObserveContestUseCase(repository),
        refreshContestListUseCase = RefreshContestListUseCase(repository)
    )

    @Test
    fun refreshFillsTheContestList() = runTest(dispatcher) {
        repository.remoteContests = Either.Right(listOf(contest(1), contest(2)))

        val viewModel = listViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.contestList.size)
        assertFalse(state.loading)
        assertEquals("", state.userMessage)
    }

    @Test
    fun savedContestsStayVisibleWhenRefreshFails() = runTest(dispatcher) {
        repository.cachedContests.value = listOf(contest(1))
        repository.remoteContests = Either.Left(ServerConnectionResponseError())

        val viewModel = listViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf(1), state.contestList.map { it.id })
        assertEquals("", state.userMessage)
    }

    @Test
    fun refreshErrorShowsWhenNothingIsSaved() = runTest(dispatcher) {
        repository.remoteContests = Either.Left(ServerConnectionResponseError())

        val viewModel = listViewModel()
        advanceUntilIdle()

        assertEquals("Unable to connect to the server", viewModel.uiState.value.userMessage)
    }

    @Test
    fun detailsReadTheSavedContestWithoutNetwork() = runTest(dispatcher) {
        repository.cachedContests.value = listOf(contest(7, name = "Codeforces Round 1121 (Div. 2)"))
        repository.remoteContests = Either.Left(ServerConnectionResponseError())

        val viewModel = detailsViewModel()
        viewModel.getContestById(7)
        advanceUntilIdle()

        assertEquals("Codeforces Round 1121 (Div. 2)", viewModel.uiState.value.contest?.name)
    }

    @Test
    fun detailsFetchTheListWhenTheContestIsNotSaved() = runTest(dispatcher) {
        repository.remoteContests = Either.Right(listOf(contest(7)))

        val viewModel = detailsViewModel()
        viewModel.getContestById(7)
        advanceUntilIdle()

        assertEquals(7, viewModel.uiState.value.contest?.id)
    }

    @Test
    fun detailsShowNotFoundForAnUnknownContest() = runTest(dispatcher) {
        repository.remoteContests = Either.Right(listOf(contest(7)))

        val viewModel = detailsViewModel()
        viewModel.getContestById(404)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.loading)
        assertEquals("No Data found", state.userMessage)
    }
}
