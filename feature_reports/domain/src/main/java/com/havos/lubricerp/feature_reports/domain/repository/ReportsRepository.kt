package com.havos.lubricerp.feature_reports.domain.repository

import com.havos.lubricerp.core.common.ResultState
import com.havos.lubricerp.feature_reports.domain.model.DashboardSummary
import com.havos.lubricerp.feature_reports.domain.model.DateRangeFilter
import com.havos.lubricerp.feature_reports.domain.model.PaymentReceivedItem
import com.havos.lubricerp.feature_reports.domain.model.SalesSummaryItem
import com.havos.lubricerp.feature_reports.domain.model.Customer
import com.havos.lubricerp.feature_reports.domain.model.CustomerLedgerEntry
import com.havos.lubricerp.feature_reports.domain.model.ExpenseSummaryItem
import com.havos.lubricerp.feature_reports.domain.model.NetProfitReport
import com.havos.lubricerp.feature_reports.domain.model.ProductSalesItem
import com.havos.lubricerp.feature_reports.domain.model.ReportSalesSummaryItem
import com.havos.lubricerp.feature_reports.domain.model.PackagingLossGainReport
import com.havos.lubricerp.feature_reports.domain.model.RawMaterialStockItem
import com.havos.lubricerp.feature_reports.domain.model.StockOverviewTankItem
import com.havos.lubricerp.feature_reports.domain.model.TankStockSummary

interface ReportsRepository {
    suspend fun getTankStockSummary(): ResultState<TankStockSummary>

    suspend fun getRawMaterialStock(): ResultState<List<RawMaterialStockItem>>

    suspend fun getPackagingLossGain(filter: DateRangeFilter): ResultState<PackagingLossGainReport>

    suspend fun getDashboard(token: String): ResultState<DashboardSummary>
    suspend fun getSalesSummary(token: String, filter: DateRangeFilter): ResultState<List<SalesSummaryItem>>
    suspend fun getPaymentsReceived(token: String, filter: DateRangeFilter): ResultState<List<PaymentReceivedItem>>
    suspend fun getStockOverviewTanks(token: String): ResultState<List<StockOverviewTankItem>>
    suspend fun getCustomers(token: String): ResultState<List<Customer>>
    suspend fun getCustomerLedger(token: String, customerId: Long, fromDate: String?, toDate: String?): ResultState<List<CustomerLedgerEntry>>
    suspend fun getReportSalesSummary(token: String, filter: DateRangeFilter): ResultState<List<ReportSalesSummaryItem>>
    suspend fun getProductSales(token: String, filter: DateRangeFilter): ResultState<List<ProductSalesItem>>
    suspend fun getNetProfit(token: String, filter: DateRangeFilter): ResultState<NetProfitReport>
    suspend fun getExpenseSummary(token: String, filter: DateRangeFilter): ResultState<List<ExpenseSummaryItem>>
}
