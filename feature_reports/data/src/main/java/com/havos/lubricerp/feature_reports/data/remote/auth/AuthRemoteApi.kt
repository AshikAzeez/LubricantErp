package com.havos.lubricerp.feature_reports.data.remote.auth

import com.havos.lubricerp.core.common.ResultState
import com.havos.lubricerp.core.network.safeApiCall
import com.havos.lubricerp.feature_reports.data.dto.LoginApiResponseDto
import com.havos.lubricerp.feature_reports.data.dto.LoginRequestDto
import com.havos.lubricerp.feature_reports.data.dto.LoginResponseDto
import com.havos.lubricerp.feature_reports.data.dto.LogoutResponseDto
import com.havos.lubricerp.feature_reports.data.dto.ProfileApiResponseDto
import com.havos.lubricerp.feature_reports.data.dto.ProfileDataDto
import com.havos.lubricerp.feature_reports.data.dto.RefreshTokenApiResponseDto
import com.havos.lubricerp.feature_reports.data.dto.RefreshTokenRequestDto
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.get
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.utils.io.errors.IOException

class AuthRemoteApi(
    private val client: HttpClient
) : AuthRemoteDataSource {

    override suspend fun login(request: LoginRequestDto): ResultState<LoginResponseDto> {
        if (request.email.isBlank() || request.password.isBlank()) {
            return ResultState.Error("Email and password are required")
        }

        return when (
            val result = safeApiCall<LoginApiResponseDto> {
                client.post("api/auth/login") {
                    headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    headers.append(HttpHeaders.Accept, ContentType.Application.Json.toString())
                    setBody(request)
                }
            }
        ) {
            is ResultState.Success -> {
                val payload = result.data
                val data = payload.data
                val token = data?.token.orEmpty()
                if (!payload.success || token.isBlank()) {
                    val serverMessage = payload.message?.takeIf { it.isNotBlank() }
                        ?: payload.errors?.firstOrNull()?.takeIf { it.isNotBlank() }
                        ?: "Login failed"
                    ResultState.Error(serverMessage)
                } else {
                    val displayName = data?.user?.fullName?.takeIf { it.isNotBlank() }
                        ?: data?.user?.email?.takeIf { it.isNotBlank() }
                        ?: request.email
                    ResultState.Success(
                        LoginResponseDto(
                            username = displayName,
                            token = token,
                            refreshToken = data?.refreshToken.orEmpty(),
                            expiry = data?.expiry
                        )
                    )
                }
            }

            is ResultState.Error -> ResultState.Error(resolveLoginError(result))
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun logout(token: String): ResultState<Unit> {
        return when (
            val result = safeApiCall<LogoutResponseDto> {
                client.post("api/auth/logout") {
                    headers.append(HttpHeaders.Authorization, "Bearer $token")
                    headers.append(HttpHeaders.Accept, ContentType.Application.Json.toString())
                }
            }
        ) {
            is ResultState.Success -> ResultState.Success(Unit)
            is ResultState.Error -> ResultState.Error("Unable to logout from server.")
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun refreshToken(refreshToken: String): ResultState<LoginResponseDto> {
        if (refreshToken.isBlank()) return ResultState.Error("Refresh token is missing.")

        return when (
            val result = safeApiCall<RefreshTokenApiResponseDto> {
                client.post("api/auth/refresh") {
                    headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    headers.append(HttpHeaders.Accept, ContentType.Application.Json.toString())
                    setBody(RefreshTokenRequestDto(refreshToken = refreshToken))
                }
            }
        ) {
            is ResultState.Success -> {
                val payload = result.data
                val data = payload.data
                val newToken = data?.token.orEmpty()
                if (!payload.success || newToken.isBlank()) {
                    val serverMessage = payload.message?.takeIf { it.isNotBlank() }
                        ?: payload.errors?.firstOrNull()?.takeIf { it.isNotBlank() }
                        ?: "Token refresh failed"
                    ResultState.Error(serverMessage)
                } else {
                    ResultState.Success(
                        LoginResponseDto(
                            username = "",
                            token = newToken,
                            refreshToken = data?.refreshToken.orEmpty(),
                            expiry = data?.expiry
                        )
                    )
                }
            }
            is ResultState.Error -> ResultState.Error("Token refresh failed: ${result.message}")
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun getProfile(token: String): ResultState<ProfileDataDto> {
        if (token.isBlank()) return ResultState.Error("Authentication token is missing.")

        return when (
            val result = safeApiCall<ProfileApiResponseDto> {
                client.get("api/auth/profile") {
                    headers.append(HttpHeaders.Authorization, "Bearer $token")
                    headers.append(HttpHeaders.Accept, ContentType.Application.Json.toString())
                }
            }
        ) {
            is ResultState.Success -> {
                val payload = result.data
                val data = payload.data
                if (!payload.success || data == null) {
                    val serverMessage = payload.message?.takeIf { it.isNotBlank() }
                        ?: payload.errors?.firstOrNull()?.takeIf { it.isNotBlank() }
                        ?: "Unable to fetch profile"
                    ResultState.Error(serverMessage)
                } else {
                    ResultState.Success(data)
                }
            }

            is ResultState.Error -> ResultState.Error(resolveProfileError(result))
            ResultState.Loading -> ResultState.Loading
        }
    }
}

private fun resolveLoginError(error: ResultState.Error): String {
    val message = error.message.lowercase()
    val throwable = error.cause

    return when {
        "429" in message || "too many" in message || "rate" in message -> {
            "Too many attempts. Please try again later."
        }

        "401" in message || "400" in message || "invalid credential" in message -> {
            "Invalid credentials."
        }

        "locked" in message || "disabled" in message || "inactive" in message -> {
            "Your account is locked or disabled. Please contact administrator."
        }

        "expired password" in message || ("password" in message && "expired" in message) -> {
            "Your password has expired. Please reset your password."
        }

        "timeout" in message -> {
            "Server timeout. Please try again."
        }

        throwable is IOException || "unable to resolve host" in message || "network is unreachable" in message -> {
            "No internet connection. Check network and try again."
        }

        "500" in message || "502" in message || "503" in message || "504" in message -> {
            "Server error. Please try again later."
        }

        else -> "Unable to login. Please try again."
    }
}

private fun resolveProfileError(error: ResultState.Error): String {
    val message = error.message.lowercase()
    val throwable = error.cause

    return when {
        "401" in message || "403" in message -> "Session expired. Please login again."
        "timeout" in message -> "Profile request timed out. Please try again."
        throwable is IOException || "unable to resolve host" in message || "network is unreachable" in message -> {
            "No internet connection. Check network and try again."
        }
        "500" in message || "502" in message || "503" in message || "504" in message -> {
            "Server error while loading profile."
        }
        else -> "Unable to load profile."
    }
}
