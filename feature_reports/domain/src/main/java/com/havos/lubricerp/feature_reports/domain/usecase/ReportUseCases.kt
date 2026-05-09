package com.havos.lubricerp.feature_reports.domain.usecase

import com.havos.lubricerp.core.common.ResultState
import com.havos.lubricerp.feature_reports.domain.model.DashboardSummary
import com.havos.lubricerp.feature_reports.domain.model.DateRangeFilter
import com.havos.lubricerp.feature_reports.domain.model.PackagingLossGainReport
import com.havos.lubricerp.feature_reports.domain.model.RawMaterialStockItem
import com.havos.lubricerp.feature_reports.domain.model.PaymentReceivedItem
import com.havos.lubricerp.feature_reports.domain.model.SalesSummaryItem
import com.havos.lubricerp.feature_reports.domain.model.Customer
import com.havos.lubricerp.feature_reports.domain.model.CustomerLedgerEntry
import com.havos.lubricerp.feature_reports.domain.model.CustomerMobileSummary
import com.havos.lubricerp.feature_reports.domain.model.ExpenseSummaryItem
import com.havos.lubricerp.feature_reports.domain.model.NetProfitReport
import com.havos.lubricerp.feature_reports.domain.model.ProductSalesItem
import com.havos.lubricerp.feature_reports.domain.model.ReportSalesSummaryItem
import com.havos.lubricerp.feature_reports.domain.model.StockOverviewTankItem
import com.havos.lubricerp.feature_reports.domain.model.TankStockSummary
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

class GetReportSalesSummaryUseCase(
    private val repository: ReportsRepository
) {
    suspend operator fun invoke(token: String, filter: DateRangeFilter): ResultState<List<ReportSalesSummaryItem>> {
        return repository.getReportSalesSummary(token, filter)
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
