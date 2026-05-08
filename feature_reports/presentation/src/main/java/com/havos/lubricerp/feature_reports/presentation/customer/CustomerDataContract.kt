package com.havos.lubricerp.feature_reports.presentation.customer

import com.havos.lubricerp.core.common.UiIntent
import androidx.compose.runtime.Stable
import com.havos.lubricerp.core.common.UiState
import com.havos.lubricerp.feature_reports.domain.model.Customer
import com.havos.lubricerp.feature_reports.domain.model.CustomerLedgerEntry
import com.havos.lubricerp.feature_reports.domain.model.CustomerMobileSummary

@Stable
data class CustomerDataUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLedgerLoading: Boolean = false,
    val isMobileSummaryLoading: Boolean = false,
    val isOffline: Boolean = false,
    val retryPending: Boolean = false,
    val errorMessage: String? = null,
    val customers: List<Customer> = emptyList(),
    val searchQuery: String = "",
    val selectedCustomer: Customer? = null,
    val mobileSummary: CustomerMobileSummary? = null,
    val ledgerEntries: List<CustomerLedgerEntry> = emptyList(),
    val ledgerFromDate: String = "",
    val ledgerToDate: String = "",
    /** Outstanding amounts cached from previously viewed customers (customerId -> outstanding). */
    val cachedOutstanding: Map<Long, Double> = emptyMap()
) : UiState

sealed interface CustomerDataIntent : UiIntent {
    data object Load : CustomerDataIntent
    data object Refresh : CustomerDataIntent
    data class SearchChanged(val query: String) : CustomerDataIntent
    data class CustomerSelected(val customer: Customer) : CustomerDataIntent
    data object CustomerDismissed : CustomerDataIntent
    data class LedgerFromDateChanged(val date: String) : CustomerDataIntent
    data class LedgerToDateChanged(val date: String) : CustomerDataIntent
    data object LoadLedger : CustomerDataIntent
    data object LoadMobileSummary : CustomerDataIntent
}

sealed interface CustomerDataAction {
    data class SearchChanged(val value: String) : CustomerDataAction
    data class CustomerSelected(val customer: Customer) : CustomerDataAction
    data object CustomerDismissed : CustomerDataAction
    data class CallCustomer(val phone: String) : CustomerDataAction
    data class WhatsAppCustomer(val phone: String) : CustomerDataAction
    data class LedgerFromDateChanged(val date: String) : CustomerDataAction
    data class LedgerToDateChanged(val date: String) : CustomerDataAction
    data object LoadLedger : CustomerDataAction
    data object LoadMobileSummary : CustomerDataAction
    data object Retry : CustomerDataAction
    data object Refresh : CustomerDataAction
}
