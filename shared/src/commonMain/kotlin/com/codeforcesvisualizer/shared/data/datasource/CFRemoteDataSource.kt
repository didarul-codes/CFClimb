package com.codeforcesvisualizer.shared.data.datasource

import com.codeforcesvisualizer.shared.core.AppError
import com.codeforcesvisualizer.shared.core.Either
import com.codeforcesvisualizer.shared.core.InvalidApiResponseError
import com.codeforcesvisualizer.shared.core.ServerConnectionResponseError
import com.codeforcesvisualizer.shared.data.model.BaseResponseModel
import com.codeforcesvisualizer.shared.data.model.ContestListResponseModel
import com.codeforcesvisualizer.shared.data.model.StatusModel
import com.codeforcesvisualizer.shared.data.model.UserInfoResponseModel
import com.codeforcesvisualizer.shared.data.model.UserRatingResponseModel
import com.codeforcesvisualizer.shared.data.model.UserStatusResponseModel
import com.codeforcesvisualizer.shared.data.network.CFApiResponse
import com.codeforcesvisualizer.shared.data.network.CFApiService
import com.codeforcesvisualizer.shared.data.network.RequestThrottle
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

interface CFRemoteDataSource {
    suspend fun getContestList(): Either<AppError, ContestListResponseModel>
    suspend fun getUserInfoByHandle(handle: String): Either<AppError, UserInfoResponseModel>
    suspend fun getUserStatusByHandle(handle: String): Either<AppError, UserStatusResponseModel>
    suspend fun getUserRatingByHandle(handle: String): Either<AppError, UserRatingResponseModel>
}

class CFRemoteDataSourceImpl(
    private val api: CFApiService,
    private val throttle: RequestThrottle
) : CFRemoteDataSource {
    override suspend fun getContestList(): Either<AppError, ContestListResponseModel> {
        return executeRequest(
            request = { api.getContestList() },
            hasValidResult = { it.result != null }
        )
    }

    override suspend fun getUserInfoByHandle(handle: String): Either<AppError, UserInfoResponseModel> {
        return executeRequest(
            request = { api.getUserInfoByHandle(handle) },
            hasValidResult = { !it.result.isNullOrEmpty() }
        )
    }

    override suspend fun getUserStatusByHandle(handle: String): Either<AppError, UserStatusResponseModel> {
        return executeRequest(
            request = { api.getUserStatusByHandle(handle) },
            hasValidResult = { it.result != null }
        )
    }

    override suspend fun getUserRatingByHandle(handle: String): Either<AppError, UserRatingResponseModel> {
        return executeRequest(
            request = { api.getUserRatingByHandle(handle) },
            hasValidResult = { it.result != null }
        )
    }

    private suspend inline fun <reified T : BaseResponseModel> executeRequest(
        crossinline request: suspend () -> CFApiResponse<T>,
        crossinline hasValidResult: (T) -> Boolean
    ): Either<AppError, T> {
        return withContext(Dispatchers.IO) {
            try {
                val response = throttle.run { request() }
                if (!response.statusCode.isSuccess()) {
                    return@withContext Either.Left(
                        response.body?.comment?.takeIf { it.isNotBlank() }?.let { AppError(it) }
                            ?: ServerConnectionResponseError()
                    )
                }

                val body = response.body ?: return@withContext Either.Left(InvalidApiResponseError())

                when (body.statusModel) {
                    StatusModel.OK if hasValidResult(body) -> {
                        Either.Right(body)
                    }
                    StatusModel.FAILED -> {
                        Either.Left(body.comment?.takeIf { it.isNotBlank() }
                            ?.let { AppError(it) } ?: InvalidApiResponseError())
                    }
                    else -> Either.Left(InvalidApiResponseError())
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Either.Left(ServerConnectionResponseError())
            }
        }
    }
}
