package com.codeforcesvisualizer.inject

import com.codeforcesvisualizer.core.data.RecentSearchRepository
import com.codeforcesvisualizer.core.data.UserSettingsRepository
import com.codeforcesvisualizer.compare.CompareHandlesViewModel
import com.codeforcesvisualizer.contest.details.ContestDetailsViewModel
import com.codeforcesvisualizer.contest.list.ContestViewModel
import com.codeforcesvisualizer.contest.search.ContestSearchViewModel
import com.codeforcesvisualizer.preference.PreferenceViewModel
import com.codeforcesvisualizer.preference.ThemeManager
import com.codeforcesvisualizer.preference.ThemeManagerViewModel
import com.codeforcesvisualizer.profile.ProfileSearchViewModel
import org.koin.dsl.module

val viewModelModule = module {
    single { RecentSearchRepository(get()) }
    single { UserSettingsRepository(get()) }
    factory { ContestViewModel(get(), get()) }
    factory { ContestSearchViewModel(get()) }
    factory { ContestDetailsViewModel(get(), get()) }
    factory { ProfileSearchViewModel(get(), get(), get()) }
    factory { CompareHandlesViewModel(get(), get(), get()) }
    factory<ThemeManager> { ThemeManagerViewModel(get(), get()) }
    factory { PreferenceViewModel() }
}
