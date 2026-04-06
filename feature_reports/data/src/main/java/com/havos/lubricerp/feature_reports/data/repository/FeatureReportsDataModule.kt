package com.havos.lubricerp.feature_reports.data.repository

import com.havos.lubricerp.feature_reports.data.remote.GoalErpRemoteApi
import com.havos.lubricerp.feature_reports.data.remote.GoalErpRemoteDataSource
import com.havos.lubricerp.feature_reports.domain.repository.AuthRepository
import com.havos.lubricerp.feature_reports.domain.repository.ReportsRepository
import org.koin.dsl.module

val featureReportsDataModule = module {
    single<GoalErpRemoteDataSource> { GoalErpRemoteApi(get(), get()) }
    single<AuthRepository> { AuthRepositoryImpl(get(), get(), get()) }
    single<ReportsRepository> { ReportsRepositoryImpl(get()) }
}
