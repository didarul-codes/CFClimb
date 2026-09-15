package com.codeforcesvisualizer.shared.data

import com.codeforcesvisualizer.shared.core.AppError
import com.codeforcesvisualizer.shared.core.Either
import com.codeforcesvisualizer.shared.data.datasource.CFRemoteDataSourceImpl
import com.codeforcesvisualizer.shared.data.local.CFDatabase
import com.codeforcesvisualizer.shared.data.network.ApiClient
import com.codeforcesvisualizer.shared.data.network.CFApiService
import com.codeforcesvisualizer.shared.data.network.RequestThrottle
import com.codeforcesvisualizer.shared.data.repository.CFRepositoryImpl
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.test.fail
import kotlin.time.Duration

/**
 * Answers API calls from stubs and wires the real HTTP client, data source and repository to
 * [database]. Unstubbed calls fail with HTTP 404.
 */
internal class FakeCodeforces(database: CFDatabase) {
    val requests = mutableListOf<HttpRequestData>()
    private val stubs = mutableMapOf<String, Pair<HttpStatusCode, String>>()

    private val engine = MockEngine { request ->
        requests += request
        val method = request.url.encodedPath.substringAfterLast('/')
        val (status, body) = stubs[method] ?: (HttpStatusCode.NotFound to "")
        respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
    }

    val repository = CFRepositoryImpl(
        cfRemoteDataSource = CFRemoteDataSourceImpl(
            api = CFApiService(ApiClient.getHttpClient(enableLogging = false, engine = engine)),
            throttle = RequestThrottle(minInterval = Duration.ZERO)
        ),
        contestDao = database.contestDao(),
        profileDao = database.profileDao()
    )

    /** Answers calls to the API [method], e.g. "user.info", with [body]. */
    fun stub(method: String, body: String, status: HttpStatusCode = HttpStatusCode.OK) {
        stubs[method] = status to body
    }
}

internal fun <V> Either<AppError, V>.value(): V = when (this) {
    is Either.Right -> data
    is Either.Left -> fail("Expected a result but got error: ${data.message}")
}

internal fun <V> Either<AppError, V>.error(): AppError = when (this) {
    is Either.Left -> data
    is Either.Right -> fail("Expected an error but got: $data")
}
