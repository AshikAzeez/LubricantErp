package com.havos.lubricerp.feature_reports.data.repository

import com.havos.lubricerp.core.common.ResultState
import com.havos.lubricerp.feature_reports.data.mapper.toDomain
import com.havos.lubricerp.feature_reports.data.remote.reports.ReportsRemoteDataSource
import com.havos.lubricerp.feature_reports.domain.model.DateRangeFilter
import com.havos.lubricerp.feature_reports.domain.model.PackagingLossGainReport
import com.havos.lubricerp.feature_reports.domain.model.RawMaterialStockItem
import com.havos.lubricerp.feature_reports.domain.model.TankStockSummary
import com.havos.lubricerp.feature_reports.domain.repository.ReportsRepository

class ReportsRepositoryImpl(
    private val reportsRemoteDataSource: ReportsRemoteDataSource
) : ReportsRepository {

    override suspend fun getTankStockSummary(): ResultState<TankStockSummary> {
        return when (val result = reportsRemoteDataSource.getTankStockSummary()) {
            is ResultState.Success -> ResultState.Success(result.data.toDomain())
            is ResultState.Error -> result
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun getRawMaterialStock(): ResultState<List<RawMaterialStockItem>> {
        return when (val result = reportsRemoteDataSource.getRawMaterialStock()) {
            is ResultState.Success -> ResultState.Success(result.data.map { it.toDomain() })
            is ResultState.Error -> result
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun getPackagingLossGain(filter: DateRangeFilter): ResultState<PackagingLossGainReport> {
        return when (val result = reportsRemoteDataSource.getPackagingLossGain(filter.fromDate, filter.toDate)) {
            is ResultState.Success -> ResultState.Success(result.data.toDomain())
            is ResultState.Error -> result
            ResultState.Loading -> ResultState.Loading
        }
    }
}
