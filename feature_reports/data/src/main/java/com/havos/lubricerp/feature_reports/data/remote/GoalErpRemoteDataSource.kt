package com.havos.lubricerp.feature_reports.data.remote

import com.havos.lubricerp.core.common.ResultState
import com.havos.lubricerp.feature_reports.data.dto.LoginRequestDto
import com.havos.lubricerp.feature_reports.data.dto.LoginResponseDto
import com.havos.lubricerp.feature_reports.data.dto.PackagingLossGainReportDto
import com.havos.lubricerp.feature_reports.data.dto.ProfileDataDto
import com.havos.lubricerp.feature_reports.data.dto.RawMaterialStockItemDto
import com.havos.lubricerp.feature_reports.data.dto.TankStockSummaryDto

interface GoalErpRemoteDataSource {
    suspend fun login(request: LoginRequestDto): ResultState<LoginResponseDto>
    suspend fun logout(token: String): ResultState<Unit>
    suspend fun getProfile(token: String): ResultState<ProfileDataDto>

    suspend fun getTankStockSummary(): ResultState<TankStockSummaryDto>

    suspend fun getRawMaterialStock(): ResultState<List<RawMaterialStockItemDto>>

    suspend fun getPackagingLossGain(fromDate: String, toDate: String): ResultState<PackagingLossGainReportDto>
}
