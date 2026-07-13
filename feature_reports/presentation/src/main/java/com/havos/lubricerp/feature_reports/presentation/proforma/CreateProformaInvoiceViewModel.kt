package com.havos.lubricerp.feature_reports.presentation.proforma

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.havos.lubricerp.core.common.ResultState
import com.havos.lubricerp.feature_reports.domain.model.Customer
import com.havos.lubricerp.feature_reports.domain.model.CreateProformaInvoiceLine
import com.havos.lubricerp.feature_reports.domain.model.CreateProformaInvoiceRequest
import com.havos.lubricerp.feature_reports.domain.usecase.CreateProformaInvoiceUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.GetCustomersUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.GetProductSkusUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.GetProformaInvoiceDetailUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.UpdateProformaInvoiceUseCase
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CreateProformaInvoiceViewModel(
    private val observeSessionUseCase: ObserveSessionUseCase,
    private val getCustomersUseCase: GetCustomersUseCase,
    private val createProformaInvoiceUseCase: CreateProformaInvoiceUseCase,
    private val getProductSkusUseCase: GetProductSkusUseCase,
    private val getProformaInvoiceDetailUseCase: GetProformaInvoiceDetailUseCase,
    private val updateProformaInvoiceUseCase: UpdateProformaInvoiceUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CreateProformaInvoiceUiState())
    val state: StateFlow<CreateProformaInvoiceUiState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<CreateProformaInvoiceEffect>(extraBufferCapacity = 2)
    val effect: SharedFlow<CreateProformaInvoiceEffect> = _effect.asSharedFlow()

    init {
        // Automatically default dates to today and +14 days respectively
        val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val twoWeeksLater = LocalDate.now().plusDays(14).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        _state.update {
            it.copy(
                proformaDate = today,
                validUntilDate = twoWeeksLater
            )
        }
        loadInitialData()
    }

    fun onIntent(intent: CreateProformaInvoiceIntent) {
        when (intent) {
            CreateProformaInvoiceIntent.LoadCustomers -> loadInitialData()
            CreateProformaInvoiceIntent.LoadProducts -> loadInitialData()
            is CreateProformaInvoiceIntent.LoadInvoiceForEdit -> loadInvoiceForEdit(intent.id)
            is CreateProformaInvoiceIntent.CustomerSelected -> {
                _state.update { it.copy(selectedCustomer = intent.customer, error = null) }
            }
            is CreateProformaInvoiceIntent.ProformaDateChanged -> {
                _state.update { it.copy(proformaDate = intent.date, error = null) }
            }
            is CreateProformaInvoiceIntent.ValidUntilDateChanged -> {
                _state.update { it.copy(validUntilDate = intent.date, error = null) }
            }
            is CreateProformaInvoiceIntent.RemarksChanged -> {
                if (intent.remarks.length <= 100) {
                    _state.update { it.copy(remarks = intent.remarks, error = null) }
                } else {
                    viewModelScope.launch {
                        _effect.tryEmit(CreateProformaInvoiceEffect.ShowToast("Remarks cannot exceed 100 characters"))
                    }
                }
            }
            is CreateProformaInvoiceIntent.TermsChanged -> {
                if (intent.terms.length <= 500) {
                    _state.update { it.copy(termsAndConditions = intent.terms, error = null) }
                } else {
                    viewModelScope.launch {
                        _effect.tryEmit(CreateProformaInvoiceEffect.ShowToast("Terms cannot exceed 500 characters"))
                    }
                }
            }
            is CreateProformaInvoiceIntent.AddLineItem -> {
                val list = _state.value.lines.toMutableList().apply { add(intent.line) }
                _state.update { it.copy(lines = list, error = null) }
            }
            is CreateProformaInvoiceIntent.RemoveLineItem -> {
                val list = _state.value.lines.toMutableList().apply {
                    if (intent.index in indices) removeAt(intent.index)
                }
                _state.update { it.copy(lines = list) }
            }
            CreateProformaInvoiceIntent.Submit -> submit()
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, isLoadingProducts = true, error = null) }
            val session = observeSessionUseCase().first()
            if (session == null) {
                _state.update { it.copy(isLoading = false, isLoadingProducts = false, error = "Session expired. Log in again.") }
                return@launch
            }

            val customersResult = getCustomersUseCase(session.token)
            val productsResult = getProductSkusUseCase(session.token)

            _state.update { state ->
                var currentError: String? = null
                val customersList = when (customersResult) {
                    is ResultState.Success -> customersResult.data
                    is ResultState.Error -> {
                        currentError = customersResult.message
                        emptyList()
                    }
                    ResultState.Loading -> emptyList()
                }

                val productsList = when (productsResult) {
                    is ResultState.Success -> productsResult.data
                    is ResultState.Error -> {
                        if (currentError == null) currentError = productsResult.message
                        emptyList()
                    }
                    ResultState.Loading -> emptyList()
                }

                state.copy(
                    isLoading = false,
                    isLoadingProducts = false,
                    customers = customersList,
                    products = productsList,
                    error = currentError
                )
            }
        }
    }

    private fun loadInvoiceForEdit(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, editingInvoiceId = id, error = null) }
            val session = observeSessionUseCase().first()
            if (session == null) {
                _state.update { it.copy(isLoading = false, error = "Session expired. Log in again.") }
                return@launch
            }

            val customersResult = getCustomersUseCase(session.token)
            val productsResult = getProductSkusUseCase(session.token)

            val customersList = when (customersResult) {
                is ResultState.Success -> customersResult.data
                else -> emptyList()
            }
            val productsList = when (productsResult) {
                is ResultState.Success -> productsResult.data
                else -> emptyList()
            }

            when (val detailResult = getProformaInvoiceDetailUseCase(session.token, id)) {
                is ResultState.Success -> {
                    val detail = detailResult.data
                    val selectedCustomer = customersList.find { it.id == detail.customerId }
                    val lineList = detail.lines.map { line ->
                        CreateProformaInvoiceLine(
                            deliveryType = line.deliveryType,
                            productGradeId = line.productGradeId,
                            productSKUId = line.productSKUId,
                            hsnCode = line.hsnCode,
                            quantity = line.quantity.toInt(),
                            unitPrice = line.unitPrice,
                            taxRate = line.taxRate,
                            discountPercent = line.discountPercent
                        )
                    }

                    _state.update {
                        it.copy(
                            isLoading = false,
                            isLoadingProducts = false,
                            customers = customersList,
                            products = productsList,
                            selectedCustomer = selectedCustomer ?: Customer(
                                id = detail.customerId,
                                name = detail.customerName,
                                code = detail.customerCode,
                                gstNumber = detail.customerGST ?: "",
                                state = detail.customerState ?: "",
                                address = detail.customerAddress,
                                phone = "",
                                email = ""
                            ),
                            proformaDate = detail.date.substringBefore("T"),
                            validUntilDate = detail.validUntilDate.substringBefore("T"),
                            remarks = detail.remarks ?: "",
                            termsAndConditions = detail.termsAndConditions ?: "",
                            lines = lineList,
                            error = null
                        )
                    }
                }
                is ResultState.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            customers = customersList,
                            products = productsList,
                            error = detailResult.message
                        )
                    }
                }
                ResultState.Loading -> Unit
            }
        }
    }

    private fun submit() {
        val currentState = _state.value
        if (currentState.selectedCustomer == null) {
            _state.update { it.copy(error = "Please select a customer.") }
            return
        }
        if (currentState.proformaDate.isBlank()) {
            _state.update { it.copy(error = "Please specify proforma date.") }
            return
        }
        if (currentState.validUntilDate.isBlank()) {
            _state.update { it.copy(error = "Please specify validation expiration date.") }
            return
        }
        if (currentState.lines.isEmpty()) {
            _state.update { it.copy(error = "Please add at least one line item.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, error = null) }
            val session = observeSessionUseCase().first()
            if (session == null) {
                _state.update { it.copy(isSubmitting = false, error = "Session expired. Log in again.") }
                return@launch
            }

            val request = CreateProformaInvoiceRequest(
                customerId = currentState.selectedCustomer.id,
                proformaDate = currentState.proformaDate,
                validUntilDate = currentState.validUntilDate,
                remarks = currentState.remarks.takeIf { it.isNotBlank() },
                termsAndConditions = currentState.termsAndConditions.takeIf { it.isNotBlank() },
                lines = currentState.lines
            )

            val invoiceId = currentState.editingInvoiceId
            if (invoiceId != null) {
                when (val result = updateProformaInvoiceUseCase(session.token, invoiceId, request)) {
                    is ResultState.Success -> {
                        _state.update {
                            it.copy(
                                isSubmitting = false,
                                successMessage = "Proforma Invoice updated successfully!"
                            )
                        }
                        _effect.emit(CreateProformaInvoiceEffect.ShowToast("Invoice updated successfully!"))
                        _effect.emit(CreateProformaInvoiceEffect.NavigateBack)
                    }
                    is ResultState.Error -> {
                        _state.update { it.copy(isSubmitting = false, error = result.message) }
                    }
                    ResultState.Loading -> Unit
                }
            } else {
                when (val result = createProformaInvoiceUseCase(session.token, request)) {
                    is ResultState.Success -> {
                        _state.update {
                            it.copy(
                                isSubmitting = false,
                                successMessage = "Proforma Invoice created successfully!"
                            )
                        }
                        _effect.emit(CreateProformaInvoiceEffect.ShowToast("Invoice created successfully!"))
                        _effect.emit(CreateProformaInvoiceEffect.NavigateBack)
                    }
                    is ResultState.Error -> {
                        _state.update { it.copy(isSubmitting = false, error = result.message) }
                    }
                    ResultState.Loading -> Unit
                }
            }
        }
    }
}
