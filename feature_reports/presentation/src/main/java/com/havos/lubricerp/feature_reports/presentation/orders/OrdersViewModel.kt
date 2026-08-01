package com.havos.lubricerp.feature_reports.presentation.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.havos.lubricerp.core.common.ResultState
import com.havos.lubricerp.feature_reports.domain.usecase.GetSalesInvoiceDetailUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.GetSalesInvoicesUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.GetSalesOrderDetailUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.GetSalesOrdersUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.ObserveSessionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OrdersViewModel(
    private val getSalesOrdersUseCase: GetSalesOrdersUseCase,
    private val getSalesOrderDetailUseCase: GetSalesOrderDetailUseCase,
    private val getSalesInvoicesUseCase: GetSalesInvoicesUseCase,
    private val getSalesInvoiceDetailUseCase: GetSalesInvoiceDetailUseCase,
    private val observeSessionUseCase: ObserveSessionUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(OrdersUiState())
    val state: StateFlow<OrdersUiState> = _state.asStateFlow()

    init {
        loadAll()
    }

    private fun loadAll() {
        viewModelScope.launch {
            val token = observeSessionUseCase().first()?.token.orEmpty()
            if (token.isBlank()) return@launch
            loadPendingOrders(token)
            loadDispatchedOrders(token)
            loadInvoices(token)
        }
    }

    private fun loadPendingOrders(token: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getSalesOrdersUseCase(token, "Confirmed")) {
                is ResultState.Success -> _state.update {
                    it.copy(isLoading = false, pendingOrders = result.data, isRefreshing = false)
                }
                is ResultState.Error -> _state.update {
                    it.copy(isLoading = false, errorMessage = result.message, isRefreshing = false)
                }
                ResultState.Loading -> {}
            }
        }
    }

    private fun loadDispatchedOrders(token: String) {
        viewModelScope.launch {
            when (val result = getSalesOrdersUseCase(token, "Dispatched")) {
                is ResultState.Success -> _state.update { it.copy(dispatchedOrders = result.data) }
                is ResultState.Error -> {}
                ResultState.Loading -> {}
            }
        }
    }

    private fun loadInvoices(token: String) {
        viewModelScope.launch {
            val s = _state.value
            when (val result = getSalesInvoicesUseCase(
                token = token,
                fromDate = s.invoiceFromDate.ifBlank { null },
                toDate = s.invoiceToDate.ifBlank { null },
                paymentStatus = s.invoiceFilterStatus
            )) {
                is ResultState.Success -> _state.update { it.copy(invoices = result.data) }
                is ResultState.Error -> {}
                ResultState.Loading -> {}
            }
        }
    }

    fun onIntent(intent: OrdersIntent) {
        when (intent) {
            OrdersIntent.Load -> loadAll()
            OrdersIntent.Refresh -> {
                _state.update { it.copy(isRefreshing = true) }
                loadAll()
            }
            OrdersIntent.Retry -> loadAll()
            is OrdersIntent.TabSelected -> _state.update { it.copy(selectedTab = intent.index) }
            is OrdersIntent.OrderClicked -> loadOrderDetail(intent.orderId)
            is OrdersIntent.InvoiceClicked -> loadInvoiceDetail(intent.invoiceId)
            OrdersIntent.DismissDetail -> _state.update {
                it.copy(showOrderDetail = false, showInvoiceDetail = false, selectedOrderDetail = null, selectedInvoiceDetail = null, loadingOrderId = null, loadingInvoiceId = null)
            }
            is OrdersIntent.FromDateChanged -> _state.update { it.copy(invoiceFromDate = intent.value) }
            is OrdersIntent.ToDateChanged -> _state.update { it.copy(invoiceToDate = intent.value) }
            OrdersIntent.ApplyInvoiceFilter -> viewModelScope.launch {
                val token = observeSessionUseCase().first()?.token.orEmpty()
                if (token.isNotBlank()) loadInvoices(token)
            }
        }
    }

    private fun loadOrderDetail(orderId: Long) {
        viewModelScope.launch {
            val token = observeSessionUseCase().first()?.token.orEmpty()
            _state.update { it.copy(loadingOrderId = orderId) }
            when (val result = getSalesOrderDetailUseCase(token, orderId)) {
                is ResultState.Success -> _state.update {
                    it.copy(loadingOrderId = null, showOrderDetail = true, selectedOrderDetail = result.data)
                }
                is ResultState.Error -> _state.update { it.copy(loadingOrderId = null) }
                ResultState.Loading -> {}
            }
        }
    }

    private fun loadInvoiceDetail(invoiceId: Long) {
        viewModelScope.launch {
            val token = observeSessionUseCase().first()?.token.orEmpty()
            _state.update { it.copy(loadingInvoiceId = invoiceId) }
            when (val result = getSalesInvoiceDetailUseCase(token, invoiceId)) {
                is ResultState.Success -> _state.update {
                    it.copy(loadingInvoiceId = null, showInvoiceDetail = true, selectedInvoiceDetail = result.data)
                }
                is ResultState.Error -> _state.update { it.copy(loadingInvoiceId = null) }
                ResultState.Loading -> {}
            }
        }
    }
}
