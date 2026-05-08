package com.havos.lubricerp.feature_reports.data.remote.reports

import com.havos.lubricerp.core.common.ResultState
import com.havos.lubricerp.feature_reports.data.dto.CustomerDto
import com.havos.lubricerp.feature_reports.data.dto.CustomerLedgerEntryDto
import com.havos.lubricerp.feature_reports.data.dto.ExpenseSummaryItemDto
import com.havos.lubricerp.feature_reports.data.dto.NetProfitReportDto
import com.havos.lubricerp.feature_reports.data.dto.ProductSalesItemDto
import com.havos.lubricerp.feature_reports.data.dto.ReportSalesSummaryItemDto
import com.havos.lubricerp.feature_reports.data.dto.DashboardDto
import com.havos.lubricerp.feature_reports.data.dto.PackagingLossGainReportDto
import com.havos.lubricerp.feature_reports.data.dto.PaymentReceivedItemDto
import com.havos.lubricerp.feature_reports.data.dto.RawMaterialStockItemDto
import com.havos.lubricerp.feature_reports.data.dto.SalesSummaryItemDto
import com.havos.lubricerp.feature_reports.data.dto.StockOverviewTankItemDto
import com.havos.lubricerp.feature_reports.data.dto.TankStockSummaryDto

interface ReportsRemoteDataSource {
    suspend fun getTankStockSummary(): ResultState<TankStockSummaryDto>
    suspend fun getRawMaterialStock(): ResultState<List<RawMaterialStockItemDto>>
    suspend fun getPackagingLossGain(fromDate: String, toDate: String): ResultState<PackagingLossGainReportDto>
    suspend fun getDashboard(token: String): ResultState<DashboardDto>
    suspend fun getSalesSummary(token: String, fromDate: String, toDate: String): ResultState<List<SalesSummaryItemDto>>
    suspend fun getPaymentsReceived(token: String, fromDate: String, toDate: String): ResultState<List<PaymentReceivedItemDto>>
    suspend fun getStockOverviewTanks(token: String): ResultState<List<StockOverviewTankItemDto>>
    suspend fun getCustomers(token: String): ResultState<List<CustomerDto>>
    suspend fun getCustomerLedger(token: String, customerId: Long, fromDate: String?, toDate: String?): ResultState<List<CustomerLedgerEntryDto>>
    suspend fun getReportSalesSummary(token: String, fromDate: String, toDate: String): ResultState<List<ReportSalesSummaryItemDto>>
    suspend fun getProductSales(token: String, fromDate: String, toDate: String): ResultState<List<ProductSalesItemDto>>
    suspend fun getNetProfit(token: String, fromDate: String, toDate: String): ResultState<NetProfitReportDto>
    suspend fun getExpenseSummary(token: String, fromDate: String, toDate: String): ResultState<List<ExpenseSummaryItemDto>>
}
