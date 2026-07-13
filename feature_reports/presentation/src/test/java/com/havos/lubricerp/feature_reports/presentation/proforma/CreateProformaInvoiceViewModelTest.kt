package com.havos.lubricerp.feature_reports.presentation.proforma

import com.havos.lubricerp.core.common.ResultState
import com.havos.lubricerp.feature_reports.domain.model.*
import com.havos.lubricerp.feature_reports.domain.usecase.CreateProformaInvoiceUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.GetProformaInvoiceDetailUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.UpdateProformaInvoiceUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.GetCustomersUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.GetProductSkusUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.ObserveSessionUseCase
import com.havos.lubricerp.feature_reports.presentation.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class CreateProformaInvoiceViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val observeSessionUseCase: ObserveSessionUseCase = mock()
    private val getCustomersUseCase: GetCustomersUseCase = mock()
    private val createProformaInvoiceUseCase: CreateProformaInvoiceUseCase = mock()
    private val getProductSkusUseCase: GetProductSkusUseCase = mock()
    private val getProformaInvoiceDetailUseCase: GetProformaInvoiceDetailUseCase = mock()
    private val updateProformaInvoiceUseCase: UpdateProformaInvoiceUseCase = mock()

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
        )
    )

    private val testLine = CreateProformaInvoiceLine(
        deliveryType = "Packaged",
        productGradeId = 1,
        productSKUId = 2,
        hsnCode = "27101980",
        quantity = 10,
        unitPrice = 100.0,
        taxRate = 18.0,
        discountPercent = 0.0
    )

    private val testDetail = ProformaInvoiceDetail(
        id = 45L,
        proformaNumber = "PI-2526-0045",
        date = "2026-05-24",
        validUntilDate = "2026-06-07",
        customerId = 1L,
        customerName = "Alpha Customer",
        customerCode = "C01",
        customerGST = "GST123",
        customerAddress = "Address 1",
        customerState = "State",
        customerStateCode = "29",
        status = "Draft",
        isInterState = false,
        remarks = "Remarks 1",
        termsAndConditions = "Terms 1",
        subTotal = 100.0,
        cgstAmount = 9.0,
        sgstAmount = 9.0,
        igstAmount = 0.0,
        roundOffAmount = 0.0,
        totalAmount = 118.0,
        salesOrderId = null,
        salesOrderNumber = null,
        lines = listOf(
            ProformaInvoiceLine(
                id = 1L,
                deliveryType = "Packaged",
                productGradeId = 1,
                productGradeName = "Grade",
                productSKUId = 2,
                productSKUName = "SKU",
                hsnCode = "27101980",
                description = "Test Product Description",
        quantity = 10.0,
                unitOfMeasurement = "NOS",
                unitPrice = 10.0,
                discountPercent = 0.0,
                taxRate = 18.0,
                lineSubTotal = 100.0,
                taxAmount = 18.0,
                lineTotal = 118.0
            )
        )
    )

    private suspend fun createViewModel(): CreateProformaInvoiceViewModel {
        whenever(getProductSkusUseCase(any(), anyOrNull())).thenReturn(ResultState.Success(emptyList()))
        return CreateProformaInvoiceViewModel(
            observeSessionUseCase = observeSessionUseCase,
            getCustomersUseCase = getCustomersUseCase,
            createProformaInvoiceUseCase = createProformaInvoiceUseCase,
            getProductSkusUseCase = getProductSkusUseCase,
            getProformaInvoiceDetailUseCase = getProformaInvoiceDetailUseCase,
            updateProformaInvoiceUseCase = updateProformaInvoiceUseCase
        )
    }

    @Test
    fun init_loadsCustomers_success() = runTest {
        // Given
        val session = AuthSession(username = "test", token = "token123")
        whenever(observeSessionUseCase()).thenReturn(flowOf(session))
        whenever(getCustomersUseCase(any())).thenReturn(ResultState.Success(testCustomers))

        // When
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertEquals(false, state.isLoading)
        assertEquals(testCustomers, state.customers)
        assertNotNull(state.proformaDate)
        assertNotNull(state.validUntilDate)
    }

    @Test
    fun init_loadsCustomers_error() = runTest {
        // Given
        val session = AuthSession(username = "test", token = "token123")
        whenever(observeSessionUseCase()).thenReturn(flowOf(session))
        whenever(getCustomersUseCase(any())).thenReturn(ResultState.Error("Failed to fetch customers"))

        // When
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertEquals(false, state.isLoading)
        assertEquals("Failed to fetch customers", state.error)
    }

    @Test
    fun customerSelected_updatesState() = runTest {
        // Given
        val session = AuthSession(username = "test", token = "token123")
        whenever(observeSessionUseCase()).thenReturn(flowOf(session))
        whenever(getCustomersUseCase(any())).thenReturn(ResultState.Success(testCustomers))
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.onIntent(CreateProformaInvoiceIntent.CustomerSelected(testCustomers.first()))

        // Then
        assertEquals(testCustomers.first(), viewModel.state.value.selectedCustomer)
    }

    @Test
    fun remarksChanged_withinLimit_updatesState() = runTest {
        // Given
        val session = AuthSession(username = "test", token = "token123")
        whenever(observeSessionUseCase()).thenReturn(flowOf(session))
        whenever(getCustomersUseCase(any())).thenReturn(ResultState.Success(testCustomers))
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.onIntent(CreateProformaInvoiceIntent.RemarksChanged("Valid remarks"))

        // Then
        assertEquals("Valid remarks", viewModel.state.value.remarks)
    }

    @Test
    fun remarksChanged_exceedLimit_showsToastEffect() = runTest {
        // Given
        val session = AuthSession(username = "test", token = "token123")
        whenever(observeSessionUseCase()).thenReturn(flowOf(session))
        whenever(getCustomersUseCase(any())).thenReturn(ResultState.Success(testCustomers))
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When
        val longRemarks = "a".repeat(101)
        viewModel.onIntent(CreateProformaInvoiceIntent.RemarksChanged(longRemarks))

        // Then
        // The state should not be updated with the invalid value
        assertEquals("", viewModel.state.value.remarks)
    }

    @Test
    fun termsChanged_exceedLimit_showsToastEffect() = runTest {
        // Given
        val session = AuthSession(username = "test", token = "token123")
        whenever(observeSessionUseCase()).thenReturn(flowOf(session))
        whenever(getCustomersUseCase(any())).thenReturn(ResultState.Success(testCustomers))
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When
        val longTerms = "a".repeat(501)
        viewModel.onIntent(CreateProformaInvoiceIntent.TermsChanged(longTerms))

        // Then
        // The state should not be updated with the invalid value
        assertEquals("", viewModel.state.value.termsAndConditions)
    }

    @Test
    fun addLineItem_updatesStateLines() = runTest {
        // Given
        val session = AuthSession(username = "test", token = "token123")
        whenever(observeSessionUseCase()).thenReturn(flowOf(session))
        whenever(getCustomersUseCase(any())).thenReturn(ResultState.Success(testCustomers))
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.onIntent(CreateProformaInvoiceIntent.AddLineItem(testLine))

        // Then
        assertEquals(1, viewModel.state.value.lines.size)
        assertEquals(testLine, viewModel.state.value.lines.first())
    }

    @Test
    fun removeLineItem_updatesStateLines() = runTest {
        // Given
        val session = AuthSession(username = "test", token = "token123")
        whenever(observeSessionUseCase()).thenReturn(flowOf(session))
        whenever(getCustomersUseCase(any())).thenReturn(ResultState.Success(testCustomers))
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onIntent(CreateProformaInvoiceIntent.AddLineItem(testLine))
        assertEquals(1, viewModel.state.value.lines.size)

        // When
        viewModel.onIntent(CreateProformaInvoiceIntent.RemoveLineItem(0))

        // Then
        assertEquals(0, viewModel.state.value.lines.size)
    }

    @Test
    fun submit_failsWhenNoCustomerSelected() = runTest {
        // Given
        val session = AuthSession(username = "test", token = "token123")
        whenever(observeSessionUseCase()).thenReturn(flowOf(session))
        whenever(getCustomersUseCase(any())).thenReturn(ResultState.Success(testCustomers))
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onIntent(CreateProformaInvoiceIntent.AddLineItem(testLine))

        // When
        viewModel.onIntent(CreateProformaInvoiceIntent.Submit)

        // Then
        assertEquals("Please select a customer.", viewModel.state.value.error)
    }

    @Test
    fun submit_failsWhenNoLineItems() = runTest {
        // Given
        val session = AuthSession(username = "test", token = "token123")
        whenever(observeSessionUseCase()).thenReturn(flowOf(session))
        whenever(getCustomersUseCase(any())).thenReturn(ResultState.Success(testCustomers))
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onIntent(CreateProformaInvoiceIntent.CustomerSelected(testCustomers.first()))

        // When
        viewModel.onIntent(CreateProformaInvoiceIntent.Submit)

        // Then
        assertEquals("Please add at least one line item.", viewModel.state.value.error)
    }

    @Test
    fun submit_success_callsUseCaseAndTriggersEffect() = runTest {
        // Given
        val session = AuthSession(username = "test", token = "token123")
        whenever(observeSessionUseCase()).thenReturn(flowOf(session))
        whenever(getCustomersUseCase(any())).thenReturn(ResultState.Success(testCustomers))
        val response = CreateProformaInvoiceResponse(id = 45, proformaNumber = "PI-2526-0045")
        whenever(createProformaInvoiceUseCase(any(), any())).thenReturn(ResultState.Success(response))

        val viewModel = createViewModel()
        advanceUntilIdle()
        
        viewModel.onIntent(CreateProformaInvoiceIntent.CustomerSelected(testCustomers.first()))
        viewModel.onIntent(CreateProformaInvoiceIntent.AddLineItem(testLine))
        viewModel.onIntent(CreateProformaInvoiceIntent.RemarksChanged("As per inquiry"))
        viewModel.onIntent(CreateProformaInvoiceIntent.TermsChanged("Payment within 14 days."))

        // When
        viewModel.onIntent(CreateProformaInvoiceIntent.Submit)
        advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertNull(state.error)
        assertEquals("Proforma Invoice created successfully!", state.successMessage)

        // Verify the use case was invoked with the correct token
        verify(createProformaInvoiceUseCase).invoke(eq("token123"), any())
    }

    @Test
    fun loadInvoiceForEdit_success_populatesState() = runTest {
        // Given
        val session = AuthSession(username = "test", token = "token123")
        whenever(observeSessionUseCase()).thenReturn(flowOf(session))
        whenever(getCustomersUseCase(any())).thenReturn(ResultState.Success(testCustomers))
        whenever(getProformaInvoiceDetailUseCase(any(), any())).thenReturn(ResultState.Success(testDetail))

        val viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.onIntent(CreateProformaInvoiceIntent.LoadInvoiceForEdit(45L))
        advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertEquals(false, state.isLoading)
        assertEquals(45L, state.editingInvoiceId)
        assertEquals("2026-05-24", state.proformaDate)
        assertEquals("2026-06-07", state.validUntilDate)
        assertEquals("Remarks 1", state.remarks)
        assertEquals("Terms 1", state.termsAndConditions)
        assertEquals(1, state.lines.size)
        assertEquals("Packaged", state.lines.first().deliveryType)
    }

    @Test
    fun submit_editMode_callsUpdateUseCaseAndTriggersEffect() = runTest {
        // Given
        val session = AuthSession(username = "test", token = "token123")
        whenever(observeSessionUseCase()).thenReturn(flowOf(session))
        whenever(getCustomersUseCase(any())).thenReturn(ResultState.Success(testCustomers))
        whenever(getProformaInvoiceDetailUseCase(any(), any())).thenReturn(ResultState.Success(testDetail))
        whenever(updateProformaInvoiceUseCase(any(), any(), any())).thenReturn(ResultState.Success(Unit))

        val viewModel = createViewModel()
        advanceUntilIdle()
        
        viewModel.onIntent(CreateProformaInvoiceIntent.LoadInvoiceForEdit(45L))
        advanceUntilIdle()

        // When
        viewModel.onIntent(CreateProformaInvoiceIntent.Submit)
        advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertNull(state.error)
        assertEquals("Proforma Invoice updated successfully!", state.successMessage)
        verify(updateProformaInvoiceUseCase).invoke(eq("token123"), eq(45L), any())
    }
}
