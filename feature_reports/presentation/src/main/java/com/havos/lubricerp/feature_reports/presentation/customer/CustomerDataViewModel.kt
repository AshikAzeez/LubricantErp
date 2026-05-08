package com.havos.lubricerp.feature_reports.presentation.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.havos.lubricerp.core.common.ResultState
import com.havos.lubricerp.core.common.isOffline
import com.havos.lubricerp.core.network.NetworkMonitor
import com.havos.lubricerp.feature_reports.domain.model.Customer
import com.havos.lubricerp.feature_reports.domain.model.CustomerLedgerEntry
import com.havos.lubricerp.feature_reports.domain.model.CustomerMobileSummary
import com.havos.lubricerp.feature_reports.domain.usecase.GetCustomerLedgerUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.GetCustomerMobileSummaryUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.GetCustomersUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.ObserveSessionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CustomerDataViewModel(
    private val observeSessionUseCase: ObserveSessionUseCase,
    private val getCustomersUseCase: GetCustomersUseCase,
    private val getCustomerLedgerUseCase: GetCustomerLedgerUseCase,
    private val getCustomerMobileSummaryUseCase: GetCustomerMobileSummaryUseCase,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _state = MutableStateFlow(CustomerDataUiState())
    val state: StateFlow<CustomerDataUiState> = _state.asStateFlow()

    // ---------------------------------------------------------------------------------
    // Cache — scoped to this ViewModel instance (= CustomerDataScreen on the back stack).
    // Destroyed automatically when the user navigates away from the screen.
    //
    // Set CACHE_ENABLED = false to bypass all cache reads and writes for debugging.
    // MOBILE_SUMMARY_CACHE_MAX / LEDGER_CACHE_MAX cap total entries via LRU eviction.
    // ---------------------------------------------------------------------------------
    private val mobileSummaryCache = lruCache<Long, CustomerMobileSummary>(MOBILE_SUMMARY_CACHE_MAX)

    // Ledger responses are date-range specific: key must include (customerId + fromDate + toDate)
    // to avoid serving data for date-range A when the user later requests date-range B.
    private data class LedgerCacheKey(val customerId: Long, val fromDate: String, val toDate: String)
    private val ledgerCache = lruCache<LedgerCacheKey, List<CustomerLedgerEntry>>(LEDGER_CACHE_MAX)

    // Duplicate-call guard: set while loadCustomers() is in-flight.
    private var isLoadingCustomers = false

    init {
        onIntent(CustomerDataIntent.Load)
        observeConnectivity()
    }

    private fun observeConnectivity() {
        viewModelScope.launch {
            networkMonitor.isOnline
                .distinctUntilChanged()
                .drop(1)
                .filter { online -> online && _state.value.retryPending }
                .collect {
                    _state.update { it.copy(isOffline = false, retryPending = false) }
                    loadCustomers()
                }
        }
    }

    fun onIntent(intent: CustomerDataIntent) {
        when (intent) {
            CustomerDataIntent.Load -> loadCustomers()
            CustomerDataIntent.Refresh -> refresh()
            is CustomerDataIntent.SearchChanged -> _state.update { it.copy(searchQuery = intent.query) }
            is CustomerDataIntent.CustomerSelected -> selectCustomer(intent.customer)
            CustomerDataIntent.CustomerDismissed -> _state.update {
                it.copy(selectedCustomer = null, mobileSummary = null, ledgerEntries = emptyList())
            }
            is CustomerDataIntent.LedgerFromDateChanged -> _state.update { it.copy(ledgerFromDate = intent.date) }
            is CustomerDataIntent.LedgerToDateChanged -> _state.update { it.copy(ledgerToDate = intent.date) }
            CustomerDataIntent.LoadLedger -> {
                _state.value.selectedCustomer?.let { loadLedger(it) }
            }
            CustomerDataIntent.LoadMobileSummary -> {
                _state.value.selectedCustomer?.let { loadMobileSummary(it.id) }
            }
        }
    }

    private fun refresh() {
        if (isLoadingCustomers) {
            _state.update { it.copy(isRefreshing = false) }
            return
        }
        viewModelScope.launch {
            isLoadingCustomers = true
            _state.update { it.copy(isRefreshing = true, errorMessage = null, isOffline = false) }
            val token = observeSessionUseCase().first()?.token.orEmpty()
            if (token.isBlank()) {
                _state.update { it.copy(isRefreshing = false, errorMessage = "Session not available.") }
                isLoadingCustomers = false
                return@launch
            }
            when (val result = getCustomersUseCase(token)) {
                is ResultState.Success -> _state.update {
                    it.copy(isRefreshing = false, customers = result.data, isOffline = false, retryPending = false)
                }
                is ResultState.Error -> _state.update {
                    it.copy(
                        isRefreshing = false,
                        isOffline = result.isOffline,
                        retryPending = result.isOffline,
                        errorMessage = if (result.isOffline) null else result.message
                    )
                }
                ResultState.Loading -> Unit
            }
            isLoadingCustomers = false
        }
    }

    private fun loadCustomers() {
        if (isLoadingCustomers) return
        viewModelScope.launch {
            isLoadingCustomers = true
            _state.update { it.copy(isLoading = true, errorMessage = null, isOffline = false) }
            val token = observeSessionUseCase().first()?.token.orEmpty()
            if (token.isBlank()) {
                _state.update { it.copy(isLoading = false, errorMessage = "Session not available.") }
                isLoadingCustomers = false
                return@launch
            }
            when (val result = getCustomersUseCase(token)) {
                is ResultState.Success -> _state.update {
                    it.copy(isLoading = false, customers = result.data, isOffline = false, retryPending = false)
                }
                is ResultState.Error -> _state.update {
                    it.copy(
                        isLoading = false,
                        isOffline = result.isOffline,
                        retryPending = result.isOffline,
                        errorMessage = if (result.isOffline) null else result.message
                    )
                }
                ResultState.Loading -> Unit
            }
            isLoadingCustomers = false
        }
    }

    private fun selectCustomer(customer: Customer) {
        // On initial open the date fields are blank — that is the key for the auto-load entry.
        val snapshot = _state.value
        val ledgerKey = LedgerCacheKey(
            customerId = customer.id,
            fromDate   = snapshot.ledgerFromDate,
            toDate     = snapshot.ledgerToDate
        )
        val cachedSummary = if (CACHE_ENABLED) mobileSummaryCache[customer.id] else null
        val cachedLedger  = if (CACHE_ENABLED) ledgerCache[ledgerKey]           else null
        _state.update {
            it.copy(
                selectedCustomer       = customer,
                mobileSummary          = cachedSummary,
                ledgerEntries          = cachedLedger ?: emptyList(),
                isMobileSummaryLoading = cachedSummary == null,
                isLedgerLoading        = cachedLedger  == null
            )
        }
        if (cachedSummary == null) loadMobileSummary(customer.id)
        if (cachedLedger  == null) loadLedger(customer)
    }

    private fun loadMobileSummary(customerId: Long) {
        viewModelScope.launch {
            val token = observeSessionUseCase().first()?.token.orEmpty()
            when (val result = getCustomerMobileSummaryUseCase(token, customerId)) {
                is ResultState.Success -> {
                    if (CACHE_ENABLED) mobileSummaryCache[customerId] = result.data
                    _state.update {
                        it.copy(
                            isMobileSummaryLoading = false,
                            mobileSummary = result.data,
                            cachedOutstanding = it.cachedOutstanding + (customerId to result.data.outstandingAmount)
                        )
                    }
                }
                is ResultState.Error -> _state.update { it.copy(isMobileSummaryLoading = false) }
                ResultState.Loading -> Unit
            }
        }
    }

    // Every call (auto-open or explicit Load button) uses the current date fields as part of the
    // cache key, so different date ranges are always stored and retrieved independently.
    private fun loadLedger(customer: Customer) {
        viewModelScope.launch {
            _state.update { it.copy(isLedgerLoading = true) }
            val token = observeSessionUseCase().first()?.token.orEmpty()
            // Snapshot dates at the moment of the actual network call (not before the coroutine starts)
            // to guarantee key consistency between what we send to the API and what we cache.
            val snapshot = _state.value
            val fromDate = snapshot.ledgerFromDate
            val toDate   = snapshot.ledgerToDate
            val ledgerKey = LedgerCacheKey(
                customerId = customer.id,
                fromDate   = fromDate,
                toDate     = toDate
            )
            // Check cache again inside the coroutine — a previous parallel call may have filled it.
            if (CACHE_ENABLED) {
                val alreadyCached = ledgerCache[ledgerKey]
                if (alreadyCached != null) {
                    _state.update { it.copy(isLedgerLoading = false, ledgerEntries = alreadyCached) }
                    return@launch
                }
            }
            when (val result = getCustomerLedgerUseCase(
                token      = token,
                customerId = customer.id,
                fromDate   = fromDate.ifBlank { null },
                toDate     = toDate.ifBlank   { null }
            )) {
                is ResultState.Success -> {
                    if (CACHE_ENABLED) ledgerCache[ledgerKey] = result.data
                    _state.update { it.copy(isLedgerLoading = false, ledgerEntries = result.data) }
                }
                is ResultState.Error -> _state.update {
                    it.copy(isLedgerLoading = false, ledgerEntries = emptyList())
                }
                ResultState.Loading -> Unit
            }
        }
    }

    companion object {
        /** Set to false to disable all caching (e.g. for debug / QA builds). */
        private const val CACHE_ENABLED = true

        /** Max mobile-summary entries kept in memory. Each entry ≈ 200 bytes. */
        private const val MOBILE_SUMMARY_CACHE_MAX = 100

        /**
         * Max ledger entries kept in memory. Ledger lists can be large;
         * keeping at most 30 unique (customer × date-range) responses limits
         * worst-case memory to a few MB even with large ledger payloads.
         */
        private const val LEDGER_CACHE_MAX = 30

        /**
         * Returns a LinkedHashMap configured for LRU eviction: once [maxSize] entries
         * are held, the least-recently-accessed entry is removed before a new one is inserted.
         * Uses only stdlib — no extra dependency required.
         */
        private fun <K, V> lruCache(maxSize: Int): LinkedHashMap<K, V> =
            object : LinkedHashMap<K, V>(maxSize, 0.75f, /* accessOrder= */ true) {
                override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>): Boolean =
                    size > maxSize
            }
    }
}
