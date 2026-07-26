package com.havos.lubricerp.feature_reports.presentation.di

import com.havos.lubricerp.feature_reports.presentation.home.HomeTabViewModel
import com.havos.lubricerp.feature_reports.presentation.home.HomeViewModel
import com.havos.lubricerp.feature_reports.presentation.home.ReportsTabViewModel
import com.havos.lubricerp.feature_reports.presentation.login.LoginViewModel
import com.havos.lubricerp.feature_reports.presentation.navigation.RootViewModel
import com.havos.lubricerp.feature_reports.presentation.reports.ReportDetailViewModel
import com.havos.lubricerp.feature_reports.presentation.customer.CustomerDataViewModel
import com.havos.lubricerp.feature_reports.presentation.notification.NotificationViewModel
import com.havos.lubricerp.feature_reports.presentation.reportmodule.ReportModuleViewModel
import com.havos.lubricerp.feature_reports.presentation.settings.SettingsViewModel
import com.havos.lubricerp.feature_reports.presentation.payment.PaymentReportViewModel
import com.havos.lubricerp.feature_reports.presentation.orders.OrdersViewModel
import com.havos.lubricerp.feature_reports.presentation.products.ProductsViewModel
import com.havos.lubricerp.feature_reports.presentation.products.CostBreakdownDetailViewModel
import com.havos.lubricerp.feature_reports.presentation.proforma.ProformaInvoiceViewModel
import com.havos.lubricerp.feature_reports.presentation.proforma.ProformaInvoiceDetailViewModel
import com.havos.lubricerp.feature_reports.presentation.proforma.CreateProformaInvoiceViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val featureReportsPresentationModule = module {
    viewModel { RootViewModel(get(), get()) }
    viewModel { LoginViewModel(get(), get(), get(), get()) }
    viewModel { HomeViewModel(get(), get(), get(), get()) }
    viewModel { HomeTabViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { ReportsTabViewModel(get(), get()) }
    viewModel { ReportDetailViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { SettingsViewModel(get(), get(), get(), get(), get()) }
    viewModel { CustomerDataViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { ReportModuleViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { NotificationViewModel(get(), get(), get(), get()) }
    viewModel { PaymentReportViewModel(get(), get(), get(), get(), get()) }
    viewModel { OrdersViewModel(get(), get(), get(), get(), get()) }
    viewModel { ProductsViewModel(get(), get()) }
    viewModel { CostBreakdownDetailViewModel(get(), get()) }
    viewModel { ProformaInvoiceViewModel(get(), get(), get()) }
    viewModel { ProformaInvoiceDetailViewModel(get(), get(), get(), get()) }
    viewModel { CreateProformaInvoiceViewModel(get(), get(), get(), get(), get(), get()) }
}
