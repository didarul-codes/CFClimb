package com.codeforcesvisualizer.shared.data.network

import com.codeforcesvisualizer.shared.data.config.BASE_URL
import com.codeforcesvisualizer.shared.data.config.NETWORK_CONNECT_TIMEOUT
import com.codeforcesvisualizer.shared.data.config.NETWORK_READ_TIMEOUT
import com.codeforcesvisualizer.shared.data.config.NETWORK_WRITE_TIMEOUT
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.serialization.kotlinx.json.json
import io.ktor.http.takeFrom
import kotlinx.serialization.json.Json

object ApiClient {
    /**
     * @param enableLogging logs request and response bodies. Keep it off in release builds:
     * `user.status` responses for active users are hundreds of kilobytes.
     */
    fun getHttpClient(enableLogging: Boolean): HttpClient {
        return HttpClient {
            expectSuccess = false

            install(HttpTimeout) {
                connectTimeoutMillis = NETWORK_CONNECT_TIMEOUT
                socketTimeoutMillis = NETWORK_READ_TIMEOUT
                requestTimeoutMillis = NETWORK_WRITE_TIMEOUT
            }

            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        coerceInputValues = true
                        explicitNulls = false
                    }
                )
            }

            if (enableLogging) {
                install(Logging) {
                    logger = Logger.DEFAULT
                    level = LogLevel.BODY
                }
            }

            defaultRequest {
                url {
                    takeFrom(BASE_URL)
                }
            }
        }
    }
}