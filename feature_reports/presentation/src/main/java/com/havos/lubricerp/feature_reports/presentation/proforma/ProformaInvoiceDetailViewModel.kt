package com.havos.lubricerp.feature_reports.presentation.proforma

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.havos.lubricerp.core.common.ResultState
import com.havos.lubricerp.feature_reports.domain.usecase.GetProformaInvoiceDetailUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.ObserveSessionUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.SendProformaInvoiceUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.CancelProformaInvoiceUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProformaInvoiceDetailViewModel(
    private val observeSessionUseCase: ObserveSessionUseCase,
    private val getProformaInvoiceDetailUseCase: GetProformaInvoiceDetailUseCase,
    private val sendProformaInvoiceUseCase: SendProformaInvoiceUseCase,
    private val cancelProformaInvoiceUseCase: CancelProformaInvoiceUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ProformaInvoiceDetailUiState())
    val state: StateFlow<ProformaInvoiceDetailUiState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<ProformaInvoiceDetailEffect>()
    val effect: SharedFlow<ProformaInvoiceDetailEffect> = _effect.asSharedFlow()

    private var currentInvoiceId: Long? = null

    fun onIntent(intent: ProformaInvoiceDetailIntent) {
        when (intent) {
            is ProformaInvoiceDetailIntent.LoadDetail -> {
                currentInvoiceId = intent.id
                loadDetail(intent.id, false)
            }
            ProformaInvoiceDetailIntent.Refresh -> {
                currentInvoiceId?.let { loadDetail(it, true) }
            }
            ProformaInvoiceDetailIntent.SendProforma -> sendProforma()
            ProformaInvoiceDetailIntent.CancelProforma -> cancelProforma()
        }
    }

    private fun sendProforma() {
        val invoiceId = currentInvoiceId ?: return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val session = observeSessionUseCase().first()
            if (session == null) {
                _state.update { it.copy(isLoading = false, error = "Session expired. Please log in again.") }
                return@launch
            }

            when (val result = sendProformaInvoiceUseCase(session.token, invoiceId)) {
                is ResultState.Success -> {
                    _effect.tryEmit(ProformaInvoiceDetailEffect.ShowToast("Invoice marked as Sent"))
                    loadDetail(invoiceId, false)
                }
                is ResultState.Error -> {
                    _state.update { it.copy(isLoading = false, error = result.message) }
                }
                ResultState.Loading -> Unit
            }
        }
    }

    private fun cancelProforma() {
        val invoiceId = currentInvoiceId ?: return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val session = observeSessionUseCase().first()
            if (session == null) {
                _state.update { it.copy(isLoading = false, error = "Session expired. Please log in again.") }
                return@launch
            }

            when (val result = cancelProformaInvoiceUseCase(session.token, invoiceId)) {
                is ResultState.Success -> {
                    _effect.tryEmit(ProformaInvoiceDetailEffect.ShowToast("Invoice cancelled successfully"))
                    loadDetail(invoiceId, false)
                }
                is ResultState.Error -> {
                    _state.update { it.copy(isLoading = false, error = result.message) }
                }
                ResultState.Loading -> Unit
            }
        }
    }

    private fun loadDetail(id: Long, isRefresh: Boolean) {
        viewModelScope.launch {
            if (isRefresh) {
                _state.update { it.copy(isRefreshing = true, error = null) }
            } else {
                _state.update { it.copy(isLoading = true, error = null) }
            }

            val session = observeSessionUseCase().first()
            if (session == null) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = "Session expired. Please log in again."
                    )
                }
                return@launch
            }

            when (val result = getProformaInvoiceDetailUseCase(session.token, id)) {
                is ResultState.Success -> {
                    _state.update {
                        it.copy(
                            invoiceDetail = result.data,
                            isLoading = false,
                            isRefreshing = false,
                            isOffline = false,
                            error = null
                        )
                    }
                }
                is ResultState.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            isOffline = result.networkErrorKind != null,
                            error = result.message
                        )
                    }
                }
                ResultState.Loading -> Unit
            }
        }
    }
}
