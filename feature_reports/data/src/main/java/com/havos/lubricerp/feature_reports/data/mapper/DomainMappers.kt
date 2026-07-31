package com.havos.lubricerp.feature_reports.data.mapper

import com.havos.lubricerp.feature_reports.data.dto.ConsolidatedStockItemDto
import com.havos.lubricerp.feature_reports.data.dto.CostBreakdownItemDto
import com.havos.lubricerp.feature_reports.data.dto.CostBreakdownDetailDto
import com.havos.lubricerp.feature_reports.data.dto.RawMaterialLineDto
import com.havos.lubricerp.feature_reports.data.dto.CustomerDto
import com.havos.lubricerp.feature_reports.data.dto.CustomerLedgerEntryDto
import com.havos.lubricerp.feature_reports.data.dto.CustomerLedgerPagedDataDto
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
import com.havos.lubricerp.feature_reports.data.dto.PaymentReceivedPagedDataDto
import com.havos.lubricerp.feature_reports.data.dto.ProductSalesItemDto
import com.havos.lubricerp.feature_reports.data.dto.ProfileDataDto
import com.havos.lubricerp.feature_reports.data.dto.RawMaterialStockItemDto
import com.havos.lubricerp.feature_reports.data.dto.RecentInvoiceDto
import com.havos.lubricerp.feature_reports.data.dto.SalesInvoiceDetailDto
import com.havos.lubricerp.feature_reports.data.dto.SalesInvoiceItemDto
import com.havos.lubricerp.feature_reports.data.dto.SalesOrderDetailDto
import com.havos.lubricerp.feature_reports.data.dto.SalesOrderItemDto
import com.havos.lubricerp.feature_reports.data.dto.SalesOrderPagedDataDto
import com.havos.lubricerp.feature_reports.data.dto.SalesOrderLineDto
import com.havos.lubricerp.feature_reports.data.dto.SalesSummaryItemDto
import com.havos.lubricerp.feature_reports.data.dto.StockOverviewTankItemDto
import com.havos.lubricerp.core.common.PagedResult
import com.havos.lubricerp.feature_reports.data.dto.TankStockItemDto
import com.havos.lubricerp.feature_reports.data.dto.WarehouseStockItemDto
import com.havos.lubricerp.feature_reports.domain.model.AuthSession
import com.havos.lubricerp.feature_reports.domain.model.ConsolidatedStockItem
import com.havos.lubricerp.feature_reports.domain.model.CostBreakdownItem
import com.havos.lubricerp.feature_reports.domain.model.CostBreakdownDetail
import com.havos.lubricerp.feature_reports.domain.model.RawMaterialLine
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
import com.havos.lubricerp.feature_reports.domain.model.TankStockItem
import com.havos.lubricerp.feature_reports.domain.model.UserProfile
import com.havos.lubricerp.feature_reports.domain.model.WarehouseStockItem
import com.havos.lubricerp.feature_reports.data.dto.AccountsSummaryDto
import com.havos.lubricerp.feature_reports.data.dto.AgingBucketDto
import com.havos.lubricerp.feature_reports.data.dto.BankAccountBalanceDto
import com.havos.lubricerp.feature_reports.data.dto.CashPositionDto
import com.havos.lubricerp.feature_reports.data.dto.PurchaseSummaryDto
import com.havos.lubricerp.feature_reports.data.dto.RecentPurchaseOrderDto
import com.havos.lubricerp.feature_reports.data.dto.ReceivablesAgingDto
import com.havos.lubricerp.feature_reports.data.dto.PaymentPendingCustomerDto
import com.havos.lubricerp.feature_reports.data.dto.PaymentPendingPagedDataDto
import com.havos.lubricerp.feature_reports.data.dto.RecordPaymentResponseDto
import com.havos.lubricerp.feature_reports.domain.model.AccountsSummary
import com.havos.lubricerp.feature_reports.domain.model.AgingBucket
import com.havos.lubricerp.feature_reports.domain.model.BankAccountBalance
import com.havos.lubricerp.feature_reports.domain.model.CashPosition
import com.havos.lubricerp.feature_reports.domain.model.PaymentPendingCustomer
import com.havos.lubricerp.feature_reports.domain.model.PurchaseSummary
import com.havos.lubricerp.feature_reports.domain.model.RecentPurchaseOrder
import com.havos.lubricerp.feature_reports.domain.model.ReceivablesAging
import com.havos.lubricerp.feature_reports.domain.model.RecordPaymentResponse

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

fun TankStockItemDto.toDomain(): TankStockItem = TankStockItem(
    tankId = tankId,
    tankName = tankName,
    tankCode = tankCode,
    capacity = capacity,
    currentStock = currentStock,
    availableCapacity = availableCapacity,
    utilizationPercent = utilizationPercent,
    lastGrade = lastGrade,
    tankType = tankType
)

