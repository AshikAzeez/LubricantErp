package com.havos.lubricerp.feature_reports.domain.model

data class TankStockSummary(
    val totalCapacityLiters: Double,
    val currentStockLiters: Double,
    val availableCapacityLiters: Double,
    val tanks: List<TankInfo>
)

data class TankInfo(
    val name: String,
    val code: String,
    val location: String,
    val productGrade: String,
    val capacityLiters: Double,
    val currentStockLiters: Double,
    val availableLiters: Double,
    val fillPercent: Double
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
    val id: Long,
    val receiptNumber: String,
    val paymentDate: String,
    val customerName: String,
    val invoiceNumber: String,
    val amount: Double,
    val paymentMode: String,
    val reference: String?,
    val remarks: String?
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
    val debit: Double = 0.0,
    val credit: Double = 0.0,
    val runningBalance: Double = 0.0,
    val invoiceId: Long = 0
)

data class ProductSalesItem(
    val productSKUId: Long,
    val productSKUName: String,
    val productSKUCode: String,
    val productGrade: String,
    val productFamily: String,
    val quantitySold: Double,
    val totalRevenue: Double,
    val totalTax: Double,
    val netRevenue: Double
)

data class NetProfitReport(
    val fromDate: String,
    val toDate: String,
    val totalRevenue: Double,
    val totalTaxCollected: Double,
    val netRevenue: Double,
    val costOfGoodsSold: Double,
    val grossProfit: Double,
    val grossMarginPercent: Double,
    val operatingExpenses: Double,
    val netProfit: Double,
    val netMarginPercent: Double
)

data class ExpenseSummaryItem(
    val vendorId: Long,
    val vendorName: String,
    val vendorCode: String,
    val invoiceCount: Int,
    val totalPurchase: Double,
    val totalPaid: Double,
    val balanceDue: Double
)

data class CustomerMobileSummary(
    val id: Long,
    val name: String,
    val code: String,
    val phone: String,
    val email: String,
    val gstNumber: String,
    val address: String,
    val outstandingAmount: Double,
    val overdueAmount: Double,
    val lastPurchaseDate: String?,
    val lastInvoiceNumber: String?,
    val totalLifetimePurchases: Double
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

data class WarehouseStockItem(
    val warehouseId: Int,
    val warehouseName: String,
    val productSKUId: Int,
    val productSKUName: String,
    val productSKUCode: String,
    val productGrade: String,
    val productFamily: String,
    val currentStock: Double,
    val reorderLevel: Double
)

data class ConsolidatedStockItem(
    val itemType: String,
    val itemCode: String,
    val itemName: String,
    val quantity: Double,
    val unit: String,
    val location: String
)

data class LowStockItem(
    val productSKUId: Int,
    val productSKUName: String,
    val productSKUCode: String,
    val productGrade: String,
    val productFamily: String,
    val warehouseName: String,
    val currentStock: Double,
    val reorderLevel: Double,
    val shortageQuantity: Double
)

data class FastMovingItem(
    val rank: Int,
    val productSKUId: Int,
    val productSKUName: String,
    val productSKUCode: String,
    val productGrade: String,
    val productFamily: String,
    val quantitySold: Double,
    val totalRevenue: Double
)

data class SalesOrderItem(
    val id: Long,
    val soNumber: String,
    val soDate: String,
    val customerName: String,
    val status: String,
    val totalAmount: Double,
    val expectedDeliveryDate: String,
    val lineCount: Int,
    val deliveredPercentage: Double,
    val salesmanName: String
)

data class SalesOrderDetail(
    val id: Long,
    val soNumber: String,
    val soDate: String,
    val expectedDeliveryDate: String,
    val customerId: Long,
    val customerName: String,
    val customerCode: String,
    val customerGST: String?,
    val customerAddress: String,
    val status: String,
    val totalAmount: Double,
    val salesmanName: String,
    val remarks: String?,
    val lines: List<SalesOrderLine>,
    val deliveryNotes: List<DeliveryNote>
)

data class SalesOrderLine(
    val id: Long,
    val productSKUId: Int,
    val productSKUName: String,
    val productSKUCode: String,
    val hsnCode: String,
    val deliveryType: String,
    val quantity: Double,
    val unitPrice: Double,
    val taxRate: Double,
    val lineTotal: Double,
    val deliveredQuantity: Double,
    val pendingQuantity: Double
)

data class DeliveryNote(
    val id: Long,
    val dnNumber: String,
    val dnDate: String,
    val status: String
)

data class SalesInvoiceItem(
    val id: Long,
    val invoiceNumber: String,
    val invoiceDate: String,
    val customerName: String,
    val customerCode: String,
    val dnNumber: String?,
    val totalAmount: Double,
    val paymentStatus: String,
    val paidAmount: Double,
    val balanceAmount: Double,
    val dueDate: String,
    val isOverdue: Boolean,
    val isInterState: Boolean
)

data class SalesInvoiceDetail(
    val id: Long,
    val invoiceNumber: String,
    val invoiceDate: String,
    val dueDate: String,
    val customerId: Long,
    val customerName: String,
    val customerCode: String,
    val customerGST: String?,
    val customerAddress: String,
    val customerState: String,
    val customerStateCode: String,
    val dnNumber: String?,
    val eWayBillNumber: String?,
    val isInterState: Boolean,
    val subTotal: Double,
    val cgstAmount: Double,
    val sgstAmount: Double,
    val igstAmount: Double,
    val roundOffAmount: Double,
    val totalAmount: Double,
    val paidAmount: Double,
    val balanceAmount: Double,
    val paymentStatus: String,
    val lines: List<InvoiceLine>,
    val paymentReceipts: List<PaymentReceipt>
)

data class InvoiceLine(
    val id: Long,
    val productSKUName: String,
    val productSKUCode: String,
    val hsnCode: String,
    val description: String?,
    val quantity: Double,
    val unitOfMeasurement: String,
    val unitPrice: Double,
    val discountPercent: Double,
    val taxRate: Double,
    val lineSubTotal: Double,
    val taxAmount: Double,
    val lineTotal: Double
)

data class PaymentReceipt(
    val id: Long,
    val receiptNumber: String,
    val receiptDate: String,
    val amount: Double,
    val paymentMode: String,
    val reference: String?
)

data class RecordPaymentRequest(
    val invoiceId: Long,
    val amount: Double,
    val paymentMode: String,
    val paymentDate: String,
    val reference: String = "",
    val remarks: String = ""
)

data class RecordPaymentResponse(
    val receiptNumber: String,
    val invoiceId: Long,
    val amountPaid: Double,
    val newBalance: Double,
    val newPaymentStatus: String
)

data class PaymentPendingCustomer(
    val customerId: Long,
    val customerName: String,
    val customerCode: String,
    val phone: String,
    val outstandingAmount: Double,
    val overdueAmount: Double,
    val oldestDueDate: String,
    val unpaidInvoiceCount: Int
) {
    val isOverdue: Boolean get() = overdueAmount > 0
}

data class AccountsSummary(
    val fromDate: String,
    val toDate: String,
    val totalSales: Double,
    val totalPurchases: Double,
    val totalReceiptsCollected: Double,
    val totalPaymentsMade: Double,
    val totalOutstandingReceivables: Double,
    val totalOutstandingPayables: Double,
    val netCashFlow: Double
)
