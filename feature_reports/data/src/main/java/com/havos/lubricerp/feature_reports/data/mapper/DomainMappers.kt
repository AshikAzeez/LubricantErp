package com.havos.lubricerp.feature_reports.data.mapper

import com.havos.lubricerp.feature_reports.data.dto.ConsolidatedStockItemDto
import com.havos.lubricerp.feature_reports.data.dto.CustomerDto
import com.havos.lubricerp.feature_reports.data.dto.CustomerLedgerEntryDto
import com.havos.lubricerp.feature_reports.data.dto.CustomerMobileSummaryDto
import com.havos.lubricerp.feature_reports.data.dto.DashboardDto
import com.havos.lubricerp.feature_reports.data.dto.DeliveryNoteDto
import com.havos.lubricerp.feature_reports.data.dto.ExpenseSummaryItemDto
import com.havos.lubricerp.feature_reports.data.dto.FastMovingItemDto
import com.havos.lubricerp.feature_reports.data.dto.InvoiceLineDto
import com.havos.lubricerp.feature_reports.data.dto.LoginResponseDto
import com.havos.lubricerp.feature_reports.data.dto.LowStockItemDto
import com.havos.lubricerp.feature_reports.data.dto.NetProfitReportDto
import com.havos.lubricerp.feature_reports.data.dto.NotificationItemDto
import com.havos.lubricerp.feature_reports.data.dto.PackagingLossGainReportDto
import com.havos.lubricerp.feature_reports.data.dto.PackagingLossGainRowDto
import com.havos.lubricerp.feature_reports.data.dto.PaymentReceiptDto
import com.havos.lubricerp.feature_reports.data.dto.PaymentReceivedItemDto
import com.havos.lubricerp.feature_reports.data.dto.ProductSalesItemDto
import com.havos.lubricerp.feature_reports.data.dto.ProfileDataDto
import com.havos.lubricerp.feature_reports.data.dto.RawMaterialStockItemDto
import com.havos.lubricerp.feature_reports.data.dto.RecentInvoiceDto
import com.havos.lubricerp.feature_reports.data.dto.SalesInvoiceDetailDto
import com.havos.lubricerp.feature_reports.data.dto.SalesInvoiceItemDto
import com.havos.lubricerp.feature_reports.data.dto.SalesOrderDetailDto
import com.havos.lubricerp.feature_reports.data.dto.SalesOrderItemDto
import com.havos.lubricerp.feature_reports.data.dto.SalesOrderLineDto
import com.havos.lubricerp.feature_reports.data.dto.SalesSummaryItemDto
import com.havos.lubricerp.feature_reports.data.dto.StockOverviewTankItemDto
import com.havos.lubricerp.feature_reports.data.dto.TankInfoDto
import com.havos.lubricerp.feature_reports.data.dto.TankStockSummaryDto
import com.havos.lubricerp.feature_reports.data.dto.WarehouseStockItemDto
import com.havos.lubricerp.feature_reports.domain.model.AuthSession
import com.havos.lubricerp.feature_reports.domain.model.ConsolidatedStockItem
import com.havos.lubricerp.feature_reports.domain.model.Customer
import com.havos.lubricerp.feature_reports.domain.model.CustomerLedgerEntry
import com.havos.lubricerp.feature_reports.domain.model.CustomerMobileSummary
import com.havos.lubricerp.feature_reports.domain.model.DashboardSummary
import com.havos.lubricerp.feature_reports.domain.model.DeliveryNote
import com.havos.lubricerp.feature_reports.domain.model.ExpenseSummaryItem
import com.havos.lubricerp.feature_reports.domain.model.FastMovingItem
import com.havos.lubricerp.feature_reports.domain.model.InvoiceLine
import com.havos.lubricerp.feature_reports.domain.model.LowStockItem
import com.havos.lubricerp.feature_reports.domain.model.NetProfitReport
import com.havos.lubricerp.feature_reports.domain.model.NotificationItem
import com.havos.lubricerp.feature_reports.domain.model.PackagingLossGainReport
import com.havos.lubricerp.feature_reports.domain.model.PackagingLossGainRow
import com.havos.lubricerp.feature_reports.domain.model.PaymentReceipt
import com.havos.lubricerp.feature_reports.domain.model.PaymentReceivedItem
import com.havos.lubricerp.feature_reports.domain.model.ProductSalesItem
import com.havos.lubricerp.feature_reports.domain.model.RawMaterialStockItem
import com.havos.lubricerp.feature_reports.domain.model.RecentInvoice
import com.havos.lubricerp.feature_reports.domain.model.SalesInvoiceDetail
import com.havos.lubricerp.feature_reports.domain.model.SalesInvoiceItem
import com.havos.lubricerp.feature_reports.domain.model.SalesOrderDetail
import com.havos.lubricerp.feature_reports.domain.model.SalesOrderItem
import com.havos.lubricerp.feature_reports.domain.model.SalesOrderLine
import com.havos.lubricerp.feature_reports.domain.model.SalesSummaryItem
import com.havos.lubricerp.feature_reports.domain.model.StockOverviewTankItem
import com.havos.lubricerp.feature_reports.domain.model.TankInfo
import com.havos.lubricerp.feature_reports.domain.model.TankStockSummary
import com.havos.lubricerp.feature_reports.domain.model.UserProfile
import com.havos.lubricerp.feature_reports.domain.model.WarehouseStockItem
import com.havos.lubricerp.feature_reports.data.dto.RecordPaymentResponseDto
import com.havos.lubricerp.feature_reports.domain.model.RecordPaymentResponse
import com.havos.lubricerp.feature_reports.data.dto.PaymentPendingCustomerDto
import com.havos.lubricerp.feature_reports.data.dto.AccountsSummaryDto
import com.havos.lubricerp.feature_reports.domain.model.PaymentPendingCustomer
import com.havos.lubricerp.feature_reports.domain.model.AccountsSummary

