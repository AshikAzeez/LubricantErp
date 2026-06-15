package com.havos.lubricerp.feature_reports.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.havos.lubricerp.core.common.ResultState
import com.havos.lubricerp.core.common.isOffline
import com.havos.lubricerp.core.network.NetworkMonitor
import com.havos.lubricerp.feature_reports.domain.model.DateRangeFilter
import com.havos.lubricerp.feature_reports.domain.usecase.EnsureProfileLoadedUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.GetCashPositionUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.GetDashboardUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.GetNetProfitUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.GetPurchaseSummaryUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.GetReceivablesAgingUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.ObserveSessionUseCase
import kotlinx.coroutines.async
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeTabViewModel(
    private val observeSessionUseCase: ObserveSessionUseCase,
    private val ensureProfileLoadedUseCase: EnsureProfileLoadedUseCase,
    private val getDashboardUseCase: GetDashboardUseCase,
    private val getNetProfitUseCase: GetNetProfitUseCase,
    private val getReceivablesAgingUseCase: GetReceivablesAgingUseCase,
    private val getPurchaseSummaryUseCase: GetPurchaseSummaryUseCase,
    private val getCashPositionUseCase: GetCashPositionUseCase,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private var isDashboardInFlight = false
    private var userRoles: List<String> = emptyList()

    private val _state = MutableStateFlow(HomeTabUiState())
    val state: StateFlow<HomeTabUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            observeSessionUseCase().collect { session ->
                if (session == null) {
                    _state.update { it.copy(greetingName = "", isProfileLoading = false) }
                    return@collect
                }
                _state.update { HomeTabReducer.reduceForProfileLoading(it, true) }
                when (val result = ensureProfileLoadedUseCase()) {
                    is ResultState.Success -> {
                        userRoles = result.data.roles
                        val canFinancials = hasFinancialsAccess(userRoles)
                        _state.update {
                            HomeTabReducer.reduceForUser(it, result.data.fullName, userRoles)
                                .copy(canViewFinancials = canFinancials)
                        }
                    }
                    is ResultState.Error -> {
                        _state.update {
                            HomeTabReducer.reduceForUser(it, displayNameFromUsername(session.username))
                        }
                    }
                    ResultState.Loading -> Unit
                }
                loadDashboard(session.token)
            }
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
                    val token = observeSessionUseCase().first()?.token.orEmpty()
                    if (token.isNotBlank()) loadDashboard(token)
                }
        }
    }

    fun onIntent(intent: HomeTabIntent) {
        when (intent) {
            HomeTabIntent.LoadDashboard -> Unit
            HomeTabIntent.Refresh -> refresh()
            is HomeTabIntent.NetProfitPeriodChanged -> {
                _state.update { HomeTabReducer.reduceForNetProfitPeriod(it, intent.period) }
                viewModelScope.launch { loadNetProfit() }
            }
            is HomeTabIntent.NetProfitCustomDateChanged -> {
                _state.update {
                    HomeTabReducer.reduceForNetProfitCustomDate(it, intent.fromDate, intent.toDate)
                }
            }
            HomeTabIntent.NetProfitCustomApply -> {
                val s = _state.value
                if (s.netProfitCustomFrom.isNotBlank() && s.netProfitCustomTo.isNotBlank()) {
                    _state.update { HomeTabReducer.reduceForNetProfitLoading(it) }
                    viewModelScope.launch { loadNetProfit() }
                }
            }
        }
    }

    fun refresh() {
        if (isDashboardInFlight) {
            _state.update { it.copy(isRefreshing = false) }
            return
        }
        viewModelScope.launch {
            isDashboardInFlight = true
            _state.update { it.copy(isRefreshing = true, dashboardError = null, isOffline = false) }
            val token = observeSessionUseCase().first()?.token.orEmpty()
            if (token.isBlank()) {
                _state.update { it.copy(isRefreshing = false) }
                return@launch
            }
            if (userRoles.isEmpty()) {
                when (val profileResult = ensureProfileLoadedUseCase()) {
                    is ResultState.Success -> {
                        userRoles = profileResult.data.roles
                        _state.update {
                            it.copy(
                                canViewFinancials = hasFinancialsAccess(userRoles),
                                userRoles = userRoles
                            )
                        }
                    }
                    else -> Unit
                }
            }
            val filter = periodFilter(_state.value)
            val dashboardDeferred = async { getDashboardUseCase(token) }
            val netProfitDeferred = async { getNetProfitUseCase(token, filter, userRoles) }
            val receivablesAgingDeferred = async { getReceivablesAgingUseCase(token) }
            val purchaseSummaryDeferred = async { getPurchaseSummaryUseCase(token) }
            val cashPositionDeferred = async { getCashPositionUseCase(token) }
            when (val result = dashboardDeferred.await()) {
                is ResultState.Success -> _state.update {
                    HomeTabReducer.reduceForDashboardSuccess(it, result.data).copy(isRefreshing = false)
                }
                is ResultState.Error -> _state.update {
                    HomeTabReducer.reduceForDashboardError(it, result.message, result.isOffline).copy(isRefreshing = false)
                }
                ResultState.Loading -> Unit
            }
            when (val result = netProfitDeferred.await()) {
                is ResultState.Success -> _state.update {
                    HomeTabReducer.reduceForNetProfit(it, result.data.netProfit)
                }
                else -> Unit
            }
            when (val result = receivablesAgingDeferred.await()) {
                is ResultState.Success -> _state.update {
                    HomeTabReducer.reduceForReceivablesAgingSuccess(it, result.data)
                }
                else -> Unit
            }
            when (val result = purchaseSummaryDeferred.await()) {
                is ResultState.Success -> _state.update {
                    HomeTabReducer.reduceForPurchaseSummarySuccess(it, result.data)
                }
                else -> Unit
            }
            when (val result = cashPositionDeferred.await()) {
                is ResultState.Success -> _state.update {
                    HomeTabReducer.reduceForCashPositionSuccess(it, result.data)
                }
                else -> Unit
            }
            isDashboardInFlight = false
        }
    }

    private fun loadDashboard(token: String) {
        if (isDashboardInFlight) return
        viewModelScope.launch {
            isDashboardInFlight = true
            _state.update { HomeTabReducer.reduceForDashboardLoading(it) }
            val filter = periodFilter(_state.value)
            val dashboardDeferred = async { getDashboardUseCase(token) }
            val netProfitDeferred = async { getNetProfitUseCase(token, filter, userRoles) }
            val receivablesAgingDeferred = async { getReceivablesAgingUseCase(token) }
            val purchaseSummaryDeferred = async { getPurchaseSummaryUseCase(token) }
            val cashPositionDeferred = async { getCashPositionUseCase(token) }
            when (val result = dashboardDeferred.await()) {
                is ResultState.Success -> _state.update {
                    HomeTabReducer.reduceForDashboardSuccess(it, result.data)
                }
                is ResultState.Error -> _state.update {
                    HomeTabReducer.reduceForDashboardError(it, result.message, result.isOffline)
                }
                ResultState.Loading -> Unit
            }
            when (val result = netProfitDeferred.await()) {
                is ResultState.Success -> _state.update {
                    HomeTabReducer.reduceForNetProfit(it, result.data.netProfit)
                }
                else -> Unit
            }
            when (val result = receivablesAgingDeferred.await()) {
                is ResultState.Success -> _state.update {
                    HomeTabReducer.reduceForReceivablesAgingSuccess(it, result.data)
                }
                else -> Unit
            }
            when (val result = purchaseSummaryDeferred.await()) {
                is ResultState.Success -> _state.update {
                    HomeTabReducer.reduceForPurchaseSummarySuccess(it, result.data)
                }
                else -> Unit
            }
            when (val result = cashPositionDeferred.await()) {
                is ResultState.Success -> _state.update {
                    HomeTabReducer.reduceForCashPositionSuccess(it, result.data)
                }
                else -> Unit
            }
            isDashboardInFlight = false
        }
    }

    private suspend fun loadNetProfit() {
        val token = observeSessionUseCase().first()?.token.orEmpty()
        if (token.isBlank()) return
        _state.update { HomeTabReducer.reduceForNetProfitLoading(it) }
        val filter = periodFilter(_state.value)
        when (val result = getNetProfitUseCase(token, filter, userRoles)) {
            is ResultState.Success -> _state.update {
                HomeTabReducer.reduceForNetProfit(it, result.data.netProfit)
            }
            is ResultState.Error -> _state.update { it.copy(isNetProfitLoading = false) }
            ResultState.Loading -> Unit
        }
    }

    private fun periodFilter(state: HomeTabUiState): DateRangeFilter {
        val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val today = LocalDate.now()
        return when (state.netProfitPeriod) {
            NetProfitPeriod.THIS_MONTH -> DateRangeFilter(
                fromDate = today.withDayOfMonth(1).format(fmt),
                toDate = today.format(fmt)
            )
            NetProfitPeriod.LAST_MONTH -> {
                val firstOfLastMonth = today.minusMonths(1).withDayOfMonth(1)
                val lastOfLastMonth = firstOfLastMonth.plusMonths(1).minusDays(1)
                DateRangeFilter(
                    fromDate = firstOfLastMonth.format(fmt),
                    toDate = lastOfLastMonth.format(fmt)
                )
            }
            NetProfitPeriod.THIS_YEAR -> DateRangeFilter(
                fromDate = today.withDayOfYear(1).format(fmt),
                toDate = today.format(fmt)
            )
            NetProfitPeriod.CUSTOM -> DateRangeFilter(
                fromDate = state.netProfitCustomFrom,
                toDate = state.netProfitCustomTo
            )
        }
    }

    private fun hasFinancialsAccess(roles: List<String>): Boolean =
        roles.any { it.equals("Admin", ignoreCase = true) || it.equals("Manager", ignoreCase = true) }

    private fun displayNameFromUsername(username: String): String {
        if (username.isBlank()) return ""
        val base = username.substringBefore("@")
            .replace(".", " ")
            .replace("_", " ")
            .replace("-", " ")
            .trim()
        if (base.isBlank()) return username
        return base.split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .joinToString(" ") { part ->
                part.lowercase().replaceFirstChar { c -> c.titlecase() }
            }
    }
}
