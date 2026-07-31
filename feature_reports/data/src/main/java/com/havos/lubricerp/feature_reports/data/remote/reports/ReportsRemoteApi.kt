package com.havos.lubricerp.feature_reports.data.remote.reports

import com.havos.lubricerp.core.common.ResultState
import com.havos.lubricerp.core.network.safeApiCall
import com.havos.lubricerp.feature_reports.data.dto.AccountsSummaryApiResponseDto
import com.havos.lubricerp.feature_reports.data.dto.AccountsSummaryDto
import com.havos.lubricerp.feature_reports.data.dto.CashPositionApiResponseDto
import com.havos.lubricerp.feature_reports.data.dto.CashPositionDto
import com.havos.lubricerp.feature_reports.data.dto.PurchaseSummaryApiResponseDto
import com.havos.lubricerp.feature_reports.data.dto.PurchaseSummaryDto
import com.havos.lubricerp.feature_reports.data.dto.ReceivablesAgingApiResponseDto
import com.havos.lubricerp.feature_reports.data.dto.ReceivablesAgingDto
import com.havos.lubricerp.feature_reports.data.dto.ConsolidatedStockApiResponseDto
import com.havos.lubricerp.feature_reports.data.dto.ConsolidatedStockItemDto
import com.havos.lubricerp.feature_reports.data.dto.CostBreakdownDetailApiResponseDto
import com.havos.lubricerp.feature_reports.data.dto.CostBreakdownDetailDto
import com.havos.lubricerp.feature_reports.data.dto.CostBreakdownListApiResponseDto
import com.havos.lubricerp.feature_reports.data.dto.CostBreakdownPagedDataDto
import com.havos.lubricerp.feature_reports.data.dto.CustomerDto
import com.havos.lubricerp.feature_reports.data.dto.CustomerLedgerApiResponseDto
import com.havos.lubricerp.feature_reports.data.dto.CustomerLedgerEntryDto
import com.havos.lubricerp.feature_reports.data.dto.CustomerListApiResponseDto
import com.havos.lubricerp.feature_reports.data.dto.CustomerMobileSummaryApiResponseDto
import com.havos.lubricerp.feature_reports.data.dto.CustomerMobileSummaryDto
import com.havos.lubricerp.feature_reports.data.dto.DashboardApiResponseDto
import com.havos.lubricerp.feature_reports.data.dto.DashboardDto
import com.havos.lubricerp.feature_reports.data.dto.ExpenseSummaryApiResponseDto
import com.havos.lubricerp.feature_reports.data.dto.ExpenseSummaryItemDto
import com.havos.lubricerp.feature_reports.data.dto.FastMovingApiResponseDto
import com.havos.lubricerp.feature_reports.data.dto.FastMovingItemDto
import com.havos.lubricerp.feature_reports.data.dto.LowStockApiResponseDto
import com.havos.lubricerp.feature_reports.data.dto.LowStockItemDto
import com.havos.lubricerp.feature_reports.data.dto.NetProfitApiResponseDto
import com.havos.lubricerp.feature_reports.data.dto.NetProfitReportDto
import com.havos.lubricerp.feature_reports.data.dto.PackagingLossGainReportDto
import com.havos.lubricerp.feature_reports.data.dto.PaymentPendingApiResponseDto
import com.havos.lubricerp.feature_reports.data.dto.PaymentPendingCustomerDto
import com.havos.lubricerp.feature_reports.data.dto.PaymentPendingPagedDataDto
import com.havos.lubricerp.feature_reports.data.dto.PaymentReceivedApiResponseDto
import com.havos.lubricerp.feature_reports.data.dto.PaymentReceivedItemDto
import com.havos.lubricerp.feature_reports.data.dto.PaymentReceivedPagedDataDto
import com.havos.lubricerp.feature_reports.data.dto.ProformaInvoicePagedDataDto
import com.havos.lubricerp.feature_reports.data.dto.SalesInvoicePagedDataDto
import com.havos.lubricerp.feature_reports.data.dto.ProductSalesApiResponseDto
import com.havos.lubricerp.feature_reports.data.dto.ProductSalesItemDto
import com.havos.lubricerp.feature_reports.data.dto.RawMaterialListApiResponseDto
import com.havos.lubricerp.feature_reports.data.dto.RawMaterialStockItemDto
import com.havos.lubricerp.feature_reports.data.dto.RecordPaymentApiResponseDto
import com.havos.lubricerp.feature_reports.data.dto.RecordPaymentRequestDto
import com.havos.lubricerp.feature_reports.data.dto.RecordPaymentResponseDto
import com.havos.lubricerp.feature_reports.data.dto.SalesInvoiceDetailApiResponseDto
import com.havos.lubricerp.feature_reports.data.dto.SalesInvoiceDetailDto
import com.havos.lubricerp.feature_reports.data.dto.SalesInvoiceItemDto
import com.havos.lubricerp.feature_reports.data.dto.SalesInvoiceListApiResponseDto
import com.havos.lubricerp.feature_reports.data.dto.SalesOrderDetailApiResponseDto
import com.havos.lubricerp.feature_reports.data.dto.SalesOrderDetailDto
import com.havos.lubricerp.feature_reports.data.dto.SalesOrderItemDto
import com.havos.lubricerp.feature_reports.data.dto.SalesOrderPagedDataDto
import com.havos.lubricerp.feature_reports.data.dto.SalesOrderListApiResponseDto
import com.havos.lubricerp.feature_reports.data.dto.SalesSummaryApiResponseDto
import com.havos.lubricerp.feature_reports.data.dto.SalesSummaryItemDto
import com.havos.lubricerp.feature_reports.data.dto.StockOverviewTankApiResponseDto
import com.havos.lubricerp.feature_reports.data.dto.StockOverviewTankItemDto
import com.havos.lubricerp.feature_reports.data.dto.TankStockListApiResponseDto
import com.havos.lubricerp.feature_reports.data.dto.TankStockItemDto
import com.havos.lubricerp.feature_reports.data.dto.CustomerLedgerPagedDataDto
import com.havos.lubricerp.feature_reports.data.dto.WarehouseStockApiResponseDto
import com.havos.lubricerp.feature_reports.data.dto.WarehouseStockItemDto
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders

