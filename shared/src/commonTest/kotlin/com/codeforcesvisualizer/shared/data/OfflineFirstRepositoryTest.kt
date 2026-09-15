package com.codeforcesvisualizer.shared.data

import com.codeforcesvisualizer.shared.core.Either
import com.codeforcesvisualizer.shared.data.local.buildCFDatabase
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** The database is the source of truth: saved data outlives failed requests and app restarts. */
class OfflineFirstRepositoryTest : DatabaseTest() {

    private val database = inMemoryDatabase()
    private val codeforces = FakeCodeforces(database)
    private val repository = codeforces.repository

    @AfterTest
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun savedContestIsReadableAfterRestartWithoutNetwork() = runTest {
        codeforces.stub("contest.list", ApiFixtures.CONTEST_LIST)
        repository.refreshContestList().value()

        // A new repository on the same database, with no API responses, stands in for the app
        // coming back after process death while offline.
        val restarted = FakeCodeforces(database).repository

        assertEquals("Codeforces Round 1121 (Div. 2)", restarted.observeContest(2264).first()?.name)
        assertEquals(2, restarted.observeContestList().first().size)
    }

    @Test
    fun failedRefreshKeepsTheSavedProfile() = runTest {
        codeforces.stub("user.info", ApiFixtures.USER_INFO)
        codeforces.stub("user.rating", ApiFixtures.USER_RATING)
        repository.refreshUser("tourist").value()
        repository.refreshUserRatings("tourist").value()

        codeforces.stub("user.info", "", HttpStatusCode.ServiceUnavailable)
        codeforces.stub("user.rating", "", HttpStatusCode.ServiceUnavailable)

        assertTrue(repository.refreshUser("tourist") is Either.Left)
        assertEquals(3301, repository.observeUser("tourist").first()?.rating)
        assertEquals(2, repository.getUserRatingByHandle("tourist").value().size)
    }

    @Test
    fun handlesAreCaseInsensitive() = runTest {
        codeforces.stub("user.info", ApiFixtures.USER_INFO)

        repository.refreshUser("Tourist").value()

        assertEquals("tourist", repository.observeUser("TOURIST").first()?.handle)
    }

    @Test
    fun ratingsAreNullUntilFetchedAndEmptyForUnratedUsers() = runTest {
        assertNull(repository.observeUserRatings("newcomer").first())

        codeforces.stub("user.rating", """{"status":"OK","result":[]}""")
        repository.refreshUserRatings("newcomer").value()

        assertEquals(emptyList(), repository.observeUserRatings("newcomer").first())
    }

    @Test
    fun refreshReplacesPreviouslySavedSubmissions() = runTest {
        codeforces.stub("user.status", ApiFixtures.USER_STATUS)
        repository.refreshUserSubmissions("tourist").value()

        codeforces.stub("user.status", ApiFixtures.USER_STATUS_OPTIONAL_FIELDS)
        repository.refreshUserSubmissions("tourist").value()

        assertEquals(listOf(1L), repository.observeUserSubmissions("tourist").first()?.map { it.id })
    }
}
