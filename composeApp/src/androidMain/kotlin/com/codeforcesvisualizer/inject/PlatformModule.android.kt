package com.codeforcesvisualizer.inject

import com.codeforcesvisualizer.Application
import com.codeforcesvisualizer.core.ratingalerts.AndroidRatingChangeNotifier
import com.codeforcesvisualizer.core.ratingalerts.AndroidRatingCheckScheduler
import com.codeforcesvisualizer.core.ratingalerts.RatingChangeNotifier
import com.codeforcesvisualizer.core.ratingalerts.RatingCheckScheduler
import com.codeforcesvisualizer.core.reminders.AndroidReminderScheduler
import com.codeforcesvisualizer.core.reminders.ReminderScheduler
import com.codeforcesvisualizer.core.widget.GlanceWidgetUpdater
import com.codeforcesvisualizer.core.widget.WidgetUpdater
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<ReminderScheduler> { AndroidReminderScheduler(Application.context) }
    single<RatingCheckScheduler> { AndroidRatingCheckScheduler(Application.context) }
    single<RatingChangeNotifier> { AndroidRatingChangeNotifier(Application.context) }
    single<WidgetUpdater> { GlanceWidgetUpdater(Application.context) }
}