fun LoginResponseDto.toDomain(): AuthSession = AuthSession(
    username = username,
    token = token,
    refreshToken = refreshToken
)

fun ProfileDataDto.toDomain(): UserProfile = UserProfile(
    id = id,
    email = email,
    fullName = fullName,
    branchId = branchId,
    roles = roles
)

fun TankStockSummaryDto.toDomain(): TankStockSummary = TankStockSummary(
    totalCapacityLiters = totalCapacityLiters,
    currentStockLiters = currentStockLiters,
    availableCapacityLiters = availableCapacityLiters,
    tanks = tanks.map(TankInfoDto::toDomain)
)

fun TankInfoDto.toDomain(): TankInfo = TankInfo(
    name = name,
    code = code,
    location = location,
    productGrade = productGrade,
    capacityLiters = capacityLiters,
    currentStockLiters = currentStockLiters,
    availableLiters = availableLiters,
    fillPercent = fillPercent
)

fun RawMaterialStockItemDto.toDomain(): RawMaterialStockItem = RawMaterialStockItem(
    code = code,
    name = name,
    type = type,
    uom = uom,
    costPerUnit = costPerUnit,
    reorderLevel = reorderLevel
)

fun PackagingLossGainReportDto.toDomain(): PackagingLossGainReport = PackagingLossGainReport(
    totalPlannedLiters = totalPlannedLiters,
    totalActualLiters = totalActualLiters,
    totalVarianceLiters = totalVarianceLiters,
    rows = rows.map(PackagingLossGainRowDto::toDomain)
)

fun PackagingLossGainRowDto.toDomain(): PackagingLossGainRow = PackagingLossGainRow(
    orderNo = orderNo,
    date = date,
    productGrade = productGrade,
    sourceTank = sourceTank,
    plannedLiters = plannedLiters,
    actualLiters = actualLiters,
    varianceLiters = varianceLiters,
    variancePercent = variancePercent,
    status = status
)

