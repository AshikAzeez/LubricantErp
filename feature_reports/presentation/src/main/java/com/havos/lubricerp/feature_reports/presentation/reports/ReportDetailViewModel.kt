package com.havos.lubricerp.feature_reports.presentation.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.havos.lubricerp.core.common.ResultState
import com.havos.lubricerp.core.database.SecureSessionStore
import com.havos.lubricerp.feature_reports.domain.model.DateRangeFilter
import com.havos.lubricerp.feature_reports.domain.usecase.GetDashboardUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.GetPackagingLossGainUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.GetPaymentsReceivedUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.GetRawMaterialStockUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.GetSalesSummaryUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.GetStockOverviewUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.GetTankStockSummaryUseCase
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

class ReportDetailViewModel(
    private val getTankStockSummaryUseCase: GetTankStockSummaryUseCase,
    private val getRawMaterialStockUseCase: GetRawMaterialStockUseCase,
    private val getPackagingLossGainUseCase: GetPackagingLossGainUseCase,
    private val getSalesSummaryUseCase: GetSalesSummaryUseCase,
    private val getPaymentsReceivedUseCase: GetPaymentsReceivedUseCase,
    private val getDashboardUseCase: GetDashboardUseCase,
    private val observeSessionUseCase: ObserveSessionUseCase,
    private val secureSessionStore: SecureSessionStore,
    private val getStockOverviewUseCase: GetStockOverviewUseCase
) : ViewModel() {

    // Instance-level: SimpleDateFormat is NOT thread-safe; never share across coroutines.
    // Declared before _state because _state init calls defaultFromDate()/defaultToDate().
    private val displayFmt = SimpleDateFormat(DISPLAY_PATTERN, Locale.getDefault())
    private val apiFmt     = SimpleDateFormat(API_PATTERN, Locale.getDefault())

    private val _state = MutableStateFlow(
        ReportDetailUiState(fromDate = defaultFromDate(), toDate = defaultToDate())
    )

    val state: StateFlow<ReportDetailUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val saved = secureSessionStore.salesFilterFlow.first()
            if (saved != null) {
                _state.update { it.copy(fromDate = saved.first, toDate = saved.second) }
            }
        }
    }

    fun onIntent(intent: ReportDetailIntent) {
        when (intent) {
            is ReportDetailIntent.Load -> loadReport(intent.reportKey)
            is ReportDetailIntent.FromDateChanged -> {
                _state.update { it.copy(fromDate = intent.date) }
                applyFilter()
            }
            is ReportDetailIntent.ToDateChanged -> {
                _state.update { it.copy(toDate = intent.date) }
                applyFilter()
            }
            is ReportDetailIntent.SearchChanged -> _state.update { it.copy(searchQuery = intent.query) }
            is ReportDetailIntent.DaysThresholdChanged -> _state.update { it.copy(daysThreshold = intent.days) }
            is ReportDetailIntent.GroupByChanged -> _state.update { it.copy(groupBy = intent.group) }
            ReportDetailIntent.ApplyFilter -> applyFilter()
            ReportDetailIntent.ResetFilter -> resetFilters()
            ReportDetailIntent.ToggleTopCustomers -> _state.update { ReportDetailReducer.reduceToggleTopCustomers(it) }
            ReportDetailIntent.Refresh -> refresh()
        }
    }

    private fun refresh() {
        _state.update { it.copy(isRefreshing = true, errorMessage = null) }
        when (_state.value.selectedReport) {
            ReportItem.TANK_STOCK_SUMMARY -> fetchTankStockSummary(isRefresh = true)
            ReportItem.RAW_MATERIAL_STOCK -> fetchRawMaterialStock(isRefresh = true)
            ReportItem.PACKAGING_LOSS_GAIN -> fetchPackagingLossGain(isRefresh = true)
            ReportItem.SALES_SUMMARY -> fetchSalesSummary(isRefresh = true)
            ReportItem.CONSOLIDATED_STOCK -> fetchStockOverview(isRefresh = true)
            else -> _state.update { it.copy(isRefreshing = false) }
        }
    }

    private fun loadReport(reportKey: String) {
        val report = reportItemByKey(reportKey)
        _state.update { ReportDetailReducer.reduceForLoading(it, report) }
        when (report) {
            ReportItem.TANK_STOCK_SUMMARY -> fetchTankStockSummary()
            ReportItem.RAW_MATERIAL_STOCK -> fetchRawMaterialStock()
            ReportItem.PACKAGING_LOSS_GAIN -> fetchPackagingLossGain()
            ReportItem.SALES_SUMMARY -> fetchSalesSummary()
            ReportItem.CONSOLIDATED_STOCK -> fetchStockOverview()
            else -> _state.update { it.copy(isLoading = false, errorMessage = null) }
        }
    }

    private fun validateDateRange(fromDate: String, toDate: String): String? {
        val utc = TimeZone.getTimeZone("UTC")
        val today = Calendar.getInstance(utc).apply {
            set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
        }.timeInMillis
        // Temporarily set UTC for parsing; restore after to avoid side-effects.
        val prevTz = displayFmt.timeZone
        displayFmt.timeZone = utc
        val from = runCatching { displayFmt.parse(fromDate)?.time }.getOrNull()
        val to   = runCatching { displayFmt.parse(toDate)?.time }.getOrNull()
        displayFmt.timeZone = prevTz
        if (from != null && from > today) return "Start date cannot be in the future"
        if (to   != null && to   > today) return "End date cannot be in the future"
        if (from != null && to != null && from > to) return "Start date must be before end date"
        return null
    }

    private fun applyFilter() {
        // Snapshot once — avoids double _state.value reads that could diverge.
        val current = _state.value
        // Do not call API until both dates are chosen.
        if (current.fromDate.isBlank() || current.toDate.isBlank()) return
        val dateError = validateDateRange(current.fromDate, current.toDate)
        if (dateError != null) {
            _state.update { it.copy(dateError = dateError, isLoading = false) }
            return
        }
        _state.update { it.copy(dateError = null, isLoading = true, errorMessage = null) }
        when (current.selectedReport) {
            ReportItem.PACKAGING_LOSS_GAIN -> fetchPackagingLossGain()
            ReportItem.SALES_SUMMARY -> fetchSalesSummary()
            ReportItem.SLOW_MOVING_STOCK,
            ReportItem.PACKAGING_SUMMARY,
            ReportItem.RAW_MATERIAL_STOCK,
            ReportItem.TANK_STOCK_SUMMARY -> _state.update { it.copy(isLoading = false) }

            else -> Unit
        }
    }

    private fun resetFilters() {
        _state.update {
            it.copy(
                fromDate = defaultFromDate(),
                toDate = defaultToDate(),
                daysThreshold = "30 days",
                groupBy = "Daily",
                searchQuery = "",
                dateError = null
            )
        }
        if (_state.value.selectedReport == ReportItem.SALES_SUMMARY) {
            viewModelScope.launch {
                secureSessionStore.clearSalesFilter()
            }
        }
        applyFilter()
    }

    private fun fetchTankStockSummary(isRefresh: Boolean = false) {
        viewModelScope.launch {
            when (val result = getTankStockSummaryUseCase()) {
                is ResultState.Success -> {
                    _state.update { ReportDetailReducer.reduceForTankSuccess(it, result.data).copy(isRefreshing = false) }
                }
                is ResultState.Error -> {
                    _state.update { ReportDetailReducer.reduceForError(it, result.message).copy(isRefreshing = false) }
                }
                ResultState.Loading -> if (!isRefresh) _state.update { it.copy(isLoading = true) }
            }
        }
    }

    private fun fetchRawMaterialStock(isRefresh: Boolean = false) {
        viewModelScope.launch {
            when (val result = getRawMaterialStockUseCase()) {
                is ResultState.Success -> {
                    _state.update { ReportDetailReducer.reduceForRawMaterialSuccess(it, result.data).copy(isRefreshing = false) }
                }
                is ResultState.Error -> {
                    _state.update { ReportDetailReducer.reduceForError(it, result.message).copy(isRefreshing = false) }
                }
                ResultState.Loading -> if (!isRefresh) _state.update { it.copy(isLoading = true) }
            }
        }
    }

    private fun fetchPackagingLossGain(isRefresh: Boolean = false) {
        viewModelScope.launch {
            val current = _state.value
            when (
                val result = getPackagingLossGainUseCase(
                    DateRangeFilter(
                        fromDate = current.fromDate,
                        toDate = current.toDate
                    )
                )
            ) {
                is ResultState.Success -> {
                    _state.update { ReportDetailReducer.reduceForPackagingSuccess(it, result.data).copy(isRefreshing = false) }
                }
                is ResultState.Error -> {
                    _state.update { ReportDetailReducer.reduceForError(it, result.message).copy(isRefreshing = false) }
                }
                ResultState.Loading -> if (!isRefresh) _state.update { it.copy(isLoading = true) }
            }
        }
    }

    private fun fetchSalesSummary(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (!isRefresh) _state.update { it.copy(isLoading = true, errorMessage = null) }
            val token = observeSessionUseCase().first()?.token.orEmpty()
            if (token.isBlank()) {
                _state.update { ReportDetailReducer.reduceForError(it, "Session not available.") }
                return@launch
            }
            val current = _state.value
            val fromApi = runCatching { apiFmt.format(displayFmt.parse(current.fromDate)!!) }.getOrElse { current.fromDate }
            val toApi   = runCatching { apiFmt.format(displayFmt.parse(current.toDate)!!) }.getOrElse { current.toDate }

            when (
                val result = getSalesSummaryUseCase(
                    token = token,
                    filter = DateRangeFilter(
                        fromDate = fromApi,
                        toDate = toApi
                    )
                )
            ) {
                is ResultState.Success -> {
                    // Run dashboard and payments in parallel within this scope (structured, not orphaned).
                    val dashboardDeferred = async { getDashboardUseCase(token) }
                    val paymentsDeferred  = async {
                        val today    = defaultToDate()
                        val todayApi = runCatching { apiFmt.format(displayFmt.parse(today)!!) }.getOrElse { today }
                        getPaymentsReceivedUseCase(token, DateRangeFilter(fromDate = todayApi, toDate = todayApi))
                    }
                    val dashboardCount = when (val d = dashboardDeferred.await()) {
                        is ResultState.Success -> d.data.monthlySalesCount
                        else -> 0
                    }
                    val paymentsResult = paymentsDeferred.await()
                    secureSessionStore.saveSalesFilter(current.fromDate, current.toDate)
                    _state.update {
                        val withSales = ReportDetailReducer.reduceForSalesSummarySuccess(it, result.data, dashboardCount).copy(isRefreshing = false)
                        when (paymentsResult) {
                            is ResultState.Success -> ReportDetailReducer.reduceForPaymentsReceivedSuccess(withSales, paymentsResult.data)
                            else -> withSales.copy(paymentReceivedItems = emptyList())
                        }
                    }
                }
                is ResultState.Error -> {
                    _state.update { ReportDetailReducer.reduceForError(it, result.message).copy(isRefreshing = false) }
                }
                ResultState.Loading -> if (!isRefresh) _state.update { it.copy(isLoading = true) }
            }
        }
    }

    private fun fetchStockOverview(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (!isRefresh) _state.update { it.copy(isLoading = true, errorMessage = null) }
            val token = observeSessionUseCase().first()?.token.orEmpty()
            if (token.isBlank()) {
                _state.update { it.copy(isLoading = false, isRefreshing = false, errorMessage = "Session not available.") }
                return@launch
            }
            val tanks = when (val result = getStockOverviewUseCase(token)) {
                is ResultState.Success -> result.data
                else -> emptyList()
            }
            _state.update {
                it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    stockOverviewTankItems = tanks,
                    errorMessage = null
                )
            }
        }
    }

    fun defaultFromDate(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, -6)
        return displayFmt.format(cal.time)
    }

    fun defaultToDate(): String = displayFmt.format(Calendar.getInstance().time)

    companion object {
        private const val DISPLAY_PATTERN = "dd/MM/yyyy"
        private const val API_PATTERN     = "yyyy-MM-dd"
    }
}
