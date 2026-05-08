package com.havos.lubricerp.feature_reports.data.repository

import com.havos.lubricerp.core.common.ResultState
import com.havos.lubricerp.feature_reports.data.mapper.toDomain
import com.havos.lubricerp.feature_reports.data.remote.reports.ReportsRemoteDataSource
import com.havos.lubricerp.feature_reports.domain.model.DashboardSummary
import com.havos.lubricerp.feature_reports.domain.model.DateRangeFilter
import com.havos.lubricerp.feature_reports.domain.model.Customer
import com.havos.lubricerp.feature_reports.domain.model.CustomerLedgerEntry
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
import com.havos.lubricerp.feature_reports.domain.repository.ReportsRepository

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
}
