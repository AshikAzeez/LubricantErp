package com.havos.lubricerp.feature_reports.domain.usecase

import com.havos.lubricerp.core.common.ResultState
import com.havos.lubricerp.feature_reports.domain.model.AuthSession
import com.havos.lubricerp.feature_reports.domain.model.UserProfile
import com.havos.lubricerp.feature_reports.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

class ObserveSessionUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<AuthSession?> = authRepository.observeSession()
}

class LoginUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        username: String,
        password: String,
        rememberMe: Boolean
    ): ResultState<AuthSession> {
        return authRepository.login(username, password, rememberMe)
    }
}

class LogoutUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke() {
        authRepository.logout()
    }
}

class ObserveRememberedUsernameUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<String> = authRepository.observeRememberedUsername()
}

class ObserveRememberMeEnabledUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<Boolean> = authRepository.observeRememberMeEnabled()
}

class ObserveProfileUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<UserProfile?> = authRepository.observeProfile()
}

class EnsureProfileLoadedUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(forceRefresh: Boolean = false): ResultState<UserProfile> {
        return authRepository.ensureProfileLoaded(forceRefresh)
    }
}
