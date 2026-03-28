package com.havos.lubricerp.feature_reports.data.remote

import com.havos.lubricerp.core.common.ResultState
import com.havos.lubricerp.core.network.safeApiCall
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

class GoalErpRemoteApi(
    private val client: HttpClient
) : GoalErpRemoteDataSource {

    override suspend fun login(request: LoginRequestDto): ResultState<LoginResponseDto> {
        if (request.username.isBlank() || request.password.isBlank()) {
            return ResultState.Error("Username and password are required")
        }

        return when (
            val result = safeApiCall<LoginResponseDto> {
                client.post("auth/login") {
                    headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    headers.append(HttpHeaders.Accept, ContentType.Application.Json.toString())
                    setBody(request)
                }
            }
        ) {
            is ResultState.Success -> {
                ResultState.Success(
                    result.data.copy(username = request.username)
                )
            }

            is ResultState.Error -> ResultState.Error("Unable to login. ${result.message}")
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
}
