package com.havos.lubricerp.feature_reports.data.repository

import com.havos.lubricerp.core.common.ResultState
import com.havos.lubricerp.feature_reports.data.dto.RecordPaymentRequestDto
import com.havos.lubricerp.feature_reports.data.mapper.toDomain
import com.havos.lubricerp.feature_reports.data.mapper.toDto
import com.havos.lubricerp.feature_reports.data.dto.SalesOrderPagedDataDto
import com.havos.lubricerp.feature_reports.data.remote.reports.ReportsRemoteDataSource
import com.havos.lubricerp.feature_reports.domain.model.AccountsSummary
import com.havos.lubricerp.feature_reports.domain.model.CashPosition
import com.havos.lubricerp.feature_reports.domain.model.ConsolidatedStockItem
import com.havos.lubricerp.feature_reports.domain.model.CostBreakdownDetail
import com.havos.lubricerp.feature_reports.domain.model.CostBreakdownItem
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
import com.havos.lubricerp.feature_reports.domain.repository.ReportsRepository

class ReportsRepositoryImpl(
    private val reportsRemoteDataSource: ReportsRemoteDataSource
) : ReportsRepository {

    override suspend fun getTankStockSummary(token: String): ResultState<List<TankStockItem>> {
        return when (val result = reportsRemoteDataSource.getTankStockSummary(token)) {
            is ResultState.Success -> ResultState.Success(result.data.map { it.toDomain() })
            is ResultState.Error -> result
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun getRawMaterialStock(): ResultState<List<RawMaterialStockItem>> {
        return when (val result = reportsRemoteDataSource.getRawMaterialStock()) {
            is ResultState.Success -> ResultState.Success(result.data.map { it.toDomain() })
            is ResultState.Error -> result
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun getPackagingLossGain(filter: DateRangeFilter): ResultState<PackagingLossGainReport> {
        return when (val result = reportsRemoteDataSource.getPackagingLossGain(filter.fromDate, filter.toDate)) {
            is ResultState.Success -> ResultState.Success(result.data.toDomain())
            is ResultState.Error -> result
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun getDashboard(token: String): ResultState<DashboardSummary> {
        return when (val result = reportsRemoteDataSource.getDashboard(token)) {
            is ResultState.Success -> ResultState.Success(result.data.toDomain())
            is ResultState.Error -> result
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun getSalesSummary(token: String, filter: DateRangeFilter): ResultState<List<SalesSummaryItem>> {
        return when (val result = reportsRemoteDataSource.getSalesSummary(token, filter.fromDate, filter.toDate)) {
            is ResultState.Success -> ResultState.Success(result.data.map { it.toDomain() })
            is ResultState.Error -> result
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun getPaymentsReceived(
        token: String,
        filter: DateRangeFilter,
        skip: Int,
        take: Int
    ): ResultState<PagedResult<PaymentReceivedItem>> {
        return when (val result = reportsRemoteDataSource.getPaymentsReceived(token, filter.fromDate, filter.toDate, skip, take)) {
            is ResultState.Success -> ResultState.Success(result.data.toDomain())
            is ResultState.Error -> result
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun getStockOverviewTanks(token: String): ResultState<List<StockOverviewTankItem>> {
        return when (val result = reportsRemoteDataSource.getStockOverviewTanks(token)) {
            is ResultState.Success -> ResultState.Success(result.data.map { it.toDomain() })
            is ResultState.Error -> result
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun getCustomers(token: String): ResultState<List<Customer>> {
        return when (val result = reportsRemoteDataSource.getCustomers(token)) {
            is ResultState.Success -> ResultState.Success(result.data.map { it.toDomain() })
            is ResultState.Error -> result
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun getCustomerLedger(
        token: String,
        customerId: Long,
        fromDate: String?,
        toDate: String?,
        skip: Int,
        take: Int
    ): ResultState<PagedResult<CustomerLedgerEntry>> {
        return when (val result = reportsRemoteDataSource.getCustomerLedger(token, customerId, fromDate, toDate, skip, take)) {
            is ResultState.Success -> ResultState.Success(result.data.toDomain())
            is ResultState.Error -> result
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun getCustomerMobileSummary(token: String, customerId: Long): ResultState<CustomerMobileSummary> {
        return when (val result = reportsRemoteDataSource.getCustomerMobileSummary(token, customerId)) {
            is ResultState.Success -> ResultState.Success(result.data.toDomain())
            is ResultState.Error -> result
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun getProductSales(token: String, filter: DateRangeFilter): ResultState<List<ProductSalesItem>> {
        return when (val result = reportsRemoteDataSource.getProductSales(token, filter.fromDate, filter.toDate)) {
            is ResultState.Success -> ResultState.Success(result.data.map { it.toDomain() })
            is ResultState.Error -> result
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun getNetProfit(token: String, filter: DateRangeFilter): ResultState<NetProfitReport> {
        return when (val result = reportsRemoteDataSource.getNetProfit(token, filter.fromDate, filter.toDate)) {
            is ResultState.Success -> ResultState.Success(result.data.toDomain())
            is ResultState.Error -> result
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun getExpenseSummary(token: String, filter: DateRangeFilter): ResultState<List<ExpenseSummaryItem>> {
        return when (val result = reportsRemoteDataSource.getExpenseSummary(token, filter.fromDate, filter.toDate)) {
            is ResultState.Success -> ResultState.Success(result.data.map { it.toDomain() })
            is ResultState.Error -> result
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun getWarehouseStock(token: String, warehouseId: Int?): ResultState<List<WarehouseStockItem>> {
        return when (val result = reportsRemoteDataSource.getWarehouseStock(token, warehouseId)) {
            is ResultState.Success -> ResultState.Success(result.data.map { it.toDomain() })
            is ResultState.Error -> result
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun getConsolidatedStock(token: String): ResultState<List<ConsolidatedStockItem>> {
        return when (val result = reportsRemoteDataSource.getConsolidatedStock(token)) {
            is ResultState.Success -> ResultState.Success(result.data.map { it.toDomain() })
            is ResultState.Error -> result
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun getLowStock(token: String, threshold: Int): ResultState<List<LowStockItem>> {
        return when (val result = reportsRemoteDataSource.getLowStock(token, threshold)) {
            is ResultState.Success -> ResultState.Success(result.data.map { it.toDomain() })
            is ResultState.Error -> result
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun getFastMoving(token: String, days: Int, top: Int): ResultState<List<FastMovingItem>> {
        return when (val result = reportsRemoteDataSource.getFastMoving(token, days, top)) {
            is ResultState.Success -> ResultState.Success(result.data.map { it.toDomain() })
            is ResultState.Error -> result
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun getPaymentsPending(
        token: String,
        skip: Int,
        take: Int
    ): ResultState<PagedResult<PaymentPendingCustomer>> {
        return when (val result = reportsRemoteDataSource.getPaymentsPending(token, skip, take)) {
            is ResultState.Success -> ResultState.Success(result.data.toDomain())
            is ResultState.Error -> result
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun getAccountsSummary(token: String, filter: DateRangeFilter): ResultState<AccountsSummary> {
        return when (val result = reportsRemoteDataSource.getAccountsSummary(token, filter.fromDate, filter.toDate)) {
            is ResultState.Success -> ResultState.Success(result.data.toDomain())
            is ResultState.Error -> result
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun getSalesOrders(token: String, status: String): ResultState<List<SalesOrderItem>> {
        val allItems = mutableListOf<SalesOrderItem>()
        var skip = 0
        val take = 200
        while (true) {
            when (val result = reportsRemoteDataSource.getSalesOrders(token, status, skip, take)) {
                is ResultState.Success -> {
                    allItems.addAll(result.data.items.map { it.toDomain() })
                    if (!result.data.hasMore) break
                    skip += result.data.items.size
                }
                is ResultState.Error -> {
                    if (allItems.isEmpty()) return result
                    break
                }
                ResultState.Loading -> return ResultState.Loading
            }
        }
        return ResultState.Success(allItems)
    }

    override suspend fun getSalesOrderDetail(token: String, orderId: Long): ResultState<SalesOrderDetail> {
        return when (val result = reportsRemoteDataSource.getSalesOrderDetail(token, orderId)) {
            is ResultState.Success -> ResultState.Success(result.data.toDomain())
            is ResultState.Error -> result
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun getSalesInvoices(
        token: String,
        fromDate: String?,
        toDate: String?,
        paymentStatus: String?
    ): ResultState<List<SalesInvoiceItem>> {
        val allItems = mutableListOf<SalesInvoiceItem>()
        var skip = 0
        val take = 200
        while (true) {
            when (val result = reportsRemoteDataSource.getSalesInvoices(token, fromDate, toDate, paymentStatus, skip, take)) {
                is ResultState.Success -> {
                    allItems.addAll(result.data.items.map { it.toDomain() })
                    if (!result.data.hasMore) break
                    skip += result.data.items.size
                }
                is ResultState.Error -> {
                    if (allItems.isEmpty()) return result
                    break
                }
                ResultState.Loading -> return ResultState.Loading
            }
        }
        return ResultState.Success(allItems)
    }

    override suspend fun getSalesInvoiceDetail(token: String, invoiceId: Long): ResultState<SalesInvoiceDetail> {
        return when (val result = reportsRemoteDataSource.getSalesInvoiceDetail(token, invoiceId)) {
            is ResultState.Success -> ResultState.Success(result.data.toDomain())
            is ResultState.Error -> result
            ResultState.Loading -> ResultState.Loading
        }
    }

    // ── Dashboard: Receivables Aging ─────────────────────────────────────────
    override suspend fun getReceivablesAging(token: String): ResultState<ReceivablesAging> {
        return when (val result = reportsRemoteDataSource.getReceivablesAging(token)) {
            is ResultState.Success -> ResultState.Success(result.data.toDomain())
            is ResultState.Error -> result
            ResultState.Loading -> ResultState.Loading
        }
    }

    // ── Dashboard: Purchase Summary ──────────────────────────────────────────
    override suspend fun getPurchaseSummary(token: String): ResultState<PurchaseSummary> {
        return when (val result = reportsRemoteDataSource.getPurchaseSummary(token)) {
            is ResultState.Success -> ResultState.Success(result.data.toDomain())
            is ResultState.Error -> result
            ResultState.Loading -> ResultState.Loading
        }
    }

    // ── Dashboard: Cash Position ─────────────────────────────────────────────
    override suspend fun getCashPosition(token: String): ResultState<CashPosition> {
        return when (val result = reportsRemoteDataSource.getCashPosition(token)) {
            is ResultState.Success -> ResultState.Success(result.data.toDomain())
            is ResultState.Error -> result
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun recordPayment(token: String, request: RecordPaymentRequest): ResultState<RecordPaymentResponse> {
        val dto = RecordPaymentRequestDto(
            invoiceId = request.invoiceId,
            amount = request.amount,
            paymentMode = request.paymentMode,
            paymentDate = request.paymentDate,
            reference = request.reference,
            remarks = request.remarks
        )
        return when (val result = reportsRemoteDataSource.recordPayment(token, dto)) {
            is ResultState.Success -> ResultState.Success(result.data.toDomain())
            is ResultState.Error -> result
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun getProformaInvoices(
        token: String,
        status: String?
    ): ResultState<List<com.havos.lubricerp.feature_reports.domain.model.ProformaInvoice>> {
        val allItems = mutableListOf<com.havos.lubricerp.feature_reports.domain.model.ProformaInvoice>()
        var skip = 0
        val take = 200
        while (true) {
            when (val result = reportsRemoteDataSource.getProformaInvoices(token, status, skip, take)) {
                is ResultState.Success -> {
                    allItems.addAll(result.data.items.map { it.toDomain() })
                    if (!result.data.hasMore) break
                    skip += result.data.items.size
                }
                is ResultState.Error -> {
                    if (allItems.isEmpty()) return result
                    break
                }
                ResultState.Loading -> return ResultState.Loading
            }
        }
        return ResultState.Success(allItems)
    }

    override suspend fun getProformaInvoiceDetail(
        token: String,
        id: Long
    ): ResultState<com.havos.lubricerp.feature_reports.domain.model.ProformaInvoiceDetail> {
        return when (val result = reportsRemoteDataSource.getProformaInvoiceDetail(token, id)) {
            is ResultState.Success -> ResultState.Success(result.data.toDomain())
            is ResultState.Error -> result
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun createProformaInvoice(
        token: String,
        request: com.havos.lubricerp.feature_reports.domain.model.CreateProformaInvoiceRequest
    ): ResultState<com.havos.lubricerp.feature_reports.domain.model.CreateProformaInvoiceResponse> {
        return when (val result = reportsRemoteDataSource.createProformaInvoice(token, request.toDto())) {
            is ResultState.Success -> {
                val detail = result.data.toDomain()
                ResultState.Success(
                    com.havos.lubricerp.feature_reports.domain.model.CreateProformaInvoiceResponse(
                        id = detail.id,
                        proformaNumber = detail.proformaNumber
                    )
                )
            }
            is ResultState.Error -> result
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun getProductSkus(
        token: String,
        gradeId: Int?
    ): ResultState<List<com.havos.lubricerp.feature_reports.domain.model.ProductSku>> {
        return when (val result = reportsRemoteDataSource.getProductSkus(token, gradeId)) {
            is ResultState.Success -> ResultState.Success(result.data.map { it.toDomain() })
            is ResultState.Error -> result
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun updateProformaInvoice(
        token: String,
        id: Long,
        request: com.havos.lubricerp.feature_reports.domain.model.CreateProformaInvoiceRequest
    ): ResultState<Unit> {
        return when (val result = reportsRemoteDataSource.updateProformaInvoice(token, id, request.toDto())) {
            is ResultState.Success -> ResultState.Success(Unit)
            is ResultState.Error -> result
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun sendProformaInvoice(
        token: String,
        id: Long
    ): ResultState<Unit> {
        return when (val result = reportsRemoteDataSource.sendProformaInvoice(token, id)) {
            is ResultState.Success -> ResultState.Success(Unit)
            is ResultState.Error -> result
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun cancelProformaInvoice(
        token: String,
        id: Long
    ): ResultState<Unit> {
        return when (val result = reportsRemoteDataSource.cancelProformaInvoice(token, id)) {
            is ResultState.Success -> ResultState.Success(Unit)
            is ResultState.Error -> result
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun getCostBreakdownSheets(token: String): ResultState<List<CostBreakdownItem>> {
        val allItems = mutableListOf<CostBreakdownItem>()
        var skip = 0
        val take = 200
        while (true) {
            when (val result = reportsRemoteDataSource.getCostBreakdownSheets(token, skip, take)) {
                is ResultState.Success -> {
                    allItems.addAll(result.data.items.map { it.toDomain() })
                    if (!result.data.hasMore) break
                    skip += result.data.items.size
                }
                is ResultState.Error -> {
                    if (allItems.isEmpty()) return result
                    break
                }
                ResultState.Loading -> return ResultState.Loading
            }
        }
        return ResultState.Success(allItems)
    }

    override suspend fun getCostBreakdownDetail(token: String, id: Long): ResultState<CostBreakdownDetail> {
        return when (val result = reportsRemoteDataSource.getCostBreakdownDetail(token, id)) {
            is ResultState.Success -> ResultState.Success(result.data.toDomain())
            is ResultState.Error -> result
            ResultState.Loading -> ResultState.Loading
        }
    }
}
