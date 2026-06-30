package com.havos.lubricerp.feature_reports.presentation.proforma

import com.havos.lubricerp.core.common.UiIntent
import com.havos.lubricerp.core.common.UiState
import com.havos.lubricerp.feature_reports.domain.model.ProformaInvoice
import androidx.compose.runtime.Stable

sealed interface ProformaInvoiceIntent : UiIntent {
    data object LoadInvoices : ProformaInvoiceIntent
    data object Refresh : ProformaInvoiceIntent
    data class SearchChanged(val query: String) : ProformaInvoiceIntent
    data class StatusFilterChanged(val status: String?) : ProformaInvoiceIntent
    data class DateFilterChanged(val fromDate: String?, val toDate: String?) : ProformaInvoiceIntent
    data class SortTypeChanged(val sortType: SortType) : ProformaInvoiceIntent
    data class CustomerFilterChanged(val customerName: String?) : ProformaInvoiceIntent
}

enum class SortType(val title: String) {
    DATE_DESC("Date (Newest)"),
    DATE_ASC("Date (Oldest)"),
    AMOUNT_DESC("Amount (High to Low)"),
    AMOUNT_ASC("Amount (Low to High)"),
    CUSTOMER_ASC("Customer Name (A-Z)")
}

@Stable
data class ProformaInvoiceUiState(
    val invoices: List<ProformaInvoice> = emptyList(),
    val filteredInvoices: List<ProformaInvoice> = emptyList(),
    val customers: List<com.havos.lubricerp.feature_reports.domain.model.Customer> = emptyList(),
    val selectedCustomerName: String? = null, // null means "All Customers"
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isOffline: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val selectedStatus: String? = null, // null means "All"
    val fromDate: String? = null,
    val toDate: String? = null,
    val sortType: SortType = SortType.DATE_DESC,
    
    // Quick statistics
    val totalAmount: Double = 0.0,
    val sentCount: Int = 0,
    val convertedCount: Int = 0
) : UiState

sealed interface ProformaInvoiceEffect {
    data class ShowToast(val message: String) : ProformaInvoiceEffect
    data class OpenSalesOrder(val salesOrderId: Long) : ProformaInvoiceEffect
}
