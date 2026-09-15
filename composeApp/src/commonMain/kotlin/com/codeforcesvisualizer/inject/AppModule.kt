package com.codeforcesvisualizer.inject

import com.codeforcesvisualizer.shared.data.datasource.CFRemoteDataSource
import com.codeforcesvisualizer.shared.data.datasource.CFRemoteDataSourceImpl
import com.codeforcesvisualizer.shared.data.local.CFDatabase
import com.codeforcesvisualizer.shared.data.local.buildCFDatabase
import com.codeforcesvisualizer.shared.data.network.RequestThrottle
import com.codeforcesvisualizer.shared.data.repository.CFRepositoryImpl
import com.codeforcesvisualizer.shared.data.repository.ThemeRepositoryImpl
import com.codeforcesvisualizer.shared.domain.repository.CFRepository
import com.codeforcesvisualizer.shared.domain.repository.ThemeRepository
import org.koin.dsl.module

val appModule = module {
    // One throttle for the whole app: the API rate limit applies per client, not per screen.
    single { RequestThrottle() }
    single<CFRemoteDataSource> {
        CFRemoteDataSourceImpl(
            api = get(),
            throttle = get()
        )
    }
    single { buildCFDatabase(createDatabaseBuilder()) }
    single { get<CFDatabase>().contestDao() }
    single { get<CFDatabase>().profileDao() }
    single<CFRepository> {
        CFRepositoryImpl(
            cfRemoteDataSource = get(),
            contestDao = get(),
            profileDao = get()
        )
    }
    single<ThemeRepository> { ThemeRepositoryImpl() }
}
