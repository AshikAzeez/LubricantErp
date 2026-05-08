package com.havos.lubricerp.feature_reports.data.repository

import com.havos.lubricerp.core.common.ResultState
import com.havos.lubricerp.core.database.SecureProfileStore
import com.havos.lubricerp.core.database.SecureSessionStore
import com.havos.lubricerp.core.database.SessionData
import com.havos.lubricerp.core.network.SessionTokenProvider
import com.havos.lubricerp.feature_reports.data.remote.auth.AuthRemoteDataSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SessionTokenProviderImpl(
    private val secureSessionStore: SecureSessionStore,
    private val secureProfileStore: SecureProfileStore,
    private val authRemoteDataSource: AuthRemoteDataSource
) : SessionTokenProvider {

    private val refreshMutex = Mutex()

    override suspend fun getAccessToken(): String? {
        return secureSessionStore.sessionFlow.first()?.token?.takeIf { it.isNotBlank() }
    }

    override suspend fun getRefreshToken(): String? {
        return secureSessionStore.sessionFlow.first()?.refreshToken?.takeIf { it.isNotBlank() }
    }

    override suspend fun refreshAndSave(): String? = refreshMutex.withLock {
        val session = secureSessionStore.sessionFlow.first() ?: return@withLock null
        val refreshToken = session.refreshToken.takeIf { it.isNotBlank() } ?: return@withLock null

        when (val result = authRemoteDataSource.refreshToken(refreshToken)) {
            is ResultState.Success -> {
                val newToken = result.data.token
                val newRefresh = result.data.refreshToken.ifBlank { refreshToken }
                secureSessionStore.saveSession(
                    SessionData(
                        username = session.username,
                        token = newToken,
                        refreshToken = newRefresh
                    )
                )
                newToken
            }
            else -> null
        }
    }

    override suspend fun clearSession() {
        secureProfileStore.clearProfile()
        secureSessionStore.clearSession()
    }
}
