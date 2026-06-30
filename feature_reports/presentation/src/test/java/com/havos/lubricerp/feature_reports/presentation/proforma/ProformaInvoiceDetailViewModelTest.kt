package com.havos.lubricerp.feature_reports.presentation.proforma

import com.havos.lubricerp.core.common.ResultState
import com.havos.lubricerp.feature_reports.domain.model.AuthSession
import com.havos.lubricerp.feature_reports.domain.model.ProformaInvoiceDetail
import com.havos.lubricerp.feature_reports.domain.model.ProformaInvoiceLine
import com.havos.lubricerp.feature_reports.domain.usecase.GetProformaInvoiceDetailUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.ObserveSessionUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.SendProformaInvoiceUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.CancelProformaInvoiceUseCase
import com.havos.lubricerp.feature_reports.presentation.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class ProformaInvoiceDetailViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val observeSessionUseCase: ObserveSessionUseCase = mock()
    private val getProformaInvoiceDetailUseCase: GetProformaInvoiceDetailUseCase = mock()
    private val sendProformaInvoiceUseCase: SendProformaInvoiceUseCase = mock()
    private val cancelProformaInvoiceUseCase: CancelProformaInvoiceUseCase = mock()

    private val testDetail = ProformaInvoiceDetail(
        id = 45,
        proformaNumber = "PI-2526-0045",
        date = "2026-05-24",
        validUntilDate = "2026-06-07",
        customerId = 5,
        customerName = "Sunrise Petroleum",
        customerCode = "CUST-005",
        customerGST = "GST123",
        customerAddress = "Address",
        customerState = "Gujarat",
        customerStateCode = "24",
        status = "Draft",
        isInterState = true,
        remarks = "Remarks",
        termsAndConditions = "Terms",
        subTotal = 216101.70,
        cgstAmount = 0.0,
        sgstAmount = 0.0,
        igstAmount = 38898.30,
        roundOffAmount = 0.0,
        totalAmount = 255000.00,
        salesOrderId = null,
        salesOrderNumber = null,
        lines = listOf(
            ProformaInvoiceLine(
                id = 181,
                deliveryType = "Packaged",
                productGradeId = 1,
                productGradeName = "CF4 15W40",
                productSKUId = 2,
                productSKUName = "CF4 15W40 5L",
                hsnCode = "27101980",
                description = "CF4 15W40 Engine Oil 5 Litre",
                quantity = 200.0,
                unitOfMeasurement = "NOS",
                unitPrice = 1150.00,
                discountPercent = 0.00,
                taxRate = 18.00,
                lineSubTotal = 230000.00,
                taxAmount = 41400.00,
                lineTotal = 271400.00
            )
        )
    )

    private fun createViewModel(): ProformaInvoiceDetailViewModel {
        return ProformaInvoiceDetailViewModel(
            observeSessionUseCase = observeSessionUseCase,
            getProformaInvoiceDetailUseCase = getProformaInvoiceDetailUseCase,
            sendProformaInvoiceUseCase = sendProformaInvoiceUseCase,
            cancelProformaInvoiceUseCase = cancelProformaInvoiceUseCase
        )
    }

    @Test
    fun loadDetail_success_updatesState() = runTest {
        // Given
        val session = AuthSession(username = "test", token = "token123")
        whenever(observeSessionUseCase()).thenReturn(flowOf(session))
        whenever(getProformaInvoiceDetailUseCase(eq("token123"), eq(45L))).thenReturn(ResultState.Success(testDetail))

        // When
        val viewModel = createViewModel()
        viewModel.onIntent(ProformaInvoiceDetailIntent.LoadDetail(45L))
        advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertEquals(false, state.isLoading)
        assertEquals(testDetail, state.invoiceDetail)
        assertEquals("PI-2526-0045", state.invoiceDetail?.proformaNumber)
    }

    @Test
    fun loadDetail_error_updatesErrorState() = runTest {
        // Given
        val session = AuthSession(username = "test", token = "token123")
        whenever(observeSessionUseCase()).thenReturn(flowOf(session))
        whenever(getProformaInvoiceDetailUseCase(eq("token123"), eq(45L))).thenReturn(ResultState.Error("Failed to load details"))

        // When
        val viewModel = createViewModel()
        viewModel.onIntent(ProformaInvoiceDetailIntent.LoadDetail(45L))
        advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertEquals(false, state.isLoading)
        assertEquals("Failed to load details", state.error)
    }

    @Test
    fun sendProforma_success_callsUseCaseAndReloadsDetail() = runTest {
        // Given
        val session = AuthSession(username = "test", token = "token123")
        whenever(observeSessionUseCase()).thenReturn(flowOf(session))
        whenever(getProformaInvoiceDetailUseCase(eq("token123"), eq(45L))).thenReturn(ResultState.Success(testDetail))
        whenever(sendProformaInvoiceUseCase(eq("token123"), eq(45L))).thenReturn(ResultState.Success(Unit))

        val viewModel = createViewModel()
        viewModel.onIntent(ProformaInvoiceDetailIntent.LoadDetail(45L))
        advanceUntilIdle()

        // When
        viewModel.onIntent(ProformaInvoiceDetailIntent.SendProforma)
        advanceUntilIdle()

        // Then
        org.mockito.kotlin.verify(sendProformaInvoiceUseCase).invoke(eq("token123"), eq(45L))
        org.mockito.kotlin.verify(getProformaInvoiceDetailUseCase, org.mockito.kotlin.times(2)).invoke(eq("token123"), eq(45L))
    }

    @Test
    fun cancelProforma_success_callsUseCaseAndReloadsDetail() = runTest {
        // Given
        val session = AuthSession(username = "test", token = "token123")
        whenever(observeSessionUseCase()).thenReturn(flowOf(session))
        whenever(getProformaInvoiceDetailUseCase(eq("token123"), eq(45L))).thenReturn(ResultState.Success(testDetail))
        whenever(cancelProformaInvoiceUseCase(eq("token123"), eq(45L))).thenReturn(ResultState.Success(Unit))

        val viewModel = createViewModel()
        viewModel.onIntent(ProformaInvoiceDetailIntent.LoadDetail(45L))
        advanceUntilIdle()

        // When
        viewModel.onIntent(ProformaInvoiceDetailIntent.CancelProforma)
        advanceUntilIdle()

        // Then
        org.mockito.kotlin.verify(cancelProformaInvoiceUseCase).invoke(eq("token123"), eq(45L))
        org.mockito.kotlin.verify(getProformaInvoiceDetailUseCase, org.mockito.kotlin.times(2)).invoke(eq("token123"), eq(45L))
    }
}
