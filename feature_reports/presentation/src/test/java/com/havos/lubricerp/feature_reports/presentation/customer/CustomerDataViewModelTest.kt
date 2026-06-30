package com.havos.lubricerp.feature_reports.presentation.customer

import com.havos.lubricerp.core.common.PagedResult
import com.havos.lubricerp.core.common.ResultState
import com.havos.lubricerp.core.network.NetworkMonitor
import com.havos.lubricerp.feature_reports.domain.model.AuthSession
import com.havos.lubricerp.feature_reports.domain.model.Customer
import com.havos.lubricerp.feature_reports.domain.model.CustomerLedgerEntry
import com.havos.lubricerp.feature_reports.domain.model.CustomerMobileSummary
import com.havos.lubricerp.feature_reports.domain.usecase.GetCustomerLedgerUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.GetCustomerMobileSummaryUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.GetCustomersUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.ObserveSessionUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.RecordPaymentUseCase
import com.havos.lubricerp.feature_reports.presentation.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class CustomerDataViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val observeSessionUseCase: ObserveSessionUseCase = mock()
    private val getCustomersUseCase: GetCustomersUseCase = mock()
    private val getCustomerLedgerUseCase: GetCustomerLedgerUseCase = mock()
    private val getCustomerMobileSummaryUseCase: GetCustomerMobileSummaryUseCase = mock()
    private val recordPaymentUseCase: RecordPaymentUseCase = mock()
    private val networkMonitor: NetworkMonitor = mock()

    private val testCustomer = Customer(
        id = 1L, name = "Test Customer", code = "C01",
        phone = "12345", email = "t@t.com", address = "Addr",
        gstNumber = "GST01", state = "State"
    )

    private val testMobileSummary = CustomerMobileSummary(
        id = 1L, name = "Test Customer", code = "C01",
        phone = "12345", email = "t@t.com", gstNumber = "GST01",
        address = "Addr", outstandingAmount = 0.0, overdueAmount = 0.0,
        lastPurchaseDate = "", lastInvoiceNumber = "",
        totalLifetimePurchases = 0.0
    )

    private val page1Entries = listOf(
        CustomerLedgerEntry("2026-06-01", "Invoice", "INV001", 100.0, 0.0, 100.0, 1L),
        CustomerLedgerEntry("2026-06-02", "Payment", "RCP001", 0.0, 50.0, 50.0, 0L)
    )

    private val page2Entries = listOf(
        CustomerLedgerEntry("2026-06-03", "Invoice", "INV002", 200.0, 0.0, 250.0, 2L),
        CustomerLedgerEntry("2026-06-04", "Payment", "RCP002", 0.0, 100.0, 150.0, 0L)
    )

    private suspend fun createViewModel(
        ledgerResult: ResultState<PagedResult<CustomerLedgerEntry>>,
        mobileSummaryResult: ResultState<CustomerMobileSummary> = ResultState.Success(testMobileSummary),
        sessionToken: String = "token-123"
    ): CustomerDataViewModel {
        whenever(observeSessionUseCase()).thenReturn(
            flowOf(AuthSession(username = "test", token = sessionToken, refreshToken = "refresh"))
        )
        whenever(getCustomersUseCase(any())).thenReturn(ResultState.Success(listOf(testCustomer)))
        whenever(getCustomerMobileSummaryUseCase(any(), any())).thenReturn(mobileSummaryResult)
        whenever(getCustomerLedgerUseCase(any(), any(), anyOrNull(), anyOrNull(), any(), any())).thenReturn(ledgerResult)
        val onlineFlow = MutableStateFlow(true)
        whenever(networkMonitor.isOnline).thenReturn(onlineFlow)

        return CustomerDataViewModel(
            observeSessionUseCase = observeSessionUseCase,
            getCustomersUseCase = getCustomersUseCase,
            getCustomerLedgerUseCase = getCustomerLedgerUseCase,
            getCustomerMobileSummaryUseCase = getCustomerMobileSummaryUseCase,
            recordPaymentUseCase = recordPaymentUseCase,
            networkMonitor = networkMonitor
        )
    }

    @Test
    fun `loadLedger sends skip=0 take=200 on initial fetch`() = runTest {
        val pagedResult = ResultState.Success(
            PagedResult(items = page1Entries, totalCount = 4, skip = 0, take = 200, hasMore = true)
        )
        val viewModel = createViewModel(ledgerResult = pagedResult)
        advanceUntilIdle()

        viewModel.onIntent(CustomerDataIntent.CustomerSelected(testCustomer))
        advanceUntilIdle()

        verify(getCustomerLedgerUseCase).invoke(any(), eq(1L), anyOrNull(), anyOrNull(), eq(0), eq(200))
        assertEquals(page1Entries, viewModel.state.value.ledgerEntries)
        assertTrue(viewModel.state.value.ledgerHasMore)
    }

    @Test
    fun `loadMoreLedger sends skip=entriesSize take=200 and appends entries`() = runTest {
        val page1 = ResultState.Success(
            PagedResult(items = page1Entries, totalCount = 4, skip = 0, take = 200, hasMore = true)
        )
        val page2 = ResultState.Success(
            PagedResult(items = page2Entries, totalCount = 4, skip = 2, take = 200, hasMore = false)
        )

        val viewModel = createViewModel(ledgerResult = page1)
        advanceUntilIdle()

        var callCount = 0
        whenever(getCustomerLedgerUseCase(any(), any(), anyOrNull(), anyOrNull(), any(), any())).thenAnswer {
            callCount++
            if (callCount == 1) page1 else page2
        }

        viewModel.onIntent(CustomerDataIntent.CustomerSelected(testCustomer))
        advanceUntilIdle()

        assertEquals(page1Entries, viewModel.state.value.ledgerEntries)
        assertTrue(viewModel.state.value.ledgerHasMore)

        viewModel.onIntent(CustomerDataIntent.LoadMoreLedger)
        advanceUntilIdle()

        val expectedEntries = page1Entries + page2Entries
        assertEquals(expectedEntries, viewModel.state.value.ledgerEntries)
        assertFalse(viewModel.state.value.ledgerHasMore)
    }

    @Test
    fun `loadMoreLedger does nothing when hasMore is false`() = runTest {
        val pagedResult = ResultState.Success(
            PagedResult(items = page1Entries, totalCount = 2, skip = 0, take = 200, hasMore = false)
        )
        val viewModel = createViewModel(ledgerResult = pagedResult)
        advanceUntilIdle()

        viewModel.onIntent(CustomerDataIntent.CustomerSelected(testCustomer))
        advanceUntilIdle()

        viewModel.onIntent(CustomerDataIntent.LoadMoreLedger)
        advanceUntilIdle()

        // Should only have been called once (from selectCustomer), not from loadMoreLedger
        verify(getCustomerLedgerUseCase).invoke(any(), any(), anyOrNull(), anyOrNull(), eq(0), eq(200))
        assertEquals(page1Entries, viewModel.state.value.ledgerEntries)
    }

    @Test
    fun `selecting a new customer resets pagination`() = runTest {
        val customer2 = Customer(id = 2L, name = "Customer 2", code = "C02",
            phone = "", email = "", address = "", gstNumber = "", state = ""
        )

        val page1 = ResultState.Success(
            PagedResult(items = page1Entries, totalCount = 4, skip = 0, take = 200, hasMore = true)
        )
        val page2 = ResultState.Success(
            PagedResult(items = page2Entries, totalCount = 4, skip = 0, take = 200, hasMore = false)
        )

        var callCount = 0
        whenever(getCustomerLedgerUseCase(any(), any(), anyOrNull(), anyOrNull(), any(), any())).thenAnswer {
            callCount++
            if (callCount <= 2) page1 else page2
        }
        whenever(getCustomerMobileSummaryUseCase(any(), any())).thenReturn(
            ResultState.Success(testMobileSummary)
        )

        val viewModel = createViewModel(ledgerResult = page1)
        advanceUntilIdle()
        viewModel.onIntent(CustomerDataIntent.CustomerSelected(testCustomer))
        advanceUntilIdle()

        assertEquals(page1Entries, viewModel.state.value.ledgerEntries)

        viewModel.onIntent(CustomerDataIntent.CustomerSelected(customer2))
        advanceUntilIdle()

        // Should fetch with skip=0 for the new customer
        verify(getCustomerLedgerUseCase).invoke(any(), eq(2L), anyOrNull(), anyOrNull(), eq(0), eq(200))
        assertEquals(page1Entries, viewModel.state.value.ledgerEntries)
    }

    @Test
    fun `changing date range resets pagination and fetches with skip=0`() = runTest {
        val pagedResult = ResultState.Success(
            PagedResult(items = page1Entries, totalCount = 2, skip = 0, take = 200, hasMore = false)
        )
        val viewModel = createViewModel(ledgerResult = pagedResult)
        advanceUntilIdle()

        viewModel.onIntent(CustomerDataIntent.CustomerSelected(testCustomer))
        advanceUntilIdle()
        assertEquals(page1Entries, viewModel.state.value.ledgerEntries)

        viewModel.onIntent(CustomerDataIntent.LedgerFromDateChanged("01/06/2026"))
        viewModel.onIntent(CustomerDataIntent.LedgerToDateChanged("30/06/2026"))
        viewModel.onIntent(CustomerDataIntent.LoadLedger)
        advanceUntilIdle()

        // Capturing all calls to verify the last one has skip=0 for the fresh fetch
        val skipCaptor = argumentCaptor<Int>()
        val takeCaptor = argumentCaptor<Int>()
        verify(getCustomerLedgerUseCase, times(2)).invoke(any(), eq(1L), anyOrNull(), anyOrNull(), skipCaptor.capture(), takeCaptor.capture())

        // The last call should have skip=0 (reset)
        val lastIndex = skipCaptor.allValues.size - 1
        assertEquals(0, skipCaptor.allValues[lastIndex])
        assertEquals(200, takeCaptor.allValues[lastIndex])
    }
}
