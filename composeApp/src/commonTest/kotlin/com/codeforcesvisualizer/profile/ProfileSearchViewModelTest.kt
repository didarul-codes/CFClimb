package com.codeforcesvisualizer.profile

import app.cash.turbine.test
import com.codeforcesvisualizer.core.data.RecentSearchRepository
import com.codeforcesvisualizer.shared.core.Either
import com.codeforcesvisualizer.shared.domain.usecase.GetUserInfoByHandleUseCase
import com.codeforcesvisualizer.shared.domain.usecase.GetUserRatingsByHandleUseCase
import com.codeforcesvisualizer.shared.domain.usecase.GetUserStatusByHandleUseCase
import com.codeforcesvisualizer.testing.FakeCFRepository
import com.codeforcesvisualizer.testing.InMemoryPreferencesDataStore
import com.codeforcesvisualizer.testing.acceptedSubmission
import com.codeforcesvisualizer.testing.ratingChange
import com.codeforcesvisualizer.testing.user
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileSearchViewModelTest {

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

    private fun TestScope.viewModel() = ProfileSearchViewModel(
        getUserInfoByHandleUseCase = GetUserInfoByHandleUseCase(repository),
        getUserStatusByHandleUseCase = GetUserStatusByHandleUseCase(repository),
        getUserRatingByHandleUseCase = GetUserRatingsByHandleUseCase(repository),
        recentSearchRepository = RecentSearchRepository(InMemoryPreferencesDataStore())
    )

    @Test
    fun searchShowsLoadingThenTheProfile() = runTest(dispatcher) {
        repository.users["tourist"] = Either.Right(user("tourist", rating = 3301))
        repository.ratings["tourist"] = Either.Right(listOf(ratingChange(0, 3301)))
        repository.submissions["tourist"] = Either.Right(listOf(acceptedSubmission("A")))
        val viewModel = viewModel()

        viewModel.userInfoState.test {
            assertEquals(UserInfoUiState(), awaitItem())

            viewModel.search("tourist")

            assertTrue(awaitItem().loading)
            val loaded = awaitItem()
            assertFalse(loaded.loading)
            assertEquals(3301, loaded.user?.rating)
        }
        advanceUntilIdle()
        assertEquals(1, viewModel.userStatusState.value.userStatus?.size)
        assertEquals(1, viewModel.userRatingState.value.userRatings?.size)
    }

    @Test
    fun unknownHandleShowsTheError() = runTest(dispatcher) {
        val viewModel = viewModel()

        viewModel.search("zz_no_such_handle_zz")
        advanceUntilIdle()

        val state = viewModel.userInfoState.value
        assertFalse(state.loading)
        assertNull(state.user)
        assertEquals("No Data found", state.userMessage)
    }

    @Test
    fun newSearchCancelsTheSlowerPreviousOne() = runTest(dispatcher) {
        val slowResponse = CompletableDeferred<Unit>()
        repository.gates["slow"] = slowResponse
        repository.users["slow"] = Either.Right(user("slow"))
        repository.users["fast"] = Either.Right(user("fast"))
        val viewModel = viewModel()

        viewModel.search("slow")
        runCurrent()
        viewModel.search("fast")
        advanceUntilIdle()
        slowResponse.complete(Unit)
        advanceUntilIdle()

        assertEquals("fast", viewModel.userInfoState.value.user?.handle)
    }

    @Test
    fun blankSearchIsIgnored() = runTest(dispatcher) {
        val viewModel = viewModel()

        viewModel.search("   ")
        advanceUntilIdle()

        assertEquals(UserInfoUiState(), viewModel.userInfoState.value)
    }

    @Test
    fun searchedHandleAppearsInRecentSearches() = runTest(dispatcher) {
        val viewModel = viewModel()

        viewModel.recentSearches.test {
            assertEquals(emptyList(), awaitItem())

            viewModel.search("tourist")

            assertEquals(listOf("tourist"), awaitItem())
        }
    }
}
