package com.havos.lubricerp.feature_reports.domain.usecase

import com.havos.lubricerp.core.common.ResultState
import com.havos.lubricerp.feature_reports.domain.model.AccountsSummary
import com.havos.lubricerp.feature_reports.domain.model.ConsolidatedStockItem
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
import com.havos.lubricerp.feature_reports.domain.model.TankStockSummary
import com.havos.lubricerp.feature_reports.domain.model.WarehouseStockItem
import com.havos.lubricerp.feature_reports.domain.repository.ReportsRepository

class GetTankStockSummaryUseCase(
    private val repository: ReportsRepository
) {
    suspend operator fun invoke(): ResultState<TankStockSummary> = repository.getTankStockSummary()
}

class GetRawMaterialStockUseCase(
    private val repository: ReportsRepository
) {
    suspend operator fun invoke(): ResultState<List<RawMaterialStockItem>> = repository.getRawMaterialStock()
}

class GetPackagingLossGainUseCase(
    private val repository: ReportsRepository
) {
    suspend operator fun invoke(filter: DateRangeFilter): ResultState<PackagingLossGainReport> {
        return repository.getPackagingLossGain(filter)
    }
}

class GetDashboardUseCase(
    private val repository: ReportsRepository
) {
    suspend operator fun invoke(token: String): ResultState<DashboardSummary> {
        return repository.getDashboard(token)
    }
}

class GetSalesSummaryUseCase(
    private val repository: ReportsRepository
) {
    suspend operator fun invoke(token: String, filter: DateRangeFilter): ResultState<List<SalesSummaryItem>> {
        return repository.getSalesSummary(token, filter)
    }
}

class GetPaymentsReceivedUseCase(
    private val repository: ReportsRepository
) {
    suspend operator fun invoke(token: String, filter: DateRangeFilter): ResultState<List<PaymentReceivedItem>> {
        return repository.getPaymentsReceived(token, filter)
    }
}

class GetStockOverviewUseCase(
    private val repository: ReportsRepository
) {
    suspend operator fun invoke(token: String): ResultState<List<StockOverviewTankItem>> {
        return repository.getStockOverviewTanks(token)
    }
}

class GetCustomersUseCase(
    private val repository: ReportsRepository
) {
    suspend operator fun invoke(token: String): ResultState<List<Customer>> {
        return repository.getCustomers(token)
    }
}

class GetCustomerLedgerUseCase(
    private val repository: ReportsRepository
) {
    suspend operator fun invoke(token: String, customerId: Long, fromDate: String?, toDate: String?): ResultState<List<CustomerLedgerEntry>> {
        return repository.getCustomerLedger(token, customerId, fromDate, toDate)
    }
}

class GetCustomerMobileSummaryUseCase(
    private val repository: ReportsRepository
) {
    suspend operator fun invoke(token: String, customerId: Long): ResultState<CustomerMobileSummary> {
        return repository.getCustomerMobileSummary(token, customerId)
    }
}

class GetProductSalesUseCase(
    private val repository: ReportsRepository
) {
    suspend operator fun invoke(token: String, filter: DateRangeFilter): ResultState<List<ProductSalesItem>> {
        return repository.getProductSales(token, filter)
    }
}

class GetNetProfitUseCase(
    private val repository: ReportsRepository
) {
    suspend operator fun invoke(
        token: String,
        filter: DateRangeFilter,
        roles: List<String>
    ): ResultState<NetProfitReport> {
        val hasAccess = roles.any { it.equals("Admin", ignoreCase = true) || it.equals("Manager", ignoreCase = true) }
        if (!hasAccess) return ResultState.Error(ACCESS_DENIED)
        return repository.getNetProfit(token, filter)
    }

    companion object {
        const val ACCESS_DENIED = "ACCESS_DENIED"
    }
}

class GetExpenseSummaryUseCase(
    private val repository: ReportsRepository
) {
    suspend operator fun invoke(token: String, filter: DateRangeFilter): ResultState<List<ExpenseSummaryItem>> {
        return repository.getExpenseSummary(token, filter)
    }
}

class GetWarehouseStockUseCase(
    private val repository: ReportsRepository
) {
    suspend operator fun invoke(token: String, warehouseId: Int?): ResultState<List<WarehouseStockItem>> {
        return repository.getWarehouseStock(token, warehouseId)
    }
}

class GetConsolidatedStockUseCase(
    private val repository: ReportsRepository
) {
    suspend operator fun invoke(token: String): ResultState<List<ConsolidatedStockItem>> {
        return repository.getConsolidatedStock(token)
    }
}

class GetLowStockUseCase(
    private val repository: ReportsRepository
) {
    suspend operator fun invoke(token: String, threshold: Int = 10): ResultState<List<LowStockItem>> {
        return repository.getLowStock(token, threshold)
    }
}

class GetFastMovingUseCase(
    private val repository: ReportsRepository
) {
    suspend operator fun invoke(token: String, days: Int = 30, top: Int = 10): ResultState<List<FastMovingItem>> {
        return repository.getFastMoving(token, days, top)
    }
}

class RecordPaymentUseCase(
    private val repository: ReportsRepository
) {
    suspend operator fun invoke(token: String, request: RecordPaymentRequest): ResultState<RecordPaymentResponse> {
        return repository.recordPayment(token, request)
    }
}

class GetPaymentsPendingUseCase(
    private val repository: ReportsRepository
) {
    suspend operator fun invoke(token: String): ResultState<List<PaymentPendingCustomer>> {
        return repository.getPaymentsPending(token)
    }
}

class GetAccountsSummaryUseCase(
    private val repository: ReportsRepository
) {
    suspend operator fun invoke(token: String, filter: DateRangeFilter): ResultState<AccountsSummary> {
        return repository.getAccountsSummary(token, filter)
    }
}

class GetSalesOrdersUseCase(
    private val repository: ReportsRepository
) {
    suspend operator fun invoke(token: String, status: String): ResultState<List<SalesOrderItem>> {
        return repository.getSalesOrders(token, status)
    }
}

class GetSalesOrderDetailUseCase(
    private val repository: ReportsRepository
) {
    suspend operator fun invoke(token: String, orderId: Long): ResultState<SalesOrderDetail> {
        return repository.getSalesOrderDetail(token, orderId)
    }
}

class GetSalesInvoicesUseCase(
    private val repository: ReportsRepository
) {
    suspend operator fun invoke(
        token: String,
        fromDate: String? = null,
        toDate: String? = null,
        paymentStatus: String? = null
    ): ResultState<List<SalesInvoiceItem>> {
        return repository.getSalesInvoices(token, fromDate, toDate, paymentStatus)
    }
}

class GetSalesInvoiceDetailUseCase(
    private val repository: ReportsRepository
) {
    suspend operator fun invoke(token: String, invoiceId: Long): ResultState<SalesInvoiceDetail> {
        return repository.getSalesInvoiceDetail(token, invoiceId)
    }
}
