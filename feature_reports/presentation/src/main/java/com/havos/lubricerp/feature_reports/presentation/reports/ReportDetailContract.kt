package com.havos.lubricerp.feature_reports.presentation.reports

import com.havos.lubricerp.core.common.UiIntent
import androidx.compose.runtime.Stable
import com.havos.lubricerp.core.common.UiState
import com.havos.lubricerp.feature_reports.domain.model.PackagingLossGainReport
import com.havos.lubricerp.feature_reports.domain.model.PaymentReceivedItem
import com.havos.lubricerp.feature_reports.domain.model.RawMaterialStockItem
import com.havos.lubricerp.feature_reports.domain.model.SalesSummaryItem
import com.havos.lubricerp.feature_reports.domain.model.ConsolidatedStockItem
import com.havos.lubricerp.feature_reports.domain.model.FastMovingItem
import com.havos.lubricerp.feature_reports.domain.model.LowStockItem
import com.havos.lubricerp.feature_reports.domain.model.StockOverviewTankItem
import com.havos.lubricerp.feature_reports.domain.model.TankStockItem
import com.havos.lubricerp.feature_reports.domain.model.WarehouseStockItem

sealed interface ReportDetailIntent : UiIntent {
    data class Load(val reportKey: String) : ReportDetailIntent
    data class FromDateChanged(val date: String) : ReportDetailIntent
    data class ToDateChanged(val date: String) : ReportDetailIntent
    data class SearchChanged(val query: String) : ReportDetailIntent
    data class DaysThresholdChanged(val days: String) : ReportDetailIntent
    data class GroupByChanged(val group: String) : ReportDetailIntent
    data object ApplyFilter : ReportDetailIntent
    data object ResetFilter : ReportDetailIntent
    data object ToggleTopCustomers : ReportDetailIntent
    data object Refresh : ReportDetailIntent
    data class TabSelected(val index: Int) : ReportDetailIntent
    data class LowStockThresholdChanged(val value: Int) : ReportDetailIntent
    data class FastMovingDaysChanged(val value: Int) : ReportDetailIntent
    data class FastMovingTopChanged(val value: Int) : ReportDetailIntent
}

@Stable
data class ReportDetailUiState(
    val selectedReport: ReportItem = ReportItem.TANK_STOCK_SUMMARY,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isOffline: Boolean = false,
    val retryPending: Boolean = false,
    val errorMessage: String? = null,
    val fromDate: String = "",
    val toDate: String = "",
    val searchQuery: String = "",
    val daysThreshold: String = "30 days",
    val groupBy: String = "Daily",
    val tankStockItems: List<TankStockItem> = emptyList(),
    val rawMaterialItems: List<RawMaterialStockItem> = emptyList(),
    val packagingLossGainReport: PackagingLossGainReport? = null,
    val salesSummaryItems: List<SalesSummaryItem> = emptyList(),
    val monthlySalesCount: Int = 0,
    val showTopCustomers: Boolean = false,
    val paymentReceivedItems: List<PaymentReceivedItem> = emptyList(),
    val stockOverviewTankItems: List<StockOverviewTankItem> = emptyList(),
    val warehouseStockItems: List<WarehouseStockItem> = emptyList(),
    val consolidatedStockItems: List<ConsolidatedStockItem> = emptyList(),
    val lowStockItems: List<LowStockItem> = emptyList(),
    val fastMovingItems: List<FastMovingItem> = emptyList(),
    val lowStockThreshold: Int = 10,
    val fastMovingDays: Int = 30,
    val fastMovingTop: Int = 10,
    val selectedConsolidatedTab: Int = 0,
    val dateError: String? = null
) : UiState

sealed interface ReportDetailAction {
    data class FromDateChanged(val value: String) : ReportDetailAction
    data class ToDateChanged(val value: String) : ReportDetailAction
    data class SearchChanged(val value: String) : ReportDetailAction
    data class DaysThresholdChanged(val value: String) : ReportDetailAction
    data class GroupByChanged(val value: String) : ReportDetailAction
    data object ApplyFilter : ReportDetailAction
    data object ResetFilter : ReportDetailAction
    data object ToggleTopCustomers : ReportDetailAction
    data object Retry : ReportDetailAction
    data class TabSelected(val index: Int) : ReportDetailAction
    data class LowStockThresholdChanged(val value: Int) : ReportDetailAction
    data class FastMovingDaysChanged(val value: Int) : ReportDetailAction
    data class FastMovingTopChanged(val value: Int) : ReportDetailAction
}
