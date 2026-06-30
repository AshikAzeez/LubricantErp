package com.havos.lubricerp.feature_reports.domain.repository

import com.havos.lubricerp.core.common.ResultState
import com.havos.lubricerp.feature_reports.domain.model.AccountsSummary
import com.havos.lubricerp.feature_reports.domain.model.CashPosition
import com.havos.lubricerp.feature_reports.domain.model.ConsolidatedStockItem
import com.havos.lubricerp.feature_reports.domain.model.PurchaseSummary
import com.havos.lubricerp.feature_reports.domain.model.ReceivablesAging
import com.havos.lubricerp.feature_reports.domain.model.Customer
import com.havos.lubricerp.feature_reports.domain.model.CustomerLedgerEntry
import com.havos.lubricerp.feature_reports.domain.model.CustomerMobileSummary
import com.havos.lubricerp.feature_reports.domain.model.DashboardSummary
import com.havos.lubricerp.feature_reports.domain.model.DateRangeFilter
import com.havos.lubricerp.feature_reports.domain.model.ExpenseSummaryItem
import com.havos.lubricerp.feature_reports.domain.model.FastMovingItem
import com.havos.lubricerp.feature_reports.domain.model.LowStockItem
import com.havos.lubricerp.feature_reports.domain.model.NetProfitReport
import com.havos.lubricerp.feature_reports.domain.model.PackagingLossGainReport
import com.havos.lubricerp.feature_reports.domain.model.PaymentPendingCustomer
import com.havos.lubricerp.feature_reports.domain.model.PaymentReceivedItem
import com.havos.lubricerp.feature_reports.domain.model.ProductSalesItem
import com.havos.lubricerp.feature_reports.domain.model.RawMaterialStockItem
import com.havos.lubricerp.feature_reports.domain.model.RecordPaymentRequest
import com.havos.lubricerp.feature_reports.domain.model.RecordPaymentResponse
import com.havos.lubricerp.feature_reports.domain.model.SalesInvoiceDetail
import com.havos.lubricerp.feature_reports.domain.model.SalesInvoiceItem
import com.havos.lubricerp.feature_reports.domain.model.SalesOrderDetail
import com.havos.lubricerp.feature_reports.domain.model.SalesOrderItem
import com.havos.lubricerp.feature_reports.domain.model.SalesSummaryItem
import com.havos.lubricerp.feature_reports.domain.model.StockOverviewTankItem
import com.havos.lubricerp.core.common.PagedResult
import com.havos.lubricerp.feature_reports.domain.model.TankStockItem
import com.havos.lubricerp.feature_reports.domain.model.WarehouseStockItem

interface ReportsRepository {
    suspend fun getTankStockSummary(token: String): ResultState<List<TankStockItem>>

    suspend fun getRawMaterialStock(): ResultState<List<RawMaterialStockItem>>

    suspend fun getPackagingLossGain(filter: DateRangeFilter): ResultState<PackagingLossGainReport>

    suspend fun getDashboard(token: String): ResultState<DashboardSummary>
    suspend fun getSalesSummary(token: String, filter: DateRangeFilter): ResultState<List<SalesSummaryItem>>
    suspend fun getPaymentsReceived(
        token: String,
        filter: DateRangeFilter,
        skip: Int = 0,
        take: Int = 20
    ): ResultState<PagedResult<PaymentReceivedItem>>
    suspend fun getStockOverviewTanks(token: String): ResultState<List<StockOverviewTankItem>>
    suspend fun getCustomers(token: String): ResultState<List<Customer>>
    suspend fun getCustomerLedger(
        token: String,
        customerId: Long,
        fromDate: String?,
        toDate: String?,
        skip: Int = 0,
        take: Int = 200
    ): ResultState<PagedResult<CustomerLedgerEntry>>
    suspend fun getCustomerMobileSummary(token: String, customerId: Long): ResultState<CustomerMobileSummary>
    suspend fun getProductSales(token: String, filter: DateRangeFilter): ResultState<List<ProductSalesItem>>
    suspend fun getNetProfit(token: String, filter: DateRangeFilter): ResultState<NetProfitReport>
    suspend fun getExpenseSummary(token: String, filter: DateRangeFilter): ResultState<List<ExpenseSummaryItem>>

    suspend fun getWarehouseStock(token: String, warehouseId: Int?): ResultState<List<WarehouseStockItem>>

    suspend fun getConsolidatedStock(token: String): ResultState<List<ConsolidatedStockItem>>

    suspend fun getLowStock(token: String, threshold: Int): ResultState<List<LowStockItem>>

    suspend fun getFastMoving(token: String, days: Int, top: Int): ResultState<List<FastMovingItem>>
    suspend fun recordPayment(token: String, request: RecordPaymentRequest): ResultState<RecordPaymentResponse>
    suspend fun getPaymentsPending(
        token: String,
        skip: Int = 0,
        take: Int = 20
    ): ResultState<PagedResult<PaymentPendingCustomer>>
    suspend fun getAccountsSummary(token: String, filter: DateRangeFilter): ResultState<AccountsSummary>

    suspend fun getSalesOrders(token: String, status: String): ResultState<List<SalesOrderItem>>

    suspend fun getSalesOrderDetail(token: String, orderId: Long): ResultState<SalesOrderDetail>

    suspend fun getSalesInvoices(
        token: String,
        fromDate: String?,
        toDate: String?,
        paymentStatus: String?
    ): ResultState<List<SalesInvoiceItem>>

    suspend fun getSalesInvoiceDetail(token: String, invoiceId: Long): ResultState<SalesInvoiceDetail>

    // ── Dashboard APIs ───────────────────────────────────────────────────────
    suspend fun getReceivablesAging(token: String): ResultState<ReceivablesAging>
    suspend fun getPurchaseSummary(token: String): ResultState<PurchaseSummary>
    suspend fun getCashPosition(token: String): ResultState<CashPosition>

    suspend fun getProformaInvoices(
        token: String,
        status: String?
    ): ResultState<List<com.havos.lubricerp.feature_reports.domain.model.ProformaInvoice>>

    suspend fun getProformaInvoiceDetail(
        token: String,
        id: Long
    ): ResultState<com.havos.lubricerp.feature_reports.domain.model.ProformaInvoiceDetail>

    suspend fun createProformaInvoice(
        token: String,
        request: com.havos.lubricerp.feature_reports.domain.model.CreateProformaInvoiceRequest
    ): ResultState<com.havos.lubricerp.feature_reports.domain.model.CreateProformaInvoiceResponse>

    suspend fun getProductSkus(
        token: String,
        gradeId: Int?
    ): ResultState<List<com.havos.lubricerp.feature_reports.domain.model.ProductSku>>

    suspend fun updateProformaInvoice(
        token: String,
        id: Long,
        request: com.havos.lubricerp.feature_reports.domain.model.CreateProformaInvoiceRequest
    ): ResultState<Unit>

    suspend fun sendProformaInvoice(
        token: String,
        id: Long
    ): ResultState<Unit>

    suspend fun cancelProformaInvoice(
        token: String,
        id: Long
    ): ResultState<Unit>
}
