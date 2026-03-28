package com.havos.lubricerp.feature_reports.presentation.di

import com.havos.lubricerp.feature_reports.presentation.home.HomeViewModel
import com.havos.lubricerp.feature_reports.presentation.login.LoginViewModel
import com.havos.lubricerp.feature_reports.presentation.navigation.RootViewModel
import com.havos.lubricerp.feature_reports.presentation.reports.ReportDetailViewModel
import com.havos.lubricerp.feature_reports.presentation.settings.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val featureReportsPresentationModule = module {
    viewModel { RootViewModel(get()) }
    viewModel { LoginViewModel(get(), get(), get()) }
    viewModel { HomeViewModel(get(), get()) }
    viewModel { ReportDetailViewModel(get(), get(), get()) }
    viewModel { SettingsViewModel(get()) }
}
