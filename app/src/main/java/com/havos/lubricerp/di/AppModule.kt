package com.havos.lubricerp.di

import com.havos.lubricerp.BuildConfig
import com.havos.lubricerp.core.database.coreDatabaseModule
import com.havos.lubricerp.core.network.AppEnvironment
import com.havos.lubricerp.core.network.ResolvedNetworkConfig
import com.havos.lubricerp.core.network.coreNetworkModule
import com.havos.lubricerp.feature_reports.data.repository.featureReportsDataModule
import com.havos.lubricerp.feature_reports.domain.usecase.featureReportsDomainModule
import com.havos.lubricerp.feature_reports.presentation.di.featureReportsPresentationModule
import org.koin.core.module.Module
import org.koin.dsl.module

val appModule: List<Module> = listOf(
    module {
        single<ResolvedNetworkConfig> {
            ResolvedNetworkConfig(
                environment = AppEnvironment.from(BuildConfig.ENVIRONMENT),
                useMockEngine = BuildConfig.USE_MOCK_ENGINE,
                baseUrl = BuildConfig.BASE_URL
            )
        }
    },
    coreNetworkModule,
    coreDatabaseModule,
    featureReportsDataModule,
    featureReportsDomainModule,
    featureReportsPresentationModule
)
