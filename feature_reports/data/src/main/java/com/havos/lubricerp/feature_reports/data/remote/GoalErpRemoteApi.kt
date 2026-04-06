package com.havos.lubricerp.feature_reports.data.remote

import com.havos.lubricerp.core.common.ResultState
import com.havos.lubricerp.core.network.AppEnvironment
import com.havos.lubricerp.core.network.ResolvedNetworkConfig
import com.havos.lubricerp.core.network.safeApiCall
import com.havos.lubricerp.feature_reports.data.dto.LoginApiResponseDto
import com.havos.lubricerp.feature_reports.data.dto.LoginRequestDto
import com.havos.lubricerp.feature_reports.data.dto.LoginResponseDto
import com.havos.lubricerp.feature_reports.data.dto.LogoutResponseDto
import com.havos.lubricerp.feature_reports.data.dto.PackagingLossGainReportDto
import com.havos.lubricerp.feature_reports.data.dto.RawMaterialStockItemDto
import com.havos.lubricerp.feature_reports.data.dto.TankStockSummaryDto
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.utils.io.errors.IOException

class GoalErpRemoteApi(
    private val client: HttpClient,
    private val networkConfig: ResolvedNetworkConfig
) : GoalErpRemoteDataSource {

    override suspend fun login(request: LoginRequestDto): ResultState<LoginResponseDto> {
        val effectiveRequest = resolveLoginRequest(request)
        if (effectiveRequest.email.isBlank() || effectiveRequest.password.isBlank()) {
            return ResultState.Error("Email and password are required")
        }

        return when (
            val result = safeApiCall<LoginApiResponseDto> {
                client.post("api/auth/login") {
                    headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    headers.append(HttpHeaders.Accept, ContentType.Application.Json.toString())
                    setBody(effectiveRequest)
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
                        ?: effectiveRequest.email
                    ResultState.Success(
                        LoginResponseDto(
                            username = displayName,
                            token = token,
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
            safeApiCall<LogoutResponseDto> {
                client.post("auth/logout") {
                    headers.append(HttpHeaders.Accept, ContentType.Application.Json.toString())
                }
            }
        ) {
            is ResultState.Success -> ResultState.Success(Unit)
            is ResultState.Error -> ResultState.Error("Unable to logout from server.")
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun getTankStockSummary(): ResultState<TankStockSummaryDto> {
        return when (
            val result = safeApiCall<TankStockSummaryDto> {
                client.get("reports/tank-stock-summary")
            }
        ) {
            is ResultState.Success -> ResultState.Success(result.data)
            is ResultState.Error -> ResultState.Error("Unable to fetch tank summary")
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun getRawMaterialStock(): ResultState<List<RawMaterialStockItemDto>> {
        return when (
            val result = safeApiCall<List<RawMaterialStockItemDto>> {
                client.get("reports/raw-material-stock")
            }
        ) {
            is ResultState.Success -> ResultState.Success(result.data)
            is ResultState.Error -> ResultState.Error("Unable to fetch raw material stock")
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun getPackagingLossGain(
        fromDate: String,
        toDate: String
    ): ResultState<PackagingLossGainReportDto> {
        return when (
            val result = safeApiCall<PackagingLossGainReportDto> {
                client.get("reports/packaging-loss-gain") {
                    parameter("fromDate", fromDate)
                    parameter("toDate", toDate)
                }
            }
        ) {
            is ResultState.Success -> ResultState.Success(result.data)
            is ResultState.Error -> ResultState.Error("Unable to fetch packaging loss/gain")
            ResultState.Loading -> ResultState.Loading
        }
    }

    private fun resolveLoginRequest(request: LoginRequestDto): LoginRequestDto {
        return when (networkConfig.environment) {
            AppEnvironment.TEST,
            AppEnvironment.STAGE -> LoginRequestDto(
                email = "admin@baseoils.com",
                password = "Admin@123"
            )

            AppEnvironment.PRODUCTION -> request
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
