package com.havos.lubricerp.feature_reports.presentation.reportmodule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.havos.lubricerp.core.common.ResultState
import com.havos.lubricerp.feature_reports.domain.model.DateRangeFilter
import com.havos.lubricerp.feature_reports.domain.usecase.GetExpenseSummaryUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.GetNetProfitUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.GetProductSalesUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.GetReportSalesSummaryUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.ObserveSessionUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class ReportModuleViewModel(
    private val observeSessionUseCase: ObserveSessionUseCase,
    private val getReportSalesSummaryUseCase: GetReportSalesSummaryUseCase,
    private val getProductSalesUseCase: GetProductSalesUseCase,
    private val getNetProfitUseCase: GetNetProfitUseCase,
    private val getExpenseSummaryUseCase: GetExpenseSummaryUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(
        ReportModuleUiState(
            fromDate = defaultFromDate(),
            toDate   = defaultToDate()
        )
    )
    val state: StateFlow<ReportModuleUiState> = _state.asStateFlow()

    init {
        fetchAll()
    }

    fun onAction(action: ReportModuleAction) {
        when (action) {
            is ReportModuleAction.FromDateChanged -> {
                _state.update { it.copy(fromDate = action.value) }
                fetchAll()
            }
            is ReportModuleAction.ToDateChanged -> {
                _state.update { it.copy(toDate = action.value) }
                fetchAll()
            }
            ReportModuleAction.ApplyFilter -> fetchAll()
            ReportModuleAction.ResetFilter -> {
                _state.update { it.copy(fromDate = defaultFromDate(), toDate = defaultToDate(), dateError = null) }
                fetchAll()
            }
        }
    }

    private fun validateDateRange(fromDate: String, toDate: String): String? {
        val today = Calendar.getInstance(UTC).apply {
            set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
        }.timeInMillis
        val from = runCatching { displayFmt.parse(fromDate)?.time }.getOrNull()
        val to   = runCatching { displayFmt.parse(toDate)?.time }.getOrNull()
        if (from != null && from > today) return "Start date cannot be in the future"
        if (to   != null && to   > today) return "End date cannot be in the future"
        if (from != null && to != null && from > to) return "Start date must be before end date"
        return null
    }

    private fun fetchAll() {
        // Snapshot state once — immutable from this point, safe to read in the coroutine.
        val snapshot = _state.value
        // Do not call API until both dates are chosen.
        if (snapshot.fromDate.isBlank() || snapshot.toDate.isBlank()) return
        val dateError = validateDateRange(snapshot.fromDate, snapshot.toDate)
        if (dateError != null) {
            _state.update { it.copy(dateError = dateError) }
            return
        }
        _state.update { it.copy(dateError = null, isLoading = true, error = null) }

        viewModelScope.launch {
            val token = observeSessionUseCase().first()?.token ?: run {
                _state.update { it.copy(isLoading = false, error = "Session not available.") }
                return@launch
            }
            val filter = DateRangeFilter(fromDate = snapshot.fromDate, toDate = snapshot.toDate)

            // All 4 calls run in parallel.
            val salesDeferred   = async { getReportSalesSummaryUseCase(token, filter) }
            val productDeferred = async { getProductSalesUseCase(token, filter) }
            val profitDeferred  = async { getNetProfitUseCase(token, filter) }
            val expenseDeferred = async { getExpenseSummaryUseCase(token, filter) }

            val salesResult   = salesDeferred.await()
            val productResult = productDeferred.await()
            val profitResult  = profitDeferred.await()
            val expenseResult = expenseDeferred.await()

            var errorMsg: String? = null
            _state.update { current ->
                current.copy(
                    isLoading = false,
                    salesSummaryItems = when (salesResult) {
                        is ResultState.Success -> salesResult.data
                        is ResultState.Error -> { errorMsg = salesResult.message; current.salesSummaryItems }
                        ResultState.Loading -> current.salesSummaryItems
                    },
                    productSalesItems = when (productResult) {
                        is ResultState.Success -> productResult.data
                        is ResultState.Error -> { if (errorMsg == null) errorMsg = productResult.message; current.productSalesItems }
                        ResultState.Loading -> current.productSalesItems
                    },
                    netProfit = when (profitResult) {
                        is ResultState.Success -> profitResult.data
                        is ResultState.Error -> { if (errorMsg == null) errorMsg = profitResult.message; current.netProfit }
                        ResultState.Loading -> current.netProfit
                    },
                    expenseSummaryItems = when (expenseResult) {
                        is ResultState.Success -> expenseResult.data
                        is ResultState.Error -> { if (errorMsg == null) errorMsg = expenseResult.message; current.expenseSummaryItems }
                        ResultState.Loading -> current.expenseSummaryItems
                    },
                    error = errorMsg
                )
            }
        }
    }

    companion object {
        private val UTC = TimeZone.getTimeZone("UTC")
        // SimpleDateFormat is not thread-safe; each ViewModel instance gets its own copy.
        private val displayFmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
            timeZone = UTC
        }

        fun defaultFromDate(): String {
            val cal = Calendar.getInstance()
            cal.add(Calendar.MONTH, -6)
            return displayFmt.format(cal.time)
        }

        fun defaultToDate(): String = displayFmt.format(Calendar.getInstance().time)
    }
}
