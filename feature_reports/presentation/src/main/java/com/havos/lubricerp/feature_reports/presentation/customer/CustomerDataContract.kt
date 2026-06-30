package com.havos.lubricerp.feature_reports.presentation.customer

import com.havos.lubricerp.core.common.UiIntent
import androidx.compose.runtime.Stable
import com.havos.lubricerp.core.common.UiState
import com.havos.lubricerp.feature_reports.domain.model.Customer
import com.havos.lubricerp.feature_reports.domain.model.CustomerLedgerEntry
import com.havos.lubricerp.feature_reports.domain.model.CustomerMobileSummary
import com.havos.lubricerp.feature_reports.domain.model.RecordPaymentResponse

@Stable
data class CustomerDataUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLedgerLoading: Boolean = false,
    val isLedgerLoadingMore: Boolean = false,
    val ledgerHasMore: Boolean = true,
    val ledgerTotalCount: Int = 0,
    val isMobileSummaryLoading: Boolean = false,
    val isOffline: Boolean = false,
    val retryPending: Boolean = false,
    val errorMessage: String? = null,
    val customers: List<Customer> = emptyList(),
    val searchQuery: String = "",
    val selectedCustomer: Customer? = null,
    val mobileSummary: CustomerMobileSummary? = null,
    val ledgerEntries: List<CustomerLedgerEntry> = emptyList(),
    val ledgerFromDate: String = "",
    val ledgerToDate: String = "",
    /** Outstanding amounts cached from previously viewed customers (customerId -> outstanding). */
    val cachedOutstanding: Map<Long, Double> = emptyMap(),
    val isRecordingPayment: Boolean = false,
    val paymentResult: RecordPaymentResponse? = null,
    val paymentError: String? = null,
    val showPaymentSheet: Boolean = false,
    val paymentFormInvoiceId: Long = 0,
    val paymentFormAmount: String = "",
    val paymentFormMode: String = "Cash",
    val paymentFormDate: String = "",
    val paymentFormReference: String = "",
    val paymentFormRemarks: String = "",
    val paymentFormFieldErrors: Map<String, String> = emptyMap()
) : UiState {
    companion object {
        const val FIELD_INVOICE = "invoice"
        const val FIELD_AMOUNT = "amount"
        const val FIELD_DATE = "date"
        const val FIELD_REFERENCE = "reference"
        const val FIELD_REMARKS = "remarks"
    }
}

sealed interface CustomerDataIntent : UiIntent {
    data object Load : CustomerDataIntent
    data object Refresh : CustomerDataIntent
    data class SearchChanged(val query: String) : CustomerDataIntent
    data class CustomerSelected(val customer: Customer) : CustomerDataIntent
    data object CustomerDismissed : CustomerDataIntent
    data class LedgerFromDateChanged(val date: String) : CustomerDataIntent
    data class LedgerToDateChanged(val date: String) : CustomerDataIntent
    data object LoadLedger : CustomerDataIntent
    data object LoadMobileSummary : CustomerDataIntent
    data object LoadMoreLedger : CustomerDataIntent
    data class LedgerDatePreset(val label: String, val fromDate: String, val toDate: String) : CustomerDataIntent
    data object ShowPaymentSheet : CustomerDataIntent
    data object DismissPaymentSheet : CustomerDataIntent
    data class PaymentFormInvoiceChanged(val invoiceId: Long) : CustomerDataIntent
    data class PaymentFormAmountChanged(val amount: String) : CustomerDataIntent
    data class PaymentFormModeChanged(val mode: String) : CustomerDataIntent
    data class PaymentFormDateChanged(val date: String) : CustomerDataIntent
    data class PaymentFormReferenceChanged(val reference: String) : CustomerDataIntent
    data class PaymentFormRemarksChanged(val remarks: String) : CustomerDataIntent
    data object SubmitPayment : CustomerDataIntent
    data object PaymentResultDismissed : CustomerDataIntent
}

sealed interface CustomerDataAction {
    data class SearchChanged(val value: String) : CustomerDataAction
    data class CustomerSelected(val customer: Customer) : CustomerDataAction
    data object CustomerDismissed : CustomerDataAction
    data class CallCustomer(val phone: String) : CustomerDataAction
    data class WhatsAppCustomer(val phone: String) : CustomerDataAction
    data class LedgerFromDateChanged(val date: String) : CustomerDataAction
    data class LedgerToDateChanged(val date: String) : CustomerDataAction
    data object LoadLedger : CustomerDataAction
    data object LoadMobileSummary : CustomerDataAction
    data object LoadMoreLedger : CustomerDataAction
    data class LedgerDatePreset(val label: String, val fromDate: String, val toDate: String) : CustomerDataAction
    data object Retry : CustomerDataAction
    data object Refresh : CustomerDataAction
    data object ShowPaymentSheet : CustomerDataAction
    data object DismissPaymentSheet : CustomerDataAction
    data class PaymentFormInvoiceChanged(val invoiceId: Long) : CustomerDataAction
    data class PaymentFormAmountChanged(val amount: String) : CustomerDataAction
    data class PaymentFormModeChanged(val mode: String) : CustomerDataAction
    data class PaymentFormDateChanged(val date: String) : CustomerDataAction
    data class PaymentFormReferenceChanged(val reference: String) : CustomerDataAction
    data class PaymentFormRemarksChanged(val remarks: String) : CustomerDataAction
    data object SubmitPayment : CustomerDataAction
    data object PaymentResultDismissed : CustomerDataAction
}
