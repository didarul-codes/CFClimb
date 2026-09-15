package com.codeforcesvisualizer.inject

import com.codeforcesvisualizer.core.ratingalerts.RatingAlertSettingsRepository
import com.codeforcesvisualizer.core.ratingalerts.RatingChangeAlerts
import org.koin.dsl.module

val ratingAlertModule = module {
    single { RatingAlertSettingsRepository(get()) }
    single {
        RatingChangeAlerts(
            userSettings = get(),
            settings = get(),
            refreshUserProfile = get(),
            observeUserProfile = get(),
            notifier = get(),
            scheduler = get()
        )
    }
}
