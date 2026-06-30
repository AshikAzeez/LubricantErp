package com.havos.lubricerp.feature_reports.presentation.navigation

object AppRoutes {
    const val GATE = "gate"
    const val LOGIN = "login"
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val REPORT_DETAIL = "report_detail/{reportKey}"
    const val CUSTOMER_DATA = "customer_data"
    const val NOTIFICATIONS = "notifications"
    const val REPORT_MODULE = "report_module/{reportItemKey}"
    const val PAYMENT_REPORT = "payment_report"
    const val ORDERS = "orders"
    const val PROFORMA_INVOICES = "proforma_invoices"
    const val PROFORMA_INVOICE_DETAIL = "proforma_invoice_detail/{invoiceId}"
    const val CREATE_PROFORMA_INVOICE = "create_proforma_invoice?invoiceId={invoiceId}"

    fun reportDetail(reportKey: String): String = "report_detail/$reportKey"
    fun reportModule(reportItemKey: String): String = "report_module/$reportItemKey"
    fun proformaInvoiceDetail(id: Long): String = "proforma_invoice_detail/$id"
    fun createProformaInvoice(invoiceId: Long? = null): String {
        return if (invoiceId != null) {
            "create_proforma_invoice?invoiceId=$invoiceId"
        } else {
            "create_proforma_invoice"
        }
    }
}
