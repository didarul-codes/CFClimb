package com.codeforcesvisualizer.shared.data

import com.codeforcesvisualizer.shared.core.InvalidApiResponseError
import com.codeforcesvisualizer.shared.core.ServerConnectionResponseError
import com.codeforcesvisualizer.shared.data.local.buildCFDatabase
import com.codeforcesvisualizer.shared.domain.entity.ParticipantType
import com.codeforcesvisualizer.shared.domain.entity.UserStatus
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Runs recorded Codeforces responses through the real HTTP client, data source, repository and
 * database, so a change in parsing or error handling shows up here before it reaches users.
 */
class CFRepositoryContractTest : DatabaseTest() {

    private val database = inMemoryDatabase()
    private val codeforces = FakeCodeforces(database)
    private val repository = codeforces.repository

    @AfterTest
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun userInfoMapsProfileAndTrimsHandle() = runTest {
        codeforces.stub("user.info", ApiFixtures.USER_INFO)

        val user = repository.getUserInfoByHandle("  tourist ").value()

        assertEquals("tourist", user.handle)
        assertEquals(3301, user.rating)
        assertEquals(4009, user.maxRating)
        assertEquals("legendary grandmaster", user.rank)
        assertEquals("tourist", codeforces.requests.single().url.parameters["handles"])
    }

    @Test
    fun ratingHistoryIncludesContestNameAndUpdateTime() = runTest {
        codeforces.stub("user.rating", ApiFixtures.USER_RATING)

        val history = repository.getUserRatingByHandle("tourist").value()

        assertEquals(2, history.size)
        assertEquals("Codeforces Beta Round 2", history.first().contestName)
        assertEquals(1_267_124_400L, history.first().ratingUpdateTimeSeconds)
        assertEquals(1764, history.last().newRating)
    }

    @Test
    fun submissionsMapRatingTimeAndParticipantType() = runTest {
        codeforces.stub("user.status", ApiFixtures.USER_STATUS)

        val submissions = repository.getUserStatusByHandle("tourist").value()

        val accepted = submissions.first()
        assertEquals(1_789_326_747L, accepted.creationTimeSeconds)
        assertEquals(ParticipantType.PRACTICE, accepted.participantType)
        assertEquals(2400, accepted.problem.rating)
        assertEquals(listOf("binary search", "bitmasks", "data structures"), accepted.problem.tags)
        assertEquals("1401-F", accepted.problem.key)
        assertTrue(accepted.isAccepted)
        assertEquals("WRONG_ANSWER", submissions.last().verdict)
    }

    @Test
    fun optionalSubmissionFieldsDoNotBreakParsing() = runTest {
        codeforces.stub("user.status", ApiFixtures.USER_STATUS_OPTIONAL_FIELDS)

        val submission = repository.getUserStatusByHandle("tourist").value().single()

        assertEquals(UserStatus.VERDICT_TESTING, submission.verdict)
        assertEquals(ParticipantType.UNKNOWN, submission.participantType)
        assertNull(submission.problem.contestId)
        assertNull(submission.problem.rating)
        assertEquals("acmsguru-100", submission.problem.key)
    }

    @Test
    fun contestListMarksUpcomingContestsAsScheduled() = runTest {
        codeforces.stub("contest.list", ApiFixtures.CONTEST_LIST)

        repository.refreshContestList().value()
        val (upcoming, finished) = repository.observeContestList().first()

        assertTrue(upcoming.scheduled)
        assertEquals("Scheduled", upcoming.phase)
        assertEquals(false, finished.scheduled)
        assertEquals("Finished", finished.phase)
    }

    @Test
    fun unknownHandleReturnsTheApiComment() = runTest {
        codeforces.stub("user.status", ApiFixtures.USER_STATUS_NOT_FOUND, HttpStatusCode.BadRequest)

        val error = repository.getUserStatusByHandle("zz_no_such_handle_zz").error()

        assertEquals("handle: User with handle zz_no_such_handle_zz not found", error.message)
    }

    @Test
    fun unreadableBodyIsAnInvalidResponse() = runTest {
        codeforces.stub("user.rating", "<html>Codeforces is temporarily unavailable</html>")

        assertIs<InvalidApiResponseError>(repository.getUserRatingByHandle("tourist").error())
    }

    @Test
    fun serverErrorWithoutBodyIsAConnectionError() = runTest {
        codeforces.stub("user.info", "", HttpStatusCode.ServiceUnavailable)

        assertIs<ServerConnectionResponseError>(repository.getUserInfoByHandle("tourist").error())
    }
}
