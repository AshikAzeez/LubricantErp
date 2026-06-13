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
import kotlinx.coroutines.runBlocking

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

    private suspend fun getTokenSuspend(): String = observeSessionUseCase().first()?.token.orEmpty()

    private fun getToken(): String = runBlocking { getTokenSuspend() }

    private fun loadAll() {
        val token = getToken()
        if (token.isBlank()) return
        loadPendingOrders(token)
        loadDispatchedOrders(token)
        loadInvoices(token)
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
                it.copy(showOrderDetail = false, showInvoiceDetail = false, selectedOrderDetail = null, selectedInvoiceDetail = null)
            }
            is OrdersIntent.FromDateChanged -> _state.update { it.copy(invoiceFromDate = intent.value) }
            is OrdersIntent.ToDateChanged -> _state.update { it.copy(invoiceToDate = intent.value) }
            OrdersIntent.ApplyInvoiceFilter -> loadInvoices(getToken())
        }
    }

    private fun loadOrderDetail(orderId: Long) {
        val token = getToken()
        viewModelScope.launch {
            _state.update { it.copy(showOrderDetail = true) }
            when (val result = getSalesOrderDetailUseCase(token, orderId)) {
                is ResultState.Success -> _state.update { it.copy(selectedOrderDetail = result.data) }
                is ResultState.Error -> _state.update { it.copy(showOrderDetail = false) }
                ResultState.Loading -> {}
            }
        }
    }

    private fun loadInvoiceDetail(invoiceId: Long) {
        val token = getToken()
        viewModelScope.launch {
            _state.update { it.copy(showInvoiceDetail = true) }
            when (val result = getSalesInvoiceDetailUseCase(token, invoiceId)) {
                is ResultState.Success -> _state.update { it.copy(selectedInvoiceDetail = result.data) }
                is ResultState.Error -> _state.update { it.copy(showInvoiceDetail = false) }
                ResultState.Loading -> {}
            }
        }
    }
}
