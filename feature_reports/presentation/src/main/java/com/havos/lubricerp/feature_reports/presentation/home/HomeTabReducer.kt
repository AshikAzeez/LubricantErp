package com.havos.lubricerp.feature_reports.presentation.home

import com.havos.lubricerp.feature_reports.domain.model.DashboardSummary

object HomeTabReducer {
    fun reduceForProfileLoading(state: HomeTabUiState, isLoading: Boolean): HomeTabUiState {
        return state.copy(isProfileLoading = isLoading)
    }

    fun reduceForUser(state: HomeTabUiState, name: String, roles: List<String> = emptyList()): HomeTabUiState {
        return state.copy(greetingName = name, userRoles = roles, isProfileLoading = false)
    }

    fun reduceForDashboardLoading(state: HomeTabUiState): HomeTabUiState {
        return state.copy(isDashboardLoading = true, dashboardError = null)
    }

    fun reduceForDashboardSuccess(state: HomeTabUiState, data: DashboardSummary): HomeTabUiState {
        return state.copy(
            isDashboardLoading = false,
            dashboardError = null,
            isOffline = false,
            retryPending = false,
            todaySalesAmount = data.todaySalesAmount,
            todaySalesCount = data.todaySalesCount,
            monthlySalesAmount = data.monthlySalesAmount,
            monthlySalesCount = data.monthlySalesCount,
            outstandingReceivables = data.outstandingReceivables,
            pendingPayables = data.pendingPayables,
            lowStockAlertCount = data.lowStockAlertCount,
            topSellingProducts = data.topSellingProducts,
            recentInvoices = data.recentInvoices
        )
    }

    fun reduceForNetProfit(state: HomeTabUiState, netProfit: Double): HomeTabUiState {
        return state.copy(netProfit = netProfit, isNetProfitLoading = false)
    }

    fun reduceForNetProfitLoading(state: HomeTabUiState): HomeTabUiState {
        return state.copy(isNetProfitLoading = true)
    }

    fun reduceForNetProfitPeriod(state: HomeTabUiState, period: NetProfitPeriod): HomeTabUiState {
        return state.copy(netProfitPeriod = period, isNetProfitLoading = true)
    }

    fun reduceForNetProfitCustomDate(state: HomeTabUiState, fromDate: String, toDate: String): HomeTabUiState {
        return state.copy(netProfitCustomFrom = fromDate, netProfitCustomTo = toDate)
    }

    fun reduceForDashboardError(state: HomeTabUiState, message: String, isOffline: Boolean = false): HomeTabUiState {
        return state.copy(
            isDashboardLoading = false,
            dashboardError = if (isOffline) null else message,
            isOffline = isOffline,
            retryPending = isOffline
        )
    }
}
