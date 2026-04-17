package com.havos.lubricerp.feature_reports.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.havos.lubricerp.core.common.ResultState
import com.havos.lubricerp.core.ui.components.DashboardCardUi
import com.havos.lubricerp.feature_reports.domain.usecase.EnsureProfileLoadedUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.ObserveSessionUseCase
import com.havos.lubricerp.feature_reports.presentation.reports.ReportMenu
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReportsTabViewModel(
    observeSessionUseCase: ObserveSessionUseCase,
    private val ensureProfileLoadedUseCase: EnsureProfileLoadedUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(
        ReportsTabUiState(
            cards = ReportMenu.entries.map {
                DashboardCardUi(
                    id = it.key,
                    title = it.title,
                    icon = it.icon
                )
            }
        )
    )
    val state: StateFlow<ReportsTabUiState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<ReportsTabEffect>()
    val effect: SharedFlow<ReportsTabEffect> = _effect.asSharedFlow()

    init {
        viewModelScope.launch {
            observeSessionUseCase().collect { session ->
                if (session == null) {
                    _state.update { it.copy(greetingName = "", isProfileLoading = false) }
                    return@collect
                }
                _state.update { ReportsTabReducer.reduceForProfileLoading(it, true) }
                when (val result = ensureProfileLoadedUseCase()) {
                    is ResultState.Success -> {
                        _state.update { ReportsTabReducer.reduceForUser(it, result.data.fullName) }
                    }
                    is ResultState.Error -> {
                        _state.update {
                            ReportsTabReducer.reduceForUser(it, displayNameFromUsername(session.username))
                        }
                    }
                    ResultState.Loading -> Unit
                }
            }
        }
    }

    fun onIntent(intent: ReportsTabIntent) {
        when (intent) {
            is ReportsTabIntent.CardClicked -> {
                val menu = intent.menu
                if (menu.subMenus.size == 1) {
                    viewModelScope.launch {
                        _effect.emit(ReportsTabEffect.OpenReport(menu.subMenus.first()))
                    }
                } else {
                    _state.update { ReportsTabReducer.reduceForMenuSelection(it, menu) }
                }
            }
            ReportsTabIntent.BottomSheetDismissed -> _state.update { ReportsTabReducer.reduceForMenuSelection(it, null) }
        }
    }

    fun onSubMenuClicked(item: com.havos.lubricerp.feature_reports.presentation.reports.ReportItem) {
        viewModelScope.launch {
            _state.update { ReportsTabReducer.reduceForMenuSelection(it, null) }
            _effect.emit(ReportsTabEffect.OpenReport(item))
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
