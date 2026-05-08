package com.havos.lubricerp.feature_reports.domain.model

data class TankStockSummary(
    val totalCapacityLiters: Int,
    val currentStockLiters: Int,
    val availableCapacityLiters: Int,
    val tanks: List<TankInfo>
)

data class TankInfo(
    val name: String,
    val code: String,
    val location: String,
    val productGrade: String,
    val capacityLiters: Int,
    val currentStockLiters: Int,
    val availableLiters: Int,
    val fillPercent: Int
)

data class RawMaterialStockItem(
    val code: String,
    val name: String,
    val type: String,
    val uom: String,
    val costPerUnit: Double,
    val reorderLevel: Double
)

data class DateRangeFilter(
    val fromDate: String,
    val toDate: String
)

data class PackagingLossGainReport(
    val totalPlannedLiters: Double,
    val totalActualLiters: Double,
    val totalVarianceLiters: Double,
    val rows: List<PackagingLossGainRow>
)

data class PackagingLossGainRow(
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

data class DashboardSummary(
    val todaySalesAmount: Double,
    val todaySalesCount: Int,
    val monthlySalesAmount: Double,
    val monthlySalesCount: Int,
    val outstandingReceivables: Double,
    val pendingPayables: Double,
    val lowStockAlertCount: Int,
    val topSellingProducts: List<String>,
    val recentInvoices: List<RecentInvoice>
)

data class RecentInvoice(
    val id: Long,
    val invoiceNumber: String,
    val customerName: String,
    val amount: Double,
    val date: String,
    val paymentStatus: String
)

data class PaymentReceivedItem(
    val receiptNumber: String,
    val customerName: String,
    val invoiceNumber: String,
    val amount: Double,
    val paymentMode: String
)

data class SalesSummaryItem(
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

data class Customer(
    val id: Long,
    val name: String,
    val code: String,
    val phone: String,
    val email: String,
    val address: String,
    val gstNumber: String?,
    val state: String
)

data class CustomerLedgerEntry(
    val date: String,
    val type: String,
    val refNumber: String,
    val debit: Double,
    val credit: Double,
    val runningBalance: Double
)

data class ReportSalesSummaryItem(
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

data class ProductSalesItem(
    val productGrade: String,
    val deliveryType: String,
    val totalQuantity: Double,
    val totalAmount: Double
)

data class NetProfitReport(
    val totalRevenue: Double,
    val totalPurchaseCost: Double,
    val netProfit: Double,
    val fromDate: String,
    val toDate: String
)

data class ExpenseSummaryItem(
    val vendorName: String,
    val paymentCount: Int,
    val totalPaid: Double
)

data class StockOverviewTankItem(
    val tankId: Int,
    val tankName: String,
    val tankCode: String,
    val locationName: String,
    val productGrade: String?,
    val capacityLiters: Double,
    val currentStockLiters: Double,
    val fillPercentage: Double,
    val availableCapacity: Double,
    val stockInMT: Double,
    val capacityInMT: Double
)
