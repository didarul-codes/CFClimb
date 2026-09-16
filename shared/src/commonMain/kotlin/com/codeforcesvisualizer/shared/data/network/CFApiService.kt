package com.codeforcesvisualizer.shared.data.network

import com.codeforcesvisualizer.shared.data.config.CONTEST_LIST_URL
import com.codeforcesvisualizer.shared.data.config.PROBLEMSET_PROBLEMS_URL
import com.codeforcesvisualizer.shared.data.config.USER_INFO_URL
import com.codeforcesvisualizer.shared.data.config.USER_RATINGS_URL
import com.codeforcesvisualizer.shared.data.config.USER_STATUS_URL
import com.codeforcesvisualizer.shared.data.model.BaseResponseModel
import com.codeforcesvisualizer.shared.data.model.ProblemsetResponseModel
import com.codeforcesvisualizer.shared.data.model.UserInfoResponseModel
import com.codeforcesvisualizer.shared.data.model.UserRatingResponseModel
import com.codeforcesvisualizer.shared.data.model.UserStatusResponseModel
import com.codeforcesvisualizer.shared.data.config.CONTEST_LIST_URL
import com.codeforcesvisualizer.shared.data.model.ContestListResponseModel
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode

class CFApiService(
    private val httpClient: HttpClient
) {
    suspend fun getContestList(): CFApiResponse<ContestListResponseModel> {
        return httpClient.get(CONTEST_LIST_URL).toCFApiResponse()
    }

    suspend fun getUserInfoByHandle(handle: String): CFApiResponse<UserInfoResponseModel> {
        return httpClient.get(USER_INFO_URL) {
            parameter("handles", handle)
        }.toCFApiResponse()
    }

    suspend fun getUserStatusByHandle(handle: String): CFApiResponse<UserStatusResponseModel> {
        return httpClient.get(USER_STATUS_URL) {
            parameter("handle", handle)
        }.toCFApiResponse()
    }

    suspend fun getUserRatingByHandle(handle: String): CFApiResponse<UserRatingResponseModel> {
        return httpClient.get(USER_RATINGS_URL) {
            parameter("handle", handle)
        }.toCFApiResponse()
    }

    suspend fun getProblemset(): CFApiResponse<ProblemsetResponseModel> {
        return httpClient.get(PROBLEMSET_PROBLEMS_URL).toCFApiResponse()
    }
}

data class CFApiResponse<T : BaseResponseModel>(
    val statusCode: HttpStatusCode,
    val body: T?
)

private suspend inline fun <reified T : BaseResponseModel> HttpResponse.toCFApiResponse(): CFApiResponse<T> {
    val parsedBody = try {
        body<T>()
    } catch (_: Exception) {
        null
    }
    return CFApiResponse(statusCode = status, body = parsedBody)
}