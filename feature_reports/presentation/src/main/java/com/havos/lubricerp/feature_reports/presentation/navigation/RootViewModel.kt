package com.havos.lubricerp.feature_reports.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.havos.lubricerp.core.common.ResultState
import com.havos.lubricerp.feature_reports.domain.usecase.ObserveSessionUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.RefreshSessionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RootViewModel(
    private val observeSessionUseCase: ObserveSessionUseCase,
    private val refreshSessionUseCase: RefreshSessionUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RootUiState())
    val state: StateFlow<RootUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val initialSession = observeSessionUseCase().first()
            if (initialSession != null && initialSession.refreshToken.isNotBlank()) {
                when (refreshSessionUseCase()) {
                    is ResultState.Success -> { /* session updated, flow below picks it up */ }
                    else -> { /* refresh failed, fall through to observe */ }
                }
            }

            observeSessionUseCase().collect { session ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        isAuthenticated = session != null
                    )
                }
            }
        }
    }
}
