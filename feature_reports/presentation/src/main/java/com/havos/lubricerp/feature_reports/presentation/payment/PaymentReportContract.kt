package com.havos.lubricerp.feature_reports.presentation.payment

import com.havos.lubricerp.core.common.UiIntent
import androidx.compose.runtime.Stable
import com.havos.lubricerp.core.common.UiState
import com.havos.lubricerp.feature_reports.domain.model.PaymentPendingCustomer
import com.havos.lubricerp.feature_reports.domain.model.PaymentReceivedItem
import com.havos.lubricerp.feature_reports.domain.model.AccountsSummary
import com.havos.lubricerp.feature_reports.domain.model.RecordPaymentResponse

sealed interface PaymentReportIntent : UiIntent {
    data class LoadReceived(val fromDate: String, val toDate: String) : PaymentReportIntent
    data object LoadPending : PaymentReportIntent
    data object LoadMoreReceived : PaymentReportIntent
    data object LoadMorePending : PaymentReportIntent
    data class LoadAccountsSummary(val fromDate: String, val toDate: String) : PaymentReportIntent
    data class FromDateReceivedChanged(val date: String) : PaymentReportIntent
    data class ToDateReceivedChanged(val date: String) : PaymentReportIntent
    data object ApplyReceivedFilter : PaymentReportIntent
    data class FromDateAccountsChanged(val date: String) : PaymentReportIntent
    data class ToDateAccountsChanged(val date: String) : PaymentReportIntent
    data object ApplyAccountsFilter : PaymentReportIntent
    data object ToggleOverdueOnly : PaymentReportIntent
    data object OpenCollectPayment : PaymentReportIntent
    data object DismissCollectPayment : PaymentReportIntent
    data class PaymentInvoiceSelected(val invoiceId: Long) : PaymentReportIntent
    data class PaymentAmountChanged(val value: String) : PaymentReportIntent
    data class PaymentModeChanged(val mode: String) : PaymentReportIntent
    data class PaymentDateChanged(val date: String) : PaymentReportIntent
    data class PaymentReferenceChanged(val value: String) : PaymentReportIntent
    data class PaymentRemarksChanged(val value: String) : PaymentReportIntent
    data object SubmitPayment : PaymentReportIntent
    data object Retry : PaymentReportIntent
    data object Refresh : PaymentReportIntent
}

@Stable
data class PaymentReportUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val selectedTab: Int = 0,
    val receivedDateFrom: String = "",
    val receivedDateTo: String = "",
    val isReceivedLoadingMore: Boolean = false,
    val receivedHasMore: Boolean = true,
    val receivedTotalCount: Int = 0,
    val isPendingLoadingMore: Boolean = false,
    val pendingHasMore: Boolean = true,
    val pendingTotalCount: Int = 0,
    val accountsDateFrom: String = "",
    val accountsDateTo: String = "",
    val receivedItems: List<PaymentReceivedItem> = emptyList(),
    val pendingItems: List<PaymentPendingCustomer> = emptyList(),
    val overdueOnly: Boolean = false,
    val accountsSummary: AccountsSummary? = null,
    val showCollectPayment: Boolean = false,
    val paymentInvoiceId: Long = 0,
    val paymentAmount: String = "",
    val paymentMode: String = "Cash",
    val paymentDate: String = "",
    val paymentReference: String = "",
    val paymentRemarks: String = "",
    val isRecordingPayment: Boolean = false,
    val paymentResult: RecordPaymentResponse? = null,
    val paymentError: String? = null
) : UiState

sealed interface PaymentReportAction {
    data class FromDateReceivedChanged(val value: String) : PaymentReportAction
    data class ToDateReceivedChanged(val value: String) : PaymentReportAction
    data object ApplyReceivedFilter : PaymentReportAction
    data class FromDateAccountsChanged(val value: String) : PaymentReportAction
    data class ToDateAccountsChanged(val value: String) : PaymentReportAction
    data object ApplyAccountsFilter : PaymentReportAction
    data object ToggleOverdueOnly : PaymentReportAction
    data object OpenCollectPayment : PaymentReportAction
    data object DismissCollectPayment : PaymentReportAction
    data class PaymentInvoiceSelected(val invoiceId: Long) : PaymentReportAction
    data class PaymentAmountChanged(val value: String) : PaymentReportAction
    data class PaymentModeChanged(val mode: String) : PaymentReportAction
    data class PaymentDateChanged(val date: String) : PaymentReportAction
    data class PaymentReferenceChanged(val value: String) : PaymentReportAction
    data class PaymentRemarksChanged(val value: String) : PaymentReportAction
    data object SubmitPayment : PaymentReportAction
    data object Retry : PaymentReportAction
    data object Refresh : PaymentReportAction
    data object LoadMoreReceived : PaymentReportAction
    data object LoadMorePending : PaymentReportAction
}

sealed interface PaymentReportEffect {
    data object PaymentSuccess : PaymentReportEffect
}
