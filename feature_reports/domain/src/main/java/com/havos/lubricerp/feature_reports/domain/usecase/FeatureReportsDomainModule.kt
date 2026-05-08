package com.havos.lubricerp.feature_reports.domain.usecase

import org.koin.dsl.module

val featureReportsDomainModule = module {
    factory { ObserveSessionUseCase(get()) }
    factory { ObserveProfileUseCase(get()) }
    factory { EnsureProfileLoadedUseCase(get()) }
    factory { ObserveRememberedUsernameUseCase(get()) }
    factory { ObserveRememberMeEnabledUseCase(get()) }
    factory { LoginUseCase(get()) }
    factory { LogoutUseCase(get()) }
    factory { RefreshSessionUseCase(get()) }
    factory { ObserveBiometricEnabledUseCase(get()) }
    factory { SetBiometricEnabledUseCase(get()) }

    factory { GetTankStockSummaryUseCase(get()) }
    factory { GetRawMaterialStockUseCase(get()) }
    factory { GetPackagingLossGainUseCase(get()) }
    factory { GetDashboardUseCase(get()) }
    factory { GetSalesSummaryUseCase(get()) }
    factory { GetPaymentsReceivedUseCase(get()) }
    factory { GetStockOverviewUseCase(get()) }
    factory { GetCustomersUseCase(get()) }
    factory { GetCustomerLedgerUseCase(get()) }
    factory { GetCustomerMobileSummaryUseCase(get()) }
    factory { GetReportSalesSummaryUseCase(get()) }
    factory { GetProductSalesUseCase(get()) }
    factory { GetNetProfitUseCase(get()) }
    factory { GetExpenseSummaryUseCase(get()) }
}