fun DashboardDto.toDomain(): DashboardSummary = DashboardSummary(
    todaySalesAmount = todaySalesAmount,
    todaySalesCount = todaySalesCount,
    monthlySalesAmount = monthlySalesAmount,
    monthlySalesCount = monthlySalesCount,
    outstandingReceivables = outstandingReceivables,
    pendingPayables = pendingPayables,
    lowStockAlertCount = lowStockAlertCount,
    topSellingProducts = topSellingProducts,
    recentInvoices = recentInvoices.map(RecentInvoiceDto::toDomain)
)

fun RecentInvoiceDto.toDomain(): RecentInvoice = RecentInvoice(
    id = id,
    invoiceNumber = invoiceNumber,
    customerName = customerName,
    amount = amount,
    date = date,
    paymentStatus = paymentStatus
)

fun PaymentReceivedItemDto.toDomain(): PaymentReceivedItem = PaymentReceivedItem(
    id = id,
    receiptNumber = receiptNumber,
    paymentDate = paymentDate,
    customerName = customerName,
    invoiceNumber = invoiceNumber,
    amount = amount,
    paymentMode = paymentMode,
    reference = reference,
    remarks = remarks
)

fun CustomerDto.toDomain(): Customer = Customer(
    id = id,
    name = name,
    code = code,
    phone = phone,
    email = email,
    address = address,
    gstNumber = gSTNumber,
    state = state
)

fun CustomerLedgerEntryDto.toDomain(): CustomerLedgerEntry = CustomerLedgerEntry(
    date = date,
    type = type,
    refNumber = refNumber,
    debit = debit,
    credit = credit,
    runningBalance = runningBalance,
    invoiceId = invoiceId
)

fun StockOverviewTankItemDto.toDomain(): StockOverviewTankItem = StockOverviewTankItem(
    tankId = tankId,
    tankName = tankName,
    tankCode = tankCode,
    locationName = locationName,
    productGrade = productGrade,
    capacityLiters = capacityLiters,
    currentStockLiters = currentStockLiters,
    fillPercentage = fillPercentage,
    availableCapacity = availableCapacity,
    stockInMT = stockInMT,
    capacityInMT = capacityInMT
)

fun ProductSalesItemDto.toDomain(): ProductSalesItem = ProductSalesItem(
    productSKUId = productSKUId,
    productSKUName = productSKUName,
    productSKUCode = productSKUCode,
    productGrade = productGrade,
    productFamily = productFamily,
    quantitySold = quantitySold,
    totalRevenue = totalRevenue,
    totalTax = totalTax,
    netRevenue = netRevenue
)

fun NetProfitReportDto.toDomain(): NetProfitReport = NetProfitReport(
    fromDate = fromDate,
    toDate = toDate,
    totalRevenue = totalRevenue,
    totalTaxCollected = totalTaxCollected,
    netRevenue = netRevenue,
    costOfGoodsSold = costOfGoodsSold,
    grossProfit = grossProfit,
    grossMarginPercent = grossMarginPercent,
    operatingExpenses = operatingExpenses,
    netProfit = netProfit,
    netMarginPercent = netMarginPercent
)

fun ExpenseSummaryItemDto.toDomain(): ExpenseSummaryItem = ExpenseSummaryItem(
    vendorId = vendorId,
    vendorName = vendorName,
    vendorCode = vendorCode,
    invoiceCount = invoiceCount,
    totalPurchase = totalPurchase,
    totalPaid = totalPaid,
    balanceDue = balanceDue
)

