package com.havos.lubricerp.feature_reports.presentation.proforma

import com.havos.lubricerp.core.common.UiIntent
import com.havos.lubricerp.core.common.UiState
import com.havos.lubricerp.feature_reports.domain.model.Customer
import com.havos.lubricerp.feature_reports.domain.model.CreateProformaInvoiceLine
import com.havos.lubricerp.feature_reports.domain.model.ProductSku
import androidx.compose.runtime.Stable

sealed interface CreateProformaInvoiceIntent : UiIntent {
    data object LoadCustomers : CreateProformaInvoiceIntent
    data object LoadProducts : CreateProformaInvoiceIntent
    data class LoadInvoiceForEdit(val id: Long) : CreateProformaInvoiceIntent
    data class CustomerSelected(val customer: Customer?) : CreateProformaInvoiceIntent
    data class ProformaDateChanged(val date: String) : CreateProformaInvoiceIntent
    data class ValidUntilDateChanged(val date: String) : CreateProformaInvoiceIntent
    data class RemarksChanged(val remarks: String) : CreateProformaInvoiceIntent
    data class TermsChanged(val terms: String) : CreateProformaInvoiceIntent
    
    // Line item management
    data class AddLineItem(val line: CreateProformaInvoiceLine) : CreateProformaInvoiceIntent
    data class RemoveLineItem(val index: Int) : CreateProformaInvoiceIntent
    
    data object Submit : CreateProformaInvoiceIntent
}

@Stable
data class CreateProformaInvoiceUiState(
    val customers: List<Customer> = emptyList(),
    val selectedCustomer: Customer? = null,
    val products: List<ProductSku> = emptyList(),
    val proformaDate: String = "",
    val validUntilDate: String = "",
    val remarks: String = "",
    val termsAndConditions: String = "",
    val lines: List<CreateProformaInvoiceLine> = emptyList(),
    val editingInvoiceId: Long? = null,
    
    val isLoading: Boolean = false,
    val isLoadingProducts: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
) : UiState

sealed interface CreateProformaInvoiceEffect {
    data class ShowToast(val message: String) : CreateProformaInvoiceEffect
    data object NavigateBack : CreateProformaInvoiceEffect
}
