package com.havos.lubricerp.feature_reports.data.remote.reports

import com.havos.lubricerp.core.common.ResultState
import com.havos.lubricerp.feature_reports.data.dto.AccountsSummaryDto
import com.havos.lubricerp.feature_reports.data.dto.CashPositionDto
import com.havos.lubricerp.feature_reports.data.dto.ConsolidatedStockItemDto
import com.havos.lubricerp.feature_reports.data.dto.CostBreakdownDetailDto
import com.havos.lubricerp.feature_reports.data.dto.CostBreakdownPagedDataDto
import com.havos.lubricerp.feature_reports.data.dto.PurchaseSummaryDto
import com.havos.lubricerp.feature_reports.data.dto.ReceivablesAgingDto
import com.havos.lubricerp.feature_reports.data.dto.CustomerDto
import com.havos.lubricerp.feature_reports.data.dto.CustomerLedgerEntryDto
import com.havos.lubricerp.feature_reports.data.dto.CustomerLedgerPagedDataDto
import com.havos.lubricerp.feature_reports.data.dto.CustomerMobileSummaryDto
import com.havos.lubricerp.feature_reports.data.dto.DashboardDto
import com.havos.lubricerp.feature_reports.data.dto.ExpenseSummaryItemDto
import com.havos.lubricerp.feature_reports.data.dto.FastMovingItemDto
import com.havos.lubricerp.feature_reports.data.dto.LowStockItemDto
import com.havos.lubricerp.feature_reports.data.dto.NetProfitReportDto
import com.havos.lubricerp.feature_reports.data.dto.PackagingLossGainReportDto
import com.havos.lubricerp.feature_reports.data.dto.PaymentPendingCustomerDto
import com.havos.lubricerp.feature_reports.data.dto.PaymentPendingPagedDataDto
import com.havos.lubricerp.feature_reports.data.dto.PaymentReceivedItemDto
import com.havos.lubricerp.feature_reports.data.dto.PaymentReceivedPagedDataDto
import com.havos.lubricerp.feature_reports.data.dto.ProformaInvoicePagedDataDto
import com.havos.lubricerp.feature_reports.data.dto.SalesInvoicePagedDataDto
import com.havos.lubricerp.feature_reports.data.dto.ProductSalesItemDto
import com.havos.lubricerp.feature_reports.data.dto.RawMaterialStockItemDto
import com.havos.lubricerp.feature_reports.data.dto.RecordPaymentRequestDto
import com.havos.lubricerp.feature_reports.data.dto.RecordPaymentResponseDto
import com.havos.lubricerp.feature_reports.data.dto.SalesInvoiceDetailDto
import com.havos.lubricerp.feature_reports.data.dto.SalesInvoiceItemDto
import com.havos.lubricerp.feature_reports.data.dto.SalesOrderDetailDto
import com.havos.lubricerp.feature_reports.data.dto.SalesOrderItemDto
import com.havos.lubricerp.feature_reports.data.dto.SalesOrderPagedDataDto
import com.havos.lubricerp.feature_reports.data.dto.SalesSummaryItemDto
import com.havos.lubricerp.feature_reports.data.dto.StockOverviewTankItemDto
import com.havos.lubricerp.feature_reports.data.dto.TankStockItemDto
import com.havos.lubricerp.feature_reports.data.dto.WarehouseStockItemDto

interface ReportsRemoteDataSource {
    suspend fun getTankStockSummary(token: String): ResultState<List<TankStockItemDto>>
    suspend fun getRawMaterialStock(): ResultState<List<RawMaterialStockItemDto>>
    suspend fun getPackagingLossGain(fromDate: String, toDate: String): ResultState<PackagingLossGainReportDto>
    suspend fun getDashboard(token: String): ResultState<DashboardDto>
    suspend fun getSalesSummary(token: String, fromDate: String, toDate: String): ResultState<List<SalesSummaryItemDto>>
    suspend fun getPaymentsReceived(
        token: String,
        fromDate: String,
        toDate: String,
        skip: Int = 0,
        take: Int = 20
    ): ResultState<PaymentReceivedPagedDataDto>
    suspend fun getStockOverviewTanks(token: String): ResultState<List<StockOverviewTankItemDto>>
    suspend fun getCustomers(token: String): ResultState<List<CustomerDto>>
    suspend fun getCustomerLedger(
        token: String,
        customerId: Long,
        fromDate: String?,
        toDate: String?,
        skip: Int = 0,
        take: Int = 200
    ): ResultState<CustomerLedgerPagedDataDto>
    suspend fun getCustomerMobileSummary(token: String, customerId: Long): ResultState<CustomerMobileSummaryDto>
    suspend fun getProductSales(token: String, fromDate: String, toDate: String): ResultState<List<ProductSalesItemDto>>
    suspend fun getNetProfit(token: String, fromDate: String, toDate: String): ResultState<NetProfitReportDto>
    suspend fun getExpenseSummary(token: String, fromDate: String, toDate: String): ResultState<List<ExpenseSummaryItemDto>>

