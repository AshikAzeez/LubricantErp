package com.havos.lubricerp.feature_reports.domain.usecase

import com.havos.lubricerp.core.common.ResultState
import com.havos.lubricerp.feature_reports.domain.model.DateRangeFilter
import com.havos.lubricerp.feature_reports.domain.model.PackagingLossGainReport
import com.havos.lubricerp.feature_reports.domain.model.RawMaterialStockItem
import com.havos.lubricerp.feature_reports.domain.model.TankStockSummary
import com.havos.lubricerp.feature_reports.domain.repository.ReportsRepository

class GetTankStockSummaryUseCase(
    private val repository: ReportsRepository
) {
    suspend operator fun invoke(): ResultState<TankStockSummary> = repository.getTankStockSummary()
}

class GetRawMaterialStockUseCase(
    private val repository: ReportsRepository
) {
    suspend operator fun invoke(): ResultState<List<RawMaterialStockItem>> = repository.getRawMaterialStock()
}

class GetPackagingLossGainUseCase(
    private val repository: ReportsRepository
) {
    suspend operator fun invoke(filter: DateRangeFilter): ResultState<PackagingLossGainReport> {
        return repository.getPackagingLossGain(filter)
    }
}
