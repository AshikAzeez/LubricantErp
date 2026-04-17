package com.havos.lubricerp.feature_reports.data.repository

import com.havos.lubricerp.feature_reports.data.remote.auth.AuthRemoteApi
import com.havos.lubricerp.feature_reports.data.remote.auth.AuthRemoteDataSource
import com.havos.lubricerp.feature_reports.data.remote.reports.ReportsRemoteApi
import com.havos.lubricerp.feature_reports.data.remote.reports.ReportsRemoteDataSource
import com.havos.lubricerp.feature_reports.domain.repository.AuthRepository
import com.havos.lubricerp.feature_reports.domain.repository.ReportsRepository
import org.koin.dsl.module

val featureReportsDataModule = module {
    single<AuthRemoteDataSource> { AuthRemoteApi(get(), get()) }
    single<ReportsRemoteDataSource> { ReportsRemoteApi(get()) }
    single<AuthRepository> { AuthRepositoryImpl(get(), get(), get()) }
    single<ReportsRepository> { ReportsRepositoryImpl(get()) }
}
