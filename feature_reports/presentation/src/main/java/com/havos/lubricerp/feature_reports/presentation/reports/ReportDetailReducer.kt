package com.havos.lubricerp.feature_reports.presentation.reports

import com.havos.lubricerp.feature_reports.domain.model.PackagingLossGainReport
import com.havos.lubricerp.feature_reports.domain.model.PaymentReceivedItem
import com.havos.lubricerp.feature_reports.domain.model.RawMaterialStockItem
import com.havos.lubricerp.feature_reports.domain.model.SalesSummaryItem
import com.havos.lubricerp.feature_reports.domain.model.TankStockSummary

object ReportDetailReducer {
    fun reduceForLoading(state: ReportDetailUiState, report: ReportItem): ReportDetailUiState {
        return state.copy(selectedReport = report, isLoading = true, errorMessage = null)
    }

    fun reduceForTankSuccess(state: ReportDetailUiState, data: TankStockSummary): ReportDetailUiState {
        return state.copy(isLoading = false, tankStockSummary = data, errorMessage = null)
    }

    fun reduceForRawMaterialSuccess(
        state: ReportDetailUiState,
        data: List<RawMaterialStockItem>
    ): ReportDetailUiState {
        return state.copy(isLoading = false, rawMaterialItems = data, errorMessage = null)
    }

    fun reduceForPackagingSuccess(
        state: ReportDetailUiState,
        data: PackagingLossGainReport
    ): ReportDetailUiState {
        return state.copy(isLoading = false, packagingLossGainReport = data, errorMessage = null)
    }

    fun reduceForSalesSummarySuccess(
        state: ReportDetailUiState,
        data: List<SalesSummaryItem>,
        monthlySalesCount: Int
    ): ReportDetailUiState {
        return state.copy(
            isLoading = false,
            salesSummaryItems = data,
            monthlySalesCount = monthlySalesCount,
            errorMessage = null
        )
    }

    fun reduceForPaymentsReceivedSuccess(
        state: ReportDetailUiState,
        data: List<PaymentReceivedItem>
    ): ReportDetailUiState {
        return state.copy(paymentReceivedItems = data)
    }

    fun reduceToggleTopCustomers(state: ReportDetailUiState): ReportDetailUiState {
        return state.copy(showTopCustomers = !state.showTopCustomers)
    }

    fun reduceForError(state: ReportDetailUiState, message: String): ReportDetailUiState {
        return state.copy(isLoading = false, errorMessage = message)
    }
}
