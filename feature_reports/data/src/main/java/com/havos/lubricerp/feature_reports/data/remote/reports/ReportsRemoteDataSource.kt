package com.havos.lubricerp.feature_reports.data.remote.reports

import com.havos.lubricerp.core.common.ResultState
import com.havos.lubricerp.feature_reports.data.dto.PackagingLossGainReportDto
import com.havos.lubricerp.feature_reports.data.dto.RawMaterialStockItemDto
import com.havos.lubricerp.feature_reports.data.dto.TankStockSummaryDto

interface ReportsRemoteDataSource {
    suspend fun getTankStockSummary(): ResultState<TankStockSummaryDto>
    suspend fun getRawMaterialStock(): ResultState<List<RawMaterialStockItemDto>>
    suspend fun getPackagingLossGain(fromDate: String, toDate: String): ResultState<PackagingLossGainReportDto>
}
