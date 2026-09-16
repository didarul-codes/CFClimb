package com.codeforcesvisualizer.climb

import com.codeforcesvisualizer.core.data.UserSettingsRepository
import com.codeforcesvisualizer.shared.core.Either
import com.codeforcesvisualizer.shared.domain.usecase.ObserveUserProfileUseCase
import com.codeforcesvisualizer.shared.domain.usecase.RefreshUserProfileUseCase
import com.codeforcesvisualizer.testing.FakeCFRepository
import com.codeforcesvisualizer.testing.InMemoryPreferencesDataStore
import com.codeforcesvisualizer.testing.acceptedSubmission
import com.codeforcesvisualizer.testing.ratingChange
import com.codeforcesvisualizer.testing.user
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class MyClimbViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val repository = FakeCFRepository()
    private val userSettings = UserSettingsRepository(InMemoryPreferencesDataStore())
    private val now = Instant.parse("2026-09-15T12:00:00Z")

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = MyClimbViewModel(
        userSettings = userSettings,
        observeUserProfile = ObserveUserProfileUseCase(repository),
        refreshUserProfile = RefreshUserProfileUseCase(repository),
        clock = object : Clock {
            override fun now(): Instant = now
        },
        timeZone = TimeZone.UTC,
    )

    @Test
    fun withoutASavedHandleTheCardAsksForOne() = runTest(dispatcher) {
        val viewModel = viewModel()
        advanceUntilIdle()

        assertEquals(MyClimbUiState.NoHandle, viewModel.uiState.value)
    }

    @Test
    fun savedHandleShowsRatingTierAndWeek() = runTest(dispatcher) {
        givenClimber()
        userSettings.setUsername("climber")

        val viewModel = viewModel()
        advanceUntilIdle()

        val summary = assertIs<MyClimbUiState.Ready>(viewModel.uiState.value).summary
        assertEquals(1547, summary.rating)
        assertEquals(53, summary.tierProgress?.pointsToNext)
        assertEquals(1, summary.solvedThisWeek)
        assertEquals(1, summary.streakDays)
    }

    @Test
    fun failureWithNothingSavedCanBeRetried() = runTest(dispatcher) {
        userSettings.setUsername("climber")
        val viewModel = viewModel()
        advanceUntilIdle()
        assertIs<MyClimbUiState.Failed>(viewModel.uiState.value)

        givenClimber()
        viewModel.retry()
        advanceUntilIdle()

        assertIs<MyClimbUiState.Ready>(viewModel.uiState.value)
    }

    private fun givenClimber() {
        repository.users["climber"] = Either.Right(user("climber", rating = 1547))
        repository.ratings["climber"] = Either.Right(listOf(ratingChange(0, 1500), ratingChange(1500, 1547)))
        repository.submissions["climber"] = Either.Right(
            listOf(acceptedSubmission("A").copy(creationTimeSeconds = now.epochSeconds - 3_600))
        )
    }
}
