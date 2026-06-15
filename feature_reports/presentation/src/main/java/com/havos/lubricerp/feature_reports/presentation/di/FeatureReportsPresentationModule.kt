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
}
