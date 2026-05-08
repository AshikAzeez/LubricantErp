package com.havos.lubricerp.feature_reports.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class TankStockSummaryDto(
    val totalCapacityLiters: Int,
    val currentStockLiters: Int,
    val availableCapacityLiters: Int,
    val tanks: List<TankInfoDto>
)

@Serializable
data class TankInfoDto(
    val name: String,
    val code: String,
    val location: String,
    val productGrade: String,
    val capacityLiters: Int,
    val currentStockLiters: Int,
    val availableLiters: Int,
    val fillPercent: Int
)

@Serializable
data class RawMaterialStockItemDto(
    val code: String,
    val name: String,
    val type: String,
    val uom: String,
    val costPerUnit: Double,
    val reorderLevel: Double
)

@Serializable
data class PackagingLossGainReportDto(
    val totalPlannedLiters: Double,
    val totalActualLiters: Double,
    val totalVarianceLiters: Double,
    val rows: List<PackagingLossGainRowDto>
)

@Serializable
data class PackagingLossGainRowDto(
    val orderNo: String,
    val date: String,
    val productGrade: String,
    val sourceTank: String,
    val plannedLiters: Double,
    val actualLiters: Double,
    val varianceLiters: Double,
    val variancePercent: Double,
    val status: String
)

@Serializable
data class DashboardApiResponseDto(
    val success: Boolean,
    val data: DashboardDto? = null,
    val message: String? = null,
    val errors: List<String>? = null
)

@Serializable
data class DashboardDto(
    val todaySalesAmount: Double = 0.0,
    val todaySalesCount: Int = 0,
    val monthlySalesAmount: Double = 0.0,
    val monthlySalesCount: Int = 0,
    val outstandingReceivables: Double = 0.0,
    val pendingPayables: Double = 0.0,
    val lowStockAlertCount: Int = 0,
    val topSellingProducts: List<String> = emptyList(),
    val recentInvoices: List<RecentInvoiceDto> = emptyList()
)

@Serializable
data class RecentInvoiceDto(
    val id: Long,
    val invoiceNumber: String,
    val customerName: String,
    val amount: Double,
    val date: String,
    val paymentStatus: String
)

@Serializable
data class SalesSummaryApiResponseDto(
    val success: Boolean,
    val data: List<SalesSummaryItemDto> = emptyList(),
    val message: String? = null,
    val errors: List<String>? = null
)

@Serializable
data class PaymentReceivedApiResponseDto(
    val success: Boolean,
    val data: List<PaymentReceivedItemDto> = emptyList(),
    val message: String? = null,
    val errors: List<String>? = null
)

@Serializable
data class PaymentReceivedItemDto(
    val receiptNumber: String,
    val customerName: String,
    val invoiceNumber: String,
    val amount: Double,
    val paymentMode: String
)

@Serializable
data class CustomerDto(
    val id: Long,
    val name: String,
    val code: String,
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val gSTNumber: String? = null,
    val state: String = ""
)

@Serializable
data class CustomerListApiResponseDto(
    val success: Boolean,
    val data: List<CustomerDto> = emptyList(),
    val message: String? = null,
    val errors: List<String>? = null
)

@Serializable
data class CustomerLedgerEntryDto(
    val date: String,
    val type: String,
    val refNumber: String,
    val debit: Double = 0.0,
    val credit: Double = 0.0,
    val runningBalance: Double = 0.0
)

@Serializable
data class CustomerLedgerApiResponseDto(
    val success: Boolean,
    val data: List<CustomerLedgerEntryDto> = emptyList(),
    val message: String? = null,
    val errors: List<String>? = null
)

@Serializable
data class StockOverviewTankItemDto(
    val tankId: Int,
    val tankName: String,
    val tankCode: String,
    val locationName: String,
    val productGrade: String? = null,
    val capacityLiters: Double,
    val currentStockLiters: Double,
    val fillPercentage: Double,
    val availableCapacity: Double,
    val stockInMT: Double,
    val capacityInMT: Double
)

@Serializable
data class StockOverviewTankApiResponseDto(
    val success: Boolean,
    val data: List<StockOverviewTankItemDto> = emptyList(),
    val message: String? = null,
    val errors: List<String>? = null
)

@Serializable
data class ReportSalesSummaryItemDto(
    val customerId: Long,
    val customerName: String,
    val customerCode: String,
    val invoiceCount: Int,
    val subTotal: Double = 0.0,
    val taxAmount: Double = 0.0,
    val totalAmount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val balanceAmount: Double = 0.0
)

@Serializable
data class ReportSalesSummaryApiResponseDto(
    val success: Boolean,
    val data: List<ReportSalesSummaryItemDto> = emptyList(),
    val message: String? = null,
    val errors: List<String>? = null
)

@Serializable
data class ProductSalesItemDto(
    val productGrade: String = "",
    val deliveryType: String = "",
    val totalQuantity: Double = 0.0,
    val totalAmount: Double = 0.0
)

@Serializable
data class ProductSalesApiResponseDto(
    val success: Boolean,
    val data: List<ProductSalesItemDto> = emptyList(),
    val message: String? = null,
    val errors: List<String>? = null
)

@Serializable
data class NetProfitReportDto(
    val totalRevenue: Double = 0.0,
    val totalPurchaseCost: Double = 0.0,
    val netProfit: Double = 0.0,
    val fromDate: String = "",
    val toDate: String = ""
)

@Serializable
data class NetProfitApiResponseDto(
    val success: Boolean,
    val data: NetProfitReportDto? = null,
    val message: String? = null,
    val errors: List<String>? = null
)

@Serializable
data class ExpenseSummaryItemDto(
    val vendorName: String = "",
    val paymentCount: Int = 0,
    val totalPaid: Double = 0.0
)

@Serializable
data class ExpenseSummaryApiResponseDto(
    val success: Boolean,
    val data: List<ExpenseSummaryItemDto> = emptyList(),
    val message: String? = null,
    val errors: List<String>? = null
)

@Serializable
data class CustomerMobileSummaryDto(
    val id: Long,
    val name: String,
    val code: String,
    val phone: String = "",
    val email: String = "",
    val gstNumber: String = "",
    val address: String = "",
    val outstandingAmount: Double = 0.0,
    val overdueAmount: Double = 0.0,
    val lastPurchaseDate: String? = null,
    val lastInvoiceNumber: String? = null,
    val totalLifetimePurchases: Double = 0.0
)

@Serializable
data class CustomerMobileSummaryApiResponseDto(
    val success: Boolean,
    val data: CustomerMobileSummaryDto? = null,
    val message: String? = null,
    val errors: List<String>? = null
)

@Serializable
data class SalesSummaryItemDto(
    val customerId: Long,
    val customerName: String,
    val customerCode: String,
    val invoiceCount: Int,
    val subTotal: Double,
    val taxAmount: Double,
    val totalAmount: Double,
    val paidAmount: Double,
    val balanceAmount: Double
)
