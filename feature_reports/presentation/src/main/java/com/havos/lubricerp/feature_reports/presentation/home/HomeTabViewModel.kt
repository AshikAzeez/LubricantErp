package com.havos.lubricerp.feature_reports.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.havos.lubricerp.core.common.ResultState
import com.havos.lubricerp.core.common.isOffline
import com.havos.lubricerp.core.network.NetworkMonitor
import com.havos.lubricerp.feature_reports.domain.usecase.EnsureProfileLoadedUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.GetDashboardUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.ObserveSessionUseCase
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
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private var isDashboardInFlight = false

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
                        _state.update { HomeTabReducer.reduceForUser(it, result.data.fullName) }
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
            when (val result = getDashboardUseCase(token)) {
                is ResultState.Success -> _state.update {
                    HomeTabReducer.reduceForDashboardSuccess(it, result.data).copy(isRefreshing = false)
                }
                is ResultState.Error -> _state.update {
                    HomeTabReducer.reduceForDashboardError(it, result.message, result.isOffline).copy(isRefreshing = false)
                }
                ResultState.Loading -> Unit
            }
            isDashboardInFlight = false
        }
    }

    private fun loadDashboard(token: String) {
        if (isDashboardInFlight) return
        viewModelScope.launch {
            isDashboardInFlight = true
            _state.update { HomeTabReducer.reduceForDashboardLoading(it) }
            when (val result = getDashboardUseCase(token)) {
                is ResultState.Success -> _state.update {
                    HomeTabReducer.reduceForDashboardSuccess(it, result.data)
                }
                is ResultState.Error -> _state.update {
                    HomeTabReducer.reduceForDashboardError(it, result.message, result.isOffline)
                }
                ResultState.Loading -> Unit
            }
            isDashboardInFlight = false
        }
    }

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
