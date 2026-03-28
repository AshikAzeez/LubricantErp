package com.havos.lubricerp.feature_reports.domain.repository

import com.havos.lubricerp.core.common.ResultState
import com.havos.lubricerp.feature_reports.domain.model.DateRangeFilter
import com.havos.lubricerp.feature_reports.domain.model.PackagingLossGainReport
import com.havos.lubricerp.feature_reports.domain.model.RawMaterialStockItem
import com.havos.lubricerp.feature_reports.domain.model.TankStockSummary

interface ReportsRepository {
    suspend fun getTankStockSummary(): ResultState<TankStockSummary>

    suspend fun getRawMaterialStock(): ResultState<List<RawMaterialStockItem>>

    suspend fun getPackagingLossGain(filter: DateRangeFilter): ResultState<PackagingLossGainReport>
}
