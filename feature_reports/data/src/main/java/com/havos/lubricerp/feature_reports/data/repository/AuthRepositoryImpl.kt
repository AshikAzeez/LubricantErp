package com.havos.lubricerp.feature_reports.data.repository

import com.havos.lubricerp.core.common.ResultState
import com.havos.lubricerp.core.database.ProfileData
import com.havos.lubricerp.core.database.SecureProfileStore
import com.havos.lubricerp.core.database.SecureSessionStore
import com.havos.lubricerp.core.database.SessionData
import com.havos.lubricerp.feature_reports.data.dto.LoginRequestDto
import com.havos.lubricerp.feature_reports.data.mapper.toDomain
import com.havos.lubricerp.feature_reports.data.remote.auth.AuthRemoteDataSource
import com.havos.lubricerp.feature_reports.domain.model.AuthSession
import com.havos.lubricerp.feature_reports.domain.model.UserProfile
import com.havos.lubricerp.feature_reports.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class AuthRepositoryImpl(
    private val authRemoteDataSource: AuthRemoteDataSource,
    private val secureSessionStore: SecureSessionStore,
    private val secureProfileStore: SecureProfileStore
) : AuthRepository {

    override fun observeSession(): Flow<AuthSession?> {
        return secureSessionStore.sessionFlow.map { session ->
            session?.let { AuthSession(username = it.username, token = it.token) }
        }
    }

    override fun observeRememberedUsername(): Flow<String> = secureSessionStore.rememberedUsernameFlow

    override fun observeRememberMeEnabled(): Flow<Boolean> = secureSessionStore.rememberMeEnabledFlow

    override fun observeProfile(): Flow<UserProfile?> {
        return secureProfileStore.profileFlow.map { it?.toDomain() }
    }

    override suspend fun login(
        username: String,
        password: String,
        rememberMe: Boolean
    ): ResultState<AuthSession> {
        return withContext(Dispatchers.IO) {
            when (val result = authRemoteDataSource.login(LoginRequestDto(email = username, password = password))) {
                is ResultState.Success -> {
                    secureSessionStore.saveSession(
                        SessionData(
                            username = result.data.username,
                            token = result.data.token
                        )
                    )
                    secureProfileStore.clearProfile()
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
    }

    override suspend fun ensureProfileLoaded(forceRefresh: Boolean): ResultState<UserProfile> {
        return withContext(Dispatchers.IO) {
            if (!forceRefresh) {
                secureProfileStore.getProfile()?.let { cached ->
                    return@withContext ResultState.Success(cached.toDomain())
                }
            }

            val token = secureSessionStore.sessionFlow.first()?.token.orEmpty()
            if (token.isBlank()) {
                return@withContext ResultState.Error("Session not available")
            }

            when (val result = authRemoteDataSource.getProfile(token)) {
                is ResultState.Success -> {
                    val profile = result.data.toDomain()
                    secureProfileStore.saveProfile(
                        ProfileData(
                            id = profile.id,
                            email = profile.email,
                            fullName = profile.fullName,
                            branchId = profile.branchId,
                            roles = profile.roles
                        )
                    )
                    ResultState.Success(profile)
                }

                is ResultState.Error -> result
                ResultState.Loading -> ResultState.Loading
            }
        }
    }

    override suspend fun logout() {
        withContext(Dispatchers.IO) {
            authRemoteDataSource.logout()
            secureProfileStore.clearProfile()
            secureSessionStore.clearSession()
        }
    }
}

private fun ProfileData.toDomain(): UserProfile = UserProfile(
    id = id,
    email = email,
    fullName = fullName,
    branchId = branchId,
    roles = roles
)
