package com.havos.lubricerp.feature_reports.data.mapper

import com.havos.lubricerp.feature_reports.data.dto.CustomerDto
import com.havos.lubricerp.feature_reports.data.dto.CustomerLedgerEntryDto
import com.havos.lubricerp.feature_reports.data.dto.CustomerMobileSummaryDto
import com.havos.lubricerp.feature_reports.data.dto.ExpenseSummaryItemDto
import com.havos.lubricerp.feature_reports.data.dto.NetProfitReportDto
import com.havos.lubricerp.feature_reports.data.dto.ProductSalesItemDto
import com.havos.lubricerp.feature_reports.data.dto.ReportSalesSummaryItemDto
import com.havos.lubricerp.feature_reports.data.dto.DashboardDto
import com.havos.lubricerp.feature_reports.data.dto.LoginResponseDto
import com.havos.lubricerp.feature_reports.data.dto.PaymentReceivedItemDto
import com.havos.lubricerp.feature_reports.data.dto.SalesSummaryItemDto
import com.havos.lubricerp.feature_reports.data.dto.PackagingLossGainReportDto
import com.havos.lubricerp.feature_reports.data.dto.PackagingLossGainRowDto
import com.havos.lubricerp.feature_reports.data.dto.ProfileDataDto
import com.havos.lubricerp.feature_reports.data.dto.RawMaterialStockItemDto
import com.havos.lubricerp.feature_reports.data.dto.RecentInvoiceDto
import com.havos.lubricerp.feature_reports.data.dto.StockOverviewTankItemDto
import com.havos.lubricerp.feature_reports.data.dto.TankInfoDto
import com.havos.lubricerp.feature_reports.data.dto.TankStockSummaryDto
import com.havos.lubricerp.feature_reports.domain.model.Customer
import com.havos.lubricerp.feature_reports.domain.model.CustomerLedgerEntry
import com.havos.lubricerp.feature_reports.domain.model.CustomerMobileSummary
import com.havos.lubricerp.feature_reports.domain.model.ExpenseSummaryItem
import com.havos.lubricerp.feature_reports.domain.model.NetProfitReport
import com.havos.lubricerp.feature_reports.domain.model.ProductSalesItem
import com.havos.lubricerp.feature_reports.domain.model.ReportSalesSummaryItem
import com.havos.lubricerp.feature_reports.domain.model.DashboardSummary
import com.havos.lubricerp.feature_reports.domain.model.PaymentReceivedItem
import com.havos.lubricerp.feature_reports.domain.model.RecentInvoice
import com.havos.lubricerp.feature_reports.domain.model.SalesSummaryItem
import com.havos.lubricerp.feature_reports.domain.model.UserProfile
import com.havos.lubricerp.feature_reports.domain.model.AuthSession
import com.havos.lubricerp.feature_reports.domain.model.PackagingLossGainReport
import com.havos.lubricerp.feature_reports.domain.model.PackagingLossGainRow
import com.havos.lubricerp.feature_reports.domain.model.RawMaterialStockItem
import com.havos.lubricerp.feature_reports.domain.model.StockOverviewTankItem
import com.havos.lubricerp.feature_reports.domain.model.TankInfo
import com.havos.lubricerp.feature_reports.domain.model.TankStockSummary

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
    receiptNumber = receiptNumber,
    customerName = customerName,
    invoiceNumber = invoiceNumber,
    amount = amount,
    paymentMode = paymentMode
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
    runningBalance = runningBalance
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

fun ReportSalesSummaryItemDto.toDomain(): ReportSalesSummaryItem = ReportSalesSummaryItem(
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

fun ProductSalesItemDto.toDomain(): ProductSalesItem = ProductSalesItem(
    productGrade = productGrade,
    deliveryType = deliveryType,
    totalQuantity = totalQuantity,
    totalAmount = totalAmount
)

fun NetProfitReportDto.toDomain(): NetProfitReport = NetProfitReport(
    totalRevenue = totalRevenue,
    totalPurchaseCost = totalPurchaseCost,
    netProfit = netProfit,
    fromDate = fromDate,
    toDate = toDate
)

fun ExpenseSummaryItemDto.toDomain(): ExpenseSummaryItem = ExpenseSummaryItem(
    vendorName = vendorName,
    paymentCount = paymentCount,
    totalPaid = totalPaid
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
