package com.havos.lubricerp.feature_reports.presentation.reportmodule

import androidx.compose.runtime.Stable
import com.havos.lubricerp.core.common.UiState
import com.havos.lubricerp.feature_reports.domain.model.ExpenseSummaryItem
import com.havos.lubricerp.feature_reports.domain.model.NetProfitReport
import com.havos.lubricerp.feature_reports.domain.model.ProductSalesItem
import com.havos.lubricerp.feature_reports.domain.model.ReportSalesSummaryItem

@Stable
data class ReportModuleUiState(
    val fromDate: String = "",
    val toDate: String = "",
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isOffline: Boolean = false,
    val retryPending: Boolean = false,
    val error: String? = null,
    val dateError: String? = null,
    val salesSummaryItems: List<ReportSalesSummaryItem> = emptyList(),
    val productSalesItems: List<ProductSalesItem> = emptyList(),
    val netProfit: NetProfitReport? = null,
    val canViewNetProfit: Boolean = true,
    val expenseSummaryItems: List<ExpenseSummaryItem> = emptyList()
) : UiState

sealed interface ReportModuleAction {
    data class FromDateChanged(val value: String) : ReportModuleAction
    data class ToDateChanged(val value: String) : ReportModuleAction
    data object ApplyFilter : ReportModuleAction
    data object ResetFilter : ReportModuleAction
    data object Refresh : ReportModuleAction
}
