package com.havos.lubricerp.feature_reports.data.repository

import com.havos.lubricerp.core.database.BiometricAuthManager
import com.havos.lubricerp.core.network.SessionTokenProvider
import com.havos.lubricerp.feature_reports.data.remote.auth.AuthRemoteApi
import com.havos.lubricerp.feature_reports.data.remote.auth.AuthRemoteDataSource
import com.havos.lubricerp.feature_reports.data.remote.notifications.NotificationRemoteApi
import com.havos.lubricerp.feature_reports.data.remote.notifications.NotificationRemoteDataSource
import com.havos.lubricerp.feature_reports.data.remote.reports.ReportsRemoteApi
import com.havos.lubricerp.feature_reports.data.remote.reports.ReportsRemoteDataSource
import com.havos.lubricerp.feature_reports.domain.repository.AuthRepository
import com.havos.lubricerp.feature_reports.domain.repository.NotificationRepository
import com.havos.lubricerp.feature_reports.domain.repository.ReportsRepository
import org.koin.dsl.module

val featureReportsDataModule = module {
    single<AuthRemoteDataSource> { AuthRemoteApi(get(), get()) }
    single<ReportsRemoteDataSource> { ReportsRemoteApi(get()) }
    single<NotificationRemoteDataSource> { NotificationRemoteApi(get()) }
    single<AuthRepository> { AuthRepositoryImpl(get(), get(), get()) }
    single<ReportsRepository> { ReportsRepositoryImpl(get()) }
    single<NotificationRepository> { NotificationRepositoryImpl(get()) }
    single<SessionTokenProvider> { SessionTokenProviderImpl(get(), get(), get()) }
    single { BiometricAuthManager() }
}
