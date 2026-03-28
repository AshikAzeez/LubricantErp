package com.havos.lubricerp.feature_reports.domain.usecase

import org.koin.dsl.module

val featureReportsDomainModule = module {
    factory { ObserveSessionUseCase(get()) }
    factory { ObserveRememberedUsernameUseCase(get()) }
    factory { ObserveRememberMeEnabledUseCase(get()) }
    factory { LoginUseCase(get()) }
    factory { LogoutUseCase(get()) }

    factory { GetTankStockSummaryUseCase(get()) }
    factory { GetRawMaterialStockUseCase(get()) }
    factory { GetPackagingLossGainUseCase(get()) }
}
