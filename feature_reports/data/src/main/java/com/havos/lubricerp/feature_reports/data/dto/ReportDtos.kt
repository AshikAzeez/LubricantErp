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
    val id: Long = 0,
    val receiptNumber: String,
    val paymentDate: String = "",
    val customerName: String,
    val invoiceNumber: String,
    val amount: Double,
    val paymentMode: String,
    val reference: String? = null,
    val remarks: String? = null
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
    val runningBalance: Double = 0.0,
    val invoiceId: Long = 0
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
data class ProductSalesItemDto(
    val productSKUId: Long = 0,
    val productSKUName: String = "",
    val productSKUCode: String = "",
    val productGrade: String = "",
    val productFamily: String = "",
    val quantitySold: Double = 0.0,
    val totalRevenue: Double = 0.0,
    val totalTax: Double = 0.0,
    val netRevenue: Double = 0.0
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
    val fromDate: String = "",
    val toDate: String = "",
    val totalRevenue: Double = 0.0,
    val totalTaxCollected: Double = 0.0,
    val netRevenue: Double = 0.0,
    val costOfGoodsSold: Double = 0.0,
    val grossProfit: Double = 0.0,
    val grossMarginPercent: Double = 0.0,
    val operatingExpenses: Double = 0.0,
    val netProfit: Double = 0.0,
    val netMarginPercent: Double = 0.0
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
    val vendorId: Long = 0,
    val vendorName: String = "",
    val vendorCode: String = "",
    val invoiceCount: Int = 0,
    val totalPurchase: Double = 0.0,
    val totalPaid: Double = 0.0,
    val balanceDue: Double = 0.0
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

@Serializable
data class WarehouseStockItemDto(
    val warehouseId: Int,
    val warehouseName: String,
    val productSKUId: Int,
    val productSKUName: String,
    val productSKUCode: String,
    val productGrade: String,
    val productFamily: String,
    val currentStock: Int,
    val reorderLevel: Int
)

@Serializable
data class WarehouseStockApiResponseDto(
    val success: Boolean,
    val data: List<WarehouseStockItemDto> = emptyList(),
    val message: String? = null,
    val errors: List<String>? = null
)

@Serializable
data class ConsolidatedStockItemDto(
    val itemType: String,
    val itemCode: String,
    val itemName: String,
    val quantity: Double,
    val unit: String,
    val location: String
)

@Serializable
data class ConsolidatedStockApiResponseDto(
    val success: Boolean,
    val data: List<ConsolidatedStockItemDto> = emptyList(),
    val message: String? = null,
    val errors: List<String>? = null
)

@Serializable
data class LowStockItemDto(
    val productSKUId: Int,
    val productSKUName: String,
    val productSKUCode: String,
    val productGrade: String,
    val productFamily: String,
    val warehouseName: String,
    val currentStock: Int,
    val reorderLevel: Int,
    val shortageQuantity: Int
)

@Serializable
data class LowStockApiResponseDto(
    val success: Boolean,
    val data: List<LowStockItemDto> = emptyList(),
    val message: String? = null,
    val errors: List<String>? = null
)

@Serializable
data class FastMovingItemDto(
    val rank: Int,
    val productSKUId: Int,
    val productSKUName: String,
    val productSKUCode: String,
    val productGrade: String,
    val productFamily: String,
    val quantitySold: Int,
    val totalRevenue: Double
)

@Serializable
data class FastMovingApiResponseDto(
    val success: Boolean,
    val data: List<FastMovingItemDto> = emptyList(),
    val message: String? = null,
    val errors: List<String>? = null
)

@Serializable
data class RecordPaymentRequestDto(
    val invoiceId: Long,
    val amount: Double,
    val paymentMode: String,
    val paymentDate: String,
    val reference: String = "",
    val remarks: String = ""
)

@Serializable
data class RecordPaymentResponseDto(
    val receiptNumber: String,
    val invoiceId: Long,
    val amountPaid: Double,
    val newBalance: Double,
    val newPaymentStatus: String
)

@Serializable
data class RecordPaymentApiResponseDto(
    val success: Boolean,
    val data: RecordPaymentResponseDto? = null,
    val message: String? = null,
    val errors: List<String>? = null
)

@Serializable
data class PaymentPendingCustomerDto(
    val customerId: Long,
    val customerName: String,
    val customerCode: String = "",
    val phone: String = "",
    val outstandingAmount: Double,
    val overdueAmount: Double = 0.0,
    val oldestDueDate: String = "",
    val unpaidInvoiceCount: Int = 0
)

@Serializable
data class PaymentPendingApiResponseDto(
    val success: Boolean,
    val data: List<PaymentPendingCustomerDto> = emptyList(),
    val message: String? = null,
    val errors: List<String>? = null
)

@Serializable
data class AccountsSummaryDto(
    val fromDate: String = "",
    val toDate: String = "",
    val totalSales: Double = 0.0,
    val totalPurchases: Double = 0.0,
    val totalReceiptsCollected: Double = 0.0,
    val totalPaymentsMade: Double = 0.0,
    val totalOutstandingReceivables: Double = 0.0,
    val totalOutstandingPayables: Double = 0.0,
    val netCashFlow: Double = 0.0
)

@Serializable
data class AccountsSummaryApiResponseDto(
    val success: Boolean,
    val data: AccountsSummaryDto? = null,
    val message: String? = null,
    val errors: List<String>? = null
)

@Serializable
data class SalesOrderItemDto(
    val id: Long,
    val soNumber: String,
    val soDate: String,
    val customerName: String,
    val status: String,
    val totalAmount: Double,
    val expectedDeliveryDate: String,
    val lineCount: Int = 0,
    val deliveredPercentage: Double = 0.0,
    val salesmanName: String = ""
)

@Serializable
data class SalesOrderListApiResponseDto(
    val success: Boolean,
    val data: List<SalesOrderItemDto> = emptyList(),
    val message: String? = null,
    val errors: List<String>? = null
)

@Serializable
data class SalesOrderDetailDto(
    val id: Long,
    val soNumber: String,
    val soDate: String,
    val expectedDeliveryDate: String,
    val customerId: Long,
    val customerName: String,
    val customerCode: String,
    val customerGST: String? = null,
    val customerAddress: String = "",
    val status: String,
    val totalAmount: Double = 0.0,
    val salesmanName: String = "",
    val remarks: String? = null,
    val lines: List<SalesOrderLineDto> = emptyList(),
    val deliveryNotes: List<DeliveryNoteDto> = emptyList()
)

@Serializable
data class SalesOrderLineDto(
    val id: Long,
    val productSKUId: Int,
    val productSKUName: String,
    val productSKUCode: String = "",
    val hsnCode: String = "",
    val deliveryType: String = "",
    val quantity: Int,
    val unitPrice: Double = 0.0,
    val taxRate: Double = 0.0,
    val lineTotal: Double = 0.0,
    val deliveredQuantity: Int = 0,
    val pendingQuantity: Int = 0
)

@Serializable
data class DeliveryNoteDto(
    val id: Long,
    val dnNumber: String = "",
    val dnDate: String = "",
    val status: String = ""
)

@Serializable
data class SalesOrderDetailApiResponseDto(
    val success: Boolean,
    val data: SalesOrderDetailDto? = null,
    val message: String? = null,
    val errors: List<String>? = null
)

@Serializable
data class SalesInvoiceItemDto(
    val id: Long,
    val invoiceNumber: String,
    val invoiceDate: String,
    val customerName: String,
    val customerCode: String = "",
    val dnNumber: String? = null,
    val totalAmount: Double,
    val paymentStatus: String,
    val paidAmount: Double = 0.0,
    val balanceAmount: Double = 0.0,
    val dueDate: String = "",
    val isOverdue: Boolean = false,
    val isInterState: Boolean = false
)

@Serializable
data class SalesInvoiceListApiResponseDto(
    val success: Boolean,
    val data: List<SalesInvoiceItemDto> = emptyList(),
    val message: String? = null,
    val errors: List<String>? = null
)

@Serializable
data class SalesInvoiceDetailDto(
    val id: Long,
    val invoiceNumber: String,
    val invoiceDate: String,
    val dueDate: String,
    val customerId: Long,
    val customerName: String,
    val customerCode: String = "",
    val customerGST: String? = null,
    val customerAddress: String = "",
    val customerState: String = "",
    val customerStateCode: String = "",
    val dnNumber: String? = null,
    val eWayBillNumber: String? = null,
    val isInterState: Boolean = false,
    val subTotal: Double = 0.0,
    val cgstAmount: Double = 0.0,
    val sgstAmount: Double = 0.0,
    val igstAmount: Double = 0.0,
    val roundOffAmount: Double = 0.0,
    val totalAmount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val balanceAmount: Double = 0.0,
    val paymentStatus: String = "",
    val lines: List<InvoiceLineDto> = emptyList(),
    val paymentReceipts: List<PaymentReceiptDto> = emptyList()
)

@Serializable
data class InvoiceLineDto(
    val id: Long,
    val productSKUName: String = "",
    val productSKUCode: String = "",
    val hsnCode: String = "",
    val description: String? = null,
    val quantity: Int,
    val unitOfMeasurement: String = "",
    val unitPrice: Double = 0.0,
    val discountPercent: Double = 0.0,
    val taxRate: Double = 0.0,
    val lineSubTotal: Double = 0.0,
    val taxAmount: Double = 0.0,
    val lineTotal: Double = 0.0
)

@Serializable
data class PaymentReceiptDto(
    val id: Long,
    val receiptNumber: String = "",
    val receiptDate: String = "",
    val amount: Double = 0.0,
    val paymentMode: String = "",
    val reference: String? = null
)

@Serializable
data class SalesInvoiceDetailApiResponseDto(
    val success: Boolean,
    val data: SalesInvoiceDetailDto? = null,
    val message: String? = null,
    val errors: List<String>? = null
)
