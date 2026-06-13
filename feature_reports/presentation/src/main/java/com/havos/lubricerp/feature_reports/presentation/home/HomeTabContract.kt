package com.havos.lubricerp.feature_reports.presentation.home

import com.havos.lubricerp.core.common.UiIntent
import androidx.compose.runtime.Stable
import com.havos.lubricerp.core.common.UiState
import com.havos.lubricerp.feature_reports.domain.model.RecentInvoice

enum class NetProfitPeriod(val label: String) {
    THIS_MONTH("This Month"),
    LAST_MONTH("Last Month"),
    THIS_YEAR("This Year"),
    CUSTOM("Custom")
}

sealed interface HomeTabIntent : UiIntent {
    data object LoadDashboard : HomeTabIntent
    data object Refresh : HomeTabIntent
    data class NetProfitPeriodChanged(val period: NetProfitPeriod) : HomeTabIntent
    data class NetProfitCustomDateChanged(val fromDate: String, val toDate: String) : HomeTabIntent
    data object NetProfitCustomApply : HomeTabIntent
}

@Stable
data class HomeTabUiState(
    val greetingName: String = "",
    val userRoles: List<String> = emptyList(),
    val isProfileLoading: Boolean = false,
    val isDashboardLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isOffline: Boolean = false,
    val retryPending: Boolean = false,
    val dashboardError: String? = null,
    val todaySalesAmount: Double = 0.0,
    val todaySalesCount: Int = 0,
    val monthlySalesAmount: Double = 0.0,
    val monthlySalesCount: Int = 0,
    val canViewFinancials: Boolean = true,
    val outstandingReceivables: Double = 0.0,
    val pendingPayables: Double = 0.0,
    val netProfit: Double? = null,
    val isNetProfitLoading: Boolean = false,
    val netProfitPeriod: NetProfitPeriod = NetProfitPeriod.THIS_MONTH,
    val netProfitCustomFrom: String = "",
    val netProfitCustomTo: String = "",
    val lowStockAlertCount: Int = 0,
    val topSellingProducts: List<String> = emptyList(),
    val recentInvoices: List<RecentInvoice> = emptyList()
) : UiState

sealed interface HomeTabEffect
