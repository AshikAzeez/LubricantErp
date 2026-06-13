package com.havos.lubricerp.feature_reports.presentation.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.havos.lubricerp.core.common.ResultState
import com.havos.lubricerp.feature_reports.domain.model.DateRangeFilter
import com.havos.lubricerp.feature_reports.domain.model.RecordPaymentRequest
import com.havos.lubricerp.feature_reports.domain.usecase.GetPaymentsPendingUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.GetPaymentsReceivedUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.GetAccountsSummaryUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.RecordPaymentUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.ObserveSessionUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PaymentReportViewModel(
    private val getPaymentsReceivedUseCase: GetPaymentsReceivedUseCase,
    private val getPaymentsPendingUseCase: GetPaymentsPendingUseCase,
    private val getAccountsSummaryUseCase: GetAccountsSummaryUseCase,
    private val recordPaymentUseCase: RecordPaymentUseCase,
    private val observeSessionUseCase: ObserveSessionUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(PaymentReportUiState())
    val state: StateFlow<PaymentReportUiState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<PaymentReportEffect>()
    val effect: SharedFlow<PaymentReportEffect> = _effect.asSharedFlow()

    init {
        val now = Calendar.getInstance()
        val monthStart = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1) }
        _state.update {
            it.copy(
                receivedDateFrom = displayFmt.format(monthStart.time),
                receivedDateTo = displayFmt.format(now.time),
                accountsDateFrom = displayFmt.format(monthStart.time),
                accountsDateTo = displayFmt.format(now.time)
            )
        }
        loadAll(getToken())
    }

    private suspend fun getTokenSuspend(): String = observeSessionUseCase().first()?.token.orEmpty()

    private fun getToken(): String = runBlockingCatching { getTokenSuspend() }

    private fun loadAll(token: String) {
        if (token.isBlank()) return
        val s = _state.value
        loadReceived(token, s.receivedDateFrom, s.receivedDateTo)
        loadPending(token)
        loadAccountsSummary(token, s.accountsDateFrom, s.accountsDateTo)
    }

    private fun toApiDate(displayDate: String): String {
        return runCatching { apiFmt.format(displayFmt.parse(displayDate)!!) }.getOrElse { displayDate }
    }

    private fun loadReceived(token: String, displayFrom: String, displayTo: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getPaymentsReceivedUseCase(token, DateRangeFilter(toApiDate(displayFrom), toApiDate(displayTo)))) {
                is ResultState.Success -> _state.update { it.copy(isLoading = false, receivedItems = result.data, isRefreshing = false) }
                is ResultState.Error -> _state.update { it.copy(isLoading = false, errorMessage = result.message, isRefreshing = false) }
                ResultState.Loading -> {}
            }
        }
    }

    private fun loadPending(token: String) {
        viewModelScope.launch {
            when (val result = getPaymentsPendingUseCase(token)) {
                is ResultState.Success -> _state.update { it.copy(pendingItems = result.data) }
                is ResultState.Error -> {}
                ResultState.Loading -> {}
            }
        }
    }

    private fun loadAccountsSummary(token: String, displayFrom: String, displayTo: String) {
        viewModelScope.launch {
            when (val result = getAccountsSummaryUseCase(token, DateRangeFilter(toApiDate(displayFrom), toApiDate(displayTo)))) {
                is ResultState.Success -> _state.update { it.copy(accountsSummary = result.data) }
                is ResultState.Error -> {}
                ResultState.Loading -> {}
            }
        }
    }

    fun onIntent(intent: PaymentReportIntent) {
        when (intent) {
            is PaymentReportIntent.FromDateReceivedChanged -> _state.update { it.copy(receivedDateFrom = intent.date) }
            is PaymentReportIntent.ToDateReceivedChanged -> _state.update { it.copy(receivedDateTo = intent.date) }
            PaymentReportIntent.ApplyReceivedFilter -> {
                val s = _state.value; loadReceived(getToken(), s.receivedDateFrom, s.receivedDateTo)
            }
            is PaymentReportIntent.FromDateAccountsChanged -> _state.update { it.copy(accountsDateFrom = intent.date) }
            is PaymentReportIntent.ToDateAccountsChanged -> _state.update { it.copy(accountsDateTo = intent.date) }
            PaymentReportIntent.ApplyAccountsFilter -> {
                val s = _state.value; loadAccountsSummary(getToken(), s.accountsDateFrom, s.accountsDateTo)
            }
            PaymentReportIntent.ToggleOverdueOnly -> _state.update { it.copy(overdueOnly = !it.overdueOnly) }
            PaymentReportIntent.OpenCollectPayment -> _state.update { it.copy(showCollectPayment = true, paymentResult = null, paymentError = null) }
            PaymentReportIntent.DismissCollectPayment -> _state.update {
                it.copy(showCollectPayment = false, paymentInvoiceId = 0, paymentAmount = "", paymentMode = "Cash", paymentDate = "", paymentReference = "", paymentRemarks = "", paymentResult = null, paymentError = null)
            }
            is PaymentReportIntent.PaymentInvoiceSelected -> _state.update { it.copy(paymentInvoiceId = intent.invoiceId) }
            is PaymentReportIntent.PaymentAmountChanged -> {
                val sanitized = intent.value.filter { c -> c.isDigit() || c == '.' }
                    .let { if (it.contains(".")) it.substringBefore(".") + "." + it.substringAfter(".").take(2) else it }
                _state.update { it.copy(paymentAmount = sanitized) }
            }
            is PaymentReportIntent.PaymentModeChanged -> _state.update { it.copy(paymentMode = intent.mode) }
            is PaymentReportIntent.PaymentDateChanged -> _state.update { it.copy(paymentDate = intent.date) }
            is PaymentReportIntent.PaymentReferenceChanged -> _state.update { it.copy(paymentReference = intent.value.take(30)) }
            is PaymentReportIntent.PaymentRemarksChanged -> _state.update { it.copy(paymentRemarks = intent.value.take(100)) }
            PaymentReportIntent.SubmitPayment -> recordPayment()
            PaymentReportIntent.Retry -> loadAll(getToken())
            PaymentReportIntent.Refresh -> {
                _state.update { it.copy(isRefreshing = true) }
                loadAll(getToken())
            }
            is PaymentReportIntent.LoadReceived -> loadReceived(getToken(), intent.fromDate, intent.toDate)
            PaymentReportIntent.LoadPending -> loadPending(getToken())
            is PaymentReportIntent.LoadAccountsSummary -> loadAccountsSummary(getToken(), intent.fromDate, intent.toDate)
        }
    }

    private fun recordPayment() {
        val s = _state.value
        if (s.paymentInvoiceId == 0L) { _state.update { it.copy(paymentError = "Please select an invoice") }; return }
        val amount = s.paymentAmount.toDoubleOrNull()
        if (amount == null || amount <= 0) { _state.update { it.copy(paymentError = "Enter a valid amount") }; return }
        if (s.paymentDate.isBlank()) { _state.update { it.copy(paymentError = "Select payment date") }; return }
        val token = getToken()
        viewModelScope.launch {
            _state.update { it.copy(isRecordingPayment = true, paymentError = null) }
            val request = RecordPaymentRequest(
                invoiceId = s.paymentInvoiceId, amount = amount, paymentMode = s.paymentMode,
                paymentDate = toApiDate(s.paymentDate), reference = s.paymentReference, remarks = s.paymentRemarks
            )
            when (val result = recordPaymentUseCase(token, request)) {
                is ResultState.Success -> {
                    _state.update { it.copy(isRecordingPayment = false, paymentResult = result.data, paymentError = null) }
                    _effect.emit(PaymentReportEffect.PaymentSuccess)
                    loadAll(token)
                }
                is ResultState.Error -> _state.update { it.copy(isRecordingPayment = false, paymentError = result.message) }
                ResultState.Loading -> {}
            }
        }
    }

    companion object {
        private fun runBlockingCatching(block: suspend () -> String): String = runBlocking { block() }
        private val displayFmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
        private val apiFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
    }
}
