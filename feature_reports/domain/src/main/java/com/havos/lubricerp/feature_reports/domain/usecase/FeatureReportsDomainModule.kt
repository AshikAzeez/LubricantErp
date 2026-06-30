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
    factory { GetProductSalesUseCase(get()) }
    factory { GetNetProfitUseCase(get()) }
    factory { GetExpenseSummaryUseCase(get()) }
    factory { GetWarehouseStockUseCase(get()) }
    factory { GetConsolidatedStockUseCase(get()) }
    factory { GetLowStockUseCase(get()) }
    factory { GetFastMovingUseCase(get()) }
    factory { RecordPaymentUseCase(get()) }
    factory { GetPaymentsPendingUseCase(get()) }
    factory { GetAccountsSummaryUseCase(get()) }
    factory { GetSalesOrdersUseCase(get()) }
    factory { GetSalesOrderDetailUseCase(get()) }
    factory { GetSalesInvoicesUseCase(get()) }
    factory { GetSalesInvoiceDetailUseCase(get()) }

    factory { GetReceivablesAgingUseCase(get()) }
    factory { GetPurchaseSummaryUseCase(get()) }
    factory { GetCashPositionUseCase(get()) }
    factory { GetProformaInvoicesUseCase(get()) }
    factory { GetProformaInvoiceDetailUseCase(get()) }
    factory { CreateProformaInvoiceUseCase(get()) }
    factory { GetProductSkusUseCase(get()) }
    factory { UpdateProformaInvoiceUseCase(get()) }
    factory { SendProformaInvoiceUseCase(get()) }
    factory { CancelProformaInvoiceUseCase(get()) }

    factory { GetNotificationsPagedUseCase(get()) }
    factory { GetUnreadNotificationCountUseCase(get()) }
    factory { MarkNotificationAsReadUseCase(get()) }
    factory { MarkAllNotificationsReadUseCase(get()) }
}