fun CustomerMobileSummaryDto.toDomain(): CustomerMobileSummary = CustomerMobileSummary(
    id = id,
    name = name,
    code = code,
    phone = phone,
    email = email,
    gstNumber = gstNumber,
    address = address,
    outstandingAmount = outstandingAmount,
    overdueAmount = overdueAmount,
    lastPurchaseDate = lastPurchaseDate,
    lastInvoiceNumber = lastInvoiceNumber,
    totalLifetimePurchases = totalLifetimePurchases
)

fun SalesSummaryItemDto.toDomain(): SalesSummaryItem = SalesSummaryItem(
    customerId = customerId,
    customerName = customerName,
    customerCode = customerCode,
    invoiceCount = invoiceCount,
    subTotal = subTotal,
    taxAmount = taxAmount,
    totalAmount = totalAmount,
    paidAmount = paidAmount,
    balanceAmount = balanceAmount
)

fun NotificationItemDto.toDomain(): NotificationItem = NotificationItem(
    id = id,
    title = title,
    message = message,
    type = type,
    linkUrl = linkUrl,
    isRead = isRead,
    readAt = readAt,
    createdAt = createdAt,
    timeAgo = timeAgo
)

fun WarehouseStockItemDto.toDomain(): WarehouseStockItem = WarehouseStockItem(
    warehouseId = warehouseId,
    warehouseName = warehouseName,
    productSKUId = productSKUId,
    productSKUName = productSKUName,
    productSKUCode = productSKUCode,
    productGrade = productGrade,
    productFamily = productFamily,
    currentStock = currentStock,
    reorderLevel = reorderLevel
)

fun ConsolidatedStockItemDto.toDomain(): ConsolidatedStockItem = ConsolidatedStockItem(
    itemType = itemType,
    itemCode = itemCode,
    itemName = itemName,
    quantity = quantity,
    unit = unit,
    location = location
)

fun LowStockItemDto.toDomain(): LowStockItem = LowStockItem(
    productSKUId = productSKUId,
    productSKUName = productSKUName,
    productSKUCode = productSKUCode,
    productGrade = productGrade,
    productFamily = productFamily,
    warehouseName = warehouseName,
    currentStock = currentStock,
    reorderLevel = reorderLevel,
    shortageQuantity = shortageQuantity
)

fun FastMovingItemDto.toDomain(): FastMovingItem = FastMovingItem(
    rank = rank,
    productSKUId = productSKUId,
    productSKUName = productSKUName,
    productSKUCode = productSKUCode,
    productGrade = productGrade,
    productFamily = productFamily,
    quantitySold = quantitySold,
    totalRevenue = totalRevenue
)

fun RecordPaymentResponseDto.toDomain(): RecordPaymentResponse = RecordPaymentResponse(
    receiptNumber = receiptNumber,
    invoiceId = invoiceId,
    amountPaid = amountPaid,
    newBalance = newBalance,
    newPaymentStatus = newPaymentStatus
)

fun PaymentPendingCustomerDto.toDomain(): PaymentPendingCustomer = PaymentPendingCustomer(
    customerId = customerId,
    customerName = customerName,
    customerCode = customerCode,
    phone = phone,
    outstandingAmount = outstandingAmount,
    overdueAmount = overdueAmount,
    oldestDueDate = oldestDueDate,
    unpaidInvoiceCount = unpaidInvoiceCount
)

fun AccountsSummaryDto.toDomain(): AccountsSummary = AccountsSummary(
    fromDate = fromDate,
    toDate = toDate,
    totalSales = totalSales,
    totalPurchases = totalPurchases,
    totalReceiptsCollected = totalReceiptsCollected,
    totalPaymentsMade = totalPaymentsMade,
    totalOutstandingReceivables = totalOutstandingReceivables,
    totalOutstandingPayables = totalOutstandingPayables,
    netCashFlow = netCashFlow
)

fun SalesOrderItemDto.toDomain(): SalesOrderItem = SalesOrderItem(
    id = id,
    soNumber = soNumber,
    soDate = soDate,
    customerName = customerName,
    status = status,
    totalAmount = totalAmount,
    expectedDeliveryDate = expectedDeliveryDate,
    lineCount = lineCount,
    deliveredPercentage = deliveredPercentage,
    salesmanName = salesmanName
)

