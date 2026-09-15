package com.codeforcesvisualizer.inject

import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.mp.KoinPlatformTools

/** Platform bindings, such as the reminder scheduler. */
expect val platformModule: Module

/**
 * Starts dependency injection once per process. Android calls it from `Application`, so
 * background components like reminder receivers can use it without any UI running; iOS calls
 * it from `MainViewController`.
 */
fun initKoin() {
    if (KoinPlatformTools.defaultContext().getOrNull() != null) return
    startKoin {
        modules(
            networkingModule,
            appModule,
            preferenceModule,
            useCaseModule,
            viewModelModule,
            reminderModule,
            ratingAlertModule,
            widgetModule,
            linkModule,
            platformModule
        )
    }
}
