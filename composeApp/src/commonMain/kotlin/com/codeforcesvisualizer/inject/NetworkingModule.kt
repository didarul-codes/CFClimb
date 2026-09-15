package com.codeforcesvisualizer.inject

import com.codeforcesvisualizer.core.platform.isDebugBuild
import com.codeforcesvisualizer.shared.data.network.ApiClient
import com.codeforcesvisualizer.shared.data.network.CFApiService
import io.ktor.client.HttpClient
import org.koin.dsl.module

val networkingModule = module {
    single<HttpClient> { ApiClient.getHttpClient(enableLogging = isDebugBuild) }
    single { CFApiService(get()) }
}
