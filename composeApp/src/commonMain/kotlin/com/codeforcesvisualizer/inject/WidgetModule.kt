package com.codeforcesvisualizer.inject

import com.codeforcesvisualizer.core.widget.HomeWidgets
import com.codeforcesvisualizer.core.widget.WidgetDataSource
import org.koin.dsl.module

val widgetModule = module {
    single {
        WidgetDataSource(
            observeContestList = get(),
            observeUserProfile = get(),
            userSettings = get()
        )
    }
    single { HomeWidgets(dataSource = get(), updater = get()) }
}
