package com.codeforcesvisualizer.inject

import com.codeforcesvisualizer.core.reminders.ContestReminders
import com.codeforcesvisualizer.core.reminders.ReminderSettingsRepository
import org.koin.dsl.module

val reminderModule = module {
    single { ReminderSettingsRepository(get()) }
    single {
        ContestReminders(
            observeContestListUseCase = get(),
            settings = get(),
            scheduler = get()
        )
    }
}
