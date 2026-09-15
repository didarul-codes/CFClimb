package com.codeforcesvisualizer.profile

import app.cash.turbine.test
import com.codeforcesvisualizer.core.data.RecentSearchRepository
import com.codeforcesvisualizer.shared.core.Either
import com.codeforcesvisualizer.shared.core.ServerConnectionResponseError
import com.codeforcesvisualizer.shared.domain.usecase.ObserveUserProfileUseCase
import com.codeforcesvisualizer.shared.domain.usecase.RefreshUserProfileUseCase
import com.codeforcesvisualizer.testing.FakeCFRepository
import com.codeforcesvisualizer.testing.InMemoryPreferencesDataStore
import com.codeforcesvisualizer.testing.acceptedSubmission
import com.codeforcesvisualizer.testing.ratingChange
import com.codeforcesvisualizer.testing.user
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
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

    private val viewModel by lazy {
        ProfileSearchViewModel(
            observeUserProfileUseCase = ObserveUserProfileUseCase(repository),
            refreshUserProfileUseCase = RefreshUserProfileUseCase(repository),
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
    fun searchShowsLoadingThenTheProfile() = runTest(dispatcher) {
        givenTourist()
        // Hold the responses so the loading state is observable before they arrive.
        val responses = CompletableDeferred<Unit>()
        repository.gates["tourist"] = responses

        viewModel.search("tourist")
        runCurrent()

        assertTrue(viewModel.userInfoState.value.loading)
        assertNull(viewModel.userInfoState.value.user)

        responses.complete(Unit)
        advanceUntilIdle()

        val loaded = viewModel.userInfoState.value
        assertFalse(loaded.loading)
        assertEquals(3301, loaded.user?.rating)
        assertEquals(1, viewModel.userStatusState.value.userStatus?.size)
        assertEquals(1, viewModel.userRatingState.value.userRatings?.size)
    }

    @Test
    fun unknownHandleShowsTheError() = runTest(dispatcher) {
        viewModel.search("zz_no_such_handle_zz")
        advanceUntilIdle()

        val state = viewModel.userInfoState.value
        assertFalse(state.loading)
        assertNull(state.user)
        assertEquals("No Data found", state.userMessage)
    }

    @Test
    fun savedProfileStaysVisibleWhenRefreshFails() = runTest(dispatcher) {
        givenTourist()
        viewModel.search("tourist")
        advanceUntilIdle()

        repository.users["tourist"] = Either.Left(ServerConnectionResponseError())
        viewModel.search("tourist")
        advanceUntilIdle()

        val state = viewModel.userInfoState.value
        assertEquals(3301, state.user?.rating)
        assertEquals("", state.userMessage)
        assertTrue(viewModel.showingSavedData.value)
    }

    @Test
    fun newSearchCancelsTheSlowerPreviousOne() = runTest(dispatcher) {
        val slowResponse = CompletableDeferred<Unit>()
        repository.gates["slow"] = slowResponse
        repository.users["slow"] = Either.Right(user("slow"))
        repository.users["fast"] = Either.Right(user("fast"))

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
        viewModel.search("   ")
        advanceUntilIdle()

        assertEquals(UserInfoUiState(), viewModel.userInfoState.value)
    }

    @Test
    fun searchedHandleAppearsInRecentSearches() = runTest(dispatcher) {
        viewModel.recentSearches.test {
            assertEquals(emptyList(), awaitItem())

            viewModel.search("tourist")

            assertEquals(listOf("tourist"), awaitItem())
        }
    }

    private fun givenTourist() {
        repository.users["tourist"] = Either.Right(user("tourist", rating = 3301))
        repository.ratings["tourist"] = Either.Right(listOf(ratingChange(0, 3301)))
        repository.submissions["tourist"] = Either.Right(listOf(acceptedSubmission("A")))
    }
}
