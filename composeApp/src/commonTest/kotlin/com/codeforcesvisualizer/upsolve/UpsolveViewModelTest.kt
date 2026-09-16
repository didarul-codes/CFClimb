package com.codeforcesvisualizer.upsolve

import com.codeforcesvisualizer.core.data.UserSettingsRepository
import com.codeforcesvisualizer.shared.core.Either
import com.codeforcesvisualizer.shared.domain.entity.ParticipantType
import com.codeforcesvisualizer.shared.domain.entity.Problem
import com.codeforcesvisualizer.shared.domain.usecase.ObserveProblemsetUseCase
import com.codeforcesvisualizer.shared.domain.usecase.ObserveUserProfileUseCase
import com.codeforcesvisualizer.shared.domain.usecase.RefreshProblemsetUseCase
import com.codeforcesvisualizer.shared.domain.usecase.RefreshUserProfileUseCase
import com.codeforcesvisualizer.testing.FakeCFRepository
import com.codeforcesvisualizer.testing.InMemoryPreferencesDataStore
import com.codeforcesvisualizer.testing.acceptedSubmission
import com.codeforcesvisualizer.testing.ratingChange
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
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class UpsolveViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val repository = FakeCFRepository()
    private val userSettings = UserSettingsRepository(InMemoryPreferencesDataStore())

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = UpsolveViewModel(
        userSettings = userSettings,
        observeUserProfile = ObserveUserProfileUseCase(repository),
        refreshUserProfile = RefreshUserProfileUseCase(repository),
        observeProblemset = ObserveProblemsetUseCase(repository),
        refreshProblemset = RefreshProblemsetUseCase(repository),
    )

    @Test
    fun withoutASavedHandleTheScreenAsksForOne() = runTest(dispatcher) {
        val viewModel = viewModel()
        advanceUntilIdle()

        assertEquals(UpsolveUiState.NoHandle, viewModel.uiState.value)
    }

    @Test
    fun queueListsTriedAndUnopenedProblemsAndFilters() = runTest(dispatcher) {
        givenClimber()
        userSettings.setUsername("climber")
        val viewModel = viewModel()
        advanceUntilIdle()

        val all = assertIs<UpsolveUiState.Ready>(viewModel.uiState.value)
        assertEquals(listOf("2264-B", "2264-C"), all.items.map { it.problem.key })
        assertEquals(1, all.triedCount)
        assertEquals(1, all.notOpenedCount)

        viewModel.setFilter(UpsolveFilter.NOT_OPENED)
        advanceUntilIdle()

        val notOpened = assertIs<UpsolveUiState.Ready>(viewModel.uiState.value)
        assertEquals(listOf("2264-C"), notOpened.items.map { it.problem.key })
    }

    @Test
    fun failureWithNothingSavedShowsTheErrorAndCanBeRetried() = runTest(dispatcher) {
        userSettings.setUsername("climber")
        val viewModel = viewModel()
        advanceUntilIdle()
        assertIs<UpsolveUiState.Failed>(viewModel.uiState.value)

        givenClimber()
        viewModel.reload()
        advanceUntilIdle()

        assertIs<UpsolveUiState.Ready>(viewModel.uiState.value)
    }

    private fun givenClimber() {
        repository.ratings["climber"] = Either.Right(
            listOf(ratingChange(1500, 1520).copy(contestId = 2264, contestName = "Codeforces Round 2264"))
        )
        repository.submissions["climber"] = Either.Right(
            listOf(
                acceptedSubmission("A").copy(problem = problem("A", 800), participantType = ParticipantType.CONTESTANT),
                acceptedSubmission("B").copy(problem = problem("B", 1200), verdict = "WRONG_ANSWER"),
            )
        )
        repository.remoteProblemset = Either.Right(
            listOf(problem("A", 800), problem("B", 1200), problem("C", 1600), problem("A", 800).copy(contestId = 1999))
        )
    }

    private fun problem(index: String, rating: Int) = Problem(
        contestId = 2264,
        problemsetName = null,
        index = index,
        name = "Problem $index",
        rating = rating,
        tags = listOf("math"),
    )
}
