package com.havos.lubricerp.feature_reports.data.repository

import com.havos.lubricerp.core.common.ResultState
import com.havos.lubricerp.feature_reports.data.mapper.toDomain
import com.havos.lubricerp.feature_reports.data.remote.reports.ReportsRemoteDataSource
import com.havos.lubricerp.feature_reports.domain.model.DashboardSummary
import com.havos.lubricerp.feature_reports.domain.model.DateRangeFilter
import com.havos.lubricerp.feature_reports.domain.model.Customer
import com.havos.lubricerp.feature_reports.domain.model.CustomerLedgerEntry
import com.havos.lubricerp.feature_reports.domain.model.CustomerMobileSummary
import com.havos.lubricerp.feature_reports.domain.model.ExpenseSummaryItem
import com.havos.lubricerp.feature_reports.domain.model.NetProfitReport
import com.havos.lubricerp.feature_reports.domain.model.ProductSalesItem
import com.havos.lubricerp.feature_reports.domain.model.ReportSalesSummaryItem
import com.havos.lubricerp.feature_reports.domain.model.PaymentReceivedItem
import com.havos.lubricerp.feature_reports.domain.model.SalesSummaryItem
import com.havos.lubricerp.feature_reports.domain.model.PackagingLossGainReport
import com.havos.lubricerp.feature_reports.domain.model.RawMaterialStockItem
import com.havos.lubricerp.feature_reports.domain.model.StockOverviewTankItem
import com.havos.lubricerp.feature_reports.domain.model.TankStockSummary
import com.havos.lubricerp.feature_reports.domain.model.ConsolidatedStockItem
import com.havos.lubricerp.feature_reports.domain.model.FastMovingItem
import com.havos.lubricerp.feature_reports.domain.model.LowStockItem
import com.havos.lubricerp.feature_reports.domain.model.WarehouseStockItem
import com.havos.lubricerp.feature_reports.domain.model.RecordPaymentRequest
import com.havos.lubricerp.feature_reports.domain.model.RecordPaymentResponse
import com.havos.lubricerp.feature_reports.domain.model.PaymentPendingCustomer
import com.havos.lubricerp.feature_reports.domain.model.AccountsSummary
import com.havos.lubricerp.feature_reports.domain.repository.ReportsRepository
import com.havos.lubricerp.feature_reports.data.dto.RecordPaymentRequestDto

class ReportsRepositoryImpl(
    private val reportsRemoteDataSource: ReportsRemoteDataSource
) : ReportsRepository {

    override suspend fun getTankStockSummary(): ResultState<TankStockSummary> {
        return when (val result = reportsRemoteDataSource.getTankStockSummary()) {
            is ResultState.Success -> ResultState.Success(result.data.toDomain())
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

    override suspend fun getPaymentsReceived(token: String, filter: DateRangeFilter): ResultState<List<PaymentReceivedItem>> {
        return when (val result = reportsRemoteDataSource.getPaymentsReceived(token, filter.fromDate, filter.toDate)) {
            is ResultState.Success -> ResultState.Success(result.data.map { it.toDomain() })
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

    override suspend fun getCustomerLedger(token: String, customerId: Long, fromDate: String?, toDate: String?): ResultState<List<CustomerLedgerEntry>> {
        return when (val result = reportsRemoteDataSource.getCustomerLedger(token, customerId, fromDate, toDate)) {
            is ResultState.Success -> ResultState.Success(result.data.map { it.toDomain() })
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

    override suspend fun getReportSalesSummary(token: String, filter: DateRangeFilter): ResultState<List<ReportSalesSummaryItem>> {
        return when (val result = reportsRemoteDataSource.getReportSalesSummary(token, filter.fromDate, filter.toDate)) {
            is ResultState.Success -> ResultState.Success(result.data.map { it.toDomain() })
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

    override suspend fun getPaymentsPending(token: String): ResultState<List<PaymentPendingCustomer>> {
        return when (val result = reportsRemoteDataSource.getPaymentsPending(token)) {
            is ResultState.Success -> ResultState.Success(result.data.map { it.toDomain() })
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
}