    suspend fun getWarehouseStock(token: String, warehouseId: Int?): ResultState<List<WarehouseStockItemDto>>

    suspend fun getConsolidatedStock(token: String): ResultState<List<ConsolidatedStockItemDto>>

    suspend fun getLowStock(token: String, threshold: Int): ResultState<List<LowStockItemDto>>

    suspend fun getFastMoving(token: String, days: Int, top: Int): ResultState<List<FastMovingItemDto>>
    suspend fun recordPayment(token: String, request: RecordPaymentRequestDto): ResultState<RecordPaymentResponseDto>
    suspend fun getPaymentsPending(
        token: String,
        skip: Int = 0,
        take: Int = 20
    ): ResultState<PaymentPendingPagedDataDto>
    suspend fun getAccountsSummary(token: String, fromDate: String, toDate: String): ResultState<AccountsSummaryDto>

    suspend fun getSalesOrders(
        token: String,
        status: String,
        skip: Int = 0,
        take: Int = 200
    ): ResultState<SalesOrderPagedDataDto>

    suspend fun getSalesOrderDetail(token: String, orderId: Long): ResultState<SalesOrderDetailDto>

    suspend fun getSalesInvoices(
        token: String,
        fromDate: String?,
        toDate: String?,
        paymentStatus: String?,
        skip: Int = 0,
        take: Int = 200
    ): ResultState<SalesInvoicePagedDataDto>

    suspend fun getSalesInvoiceDetail(token: String, invoiceId: Long): ResultState<SalesInvoiceDetailDto>

    // ── Dashboard APIs ───────────────────────────────────────────────────────
    suspend fun getReceivablesAging(token: String): ResultState<ReceivablesAgingDto>
    suspend fun getPurchaseSummary(token: String): ResultState<PurchaseSummaryDto>
    suspend fun getCashPosition(token: String): ResultState<CashPositionDto>

    suspend fun getProformaInvoices(
        token: String,
        status: String?,
        skip: Int = 0,
        take: Int = 200
    ): ResultState<ProformaInvoicePagedDataDto>

    suspend fun getProformaInvoiceDetail(
        token: String,
        id: Long
    ): ResultState<com.havos.lubricerp.feature_reports.data.dto.ProformaInvoiceDetailDto>

    suspend fun createProformaInvoice(
        token: String,
        request: com.havos.lubricerp.feature_reports.data.dto.CreateProformaInvoiceRequestDto
    ): ResultState<com.havos.lubricerp.feature_reports.data.dto.ProformaInvoiceDetailDto>

    suspend fun getProductSkus(
        token: String,
        gradeId: Int?
    ): ResultState<List<com.havos.lubricerp.feature_reports.data.dto.ProductSkuDto>>

    suspend fun updateProformaInvoice(
        token: String,
        id: Long,
        request: com.havos.lubricerp.feature_reports.data.dto.CreateProformaInvoiceRequestDto
    ): ResultState<Unit>

    suspend fun sendProformaInvoice(
        token: String,
        id: Long
    ): ResultState<Unit>

    suspend fun cancelProformaInvoice(
        token: String,
        id: Long
    ): ResultState<Unit>

    suspend fun getCostBreakdownSheets(
        token: String,
        skip: Int = 0,
        take: Int = 200
    ): ResultState<CostBreakdownPagedDataDto>

    suspend fun getCostBreakdownDetail(
        token: String,
        id: Long
    ): ResultState<CostBreakdownDetailDto>
}
