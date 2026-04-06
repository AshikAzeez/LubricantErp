package com.havos.lubricerp.feature_reports.data.repository

import com.havos.lubricerp.core.common.ResultState
import com.havos.lubricerp.core.database.SecureSessionStore
import com.havos.lubricerp.core.database.SessionData
import com.havos.lubricerp.feature_reports.data.dto.LoginRequestDto
import com.havos.lubricerp.feature_reports.data.mapper.toDomain
import com.havos.lubricerp.feature_reports.data.remote.GoalErpRemoteDataSource
import com.havos.lubricerp.feature_reports.domain.model.AuthSession
import com.havos.lubricerp.feature_reports.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class AuthRepositoryImpl(
    private val remoteDataSource: GoalErpRemoteDataSource,
    private val secureSessionStore: SecureSessionStore
) : AuthRepository {

    override fun observeSession(): Flow<AuthSession?> {
        return secureSessionStore.sessionFlow.map { session ->
            session?.let { AuthSession(username = it.username, token = it.token) }
        }
    }

    override fun observeRememberedUsername(): Flow<String> = secureSessionStore.rememberedUsernameFlow

    override fun observeRememberMeEnabled(): Flow<Boolean> = secureSessionStore.rememberMeEnabledFlow

    override suspend fun login(
        username: String,
        password: String,
        rememberMe: Boolean
    ): ResultState<AuthSession> {
        return when (val result = remoteDataSource.login(LoginRequestDto(email = username, password = password))) {
            is ResultState.Success -> {
                secureSessionStore.saveSession(
                    SessionData(
                        username = result.data.username,
                        token = result.data.token
                    )
                )
                secureSessionStore.setRememberMeEnabled(rememberMe)
                if (rememberMe) {
                    secureSessionStore.saveRememberedUsername(username)
                } else {
                    secureSessionStore.clearRememberedUsername()
                }
                ResultState.Success(result.data.toDomain())
            }

            is ResultState.Error -> result
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun logout() {
        val token = secureSessionStore.sessionFlow.first()?.token.orEmpty()
        if (token.isNotBlank()) {
            remoteDataSource.logout(token)
        }
        secureSessionStore.clearSession()
    }
}
