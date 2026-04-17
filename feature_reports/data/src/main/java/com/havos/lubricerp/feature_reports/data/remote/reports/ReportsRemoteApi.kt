package com.havos.lubricerp.feature_reports.data.remote.reports

import com.havos.lubricerp.core.common.ResultState
import com.havos.lubricerp.core.network.safeApiCall
import com.havos.lubricerp.feature_reports.data.dto.PackagingLossGainReportDto
import com.havos.lubricerp.feature_reports.data.dto.RawMaterialStockItemDto
import com.havos.lubricerp.feature_reports.data.dto.TankStockSummaryDto
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class ReportsRemoteApi(
    private val client: HttpClient
) : ReportsRemoteDataSource {

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
