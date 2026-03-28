package com.havos.lubricerp.feature_reports.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.havos.lubricerp.feature_reports.domain.usecase.ObserveSessionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RootViewModel(
    observeSessionUseCase: ObserveSessionUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RootUiState())
    val state: StateFlow<RootUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
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
