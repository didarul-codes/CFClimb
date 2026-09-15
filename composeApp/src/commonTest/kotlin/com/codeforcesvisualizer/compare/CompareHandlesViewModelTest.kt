package com.codeforcesvisualizer.compare

import app.cash.turbine.test
import com.codeforcesvisualizer.core.data.RecentSearchRepository
import com.codeforcesvisualizer.shared.core.AppError
import com.codeforcesvisualizer.shared.core.Either
import com.codeforcesvisualizer.shared.domain.usecase.GetUserRatingsByHandleUseCase
import com.codeforcesvisualizer.shared.domain.usecase.GetUserStatusByHandleUseCase
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
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CompareHandlesViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val repository = FakeCFRepository()

    private val viewModel by lazy {
        CompareHandlesViewModel(
            getUserRatingsByHandleUseCase = GetUserRatingsByHandleUseCase(repository),
            getUserStatusByHandleUseCase = GetUserStatusByHandleUseCase(repository),
            recentSearchRepository = RecentSearchRepository(InMemoryPreferencesDataStore())
        )
    }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun comparisonLoadsBothUsers() = runTest(dispatcher) {
        givenUser("tourist")
        givenUser("jiangly")

        viewModel.userRatingState.test {
            assertEquals(UserRatingUiState(), awaitItem())

            viewModel.compare("tourist", "jiangly")

            assertTrue(awaitItem().loading)
            val loaded = awaitItem()
            assertFalse(loaded.loading)
            assertNotNull(loaded.userRatings1)
            assertNotNull(loaded.userRatings2)
            assertTrue(loaded.errors.isEmpty())
        }
        advanceUntilIdle()
        assertNotNull(viewModel.userStatusState.value.userStatus2)
    }

    @Test
    fun oneUnknownHandleKeepsTheOtherUserAndShowsOneErrorPerHandle() = runTest(dispatcher) {
        givenUser("tourist")
        // The API words the same failure differently per endpoint.
        repository.ratings["nobody"] = Either.Left(AppError("handle: User nobody not found"))
        repository.submissions["nobody"] = Either.Left(AppError("handle: User with handle nobody not found"))

        viewModel.compare("tourist", "nobody")
        advanceUntilIdle()

        val ratingState = viewModel.userRatingState.value
        assertNotNull(ratingState.userRatings1)
        assertNull(ratingState.userRatings2)
        assertEquals(mapOf("nobody" to "handle: User nobody not found"), ratingState.errors)
        assertEquals(setOf("nobody"), viewModel.userStatusState.value.errors.keys)
    }

    private fun givenUser(handle: String) {
        repository.ratings[handle] = Either.Right(listOf(ratingChange(0, 1500)))
        repository.submissions[handle] = Either.Right(listOf(acceptedSubmission("A")))
    }
}
