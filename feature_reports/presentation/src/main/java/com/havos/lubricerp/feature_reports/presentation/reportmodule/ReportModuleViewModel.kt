package com.havos.lubricerp.feature_reports.presentation.reportmodule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.havos.lubricerp.core.common.ResultState
import com.havos.lubricerp.core.common.isOffline
import com.havos.lubricerp.core.network.NetworkMonitor
import com.havos.lubricerp.feature_reports.domain.model.DateRangeFilter
import com.havos.lubricerp.feature_reports.domain.usecase.EnsureProfileLoadedUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.GetExpenseSummaryUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.GetNetProfitUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.GetProductSalesUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.GetSalesSummaryUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.ObserveSessionUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class ReportModuleViewModel(
    private val observeSessionUseCase: ObserveSessionUseCase,
    private val ensureProfileLoadedUseCase: EnsureProfileLoadedUseCase,
    private val getSalesSummaryUseCase: GetSalesSummaryUseCase,
    private val getProductSalesUseCase: GetProductSalesUseCase,
    private val getNetProfitUseCase: GetNetProfitUseCase,
    private val getExpenseSummaryUseCase: GetExpenseSummaryUseCase,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private var isFetchInFlight = false
    private var userRoles: List<String> = emptyList()

    private val _state = MutableStateFlow(
        ReportModuleUiState(
            fromDate = defaultFromDate(),
            toDate   = defaultToDate()
        )
    )
    val state: StateFlow<ReportModuleUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            when (val result = ensureProfileLoadedUseCase()) {
                is ResultState.Success -> {
                    userRoles = result.data.roles
                    val hasAccess = userRoles.any {
                        it.equals("Admin", ignoreCase = true) || it.equals("Manager", ignoreCase = true)
                    }
                    _state.update { it.copy(canViewNetProfit = hasAccess) }
                }
                else -> Unit
            }
            fetchAll()
        }
        observeConnectivity()
    }

    private fun observeConnectivity() {
        viewModelScope.launch {
            networkMonitor.isOnline
                .distinctUntilChanged()
                .drop(1)
                .filter { online -> online && _state.value.retryPending }
                .collect {
                    _state.update { it.copy(isOffline = false, retryPending = false) }
                    fetchAll()
                }
        }
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
            ReportModuleAction.Refresh -> refresh()
        }
    }

    private fun refresh() {
        isFetchInFlight = false
        val snapshot = _state.value
        if (snapshot.fromDate.isBlank() || snapshot.toDate.isBlank()) return
        val dateError = validateDateRange(snapshot.fromDate, snapshot.toDate)
        if (dateError != null) { _state.update { it.copy(dateError = dateError) }; return }
        _state.update { it.copy(isRefreshing = true, error = null, isOffline = false) }
        viewModelScope.launch {
            isFetchInFlight = true
            val token = observeSessionUseCase().first()?.token ?: run {
                _state.update { it.copy(isRefreshing = false, error = "Session not available.") }
                isFetchInFlight = false
                return@launch
            }
            val filter = DateRangeFilter(fromDate = toApiDate(snapshot.fromDate), toDate = toApiDate(snapshot.toDate))
            val salesDeferred   = async { getSalesSummaryUseCase(token, filter) }
            val productDeferred = async { getProductSalesUseCase(token, filter) }
            val profitDeferred  = async { getNetProfitUseCase(token, filter, userRoles) }
            val expenseDeferred = async { getExpenseSummaryUseCase(token, filter) }
            val salesResult   = salesDeferred.await()
            val productResult = productDeferred.await()
            val profitResult  = profitDeferred.await()
            val expenseResult = expenseDeferred.await()
            val anyOffline = listOf(salesResult, productResult, profitResult, expenseResult).any {
                it is ResultState.Error && it.isOffline
            }
            var errorMsg: String? = null
            _state.update { current ->
                current.copy(
                    isRefreshing = false,
                    isOffline = anyOffline,
                    retryPending = anyOffline,
                    salesSummaryItems = when (salesResult) {
                        is ResultState.Success -> salesResult.data
                        is ResultState.Error -> { if (errorMsg == null) errorMsg = salesResult.message; current.salesSummaryItems }
                        ResultState.Loading -> current.salesSummaryItems
                    },
                    productSalesItems = when (productResult) {
                        is ResultState.Success -> productResult.data
                        is ResultState.Error -> { if (errorMsg == null) errorMsg = productResult.message; current.productSalesItems }
                        ResultState.Loading -> current.productSalesItems
                    },
                    netProfit = when (profitResult) {
                        is ResultState.Success -> profitResult.data
                        is ResultState.Error -> if (profitResult.message == GetNetProfitUseCase.ACCESS_DENIED) current.netProfit
                                               else { if (errorMsg == null) errorMsg = profitResult.message; current.netProfit }
                        ResultState.Loading -> current.netProfit
                    },
                    expenseSummaryItems = when (expenseResult) {
                        is ResultState.Success -> expenseResult.data
                        is ResultState.Error -> { if (errorMsg == null) errorMsg = expenseResult.message; current.expenseSummaryItems }
                        ResultState.Loading -> current.expenseSummaryItems
                    },
                    error = if (anyOffline) null else errorMsg
                )
            }
            isFetchInFlight = false
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
        if (isFetchInFlight) return
        // Snapshot state once — immutable from this point, safe to read in the coroutine.
        val snapshot = _state.value
        // Do not call API until both dates are chosen.
        if (snapshot.fromDate.isBlank() || snapshot.toDate.isBlank()) return
        val dateError = validateDateRange(snapshot.fromDate, snapshot.toDate)
        if (dateError != null) {
            _state.update { it.copy(dateError = dateError) }
            return
        }
        _state.update { it.copy(dateError = null, isLoading = true, error = null, isOffline = false) }

        viewModelScope.launch {
            isFetchInFlight = true
            val token = observeSessionUseCase().first()?.token ?: run {
                _state.update { it.copy(isLoading = false, error = "Session not available.") }
                isFetchInFlight = false
                return@launch
            }
            val filter = DateRangeFilter(fromDate = toApiDate(snapshot.fromDate), toDate = toApiDate(snapshot.toDate))

            // All 4 calls run in parallel.
            val salesDeferred   = async { getSalesSummaryUseCase(token, filter) }
            val productDeferred = async { getProductSalesUseCase(token, filter) }
            val profitDeferred  = async { getNetProfitUseCase(token, filter, userRoles) }
            val expenseDeferred = async { getExpenseSummaryUseCase(token, filter) }

            val salesResult   = salesDeferred.await()
            val productResult = productDeferred.await()
            val profitResult  = profitDeferred.await()
            val expenseResult = expenseDeferred.await()

            val anyOffline = listOf(salesResult, productResult, profitResult, expenseResult).any {
                it is ResultState.Error && it.isOffline
            }

            var errorMsg: String? = null
            _state.update { current ->
                current.copy(
                    isLoading = false,
                    isOffline = anyOffline,
                    retryPending = anyOffline,
                    salesSummaryItems = when (salesResult) {
                        is ResultState.Success -> salesResult.data
                        is ResultState.Error -> { if (errorMsg == null) errorMsg = salesResult.message; current.salesSummaryItems }
                        ResultState.Loading -> current.salesSummaryItems
                    },
                    productSalesItems = when (productResult) {
                        is ResultState.Success -> productResult.data
                        is ResultState.Error -> { if (errorMsg == null) errorMsg = productResult.message; current.productSalesItems }
                        ResultState.Loading -> current.productSalesItems
                    },
                    netProfit = when (profitResult) {
                        is ResultState.Success -> profitResult.data
                        is ResultState.Error -> if (profitResult.message == GetNetProfitUseCase.ACCESS_DENIED) current.netProfit
                                               else { if (errorMsg == null) errorMsg = profitResult.message; current.netProfit }
                        ResultState.Loading -> current.netProfit
                    },
                    expenseSummaryItems = when (expenseResult) {
                        is ResultState.Success -> expenseResult.data
                        is ResultState.Error -> { if (errorMsg == null) errorMsg = expenseResult.message; current.expenseSummaryItems }
                        ResultState.Loading -> current.expenseSummaryItems
                    },
                    error = if (anyOffline) null else errorMsg
                )
            }
            isFetchInFlight = false
        }
    }

    private fun toApiDate(displayDate: String): String {
        return runCatching { apiFmt.format(displayFmt.parse(displayDate)!!) }.getOrElse { displayDate }
    }

    companion object {
        private val UTC = TimeZone.getTimeZone("UTC")
        // SimpleDateFormat is not thread-safe; each ViewModel instance gets its own copy.
        private val displayFmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
            timeZone = UTC
        }
        private val apiFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
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
