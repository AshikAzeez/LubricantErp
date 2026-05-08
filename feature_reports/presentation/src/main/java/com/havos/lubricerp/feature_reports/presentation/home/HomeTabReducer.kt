package com.havos.lubricerp.feature_reports.presentation.home

import com.havos.lubricerp.feature_reports.domain.model.DashboardSummary

object HomeTabReducer {
    fun reduceForProfileLoading(state: HomeTabUiState, isLoading: Boolean): HomeTabUiState {
        return state.copy(isProfileLoading = isLoading)
    }

    fun reduceForUser(state: HomeTabUiState, name: String): HomeTabUiState {
        return state.copy(greetingName = name, isProfileLoading = false)
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
            recentInvoices = data.recentInvoices
        )
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