fun SalesOrderDetailDto.toDomain(): SalesOrderDetail = SalesOrderDetail(
    id = id,
    soNumber = soNumber,
    soDate = soDate,
    expectedDeliveryDate = expectedDeliveryDate,
    customerId = customerId,
    customerName = customerName,
    customerCode = customerCode,
    customerGST = customerGST,
    customerAddress = customerAddress,
    status = status,
    totalAmount = totalAmount,
    salesmanName = salesmanName,
    remarks = remarks,
    lines = lines.map { it.toDomain() },
    deliveryNotes = deliveryNotes.map { it.toDomain() }
)

fun SalesOrderLineDto.toDomain(): SalesOrderLine = SalesOrderLine(
    id = id,
    productSKUId = productSKUId,
    productSKUName = productSKUName,
    productSKUCode = productSKUCode,
    hsnCode = hsnCode,
    deliveryType = deliveryType,
    quantity = quantity,
    unitPrice = unitPrice,
    taxRate = taxRate,
    lineTotal = lineTotal,
    deliveredQuantity = deliveredQuantity,
    pendingQuantity = pendingQuantity
)

fun DeliveryNoteDto.toDomain(): DeliveryNote = DeliveryNote(
    id = id,
    dnNumber = dnNumber,
    dnDate = dnDate,
    status = status
)

fun SalesInvoiceItemDto.toDomain(): SalesInvoiceItem = SalesInvoiceItem(
    id = id,
    invoiceNumber = invoiceNumber,
    invoiceDate = invoiceDate,
    customerName = customerName,
    customerCode = customerCode,
    dnNumber = dnNumber,
    totalAmount = totalAmount,
    paymentStatus = paymentStatus,
    paidAmount = paidAmount,
    balanceAmount = balanceAmount,
    dueDate = dueDate,
    isOverdue = isOverdue,
    isInterState = isInterState
)

fun SalesInvoiceDetailDto.toDomain(): SalesInvoiceDetail = SalesInvoiceDetail(
    id = id,
    invoiceNumber = invoiceNumber,
    invoiceDate = invoiceDate,
    dueDate = dueDate,
    customerId = customerId,
    customerName = customerName,
    customerCode = customerCode,
    customerGST = customerGST,
    customerAddress = customerAddress,
    customerState = customerState,
    customerStateCode = customerStateCode,
    dnNumber = dnNumber,
    eWayBillNumber = eWayBillNumber,
    isInterState = isInterState,
    subTotal = subTotal,
    cgstAmount = cgstAmount,
    sgstAmount = sgstAmount,
    igstAmount = igstAmount,
    roundOffAmount = roundOffAmount,
    totalAmount = totalAmount,
    paidAmount = paidAmount,
    balanceAmount = balanceAmount,
    paymentStatus = paymentStatus,
    lines = lines.map { it.toDomain() },
    paymentReceipts = paymentReceipts.map { it.toDomain() }
)

fun InvoiceLineDto.toDomain(): InvoiceLine = InvoiceLine(
    id = id,
    productSKUName = productSKUName,
    productSKUCode = productSKUCode,
    hsnCode = hsnCode,
    description = description,
    quantity = quantity,
    unitOfMeasurement = unitOfMeasurement,
    unitPrice = unitPrice,
    discountPercent = discountPercent,
    taxRate = taxRate,
    lineSubTotal = lineSubTotal,
    taxAmount = taxAmount,
    lineTotal = lineTotal
)

fun PaymentReceiptDto.toDomain(): PaymentReceipt = PaymentReceipt(
    id = id,
    receiptNumber = receiptNumber,
    receiptDate = receiptDate,
    amount = amount,
    paymentMode = paymentMode,
    reference = reference
)
