package com.havos.lubricerp.feature_reports.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.havos.lubricerp.core.common.ResultState
import com.havos.lubricerp.feature_reports.domain.usecase.EnsureProfileLoadedUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.ObserveSessionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeTabViewModel(
    observeSessionUseCase: ObserveSessionUseCase,
    private val ensureProfileLoadedUseCase: EnsureProfileLoadedUseCase
) : ViewModel() {

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
            }
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
