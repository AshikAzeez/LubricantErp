package com.havos.lubricerp.feature_reports.data.remote.reports

import com.havos.lubricerp.core.common.ResultState
import com.havos.lubricerp.core.network.safeApiCall
import com.havos.lubricerp.feature_reports.data.dto.CustomerDto
import com.havos.lubricerp.feature_reports.data.dto.CustomerLedgerApiResponseDto
import com.havos.lubricerp.feature_reports.data.dto.CustomerLedgerEntryDto
import com.havos.lubricerp.feature_reports.data.dto.CustomerListApiResponseDto
import com.havos.lubricerp.feature_reports.data.dto.CustomerMobileSummaryApiResponseDto
import com.havos.lubricerp.feature_reports.data.dto.CustomerMobileSummaryDto
import com.havos.lubricerp.feature_reports.data.dto.ExpenseSummaryApiResponseDto
import com.havos.lubricerp.feature_reports.data.dto.ExpenseSummaryItemDto
import com.havos.lubricerp.feature_reports.data.dto.NetProfitApiResponseDto
import com.havos.lubricerp.feature_reports.data.dto.NetProfitReportDto
import com.havos.lubricerp.feature_reports.data.dto.ProductSalesApiResponseDto
import com.havos.lubricerp.feature_reports.data.dto.ProductSalesItemDto
import com.havos.lubricerp.feature_reports.data.dto.ReportSalesSummaryApiResponseDto
import com.havos.lubricerp.feature_reports.data.dto.ReportSalesSummaryItemDto
import com.havos.lubricerp.feature_reports.data.dto.DashboardApiResponseDto
import com.havos.lubricerp.feature_reports.data.dto.DashboardDto
import com.havos.lubricerp.feature_reports.data.dto.PackagingLossGainReportDto
import com.havos.lubricerp.feature_reports.data.dto.RawMaterialStockItemDto
import com.havos.lubricerp.feature_reports.data.dto.PaymentReceivedApiResponseDto
import com.havos.lubricerp.feature_reports.data.dto.PaymentReceivedItemDto
import com.havos.lubricerp.feature_reports.data.dto.SalesSummaryApiResponseDto
import com.havos.lubricerp.feature_reports.data.dto.SalesSummaryItemDto
import com.havos.lubricerp.feature_reports.data.dto.StockOverviewTankApiResponseDto
import com.havos.lubricerp.feature_reports.data.dto.StockOverviewTankItemDto
import com.havos.lubricerp.feature_reports.data.dto.TankStockSummaryDto
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class ReportsRemoteApi(
    private val client: HttpClient
) : ReportsRemoteDataSource {

    override suspend fun getTankStockSummary(): ResultState<TankStockSummaryDto> {
        return when (
            val result = safeApiCall<TankStockSummaryDto> {
                client.get("reports/tank-stock-summary")
            }
        ) {
            is ResultState.Success -> ResultState.Success(result.data)
            is ResultState.Error -> ResultState.Error("Unable to fetch tank summary")
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun getRawMaterialStock(): ResultState<List<RawMaterialStockItemDto>> {
        return when (
            val result = safeApiCall<List<RawMaterialStockItemDto>> {
                client.get("reports/raw-material-stock")
            }
        ) {
            is ResultState.Success -> ResultState.Success(result.data)
            is ResultState.Error -> ResultState.Error("Unable to fetch raw material stock")
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun getPackagingLossGain(
        fromDate: String,
        toDate: String
    ): ResultState<PackagingLossGainReportDto> {
        return when (
            val result = safeApiCall<PackagingLossGainReportDto> {
                client.get("reports/packaging-loss-gain") {
                    parameter("fromDate", fromDate)
                    parameter("toDate", toDate)
                }
            }
        ) {
            is ResultState.Success -> ResultState.Success(result.data)
            is ResultState.Error -> ResultState.Error("Unable to fetch packaging loss/gain")
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun getDashboard(token: String): ResultState<DashboardDto> {
        if (token.isBlank()) return ResultState.Error("Authentication token is missing.")

        return when (
            val result = safeApiCall<DashboardApiResponseDto> {
                client.get("api/dashboard")
            }
        ) {
            is ResultState.Success -> {
                val payload = result.data
                val data = payload.data
                if (!payload.success || data == null) {
                    val serverMessage = payload.message?.takeIf { it.isNotBlank() }
                        ?: payload.errors?.firstOrNull()?.takeIf { it.isNotBlank() }
                        ?: "Unable to fetch dashboard"
                    ResultState.Error(serverMessage)
                } else {
                    ResultState.Success(data)
                }
            }
            is ResultState.Error -> ResultState.Error("Unable to fetch dashboard data.")
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun getSalesSummary(
        token: String,
        fromDate: String,
        toDate: String
    ): ResultState<List<SalesSummaryItemDto>> {
        if (token.isBlank()) return ResultState.Error("Authentication token is missing.")

        return when (
            val result = safeApiCall<SalesSummaryApiResponseDto> {
                client.get("api/reports/sales-summary") {
                    parameter("fromDate", fromDate)
                    parameter("toDate", toDate)
                }
            }
        ) {
            is ResultState.Success -> {
                val payload = result.data
                if (!payload.success) {
                    val serverMessage = payload.message?.takeIf { it.isNotBlank() }
                        ?: payload.errors?.firstOrNull()?.takeIf { it.isNotBlank() }
                        ?: "Unable to fetch sales summary"
                    ResultState.Error(serverMessage)
                } else {
                    ResultState.Success(payload.data)
                }
            }
            is ResultState.Error -> ResultState.Error("Unable to fetch sales summary.")
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun getPaymentsReceived(
        token: String,
        fromDate: String,
        toDate: String
    ): ResultState<List<PaymentReceivedItemDto>> {
        if (token.isBlank()) return ResultState.Error("Authentication token is missing.")

        return when (
            val result = safeApiCall<PaymentReceivedApiResponseDto> {
                client.get("api/payments/received") {
                    parameter("fromDate", fromDate)
                    parameter("toDate", toDate)
                }
            }
        ) {
            is ResultState.Success -> {
                val payload = result.data
                if (!payload.success) {
                    val serverMessage = payload.message?.takeIf { it.isNotBlank() }
                        ?: payload.errors?.firstOrNull()?.takeIf { it.isNotBlank() }
                        ?: "Unable to fetch payments received"
                    ResultState.Error(serverMessage)
                } else {
                    ResultState.Success(payload.data)
                }
            }
            is ResultState.Error -> ResultState.Error("Unable to fetch payments received.")
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun getStockOverviewTanks(token: String): ResultState<List<StockOverviewTankItemDto>> {
        if (token.isBlank()) return ResultState.Error("Authentication token is missing.")
        return when (
            val result = safeApiCall<StockOverviewTankApiResponseDto> {
                client.get("api/reports/tank-stock")
            }
        ) {
            is ResultState.Success -> {
                val payload = result.data
                if (!payload.success) ResultState.Error("Unable to fetch tank stock")
                else ResultState.Success(payload.data)
            }
            is ResultState.Error -> ResultState.Error("Unable to fetch tank stock.")
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun getCustomers(token: String): ResultState<List<CustomerDto>> {
        if (token.isBlank()) return ResultState.Error("Authentication token is missing.")
        return when (
            val result = safeApiCall<CustomerListApiResponseDto> {
                client.get("api/customers")
            }
        ) {
            is ResultState.Success -> {
                val payload = result.data
                if (!payload.success) ResultState.Error(payload.message ?: "Unable to fetch customers")
                else ResultState.Success(payload.data)
            }
            is ResultState.Error -> ResultState.Error("Unable to fetch customers.")
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun getCustomerMobileSummary(token: String, customerId: Long): ResultState<CustomerMobileSummaryDto> {
        if (token.isBlank()) return ResultState.Error("Authentication token is missing.")
        return when (
            val result = safeApiCall<CustomerMobileSummaryApiResponseDto> {
                client.get("api/customers/$customerId/mobile-summary")
            }
        ) {
            is ResultState.Success -> {
                val payload = result.data
                if (!payload.success || payload.data == null)
                    ResultState.Error(payload.message ?: "Unable to fetch customer summary")
                else
                    ResultState.Success(payload.data)
            }
            is ResultState.Error -> ResultState.Error("Unable to fetch customer summary.")
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun getReportSalesSummary(token: String, fromDate: String, toDate: String): ResultState<List<ReportSalesSummaryItemDto>> {
        if (token.isBlank()) return ResultState.Error("Authentication token is missing.")
        return when (
            val result = safeApiCall<ReportSalesSummaryApiResponseDto> {
                client.get("api/reports/sales-summary") {
                    parameter("fromDate", fromDate)
                    parameter("toDate", toDate)
                }
            }
        ) {
            is ResultState.Success -> {
                val payload = result.data
                if (!payload.success) ResultState.Error(payload.message ?: "Unable to fetch sales summary")
                else ResultState.Success(payload.data)
            }
            is ResultState.Error -> ResultState.Error("Unable to fetch sales summary.")
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun getProductSales(token: String, fromDate: String, toDate: String): ResultState<List<ProductSalesItemDto>> {
        if (token.isBlank()) return ResultState.Error("Authentication token is missing.")
        return when (
            val result = safeApiCall<ProductSalesApiResponseDto> {
                client.get("api/reports/product-sales") {
                    parameter("fromDate", fromDate)
                    parameter("toDate", toDate)
                }
            }
        ) {
            is ResultState.Success -> {
                val payload = result.data
                if (!payload.success) ResultState.Error(payload.message ?: "Unable to fetch product sales")
                else ResultState.Success(payload.data)
            }
            is ResultState.Error -> ResultState.Error("Unable to fetch product sales.")
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun getNetProfit(token: String, fromDate: String, toDate: String): ResultState<NetProfitReportDto> {
        if (token.isBlank()) return ResultState.Error("Authentication token is missing.")
        return when (
            val result = safeApiCall<NetProfitApiResponseDto> {
                client.get("api/reports/net-profit") {
                    parameter("fromDate", fromDate)
                    parameter("toDate", toDate)
                }
            }
        ) {
            is ResultState.Success -> {
                val payload = result.data
                if (!payload.success || payload.data == null) ResultState.Error(payload.message ?: "Unable to fetch net profit")
                else ResultState.Success(payload.data)
            }
            is ResultState.Error -> ResultState.Error("Unable to fetch net profit.")
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun getExpenseSummary(token: String, fromDate: String, toDate: String): ResultState<List<ExpenseSummaryItemDto>> {
        if (token.isBlank()) return ResultState.Error("Authentication token is missing.")
        return when (
            val result = safeApiCall<ExpenseSummaryApiResponseDto> {
                client.get("api/reports/expense-summary") {
                    parameter("fromDate", fromDate)
                    parameter("toDate", toDate)
                }
            }
        ) {
            is ResultState.Success -> {
                val payload = result.data
                if (!payload.success) ResultState.Error(payload.message ?: "Unable to fetch expense summary")
                else ResultState.Success(payload.data)
            }
            is ResultState.Error -> ResultState.Error("Unable to fetch expense summary.")
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun getCustomerLedger(
        token: String,
        customerId: Long,
        fromDate: String?,
        toDate: String?
    ): ResultState<List<CustomerLedgerEntryDto>> {
        if (token.isBlank()) return ResultState.Error("Authentication token is missing.")
        return when (
            val result = safeApiCall<CustomerLedgerApiResponseDto> {
                client.get("api/ledger/customer/$customerId") {
                    if (!fromDate.isNullOrBlank()) parameter("fromDate", fromDate)
                    if (!toDate.isNullOrBlank()) parameter("toDate", toDate)
                }
            }
        ) {
            is ResultState.Success -> {
                val payload = result.data
                if (!payload.success) ResultState.Error(payload.message ?: "Unable to fetch ledger")
                else ResultState.Success(payload.data)
            }
            is ResultState.Error -> ResultState.Error("Unable to fetch customer ledger.")
            ResultState.Loading -> ResultState.Loading
        }
    }
}
