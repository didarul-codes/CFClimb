package com.codeforcesvisualizer.inject

import com.codeforcesvisualizer.core.ratingalerts.IosRatingChangeNotifier
import com.codeforcesvisualizer.core.ratingalerts.IosRatingCheckScheduler
import com.codeforcesvisualizer.core.ratingalerts.RatingChangeNotifier
import com.codeforcesvisualizer.core.ratingalerts.RatingCheckScheduler
import com.codeforcesvisualizer.core.reminders.IosReminderScheduler
import com.codeforcesvisualizer.core.reminders.ReminderScheduler
import com.codeforcesvisualizer.core.widget.IosWidgetUpdater
import com.codeforcesvisualizer.core.widget.WidgetUpdater
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<ReminderScheduler> { IosReminderScheduler() }
    single<RatingCheckScheduler> { IosRatingCheckScheduler() }
    single<RatingChangeNotifier> { IosRatingChangeNotifier() }
    single<WidgetUpdater> { IosWidgetUpdater(get()) }
}
