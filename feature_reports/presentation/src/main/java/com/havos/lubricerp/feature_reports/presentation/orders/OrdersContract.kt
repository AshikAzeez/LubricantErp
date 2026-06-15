package com.havos.lubricerp.feature_reports.presentation.orders

import androidx.compose.runtime.Stable
import com.havos.lubricerp.core.common.UiIntent
import com.havos.lubricerp.core.common.UiState
import com.havos.lubricerp.feature_reports.domain.model.SalesInvoiceDetail
import com.havos.lubricerp.feature_reports.domain.model.SalesInvoiceItem
import com.havos.lubricerp.feature_reports.domain.model.SalesOrderDetail
import com.havos.lubricerp.feature_reports.domain.model.SalesOrderItem

sealed interface OrdersIntent : UiIntent {
    data object Load : OrdersIntent
    data object Refresh : OrdersIntent
    data object Retry : OrdersIntent
    data class TabSelected(val index: Int) : OrdersIntent
    data class OrderClicked(val orderId: Long) : OrdersIntent
    data class InvoiceClicked(val invoiceId: Long) : OrdersIntent
    data object DismissDetail : OrdersIntent
    data class FromDateChanged(val value: String) : OrdersIntent
    data class ToDateChanged(val value: String) : OrdersIntent
    data object ApplyInvoiceFilter : OrdersIntent
}

@Stable
data class OrdersUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val selectedTab: Int = 0,
    val pendingOrders: List<SalesOrderItem> = emptyList(),
    val dispatchedOrders: List<SalesOrderItem> = emptyList(),
    val invoices: List<SalesInvoiceItem> = emptyList(),
    val selectedOrderDetail: SalesOrderDetail? = null,
    val selectedInvoiceDetail: SalesInvoiceDetail? = null,
    val showOrderDetail: Boolean = false,
    val showInvoiceDetail: Boolean = false,
    val loadingOrderId: Long? = null,
    val loadingInvoiceId: Long? = null,
    val invoiceFromDate: String = "",
    val invoiceToDate: String = "",
    val invoiceFilterStatus: String? = null
) : UiState

sealed interface OrdersAction {
    data class TabSelected(val index: Int) : OrdersAction
    data class OrderClicked(val orderId: Long) : OrdersAction
    data class InvoiceClicked(val invoiceId: Long) : OrdersAction
    data object DismissDetail : OrdersAction
    data object Retry : OrdersAction
    data object Refresh : OrdersAction
    data class FromDateChanged(val value: String) : OrdersAction
    data class ToDateChanged(val value: String) : OrdersAction
    data object ApplyInvoiceFilter : OrdersAction
}
