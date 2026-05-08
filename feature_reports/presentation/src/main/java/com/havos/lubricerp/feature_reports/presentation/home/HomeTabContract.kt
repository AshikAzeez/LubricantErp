package com.havos.lubricerp.feature_reports.presentation.home

import com.havos.lubricerp.core.common.UiIntent
import androidx.compose.runtime.Stable
import com.havos.lubricerp.core.common.UiState
import com.havos.lubricerp.feature_reports.domain.model.RecentInvoice

sealed interface HomeTabIntent : UiIntent {
    data object LoadDashboard : HomeTabIntent
    data object Refresh : HomeTabIntent
}

@Stable
data class HomeTabUiState(
    val greetingName: String = "",
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
    val outstandingReceivables: Double = 0.0,
    val pendingPayables: Double = 0.0,
    val lowStockAlertCount: Int = 0,
    val recentInvoices: List<RecentInvoice> = emptyList()
) : UiState

sealed interface HomeTabEffect
