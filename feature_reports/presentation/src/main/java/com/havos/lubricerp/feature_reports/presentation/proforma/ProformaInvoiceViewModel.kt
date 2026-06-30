package com.havos.lubricerp.feature_reports.presentation.proforma

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.havos.lubricerp.core.common.ResultState
import com.havos.lubricerp.feature_reports.domain.usecase.GetCustomersUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.GetProformaInvoicesUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.ObserveSessionUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ProformaInvoiceViewModel(
    private val observeSessionUseCase: ObserveSessionUseCase,
    private val getProformaInvoicesUseCase: GetProformaInvoicesUseCase,
    private val getCustomersUseCase: GetCustomersUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ProformaInvoiceUiState())
    val state: StateFlow<ProformaInvoiceUiState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<ProformaInvoiceEffect>()
    val effect: SharedFlow<ProformaInvoiceEffect> = _effect.asSharedFlow()

    init {
        onIntent(ProformaInvoiceIntent.LoadInvoices)
    }

    fun onIntent(intent: ProformaInvoiceIntent) {
        when (intent) {
            ProformaInvoiceIntent.LoadInvoices -> loadInvoices(false)
            ProformaInvoiceIntent.Refresh -> loadInvoices(true)
            is ProformaInvoiceIntent.SearchChanged -> {
                _state.update { it.copy(searchQuery = intent.query) }
                applyFiltersAndSorting()
            }
            is ProformaInvoiceIntent.StatusFilterChanged -> {
                _state.update { it.copy(selectedStatus = intent.status) }
                applyFiltersAndSorting()
            }
            is ProformaInvoiceIntent.DateFilterChanged -> {
                _state.update { it.copy(fromDate = intent.fromDate, toDate = intent.toDate) }
                applyFiltersAndSorting()
            }
            is ProformaInvoiceIntent.SortTypeChanged -> {
                _state.update { it.copy(sortType = intent.sortType) }
                applyFiltersAndSorting()
            }
            is ProformaInvoiceIntent.CustomerFilterChanged -> {
                _state.update { it.copy(selectedCustomerName = intent.customerName) }
                applyFiltersAndSorting()
            }
        }
    }

    private fun loadInvoices(isRefresh: Boolean) {
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

            val invoicesDeferred = async { getProformaInvoicesUseCase(session.token) }
            val customersDeferred = async { getCustomersUseCase(session.token) }

            val invoicesResult = invoicesDeferred.await()
            val customersResult = customersDeferred.await()

            val customersList = when (customersResult) {
                is ResultState.Success -> customersResult.data
                else -> emptyList()
            }

            when (invoicesResult) {
                is ResultState.Success -> {
                    val invoices = invoicesResult.data
                    _state.update {
                        it.copy(
                            invoices = invoices,
                            customers = customersList,
                            isLoading = false,
                            isRefreshing = false,
                            isOffline = false
                        )
                    }
                    applyFiltersAndSorting()
                }
                is ResultState.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            isOffline = invoicesResult.networkErrorKind != null,
                            error = invoicesResult.message
                        )
                    }
                }
                ResultState.Loading -> Unit
            }
        }
    }

    private fun applyFiltersAndSorting() {
        val currentState = _state.value
        var list = currentState.invoices

        // 1. Search Query (Proforma Number or Customer Name)
        if (currentState.searchQuery.isNotBlank()) {
            list = list.filter {
                it.proformaNumber.contains(currentState.searchQuery, ignoreCase = true) ||
                it.customerName.contains(currentState.searchQuery, ignoreCase = true)
            }
        }

        // 2. Status Filter
        if (currentState.selectedStatus != null) {
            list = list.filter {
                it.status.equals(currentState.selectedStatus, ignoreCase = true)
            }
        }

        // 2b. Customer Filter
        if (!currentState.selectedCustomerName.isNullOrBlank()) {
            list = list.filter {
                it.customerName.equals(currentState.selectedCustomerName, ignoreCase = true)
            }
        }

        // 3. Date Range Filter
        val inputDateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        if (!currentState.fromDate.isNullOrBlank()) {
            try {
                val fromLocalDate = LocalDate.parse(currentState.fromDate, inputDateFmt)
                list = list.filter {
                    val invoiceDate = parseInvoiceDate(it.date) ?: return@filter true
                    !invoiceDate.isBefore(fromLocalDate)
                }
            } catch (_: Exception) {}
        }
        if (!currentState.toDate.isNullOrBlank()) {
            try {
                val toLocalDate = LocalDate.parse(currentState.toDate, inputDateFmt)
                list = list.filter {
                    val invoiceDate = parseInvoiceDate(it.date) ?: return@filter true
                    !invoiceDate.isAfter(toLocalDate)
                }
            } catch (_: Exception) {}
        }

        // 4. Sorting
        list = when (currentState.sortType) {
            SortType.DATE_DESC -> list.sortedByDescending { it.date }
            SortType.DATE_ASC -> list.sortedBy { it.date }
            SortType.AMOUNT_DESC -> list.sortedByDescending { it.totalAmount }
            SortType.AMOUNT_ASC -> list.sortedBy { it.totalAmount }
            SortType.CUSTOMER_ASC -> list.sortedBy { it.customerName }
        }

        // Compute metrics based on total list (global context)
        val totalAmount = currentState.invoices.sumOf { it.totalAmount }
        val sentCount = currentState.invoices.count { it.status.equals("Sent", ignoreCase = true) }
        val convertedCount = currentState.invoices.count { it.status.equals("Converted", ignoreCase = true) }

        _state.update {
            it.copy(
                filteredInvoices = list,
                totalAmount = totalAmount,
                sentCount = sentCount,
                convertedCount = convertedCount
            )
        }
    }

    private fun parseInvoiceDate(dateStr: String): LocalDate? {
        if (dateStr.isBlank()) return null
        return try {
            LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME).toLocalDate()
        } catch (_: Exception) {
            try {
                LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE)
            } catch (_: Exception) {
                null
            }
        }
    }
}
