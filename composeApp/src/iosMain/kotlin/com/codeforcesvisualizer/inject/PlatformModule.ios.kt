package com.codeforcesvisualizer.inject

import com.codeforcesvisualizer.core.reminders.IosReminderScheduler
import com.codeforcesvisualizer.core.reminders.ReminderScheduler
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<ReminderScheduler> { IosReminderScheduler() }
}
