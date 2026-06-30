package com.havos.lubricerp.feature_reports.presentation.proforma

import com.havos.lubricerp.core.common.ResultState
import com.havos.lubricerp.feature_reports.domain.model.AuthSession
import com.havos.lubricerp.feature_reports.domain.model.Customer
import com.havos.lubricerp.feature_reports.domain.model.ProformaInvoice
import com.havos.lubricerp.feature_reports.domain.usecase.GetCustomersUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.GetProformaInvoicesUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.ObserveSessionUseCase
import com.havos.lubricerp.feature_reports.presentation.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class ProformaInvoiceViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val observeSessionUseCase: ObserveSessionUseCase = mock()
    private val getProformaInvoicesUseCase: GetProformaInvoicesUseCase = mock()
    private val getCustomersUseCase: GetCustomersUseCase = mock()

    private val testCustomers = listOf(
        Customer(
            id = 1L,
            name = "Alpha Customer",
            code = "C01",
            phone = "12345",
            email = "alpha@test.com",
            address = "Address 1",
            gstNumber = "123",
            state = "State"
        ),
        Customer(
            id = 2L,
            name = "Beta Customer",
            code = "C02",
            phone = "67890",
            email = "beta@test.com",
            address = "Address 2",
            gstNumber = "456",
            state = "State"
        )
    )

    private val testInvoices = listOf(
        ProformaInvoice(
            id = 1,
            proformaNumber = "PI01",
            date = "2026-06-10T10:00:00",
            validUntilDate = "2026-07-10T00:00:00",
            customerName = "Alpha Customer",
            customerCode = "C01",
            status = "Sent",
            totalAmount = 100.0,
            lineCount = 1,
            soNumber = null,
            salesOrderId = null,
            isInterState = false
        ),
        ProformaInvoice(
            id = 2,
            proformaNumber = "PI02",
            date = "2026-06-15T10:00:00",
            validUntilDate = "2026-07-15T00:00:00",
            customerName = "Beta Customer",
            customerCode = "C02",
            status = "Draft",
            totalAmount = 250.0,
            lineCount = 2,
            soNumber = null,
            salesOrderId = null,
            isInterState = false
        ),
        ProformaInvoice(
            id = 3,
            proformaNumber = "PI03",
            date = "2026-06-18T10:00:00",
            validUntilDate = "2026-07-18T00:00:00",
            customerName = "Gamma Customer",
            customerCode = "C03",
            status = "Converted",
            totalAmount = 150.0,
            lineCount = 1,
            soNumber = "SO-100",
            salesOrderId = 123L,
            isInterState = true
        )
    )

    private fun createViewModel(): ProformaInvoiceViewModel {
        return ProformaInvoiceViewModel(
            observeSessionUseCase = observeSessionUseCase,
            getProformaInvoicesUseCase = getProformaInvoicesUseCase,
            getCustomersUseCase = getCustomersUseCase
        )
    }

    @Test
    fun loadInvoices_success_updatesState() = runTest {
        // Given
        val session = AuthSession(username = "test", token = "token123")
        whenever(observeSessionUseCase()).thenReturn(flowOf(session))
        whenever(getProformaInvoicesUseCase(any(), anyOrNull())).thenReturn(ResultState.Success(testInvoices))
        whenever(getCustomersUseCase(any())).thenReturn(ResultState.Success(testCustomers))

        // When
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertEquals(false, state.isLoading)
        assertEquals(testInvoices.size, state.invoices.size)
        assertEquals(testInvoices.size, state.filteredInvoices.size)
        assertEquals(testCustomers.size, state.customers.size)
        assertEquals(500.0, state.totalAmount, 0.0) // sum of 100, 250, 150
        assertEquals(1, state.convertedCount)
        assertEquals(1, state.sentCount)
    }

    @Test
    fun loadInvoices_error_updatesErrorState() = runTest {
        // Given
        val session = AuthSession(username = "test", token = "token123")
        whenever(observeSessionUseCase()).thenReturn(flowOf(session))
        whenever(getProformaInvoicesUseCase(any(), anyOrNull())).thenReturn(ResultState.Error("Api failed"))
        whenever(getCustomersUseCase(any())).thenReturn(ResultState.Success(testCustomers))

        // When
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertEquals(false, state.isLoading)
        assertEquals("Api failed", state.error)
    }

    @Test
    fun searchChange_filtersInvoices() = runTest {
        // Given
        val session = AuthSession(username = "test", token = "token123")
        whenever(observeSessionUseCase()).thenReturn(flowOf(session))
        whenever(getProformaInvoicesUseCase(any(), anyOrNull())).thenReturn(ResultState.Success(testInvoices))
        whenever(getCustomersUseCase(any())).thenReturn(ResultState.Success(testCustomers))
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.onIntent(ProformaInvoiceIntent.SearchChanged("Alpha"))
        advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertEquals(1, state.filteredInvoices.size)
        assertEquals("PI01", state.filteredInvoices.first().proformaNumber)
    }

    @Test
    fun statusChange_filtersInvoices() = runTest {
        // Given
        val session = AuthSession(username = "test", token = "token123")
        whenever(observeSessionUseCase()).thenReturn(flowOf(session))
        whenever(getProformaInvoicesUseCase(any(), anyOrNull())).thenReturn(ResultState.Success(testInvoices))
        whenever(getCustomersUseCase(any())).thenReturn(ResultState.Success(testCustomers))
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.onIntent(ProformaInvoiceIntent.StatusFilterChanged("Draft"))
        advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertEquals(1, state.filteredInvoices.size)
        assertEquals("PI02", state.filteredInvoices.first().proformaNumber)
    }

    @Test
    fun customerChange_filtersInvoices() = runTest {
        // Given
        val session = AuthSession(username = "test", token = "token123")
        whenever(observeSessionUseCase()).thenReturn(flowOf(session))
        whenever(getProformaInvoicesUseCase(any(), anyOrNull())).thenReturn(ResultState.Success(testInvoices))
        whenever(getCustomersUseCase(any())).thenReturn(ResultState.Success(testCustomers))
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.onIntent(ProformaInvoiceIntent.CustomerFilterChanged("Beta Customer"))
        advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertEquals(1, state.filteredInvoices.size)
        assertEquals("PI02", state.filteredInvoices.first().proformaNumber)
    }

    @Test
    fun sortChange_sortsInvoices() = runTest {
        // Given
        val session = AuthSession(username = "test", token = "token123")
        whenever(observeSessionUseCase()).thenReturn(flowOf(session))
        whenever(getProformaInvoicesUseCase(any(), anyOrNull())).thenReturn(ResultState.Success(testInvoices))
        whenever(getCustomersUseCase(any())).thenReturn(ResultState.Success(testCustomers))
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When (Amount High to Low)
        viewModel.onIntent(ProformaInvoiceIntent.SortTypeChanged(SortType.AMOUNT_DESC))
        advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertEquals("PI02", state.filteredInvoices[0].proformaNumber) // 250
        assertEquals("PI03", state.filteredInvoices[1].proformaNumber) // 150
        assertEquals("PI01", state.filteredInvoices[2].proformaNumber) // 100
    }
}
