package com.havos.lubricerp.feature_reports.presentation.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.havos.lubricerp.core.common.ResultState
import com.havos.lubricerp.feature_reports.domain.model.Customer
import com.havos.lubricerp.feature_reports.domain.usecase.GetCustomerLedgerUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.GetCustomersUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.ObserveSessionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CustomerDataViewModel(
    private val observeSessionUseCase: ObserveSessionUseCase,
    private val getCustomersUseCase: GetCustomersUseCase,
    private val getCustomerLedgerUseCase: GetCustomerLedgerUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CustomerDataUiState())
    val state: StateFlow<CustomerDataUiState> = _state.asStateFlow()

    init {
        onIntent(CustomerDataIntent.Load)
    }

    fun onIntent(intent: CustomerDataIntent) {
        when (intent) {
            CustomerDataIntent.Load -> loadCustomers()
            is CustomerDataIntent.SearchChanged -> _state.update { it.copy(searchQuery = intent.query) }
            is CustomerDataIntent.CustomerSelected -> selectCustomer(intent.customer)
            CustomerDataIntent.CustomerDismissed -> _state.update {
                it.copy(selectedCustomer = null, ledgerEntries = emptyList())
            }
            is CustomerDataIntent.LedgerFromDateChanged -> _state.update { it.copy(ledgerFromDate = intent.date) }
            is CustomerDataIntent.LedgerToDateChanged -> _state.update { it.copy(ledgerToDate = intent.date) }
            CustomerDataIntent.LoadLedger -> {
                _state.value.selectedCustomer?.let { loadLedger(it) }
            }
        }
    }

    private fun loadCustomers() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            val token = observeSessionUseCase().first()?.token.orEmpty()
            if (token.isBlank()) {
                _state.update { it.copy(isLoading = false, errorMessage = "Session not available.") }
                return@launch
            }
            when (val result = getCustomersUseCase(token)) {
                is ResultState.Success -> _state.update {
                    it.copy(isLoading = false, customers = result.data)
                }
                is ResultState.Error -> _state.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
                ResultState.Loading -> Unit
            }
        }
    }

    private fun selectCustomer(customer: Customer) {
        _state.update { it.copy(selectedCustomer = customer, ledgerEntries = emptyList()) }
        loadLedger(customer)
    }

    private fun loadLedger(customer: Customer) {
        viewModelScope.launch {
            _state.update { it.copy(isLedgerLoading = true) }
            val token = observeSessionUseCase().first()?.token.orEmpty()
            val from = _state.value.ledgerFromDate.ifBlank { null }
            val to = _state.value.ledgerToDate.ifBlank { null }
            when (val result = getCustomerLedgerUseCase(token, customer.id, from, to)) {
                is ResultState.Success -> _state.update {
                    it.copy(isLedgerLoading = false, ledgerEntries = result.data)
                }
                is ResultState.Error -> _state.update {
                    it.copy(isLedgerLoading = false, ledgerEntries = emptyList())
                }
                ResultState.Loading -> Unit
            }
        }
    }
}
