package com.havos.lubricerp.feature_reports.presentation.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.havos.lubricerp.core.common.ResultState
import com.havos.lubricerp.feature_reports.domain.usecase.GetCostBreakdownDetailUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.ObserveSessionUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CostBreakdownDetailViewModel(
    private val observeSessionUseCase: ObserveSessionUseCase,
    private val getCostBreakdownDetailUseCase: GetCostBreakdownDetailUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CostBreakdownDetailUiState())
    val state: StateFlow<CostBreakdownDetailUiState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<CostBreakdownDetailEffect>()
    val effect: SharedFlow<CostBreakdownDetailEffect> = _effect.asSharedFlow()

    fun onIntent(intent: CostBreakdownDetailIntent) {
        when (intent) {
            is CostBreakdownDetailIntent.Load -> load(intent.id)
            is CostBreakdownDetailIntent.Refresh -> load(detailId())
            is CostBreakdownDetailIntent.EditClicked -> viewModelScope.launch { _effect.emit(CostBreakdownDetailEffect.NavigateToEdit) }
            is CostBreakdownDetailIntent.DeleteClicked -> _state.update { it.copy(showDeleteConfirmation = true) }
            is CostBreakdownDetailIntent.ConvertToPiClicked -> viewModelScope.launch {
                val id = detailId()
                if (id > 0) _effect.emit(CostBreakdownDetailEffect.NavigateToConvertToPi(id))
            }
        }
    }

    fun onAction(action: CostBreakdownDetailAction) {
        when (action) {
            CostBreakdownDetailAction.EditClicked -> onIntent(CostBreakdownDetailIntent.EditClicked)
            CostBreakdownDetailAction.DeleteClicked -> onIntent(CostBreakdownDetailIntent.DeleteClicked)
            CostBreakdownDetailAction.ConfirmDelete -> performDelete()
            CostBreakdownDetailAction.DismissDelete -> _state.update { it.copy(showDeleteConfirmation = false) }
            CostBreakdownDetailAction.ConvertToPiClicked -> onIntent(CostBreakdownDetailIntent.ConvertToPiClicked)
            CostBreakdownDetailAction.Refresh -> onIntent(CostBreakdownDetailIntent.Refresh)
        }
    }

    private fun detailId(): Long = _state.value.detail?.id ?: 0L

    private fun load(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            val token = runCatching { observeSessionUseCase().first()?.token.orEmpty() }.getOrElse { "" }
            when (val result = getCostBreakdownDetailUseCase(token, id)) {
                is ResultState.Success -> _state.update { it.copy(isLoading = false, detail = result.data) }
                is ResultState.Error -> _state.update { it.copy(isLoading = false, errorMessage = result.message) }
                ResultState.Loading -> {}
            }
        }
    }

    private fun performDelete() {
        viewModelScope.launch {
            _state.update { it.copy(isDeleting = true) }
            _effect.emit(CostBreakdownDetailEffect.Deleted)
        }
    }
}
