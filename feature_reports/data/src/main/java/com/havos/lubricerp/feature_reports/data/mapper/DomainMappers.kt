package com.havos.lubricerp.feature_reports.data.mapper

import com.havos.lubricerp.feature_reports.data.dto.LoginResponseDto
import com.havos.lubricerp.feature_reports.data.dto.PackagingLossGainReportDto
import com.havos.lubricerp.feature_reports.data.dto.PackagingLossGainRowDto
import com.havos.lubricerp.feature_reports.data.dto.RawMaterialStockItemDto
import com.havos.lubricerp.feature_reports.data.dto.TankInfoDto
import com.havos.lubricerp.feature_reports.data.dto.TankStockSummaryDto
import com.havos.lubricerp.feature_reports.domain.model.AuthSession
import com.havos.lubricerp.feature_reports.domain.model.PackagingLossGainReport
import com.havos.lubricerp.feature_reports.domain.model.PackagingLossGainRow
import com.havos.lubricerp.feature_reports.domain.model.RawMaterialStockItem
import com.havos.lubricerp.feature_reports.domain.model.TankInfo
import com.havos.lubricerp.feature_reports.domain.model.TankStockSummary

fun LoginResponseDto.toDomain(): AuthSession = AuthSession(
    username = username,
    token = token
)

fun TankStockSummaryDto.toDomain(): TankStockSummary = TankStockSummary(
    totalCapacityLiters = totalCapacityLiters,
    currentStockLiters = currentStockLiters,
    availableCapacityLiters = availableCapacityLiters,
    tanks = tanks.map(TankInfoDto::toDomain)
)

fun TankInfoDto.toDomain(): TankInfo = TankInfo(
    name = name,
    code = code,
    location = location,
    productGrade = productGrade,
    capacityLiters = capacityLiters,
    currentStockLiters = currentStockLiters,
    availableLiters = availableLiters,
    fillPercent = fillPercent
)

fun RawMaterialStockItemDto.toDomain(): RawMaterialStockItem = RawMaterialStockItem(
    code = code,
    name = name,
    type = type,
    uom = uom,
    costPerUnit = costPerUnit,
    reorderLevel = reorderLevel
)

fun PackagingLossGainReportDto.toDomain(): PackagingLossGainReport = PackagingLossGainReport(
    totalPlannedLiters = totalPlannedLiters,
    totalActualLiters = totalActualLiters,
    totalVarianceLiters = totalVarianceLiters,
    rows = rows.map(PackagingLossGainRowDto::toDomain)
)

fun PackagingLossGainRowDto.toDomain(): PackagingLossGainRow = PackagingLossGainRow(
    orderNo = orderNo,
    date = date,
    productGrade = productGrade,
    sourceTank = sourceTank,
    plannedLiters = plannedLiters,
    actualLiters = actualLiters,
    varianceLiters = varianceLiters,
    variancePercent = variancePercent,
    status = status
)