class ReportsRemoteApi(
    private val client: HttpClient
) : ReportsRemoteDataSource {

    override suspend fun getTankStockSummary(token: String): ResultState<List<TankStockItemDto>> {
        if (token.isBlank()) return ResultState.Error("Authentication token is missing.")
        return when (
            val result = safeApiCall<TankStockListApiResponseDto> {
                client.get("api/reports/tank-stock") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            }
        ) {
            is ResultState.Success -> {
                val payload = result.data
                if (!payload.success) ResultState.Error(payload.message ?: "Unable to fetch tank stock")
                else ResultState.Success(payload.data)
            }
            is ResultState.Error -> ResultState.Error("Unable to fetch tank stock")
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun getRawMaterialStock(token: String): ResultState<List<RawMaterialStockItemDto>> {
        if (token.isBlank()) return ResultState.Error("Authentication token is missing.")
        return when (
            val result = safeApiCall<RawMaterialListApiResponseDto> {
                client.get("api/raw-materials") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            }
        ) {
            is ResultState.Success -> {
                val payload = result.data
                if (!payload.success) ResultState.Error(payload.message ?: "Unable to fetch raw materials")
                else ResultState.Success(payload.data)
            }
            is ResultState.Error -> ResultState.Error("Unable to fetch raw materials.")
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun getPackagingLossGain(
        fromDate: String,
        toDate: String
    ): ResultState<PackagingLossGainReportDto> {
        return when (
            val result = safeApiCall<PackagingLossGainReportDto> {
                client.get("api/reports/packaging-loss-gain") {
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
                client.get("api/dashboard") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
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
                    header(HttpHeaders.Authorization, "Bearer $token")
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
        toDate: String,
        skip: Int,
        take: Int
    ): ResultState<PaymentReceivedPagedDataDto> {
        if (token.isBlank()) return ResultState.Error("Authentication token is missing.")

        return when (
            val result = safeApiCall<PaymentReceivedApiResponseDto> {
                client.get("api/payments/received") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    parameter("fromDate", fromDate)
                    parameter("toDate", toDate)
                    parameter("skip", skip)
                    parameter("take", take)
                }
            }
        ) {
            is ResultState.Success -> {
                val payload = result.data
                val pagedData = payload.data
                if (!payload.success || pagedData == null) {
                    val serverMessage = payload.message?.takeIf { it.isNotBlank() }
                        ?: payload.errors?.firstOrNull()?.takeIf { it.isNotBlank() }
                        ?: "Unable to fetch payments received"
                    ResultState.Error(serverMessage)
                } else {
                    ResultState.Success(pagedData)
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
                client.get("api/reports/tank-stock") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
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
                client.get("api/customers") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
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
                client.get("api/customers/$customerId/mobile-summary") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
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

    override suspend fun getProductSales(token: String, fromDate: String, toDate: String): ResultState<List<ProductSalesItemDto>> {
        if (token.isBlank()) return ResultState.Error("Authentication token is missing.")
        return when (
            val result = safeApiCall<ProductSalesApiResponseDto> {
                client.get("api/reports/product-sales") {
                    header(HttpHeaders.Authorization, "Bearer $token")
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
                    header(HttpHeaders.Authorization, "Bearer $token")
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
                    header(HttpHeaders.Authorization, "Bearer $token")
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

    override suspend fun getWarehouseStock(token: String, warehouseId: Int?): ResultState<List<WarehouseStockItemDto>> {
        if (token.isBlank()) return ResultState.Error("Authentication token is missing.")
        return when (val result = safeApiCall<WarehouseStockApiResponseDto> {
            client.get("api/reports/warehouse-stock") {
                header(HttpHeaders.Authorization, "Bearer $token")
                if (warehouseId != null) parameter("warehouseId", warehouseId)
            }
        }) {
            is ResultState.Success -> {
                val payload = result.data
                if (!payload.success) ResultState.Error(payload.message ?: "Unable to fetch warehouse stock")
                else ResultState.Success(payload.data)
            }
            is ResultState.Error -> ResultState.Error("Unable to fetch warehouse stock.")
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun getConsolidatedStock(token: String): ResultState<List<ConsolidatedStockItemDto>> {
        if (token.isBlank()) return ResultState.Error("Authentication token is missing.")
        return when (val result = safeApiCall<ConsolidatedStockApiResponseDto> {
            client.get("api/reports/consolidated-stock") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }) {
            is ResultState.Success -> {
                val payload = result.data
                if (!payload.success) ResultState.Error(payload.message ?: "Unable to fetch consolidated stock")
                else ResultState.Success(payload.data)
            }
            is ResultState.Error -> ResultState.Error("Unable to fetch consolidated stock.")
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun getLowStock(token: String, threshold: Int): ResultState<List<LowStockItemDto>> {
        if (token.isBlank()) return ResultState.Error("Authentication token is missing.")
        return when (val result = safeApiCall<LowStockApiResponseDto> {
            client.get("api/reports/low-stock") {
                header(HttpHeaders.Authorization, "Bearer $token")
                parameter("threshold", threshold)
            }
        }) {
            is ResultState.Success -> {
                val payload = result.data
                if (!payload.success) ResultState.Error(payload.message ?: "Unable to fetch low stock")
                else ResultState.Success(payload.data)
            }
            is ResultState.Error -> ResultState.Error("Unable to fetch low stock.")
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun getFastMoving(token: String, days: Int, top: Int): ResultState<List<FastMovingItemDto>> {
        if (token.isBlank()) return ResultState.Error("Authentication token is missing.")
        return when (val result = safeApiCall<FastMovingApiResponseDto> {
            client.get("api/reports/fast-moving") {
                header(HttpHeaders.Authorization, "Bearer $token")
                parameter("days", days)
                parameter("top", top)
            }
        }) {
            is ResultState.Success -> {
                val payload = result.data
                if (!payload.success) ResultState.Error(payload.message ?: "Unable to fetch fast moving")
                else ResultState.Success(payload.data)
            }
            is ResultState.Error -> ResultState.Error("Unable to fetch fast moving.")
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun recordPayment(token: String, request: RecordPaymentRequestDto): ResultState<RecordPaymentResponseDto> {
        if (token.isBlank()) return ResultState.Error("Authentication token is missing.")
        return when (val result = safeApiCall<RecordPaymentApiResponseDto> {
            client.post("api/payments") {
                header(HttpHeaders.Authorization, "Bearer $token")
                header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                header(HttpHeaders.Accept, ContentType.Application.Json.toString())
                setBody(request)
            }
        }) {
            is ResultState.Success -> {
                val payload = result.data
                if (!payload.success || payload.data == null)
                    ResultState.Error(payload.message ?: "Unable to record payment")
                else
                    ResultState.Success(payload.data)
            }
            is ResultState.Error -> ResultState.Error("Unable to record payment.")
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun getPaymentsPending(
        token: String,
        skip: Int,
        take: Int
    ): ResultState<PaymentPendingPagedDataDto> {
        if (token.isBlank()) return ResultState.Error("Authentication token is missing.")
        return when (val result = safeApiCall<PaymentPendingApiResponseDto> {
            client.get("api/payments/pending") {
                header(HttpHeaders.Authorization, "Bearer $token")
                parameter("skip", skip)
                parameter("take", take)
            }
        }) {
            is ResultState.Success -> {
                val payload = result.data
                val pagedData = payload.data
                if (!payload.success || pagedData == null)
                    ResultState.Error(payload.message ?: "Unable to fetch pending payments")
                else ResultState.Success(pagedData)
            }
            is ResultState.Error -> ResultState.Error("Unable to fetch pending payments.")
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun getAccountsSummary(token: String, fromDate: String, toDate: String): ResultState<AccountsSummaryDto> {
        if (token.isBlank()) return ResultState.Error("Authentication token is missing.")
        return when (val result = safeApiCall<AccountsSummaryApiResponseDto> {
            client.get("api/reports/accounts-summary") {
                header(HttpHeaders.Authorization, "Bearer $token")
                parameter("fromDate", fromDate)
                parameter("toDate", toDate)
            }
        }) {
            is ResultState.Success -> {
                val payload = result.data
                if (!payload.success || payload.data == null)
                    ResultState.Error(payload.message ?: "Unable to fetch accounts summary")
                else ResultState.Success(payload.data)
            }
            is ResultState.Error -> ResultState.Error("Unable to fetch accounts summary.")
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun getSalesOrders(
        token: String,
        status: String,
        skip: Int,
        take: Int
    ): ResultState<SalesOrderPagedDataDto> {
        if (token.isBlank()) return ResultState.Error("Authentication token is missing.")
        return when (val result = safeApiCall<SalesOrderListApiResponseDto> {
            client.get("api/sales-orders") {
                header(HttpHeaders.Authorization, "Bearer $token")
                parameter("status", status)
                parameter("skip", skip)
                parameter("take", take)
            }
        }) {
            is ResultState.Success -> {
                val payload = result.data
                val pagedData = payload.data
                if (!payload.success || pagedData == null)
                    ResultState.Error(payload.message ?: "Unable to fetch sales orders")
                else ResultState.Success(pagedData)
            }
            is ResultState.Error -> ResultState.Error("Unable to fetch sales orders.")
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun getSalesOrderDetail(
        token: String,
        orderId: Long
    ): ResultState<SalesOrderDetailDto> {
        if (token.isBlank()) return ResultState.Error("Authentication token is missing.")
        return when (val result = safeApiCall<SalesOrderDetailApiResponseDto> {
            client.get("api/sales-orders/$orderId") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }) {
            is ResultState.Success -> {
                val payload = result.data
                if (!payload.success || payload.data == null)
                    ResultState.Error(payload.message ?: "Unable to fetch order detail")
                else ResultState.Success(payload.data)
            }
            is ResultState.Error -> ResultState.Error("Unable to fetch order detail.")
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun getSalesInvoices(
        token: String,
        fromDate: String?,
        toDate: String?,
        paymentStatus: String?,
        skip: Int,
        take: Int
    ): ResultState<SalesInvoicePagedDataDto> {
        if (token.isBlank()) return ResultState.Error("Authentication token is missing.")
        return when (val result = safeApiCall<SalesInvoiceListApiResponseDto> {
            client.get("api/sales-invoices") {
                header(HttpHeaders.Authorization, "Bearer $token")
                if (!fromDate.isNullOrBlank()) parameter("fromDate", fromDate)
                if (!toDate.isNullOrBlank()) parameter("toDate", toDate)
                if (!paymentStatus.isNullOrBlank()) parameter("paymentStatus", paymentStatus)
                parameter("skip", skip)
                parameter("take", take)
            }
        }) {
            is ResultState.Success -> {
                val payload = result.data
                val pagedData = payload.data
                if (!payload.success || pagedData == null)
                    ResultState.Error(payload.message ?: "Unable to fetch invoices")
                else ResultState.Success(pagedData)
            }
            is ResultState.Error -> ResultState.Error("Unable to fetch invoices.")
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun getSalesInvoiceDetail(
        token: String,
        invoiceId: Long
    ): ResultState<SalesInvoiceDetailDto> {
        if (token.isBlank()) return ResultState.Error("Authentication token is missing.")
        return when (val result = safeApiCall<SalesInvoiceDetailApiResponseDto> {
            client.get("api/sales-invoices/$invoiceId") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }) {
            is ResultState.Success -> {
                val payload = result.data
                if (!payload.success || payload.data == null)
                    ResultState.Error(payload.message ?: "Unable to fetch invoice detail")
                else ResultState.Success(payload.data)
            }
            is ResultState.Error -> ResultState.Error("Unable to fetch invoice detail.")
            ResultState.Loading -> ResultState.Loading
        }
    }

    // ── Dashboard: Receivables Aging ─────────────────────────────────────────
    override suspend fun getReceivablesAging(token: String): ResultState<ReceivablesAgingDto> {
        if (token.isBlank()) return ResultState.Error("Authentication token is missing.")
        return when (val result = safeApiCall<ReceivablesAgingApiResponseDto> {
            client.get("api/dashboard/receivables-aging") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }) {
            is ResultState.Success -> {
                val payload = result.data
                if (!payload.success || payload.data == null)
                    ResultState.Error(payload.message ?: "Unable to fetch receivables aging")
                else ResultState.Success(payload.data)
            }
            is ResultState.Error -> ResultState.Error("Unable to fetch receivables aging.")
            ResultState.Loading -> ResultState.Loading
        }
    }

    // ── Dashboard: Purchase Summary ──────────────────────────────────────────
    override suspend fun getPurchaseSummary(token: String): ResultState<PurchaseSummaryDto> {
        if (token.isBlank()) return ResultState.Error("Authentication token is missing.")
        return when (val result = safeApiCall<PurchaseSummaryApiResponseDto> {
            client.get("api/dashboard/purchase-summary") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }) {
            is ResultState.Success -> {
                val payload = result.data
                if (!payload.success || payload.data == null)
                    ResultState.Error(payload.message ?: "Unable to fetch purchase summary")
                else ResultState.Success(payload.data)
            }
            is ResultState.Error -> ResultState.Error("Unable to fetch purchase summary.")
            ResultState.Loading -> ResultState.Loading
        }
    }

    // ── Dashboard: Cash Position ─────────────────────────────────────────────
    override suspend fun getCashPosition(token: String): ResultState<CashPositionDto> {
        if (token.isBlank()) return ResultState.Error("Authentication token is missing.")
        return when (val result = safeApiCall<CashPositionApiResponseDto> {
            client.get("api/dashboard/cash-position") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }) {
            is ResultState.Success -> {
                val payload = result.data
                if (!payload.success || payload.data == null)
                    ResultState.Error(payload.message ?: "Unable to fetch cash position")
                else ResultState.Success(payload.data)
            }
            is ResultState.Error -> ResultState.Error("Unable to fetch cash position.")
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
    ): ResultState<CustomerLedgerPagedDataDto> {
        if (token.isBlank()) return ResultState.Error("Authentication token is missing.")
        return when (
            val result = safeApiCall<CustomerLedgerApiResponseDto> {
                client.get("api/ledger/customer/$customerId") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    if (!fromDate.isNullOrBlank()) parameter("fromDate", fromDate)
                    if (!toDate.isNullOrBlank()) parameter("toDate", toDate)
                    parameter("skip", skip)
                    parameter("take", take)
                }
            }
        ) {
            is ResultState.Success -> {
                val payload = result.data
                val pagedData = payload.data
                if (!payload.success || pagedData == null)
                    ResultState.Error(payload.message ?: "Unable to fetch ledger")
                else ResultState.Success(pagedData)
            }
            is ResultState.Error -> ResultState.Error("Unable to fetch customer ledger.")
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun getProformaInvoices(
        token: String,
        status: String?,
        skip: Int,
        take: Int
    ): ResultState<ProformaInvoicePagedDataDto> {
        if (token.isBlank()) return ResultState.Error("Authentication token is missing.")
        return when (val result = safeApiCall<com.havos.lubricerp.feature_reports.data.dto.ProformaInvoiceListApiResponseDto> {
            client.get("api/proforma-invoices") {
                header(HttpHeaders.Authorization, "Bearer $token")
                if (!status.isNullOrBlank()) parameter("status", status)
                parameter("skip", skip)
                parameter("take", take)
            }
        }) {
            is ResultState.Success -> {
                val payload = result.data
                val pagedData = payload.data
                if (!payload.success || pagedData == null)
                    ResultState.Error(payload.message ?: "Unable to fetch proforma invoices")
                else ResultState.Success(pagedData)
            }
            is ResultState.Error -> ResultState.Error("Unable to fetch proforma invoices.")
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun getProformaInvoiceDetail(
        token: String,
        id: Long
    ): ResultState<com.havos.lubricerp.feature_reports.data.dto.ProformaInvoiceDetailDto> {
        if (token.isBlank()) return ResultState.Error("Authentication token is missing.")
        return when (val result = safeApiCall<com.havos.lubricerp.feature_reports.data.dto.ProformaInvoiceDetailApiResponseDto> {
            client.get("api/proforma-invoices/$id") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }) {
            is ResultState.Success -> {
                val payload = result.data
                val details = payload.data
                if (!payload.success || details == null) {
                    ResultState.Error(payload.message ?: "Unable to fetch proforma invoice detail")
                } else {
                    ResultState.Success(details)
                }
            }
            is ResultState.Error -> ResultState.Error("Unable to fetch proforma invoice detail.")
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun createProformaInvoice(
        token: String,
        request: com.havos.lubricerp.feature_reports.data.dto.CreateProformaInvoiceRequestDto
    ): ResultState<com.havos.lubricerp.feature_reports.data.dto.ProformaInvoiceDetailDto> {
        if (token.isBlank()) return ResultState.Error("Authentication token is missing.")
        return when (val result = safeApiCall<com.havos.lubricerp.feature_reports.data.dto.CreateProformaInvoiceApiResponseDto> {
            client.post("api/proforma-invoices") {
                header(HttpHeaders.Authorization, "Bearer $token")
                header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                header(HttpHeaders.Accept, ContentType.Application.Json.toString())
                setBody(request)
            }
        }) {
            is ResultState.Success -> {
                val payload = result.data
                val responseData = payload.data
                if (!payload.success || responseData == null) {
                    ResultState.Error(payload.message ?: "Unable to create proforma invoice")
                } else {
                    ResultState.Success(responseData)
                }
            }
            is ResultState.Error -> ResultState.Error(result.message)
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun getProductSkus(
        token: String,
        gradeId: Int?
    ): ResultState<List<com.havos.lubricerp.feature_reports.data.dto.ProductSkuDto>> {
        if (token.isBlank()) return ResultState.Error("Authentication token is missing.")
        return when (val result = safeApiCall<com.havos.lubricerp.feature_reports.data.dto.ProductSkuListApiResponseDto> {
            client.get("api/product-skus") {
                header(HttpHeaders.Authorization, "Bearer $token")
                if (gradeId != null) parameter("gradeId", gradeId)
            }
        }) {
            is ResultState.Success -> {
                val payload = result.data
                if (!payload.success) ResultState.Error(payload.message ?: "Unable to fetch product SKUs")
                else ResultState.Success(payload.data)
            }
            is ResultState.Error -> ResultState.Error("Unable to fetch product SKUs.")
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun updateProformaInvoice(
        token: String,
        id: Long,
        request: com.havos.lubricerp.feature_reports.data.dto.CreateProformaInvoiceRequestDto
    ): ResultState<Unit> {
        if (token.isBlank()) return ResultState.Error("Authentication token is missing.")
        return when (val result = safeApiCall<com.havos.lubricerp.feature_reports.data.dto.UpdateProformaInvoiceApiResponseDto> {
            client.put("api/proforma-invoices/$id") {
                header(HttpHeaders.Authorization, "Bearer $token")
                header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                header(HttpHeaders.Accept, ContentType.Application.Json.toString())
                setBody(request)
            }
        }) {
            is ResultState.Success -> {
                val payload = result.data
                if (!payload.success) ResultState.Error(payload.message ?: "Unable to update proforma invoice")
                else ResultState.Success(Unit)
            }
            is ResultState.Error -> ResultState.Error(result.message)
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun sendProformaInvoice(
        token: String,
        id: Long
    ): ResultState<Unit> {
        if (token.isBlank()) return ResultState.Error("Authentication token is missing.")
        return when (val result = safeApiCall<com.havos.lubricerp.feature_reports.data.dto.UpdateProformaInvoiceApiResponseDto> {
            client.post("api/proforma-invoices/$id/send") {
                header(HttpHeaders.Authorization, "Bearer $token")
                header(HttpHeaders.Accept, ContentType.Application.Json.toString())
            }
        }) {
            is ResultState.Success -> {
                val payload = result.data
                if (!payload.success) ResultState.Error(payload.message ?: "Unable to send proforma invoice")
                else ResultState.Success(Unit)
            }
            is ResultState.Error -> ResultState.Error(result.message)
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun cancelProformaInvoice(
        token: String,
        id: Long
    ): ResultState<Unit> {
        if (token.isBlank()) return ResultState.Error("Authentication token is missing.")
        return when (val result = safeApiCall<com.havos.lubricerp.feature_reports.data.dto.UpdateProformaInvoiceApiResponseDto> {
            client.post("api/proforma-invoices/$id/cancel") {
                header(HttpHeaders.Authorization, "Bearer $token")
                header(HttpHeaders.Accept, ContentType.Application.Json.toString())
            }
        }) {
            is ResultState.Success -> {
                val payload = result.data
                if (!payload.success) ResultState.Error(payload.message ?: "Unable to cancel proforma invoice")
                else ResultState.Success(Unit)
            }
            is ResultState.Error -> ResultState.Error(result.message)
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun getCostBreakdownSheets(
        token: String,
        skip: Int,
        take: Int
    ): ResultState<CostBreakdownPagedDataDto> {
        if (token.isBlank()) return ResultState.Error("Authentication token is missing.")
        return when (val result = safeApiCall<CostBreakdownListApiResponseDto> {
            client.get("api/cost-breakdown-sheets") {
                header(HttpHeaders.Authorization, "Bearer $token")
                parameter("skip", skip)
                parameter("take", take)
            }
        }) {
            is ResultState.Success -> {
                val payload = result.data
                val pagedData = payload.data
                if (!payload.success || pagedData == null) {
                    ResultState.Error(
                        payload.message?.takeIf { it.isNotBlank() }
                            ?: payload.errors?.firstOrNull()?.takeIf { it.isNotBlank() }
                            ?: "Unable to fetch cost breakdown sheets"
                    )
                } else {
                    ResultState.Success(pagedData)
                }
            }
            is ResultState.Error -> ResultState.Error(result.message)
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun getCostBreakdownDetail(
        token: String,
        id: Long
    ): ResultState<CostBreakdownDetailDto> {
        if (token.isBlank()) return ResultState.Error("Authentication token is missing.")
        return when (val result = safeApiCall<CostBreakdownDetailApiResponseDto> {
            client.get("api/cost-breakdown-sheets/$id") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }) {
            is ResultState.Success -> {
                val payload = result.data
                if (!payload.success || payload.data == null) {
                    ResultState.Error(
                        payload.message?.takeIf { it.isNotBlank() }
                            ?: payload.errors?.firstOrNull()?.takeIf { it.isNotBlank() }
                            ?: "Unable to fetch cost breakdown detail"
                    )
                } else {
                    ResultState.Success(payload.data)
                }
            }
            is ResultState.Error -> ResultState.Error(result.message)
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun createCostBreakdown(
        token: String,
        request: com.havos.lubricerp.feature_reports.data.dto.CreateCostBreakdownRequestDto
    ): ResultState<CostBreakdownDetailDto> {
        if (token.isBlank()) return ResultState.Error("Authentication token is missing.")
        return when (val result = safeApiCall<CostBreakdownDetailApiResponseDto> {
            client.post("api/cost-breakdown-sheets") {
                header(HttpHeaders.Authorization, "Bearer $token")
                header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                header(HttpHeaders.Accept, ContentType.Application.Json.toString())
                setBody(request)
            }
        }) {
            is ResultState.Success -> {
                val payload = result.data
                if (!payload.success || payload.data == null) {
                    ResultState.Error(
                        payload.message?.takeIf { it.isNotBlank() }
                            ?: payload.errors?.firstOrNull()?.takeIf { it.isNotBlank() }
                            ?: "Unable to create cost breakdown sheet"
                    )
                } else {
                    ResultState.Success(payload.data)
                }
            }
            is ResultState.Error -> ResultState.Error(result.message)
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun updateCostBreakdown(
        token: String,
        id: Long,
        request: com.havos.lubricerp.feature_reports.data.dto.CreateCostBreakdownRequestDto
    ): ResultState<Unit> {
        if (token.isBlank()) return ResultState.Error("Authentication token is missing.")
        return when (val result = safeApiCall<com.havos.lubricerp.feature_reports.data.dto.UpdateCostBreakdownApiResponseDto> {
            client.put("api/cost-breakdown-sheets/$id") {
                header(HttpHeaders.Authorization, "Bearer $token")
                header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                header(HttpHeaders.Accept, ContentType.Application.Json.toString())
                setBody(request)
            }
        }) {
            is ResultState.Success -> {
                val payload = result.data
                if (!payload.success) {
                    ResultState.Error(
                        payload.message?.takeIf { it.isNotBlank() }
                            ?: payload.errors?.firstOrNull()?.takeIf { it.isNotBlank() }
                            ?: "Unable to update cost breakdown sheet"
                    )
                } else {
                    ResultState.Success(Unit)
                }
            }
            is ResultState.Error -> ResultState.Error(result.message)
            ResultState.Loading -> ResultState.Loading
        }
    }

    override suspend fun deleteCostBreakdown(
        token: String,
        id: Long
    ): ResultState<Unit> {
        if (token.isBlank()) return ResultState.Error("Authentication token is missing.")
        return when (val result = safeApiCall<com.havos.lubricerp.feature_reports.data.dto.DeleteCostBreakdownApiResponseDto> {
            client.delete("api/cost-breakdown-sheets/$id") {
                header(HttpHeaders.Authorization, "Bearer $token")
                header(HttpHeaders.Accept, ContentType.Application.Json.toString())
            }
        }) {
            is ResultState.Success -> {
                val payload = result.data
                if (!payload.success) {
                    ResultState.Error(
                        payload.message?.takeIf { it.isNotBlank() }
                            ?: payload.errors?.firstOrNull()?.takeIf { it.isNotBlank() }
                            ?: "Unable to delete cost breakdown sheet"
                    )
                } else {
                    ResultState.Success(Unit)
                }
            }
            is ResultState.Error -> ResultState.Error(result.message)
            ResultState.Loading -> ResultState.Loading
        }
    }
}
