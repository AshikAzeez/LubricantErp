package com.havos.lubricerp.feature_reports.domain.repository

import com.havos.lubricerp.core.common.ResultState
import com.havos.lubricerp.feature_reports.domain.model.AuthSession
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observeSession(): Flow<AuthSession?>
    fun observeRememberedUsername(): Flow<String>
    fun observeRememberMeEnabled(): Flow<Boolean>

    suspend fun login(username: String, password: String, rememberMe: Boolean): ResultState<AuthSession>

    suspend fun logout()
}
