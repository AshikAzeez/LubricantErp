package com.havos.lubricerp.feature_reports.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.havos.lubricerp.core.common.ResultState
import com.havos.lubricerp.core.ui.components.DashboardCardUi
import com.havos.lubricerp.feature_reports.domain.usecase.EnsureProfileLoadedUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.LogoutUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.ObserveProfileUseCase
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

class HomeViewModel(
    observeSessionUseCase: ObserveSessionUseCase,
    observeProfileUseCase: ObserveProfileUseCase,
    private val ensureProfileLoadedUseCase: EnsureProfileLoadedUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(
        HomeUiState(
            cards = ReportMenu.entries.map {
                DashboardCardUi(
                    id = it.key,
                    title = it.title,
                    icon = it.icon
                )
            }
        )
    )
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<HomeEffect>()
    val effect: SharedFlow<HomeEffect> = _effect.asSharedFlow()

    init {
        viewModelScope.launch {
            observeSessionUseCase().collect { session ->
                if (session == null) {
                    _state.update { it.copy(greetingName = "", isProfileLoading = false) }
                    return@collect
                }
                _state.update { HomeReducer.reduceForProfileLoading(it, true) }
                when (val result = ensureProfileLoadedUseCase()) {
                    is ResultState.Success -> {
                        _state.update { HomeReducer.reduceForUser(it, result.data.fullName) }
                    }

                    is ResultState.Error -> {
                        _state.update {
                            HomeReducer.reduceForUser(
                                it,
                                displayNameFromUsername(session.username)
                            )
                        }
                    }

                    ResultState.Loading -> Unit
                }
            }
        }

        viewModelScope.launch {
            observeProfileUseCase().collect { profile ->
                if (profile != null) {
                    _state.update { HomeReducer.reduceForUser(it, profile.fullName) }
                }
            }
        }
    }

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.CardClicked -> {
                val menu = intent.menu
                if (menu.subMenus.size == 1) {
                    viewModelScope.launch {
                        _effect.emit(HomeEffect.OpenReport(menu.subMenus.first()))
                    }
                } else {
                    _state.update { HomeReducer.reduceForMenuSelection(it, menu) }
                }
            }

            HomeIntent.BottomSheetDismissed -> _state.update { HomeReducer.reduceForMenuSelection(it, null) }
            HomeIntent.LogoutClicked -> {
                viewModelScope.launch {
                    logoutUseCase()
                    _effect.emit(HomeEffect.NavigateToLogin)
                }
            }
        }
    }

    fun onSubMenuClicked(item: com.havos.lubricerp.feature_reports.presentation.reports.ReportItem) {
        viewModelScope.launch {
            _state.update { HomeReducer.reduceForMenuSelection(it, null) }
            _effect.emit(HomeEffect.OpenReport(item))
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
