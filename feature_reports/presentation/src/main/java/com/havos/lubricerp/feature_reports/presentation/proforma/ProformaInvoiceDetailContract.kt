package com.havos.lubricerp.feature_reports.presentation.proforma

import com.havos.lubricerp.core.common.UiIntent
import com.havos.lubricerp.core.common.UiState
import com.havos.lubricerp.feature_reports.domain.model.ProformaInvoiceDetail
import androidx.compose.runtime.Stable

sealed interface ProformaInvoiceDetailIntent : UiIntent {
    data class LoadDetail(val id: Long) : ProformaInvoiceDetailIntent
    data object Refresh : ProformaInvoiceDetailIntent
    data object SendProforma : ProformaInvoiceDetailIntent
    data object CancelProforma : ProformaInvoiceDetailIntent
}

@Stable
data class ProformaInvoiceDetailUiState(
    val invoiceDetail: ProformaInvoiceDetail? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isOffline: Boolean = false,
    val error: String? = null
) : UiState

sealed interface ProformaInvoiceDetailEffect {
    data class ShowToast(val message: String) : ProformaInvoiceDetailEffect
}
