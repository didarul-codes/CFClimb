package com.codeforcesvisualizer.inject

import com.codeforcesvisualizer.shared.domain.usecase.FilterContestListUseCase
import com.codeforcesvisualizer.shared.domain.usecase.GetUiThemeModeUseCase
import com.codeforcesvisualizer.shared.domain.usecase.GetUserRatingsByHandleUseCase
import com.codeforcesvisualizer.shared.domain.usecase.GetUserStatusByHandleUseCase
import com.codeforcesvisualizer.shared.domain.usecase.ObserveContestListUseCase
import com.codeforcesvisualizer.shared.domain.usecase.ObserveContestUseCase
import com.codeforcesvisualizer.shared.domain.usecase.ObserveUserProfileUseCase
import com.codeforcesvisualizer.shared.domain.usecase.RefreshContestListUseCase
import com.codeforcesvisualizer.shared.domain.usecase.RefreshUserProfileUseCase
import com.codeforcesvisualizer.shared.domain.usecase.SetUiThemeModeUseCase
import org.koin.dsl.module

val useCaseModule = module {
    single { ObserveContestListUseCase(get()) }
    single { ObserveContestUseCase(get()) }
    single { RefreshContestListUseCase(get()) }
    single { FilterContestListUseCase(get()) }
    single { ObserveUserProfileUseCase(get()) }
    single { RefreshUserProfileUseCase(get()) }
    single { GetUserStatusByHandleUseCase(get()) }
    single { GetUserRatingsByHandleUseCase(get()) }
    single { GetUiThemeModeUseCase(get(), get()) }
    single { SetUiThemeModeUseCase(get(), get()) }
}
