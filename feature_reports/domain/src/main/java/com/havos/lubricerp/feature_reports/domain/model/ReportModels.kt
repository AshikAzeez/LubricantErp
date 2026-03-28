package com.havos.lubricerp.feature_reports.domain.model

data class TankStockSummary(
    val totalCapacityLiters: Int,
    val currentStockLiters: Int,
    val availableCapacityLiters: Int,
    val tanks: List<TankInfo>
)

data class TankInfo(
    val name: String,
    val code: String,
    val location: String,
    val productGrade: String,
    val capacityLiters: Int,
    val currentStockLiters: Int,
    val availableLiters: Int,
    val fillPercent: Int
)

data class RawMaterialStockItem(
    val code: String,
    val name: String,
    val type: String,
    val uom: String,
    val costPerUnit: Double,
    val reorderLevel: Double
)

data class DateRangeFilter(
    val fromDate: String,
    val toDate: String
)

data class PackagingLossGainReport(
    val totalPlannedLiters: Double,
    val totalActualLiters: Double,
    val totalVarianceLiters: Double,
    val rows: List<PackagingLossGainRow>
)

data class PackagingLossGainRow(
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
