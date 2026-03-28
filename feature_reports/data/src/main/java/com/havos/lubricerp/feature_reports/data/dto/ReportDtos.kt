package com.havos.lubricerp.feature_reports.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class TankStockSummaryDto(
    val totalCapacityLiters: Int,
    val currentStockLiters: Int,
    val availableCapacityLiters: Int,
    val tanks: List<TankInfoDto>
)

@Serializable
data class TankInfoDto(
    val name: String,
    val code: String,
    val location: String,
    val productGrade: String,
    val capacityLiters: Int,
    val currentStockLiters: Int,
    val availableLiters: Int,
    val fillPercent: Int
)

@Serializable
data class RawMaterialStockItemDto(
    val code: String,
    val name: String,
    val type: String,
    val uom: String,
    val costPerUnit: Double,
    val reorderLevel: Double
)

@Serializable
data class PackagingLossGainReportDto(
    val totalPlannedLiters: Double,
    val totalActualLiters: Double,
    val totalVarianceLiters: Double,
    val rows: List<PackagingLossGainRowDto>
)

@Serializable
data class PackagingLossGainRowDto(
    val orderNo: String,
    val date: String,
    val productGrade: String,
    val sourceTank: String,
    val plannedLiters: Double,
    val actualLiters: Double,
    val varianceLiters: Double,
    val variancePercent: Double,
    val status: String
)
