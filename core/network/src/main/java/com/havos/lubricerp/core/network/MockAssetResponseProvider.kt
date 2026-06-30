package com.havos.lubricerp.core.network

import android.content.Context
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class MockAssetResponseProvider(
    private val context: Context
) {

    fun MockRequestHandleScope.respondFor(request: HttpRequestData) = when {

        // ── Auth ─────────────────────────────────────────────────────────────
        request.method == HttpMethod.Post && normalizedPath(request).contains("auth/login") ->
            respondAsset(this, "mock/auth/login_success.json")

        request.method == HttpMethod.Post && normalizedPath(request).contains("auth/logout") ->
            respondAsset(this, "mock/auth/logout_success.json")

        request.method == HttpMethod.Post && normalizedPath(request).contains("auth/refresh") ->
            respondAsset(this, "mock/auth/refresh_success.json")

        request.method == HttpMethod.Get && normalizedPath(request).contains("auth/profile") ->
            respondAsset(this, "mock/auth/profile_success.json")

        // ── Dashboard ────────────────────────────────────────────────────────
        request.method == HttpMethod.Get && normalizedPath(request).contains("api/dashboard/receivables-aging") ->
            respondAsset(this, "mock/dashboard/receivables_aging.json")

        request.method == HttpMethod.Get && normalizedPath(request).contains("api/dashboard/purchase-summary") ->
            respondAsset(this, "mock/dashboard/purchase_summary.json")

        request.method == HttpMethod.Get && normalizedPath(request).contains("api/dashboard/cash-position") ->
            respondAsset(this, "mock/dashboard/cash_position.json")

        request.method == HttpMethod.Get && normalizedPath(request).contains("api/dashboard") ->
            respondAsset(this, "mock/dashboard/dashboard.json")

        // ── Reports (api/ prefix) ─────────────────────────────────────────────
        request.method == HttpMethod.Get && normalizedPath(request).contains("api/reports/tank-stock") ->
            respondAsset(this, "mock/reports/tank_stock_summary.json")

        request.method == HttpMethod.Get && normalizedPath(request).contains("api/reports/raw-material-stock") ->
            respondAsset(this, "mock/reports/raw_material_stock.json")

        request.method == HttpMethod.Get && normalizedPath(request).contains("api/reports/packaging-loss-gain") ->
            respondAsset(this, "mock/reports/packaging_loss_gain.json")

        request.method == HttpMethod.Get && normalizedPath(request).contains("api/reports/warehouse-stock") ->
            respondAsset(this, "mock/reports/warehouse_stock.json")

        request.method == HttpMethod.Get && normalizedPath(request).contains("api/reports/consolidated-stock") ->
            respondAsset(this, "mock/reports/consolidated_stock.json")

        request.method == HttpMethod.Get && normalizedPath(request).contains("api/reports/low-stock") ->
            respondAsset(this, "mock/reports/low_stock.json")

        request.method == HttpMethod.Get && normalizedPath(request).contains("api/reports/fast-moving") ->
            respondAsset(this, "mock/reports/fast_moving.json")

        request.method == HttpMethod.Get && normalizedPath(request).contains("api/reports/sales-summary") ->
            respondAsset(this, "mock/reports/report_sales_summary.json")

        request.method == HttpMethod.Get && normalizedPath(request).contains("api/reports/product-sales") ->
            respondAsset(this, "mock/reports/product_sales.json")

        request.method == HttpMethod.Get && normalizedPath(request).contains("api/reports/net-profit") ->
            respondAsset(this, "mock/reports/net_profit.json")

        request.method == HttpMethod.Get && normalizedPath(request).contains("api/reports/expense-summary") ->
            respondAsset(this, "mock/reports/expense_summary.json")

        // ── Payments ─────────────────────────────────────────────────────────
        request.method == HttpMethod.Get && normalizedPath(request).contains("api/payments/received") ->
            respondAsset(this, "mock/payments/received.json")

        request.method == HttpMethod.Get && normalizedPath(request).contains("api/payments/pending") ->
            respondAsset(this, "mock/payments/pending.json")

        request.method == HttpMethod.Get && normalizedPath(request).contains("api/reports/accounts-summary") ->
            respondAsset(this, "mock/payments/accounts_summary.json")

        // ── Customers ────────────────────────────────────────────────────────
        request.method == HttpMethod.Get && normalizedPath(request).let {
            it.contains("api/customers") && it.contains("mobile-summary")
        } -> respondAsset(this, "mock/customers/customer_mobile_summary.json")

        request.method == HttpMethod.Get && normalizedPath(request).contains("api/ledger/customer") ->
            respondAsset(this, "mock/customers/customer_ledger.json")

        request.method == HttpMethod.Get && normalizedPath(request).contains("api/customers") ->
            respondAsset(this, "mock/customers/customers.json")

        // ── Payments ─────────────────────────────────────────────────────────
        request.method == HttpMethod.Post && normalizedPath(request).contains("api/payments") &&
            !normalizedPath(request).contains("received") ->
            respondAsset(this, "mock/payments/record_payment.json")

        // ── Sales Orders ──────────────────────────────────────────────────────
        request.method == HttpMethod.Get && normalizedPath(request).contains("api/sales-orders/") ->
            respondAsset(this, "mock/sales-orders/sales_order_detail.json")

        request.method == HttpMethod.Get && normalizedPath(request) == "api/sales-orders" &&
            request.url.parameters["status"] == "Confirmed" ->
            respondAsset(this, "mock/sales-orders/sales_orders_pending.json")

        request.method == HttpMethod.Get && normalizedPath(request) == "api/sales-orders" &&
            request.url.parameters["status"] == "Dispatched" ->
            respondAsset(this, "mock/sales-orders/sales_orders_dispatched.json")

        // ── Sales Invoices ────────────────────────────────────────────────────
        request.method == HttpMethod.Get && normalizedPath(request).contains("api/sales-invoices/") ->
            respondAsset(this, "mock/sales-invoices/sales_invoice_detail.json")

        request.method == HttpMethod.Get && normalizedPath(request).contains("api/sales-invoices") ->
            respondAsset(this, "mock/sales-invoices/sales_invoices.json")

        // ── Notifications ─────────────────────────────────────────────────────
        request.method == HttpMethod.Get && normalizedPath(request).contains("api/notifications/unread-count") ->
            respondAsset(this, "mock/notifications/unread_count.json")

        request.method == HttpMethod.Post && normalizedPath(request).contains("api/notifications/read-all") ->
            respondAsset(this, "mock/notifications/mark_all_read.json")

        request.method == HttpMethod.Post && normalizedPath(request).let {
            it.contains("api/notifications") && it.contains("/read")
        } -> respondAsset(this, "mock/notifications/mark_read.json")

        request.method == HttpMethod.Get && normalizedPath(request).contains("api/notifications") ->
            respondNotificationsPaged(this, request)

        // ── Proforma Invoices ────────────────────────────────────────────────
        request.method == HttpMethod.Get && normalizedPath(request).let {
            it.startsWith("api/proforma-invoices/")
        } -> respondAsset(this, "mock/proforma-invoices/proforma_invoice_detail.json")

        request.method == HttpMethod.Get && normalizedPath(request).contains("api/proforma-invoices") ->
            respondAsset(this, "mock/proforma-invoices/proforma_invoices.json")

        request.method == HttpMethod.Post && normalizedPath(request).contains("api/proforma-invoices") ->
            respondAsset(this, "mock/proforma-invoices/create_proforma_invoice.json")

        // ── Fallback ──────────────────────────────────────────────────────────
        else -> this.respond(
            content = "{\"error\":\"Mock route not found\",\"path\":\"${normalizedPath(request)}\",\"method\":\"${request.method.value}\"}",
            status = HttpStatusCode.NotFound,
            headers = jsonHeaders
        )
    }

    private fun respondNotificationsPaged(
        scope: MockRequestHandleScope,
        request: HttpRequestData
    ) = run {
        val page = request.url.parameters["page"]?.toIntOrNull() ?: 1
        val pageSize = request.url.parameters["pageSize"]?.toIntOrNull() ?: 20

        val raw = context.assets.open("mock/notifications/notifications.json")
            .bufferedReader().use { it.readText() }
        val root = lenientJson.parseToJsonElement(raw).jsonObject
        val data = root["data"]!!.jsonObject
        val allItems = data["items"]!!.jsonArray
        val unreadCount = data["unreadCount"]!!.jsonPrimitive.int

        val fromIndex = (page - 1) * pageSize
        val toIndex = minOf(fromIndex + pageSize, allItems.size)
        val pageItems = if (fromIndex >= allItems.size) emptyList()
                        else allItems.subList(fromIndex, toIndex)

        val itemsJson = pageItems.joinToString(",", "[", "]")
        val json = """{"success":true,"data":{"unreadCount":$unreadCount,"items":$itemsJson},"message":null,"errors":null}"""
        scope.respond(content = json, status = HttpStatusCode.OK, headers = jsonHeaders)
    }

    private fun respondAsset(scope: MockRequestHandleScope, path: String) = run {
        val json = context.assets.open(path).bufferedReader().use { it.readText() }
        scope.respond(
            content = json,
            status = HttpStatusCode.OK,
            headers = jsonHeaders
        )
    }

    private companion object {
        val jsonHeaders = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        val lenientJson = Json { ignoreUnknownKeys = true; isLenient = true }
    }

    private fun normalizedPath(request: HttpRequestData): String {
        return request.url.encodedPath.trim('/')
    }
}
