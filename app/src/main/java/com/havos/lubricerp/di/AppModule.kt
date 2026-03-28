package com.havos.lubricerp.di

import com.havos.lubricerp.core.database.coreDatabaseModule
import com.havos.lubricerp.core.network.coreNetworkModule
import com.havos.lubricerp.feature_reports.data.repository.featureReportsDataModule
import com.havos.lubricerp.feature_reports.domain.usecase.featureReportsDomainModule
import com.havos.lubricerp.feature_reports.presentation.di.featureReportsPresentationModule
import org.koin.core.module.Module

val appModule: List<Module> = listOf(
    coreNetworkModule,
    coreDatabaseModule,
    featureReportsDataModule,
    featureReportsDomainModule,
    featureReportsPresentationModule
)
