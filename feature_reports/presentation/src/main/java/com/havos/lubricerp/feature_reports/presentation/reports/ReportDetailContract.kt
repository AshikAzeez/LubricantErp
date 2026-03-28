package com.havos.lubricerp.feature_reports.presentation.reports

import com.havos.lubricerp.core.common.UiIntent
import com.havos.lubricerp.core.common.UiState
import com.havos.lubricerp.feature_reports.domain.model.PackagingLossGainReport
import com.havos.lubricerp.feature_reports.domain.model.RawMaterialStockItem
import com.havos.lubricerp.feature_reports.domain.model.TankStockSummary

sealed interface ReportDetailIntent : UiIntent {
    data class Load(val reportKey: String) : ReportDetailIntent
    data class FromDateChanged(val date: String) : ReportDetailIntent
    data class ToDateChanged(val date: String) : ReportDetailIntent
    data class SearchChanged(val query: String) : ReportDetailIntent
    data class DaysThresholdChanged(val days: String) : ReportDetailIntent
    data class GroupByChanged(val group: String) : ReportDetailIntent
    data object ApplyFilter : ReportDetailIntent
    data object ResetFilter : ReportDetailIntent
}

data class ReportDetailUiState(
    val selectedReport: ReportItem = ReportItem.TANK_STOCK_SUMMARY,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val fromDate: String = "28/03/2026",
    val toDate: String = "28/03/2026",
    val searchQuery: String = "",
    val daysThreshold: String = "30 days",
    val groupBy: String = "Daily",
    val tankStockSummary: TankStockSummary? = null,
    val rawMaterialItems: List<RawMaterialStockItem> = emptyList(),
    val packagingLossGainReport: PackagingLossGainReport? = null
) : UiState

sealed interface ReportDetailAction {
    data class FromDateChanged(val value: String) : ReportDetailAction
    data class ToDateChanged(val value: String) : ReportDetailAction
    data class SearchChanged(val value: String) : ReportDetailAction
    data class DaysThresholdChanged(val value: String) : ReportDetailAction
    data class GroupByChanged(val value: String) : ReportDetailAction
    data object ApplyFilter : ReportDetailAction
    data object ResetFilter : ReportDetailAction
}
