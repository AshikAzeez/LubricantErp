package com.havos.lubricerp.feature_reports.presentation.reports

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.OilBarrel
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.ui.graphics.vector.ImageVector

enum class ReportMenu(
    val key: String,
    val title: String,
    val icon: ImageVector,
    val subMenus: List<ReportItem>
) {
    TANK_REPORTS(
        key = "tank_reports",
        title = "Tank Reports",
        icon = Icons.Default.OilBarrel,
        subMenus = listOf(
            ReportItem.TANK_STOCK_SUMMARY,
            ReportItem.DIP_VARIANCE,
            ReportItem.TANK_STOCK_LEDGER
        )
    ),
    WAREHOUSE_REPORTS(
        key = "warehouse_reports",
        title = "Warehouse Reports",
        icon = Icons.Default.Warehouse,
        subMenus = listOf(
            ReportItem.SKU_STOCK_REPORT,
            ReportItem.SLOW_MOVING_STOCK
        )
    ),
    RAW_MATERIAL_STOCK(
        key = "raw_material_stock",
        title = "Raw Material Stock",
        icon = Icons.Default.Inventory2,
        subMenus = listOf(ReportItem.RAW_MATERIAL_STOCK)
    ),
    PACKAGING_REPORTS(
        key = "packaging_reports",
        title = "Packaging Reports",
        icon = Icons.Default.Analytics,
        subMenus = listOf(
            ReportItem.PACKAGING_LOSS_GAIN,
            ReportItem.PACKAGING_SUMMARY
        )
    ),
    SALES_REPORTS(
        key = "sales_reports",
        title = "Sales Reports",
        icon = Icons.Default.PieChart,
        subMenus = listOf(
            ReportItem.SALES_SUMMARY,
            ReportItem.PRODUCT_WISE_SALES,
            ReportItem.CUSTOMER_OUTSTANDING,
            ReportItem.SALES_RETURN_SUMMARY,
            ReportItem.SALESMAN_PERFORMANCE,
            ReportItem.STATE_WISE_SALES,
            ReportItem.DISTRICT_WISE_SALES
        )
    ),
    PURCHASE_REPORTS(
        key = "purchase_reports",
        title = "Purchase Reports",
        icon = Icons.Default.ReceiptLong,
        subMenus = listOf(
            ReportItem.PURCHASE_SUMMARY,
            ReportItem.GRN_SUMMARY,
            ReportItem.STATE_WISE_PURCHASE,
            ReportItem.DISTRICT_WISE_PURCHASE
        )
    ),
    CONSOLIDATED_STOCK(
        key = "consolidated_stock",
        title = "Consolidated Stock",
        icon = Icons.Default.LocalShipping,
        subMenus = listOf(ReportItem.CONSOLIDATED_STOCK)
    )
}

enum class ReportItem(
    val key: String,
    val title: String
) {
    TANK_STOCK_SUMMARY("tank_stock_summary", "Tank Stock Summary"),
    DIP_VARIANCE("dip_variance", "Dip Variance Report"),
    TANK_STOCK_LEDGER("tank_stock_ledger", "Tank Stock Ledger"),
    SKU_STOCK_REPORT("sku_stock_report", "SKU Stock Report"),
    SLOW_MOVING_STOCK("slow_moving_stock", "Slow-Moving Stock"),
    RAW_MATERIAL_STOCK("raw_material_stock", "Raw Material Stock Report"),
    PACKAGING_LOSS_GAIN("packaging_loss_gain", "Packaging Loss/Gain Report"),
    PACKAGING_SUMMARY("packaging_summary", "Packaging Summary Report"),
    SALES_SUMMARY("sales_summary", "Sales Summary"),
    PRODUCT_WISE_SALES("product_wise_sales", "Product-wise Sales"),
    CUSTOMER_OUTSTANDING("customer_outstanding", "Customer Outstanding"),
    SALES_RETURN_SUMMARY("sales_return_summary", "Sales Return Summary"),
    SALESMAN_PERFORMANCE("salesman_performance", "Salesman Performance"),
    STATE_WISE_SALES("state_wise_sales", "State-wise Sales"),
    DISTRICT_WISE_SALES("district_wise_sales", "District-wise Sales"),
    PURCHASE_SUMMARY("purchase_summary", "Purchase Summary"),
    GRN_SUMMARY("grn_summary", "GRN Summary"),
    STATE_WISE_PURCHASE("state_wise_purchase", "State-wise Purchase"),
    DISTRICT_WISE_PURCHASE("district_wise_purchase", "District-wise Purchase"),
    CONSOLIDATED_STOCK("consolidated_stock", "Consolidated Stock")
}

fun reportItemByKey(key: String): ReportItem =
    ReportItem.entries.firstOrNull { it.key == key } ?: ReportItem.TANK_STOCK_SUMMARY