fun RawMaterialStockItemDto.toDomain(): RawMaterialStockItem = RawMaterialStockItem(
    id = id,
    code = code,
    name = name,
    type = type,
    unitOfMeasureId = unitOfMeasureId,
    costPerUnit = currentCostPerUnit
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

fun PaymentReceivedPagedDataDto.toDomain(): PagedResult<PaymentReceivedItem> = PagedResult(
    items = items.map { it.toDomain() },
    totalCount = totalCount,
    skip = skip,
    take = take,
    hasMore = hasMore
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

fun CustomerLedgerPagedDataDto.toDomain(): PagedResult<CustomerLedgerEntry> = PagedResult(
    items = items.map { it.toDomain() },
    totalCount = totalCount,
    skip = skip,
    take = take,
    hasMore = hasMore
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
    invoiceCount = invoiceCount,
    totalInvoiced = totalInvoiced,
    totalPaid = totalPaid,
    totalOutstanding = totalOutstanding,
    oldestOutstandingDays = oldestOutstandingDays,
    isOverdue = isOverdue
)

fun PaymentPendingPagedDataDto.toDomain(): PagedResult<PaymentPendingCustomer> = PagedResult(
    items = items.map { it.toDomain() },
    totalCount = totalCount,
    skip = skip,
    take = take,
    hasMore = hasMore
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

fun SalesOrderPagedDataDto.toDomain(): PagedResult<SalesOrderItem> = PagedResult(
    items = items.map { it.toDomain() },
    totalCount = totalCount,
    skip = skip,
    take = take,
    hasMore = hasMore
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
    dueDate = dueDate.orEmpty(),
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

// ── Dashboard Mappers ────────────────────────────────────────────────────

fun ReceivablesAgingDto.toDomain(): ReceivablesAging = ReceivablesAging(
    agingBuckets = agingBuckets.map { it.toDomain() },
    dsoDays = dsoDays,
    totalOutstanding = totalOutstanding,
    overduePercentage = overduePercentage
)

fun AgingBucketDto.toDomain(): AgingBucket = AgingBucket(
    label = label,
    amount = amount,
    invoiceCount = invoiceCount,
    isOverdue = isOverdue
)

fun PurchaseSummaryDto.toDomain(): PurchaseSummary = PurchaseSummary(
    openPOCount = openPOCount,
    openPOValue = openPOValue,
    poDueThisWeek = poDueThisWeek,
    poDueThisWeekValue = poDueThisWeekValue,
    pendingApprovals = pendingApprovals,
    recentPurchaseOrders = recentPurchaseOrders.map { it.toDomain() }
)

fun RecentPurchaseOrderDto.toDomain(): RecentPurchaseOrder = RecentPurchaseOrder(
    id = id,
    poNumber = poNumber,
    vendorName = vendorName,
    amount = amount,
    status = status,
    expectedDate = expectedDate
)

fun CashPositionDto.toDomain(): CashPosition = CashPosition(
    currentBalance = currentBalance,
    todayInflow = todayInflow,
    todayOutflow = todayOutflow,
    todayNetCash = todayNetCash,
    projected7DayBalance = projected7DayBalance,
    bankAccounts = bankAccounts.map { it.toDomain() }
)

fun BankAccountBalanceDto.toDomain(): BankAccountBalance = BankAccountBalance(
    accountName = accountName,
    accountNumber = accountNumber,
    balance = balance
)

fun com.havos.lubricerp.feature_reports.data.dto.ProformaInvoiceDto.toDomain(): com.havos.lubricerp.feature_reports.domain.model.ProformaInvoice = com.havos.lubricerp.feature_reports.domain.model.ProformaInvoice(
    id = id,
    proformaNumber = proformaNumber,
    date = date,
    validUntilDate = validUntilDate,
    customerName = customerName,
    customerCode = customerCode,
    status = status,
    totalAmount = totalAmount,
    lineCount = lineCount,
    soNumber = soNumber,
    salesOrderId = salesOrderId,
    isInterState = isInterState
)

fun com.havos.lubricerp.feature_reports.data.dto.ProformaInvoiceLineDto.toDomain(): com.havos.lubricerp.feature_reports.domain.model.ProformaInvoiceLine = com.havos.lubricerp.feature_reports.domain.model.ProformaInvoiceLine(
    id = id,
    deliveryType = deliveryType,
    productGradeId = productGradeId,
    productGradeName = productGradeName.ifBlank { productGrade ?: "" },
    productSKUId = productSKUId,
    productSKUName = productSKUName.ifBlank { sku ?: "" },
    hsnCode = hsnCode,
    description = description,
    quantity = quantity,
    unitOfMeasurement = unitOfMeasurement,
    unitPrice = unitPrice,
    discountPercent = discountPercent ?: 0.0,
    taxRate = taxRate,
    lineSubTotal = lineSubTotal,
    taxAmount = taxAmount,
    lineTotal = lineTotal
)

fun com.havos.lubricerp.feature_reports.data.dto.ProformaInvoiceDetailDto.toDomain(): com.havos.lubricerp.feature_reports.domain.model.ProformaInvoiceDetail = com.havos.lubricerp.feature_reports.domain.model.ProformaInvoiceDetail(
    id = id,
    proformaNumber = proformaNumber,
    date = date,
    validUntilDate = validUntilDate,
    customerId = customerId,
    customerName = customerName,
    customerCode = customerCode,
    customerGST = customerGST ?: customerGSTNumber,
    customerAddress = customerAddress,
    customerState = customerState,
    customerStateCode = customerStateCode,
    status = status,
    isInterState = isInterState,
    remarks = remarks,
    termsAndConditions = termsAndConditions,
    subTotal = subTotal,
    cgstAmount = cgstAmount,
    sgstAmount = sgstAmount,
    igstAmount = igstAmount,
    roundOffAmount = roundOffAmount,
    totalAmount = totalAmount,
    salesOrderId = salesOrderId,
    salesOrderNumber = salesOrderNumber,
    lines = lines.map { it.toDomain() }
)

fun com.havos.lubricerp.feature_reports.data.dto.CreateProformaInvoiceResponseDto.toDomain(): com.havos.lubricerp.feature_reports.domain.model.CreateProformaInvoiceResponse = com.havos.lubricerp.feature_reports.domain.model.CreateProformaInvoiceResponse(
    id = id,
    proformaNumber = proformaNumber
)

fun com.havos.lubricerp.feature_reports.domain.model.CreateProformaInvoiceLine.toDto(): com.havos.lubricerp.feature_reports.data.dto.CreateProformaInvoiceLineDto = com.havos.lubricerp.feature_reports.data.dto.CreateProformaInvoiceLineDto(
    deliveryType = deliveryType,
    productGradeId = productGradeId,
    productSKUId = productSKUId,
    hsnCode = hsnCode,
    quantity = quantity,
    unitPrice = unitPrice,
    taxRate = taxRate,
    discountPercent = discountPercent
)

fun com.havos.lubricerp.feature_reports.domain.model.CreateProformaInvoiceRequest.toDto(): com.havos.lubricerp.feature_reports.data.dto.CreateProformaInvoiceRequestDto = com.havos.lubricerp.feature_reports.data.dto.CreateProformaInvoiceRequestDto(
    customerId = customerId,
    proformaDate = proformaDate,
    validUntilDate = validUntilDate,
    remarks = remarks,
    termsAndConditions = termsAndConditions,
    lines = lines.map { it.toDto() }
)

fun com.havos.lubricerp.feature_reports.data.dto.ProductSkuDto.toDomain(): com.havos.lubricerp.feature_reports.domain.model.ProductSku = com.havos.lubricerp.feature_reports.domain.model.ProductSku(
    id = id,
    name = name,
    code = code,
    productGradeId = productGradeId,
    productGrade = productGrade,
    productFamily = productFamily,
    hsnCode = hsnCode,
    packSizeLiters = packSizeLiters,
    packSizeLabel = packSizeLabel,
    unitOfMeasureId = unitOfMeasureId
)

fun CostBreakdownItemDto.toDomain(): CostBreakdownItem = CostBreakdownItem(
    id = id,
    sku = sku,
    productGrade = productGrade,
    productFamily = productFamily,
    productSKUId = productSKUId,
    effectiveFrom = effectiveFrom,
    effectiveTo = effectiveTo,
    packageCost = packageCost,
    margin = margin,
    transportCost = transportCost,
    materialCost = materialCost,
    totalCost = totalCost,
    lineCount = lineCount,
    remarks = remarks,
    status = status
)

fun RawMaterialLineDto.toDomain(): RawMaterialLine = RawMaterialLine(
    id = id,
    rawMaterialId = rawMaterialId,
    rawMaterialName = rawMaterialName,
    rawMaterialCode = rawMaterialCode,
    quantity = quantity,
    rate = rate,
    amount = amount
)

fun CostBreakdownDetailDto.toDomain(): CostBreakdownDetail = CostBreakdownDetail(
    id = id,
    sku = sku,
    skuCode = skuCode,
    productGrade = productGrade,
    productFamily = productFamily,
    productSKUId = productSKUId,
    effectiveFrom = effectiveFrom,
    effectiveTo = effectiveTo,
    packageCost = packageCost,
    margin = margin,
    transportCost = transportCost,
    materialCost = materialCost,
    totalCost = totalCost,
    remarks = remarks,
    lines = lines.map { it.toDomain() }
)

fun com.havos.lubricerp.feature_reports.domain.model.CreateCostBreakdownRequest.toDto(): com.havos.lubricerp.feature_reports.data.dto.CreateCostBreakdownRequestDto =
    com.havos.lubricerp.feature_reports.data.dto.CreateCostBreakdownRequestDto(
        productSKUId = productSKUId,
        effectiveFrom = effectiveFrom,
        effectiveTo = effectiveTo,
        remarks = remarks,
        packageCost = packageCost,
        margin = margin,
        transportCost = transportCost,
        lines = lines.map {
            com.havos.lubricerp.feature_reports.data.dto.CreateCostBreakdownLineDto(
                rawMaterialId = it.rawMaterialId,
                quantity = it.quantity,
                rate = it.rate
            )
        }
    )
