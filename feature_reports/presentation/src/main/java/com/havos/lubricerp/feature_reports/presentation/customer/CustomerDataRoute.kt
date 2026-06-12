package com.havos.lubricerp.feature_reports.presentation.customer

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

@Composable
fun CustomerDataRoute(
    onBackClick: () -> Unit,
    viewModel: CustomerDataViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    CustomerDataScreen(
        state = state,
        onBackClick = onBackClick,
        onAction = { action ->
            when (action) {
                is CustomerDataAction.SearchChanged ->
                    viewModel.onIntent(CustomerDataIntent.SearchChanged(action.value))
                is CustomerDataAction.CustomerSelected ->
                    viewModel.onIntent(CustomerDataIntent.CustomerSelected(action.customer))
                CustomerDataAction.CustomerDismissed ->
                    viewModel.onIntent(CustomerDataIntent.CustomerDismissed)
                is CustomerDataAction.CallCustomer -> {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${action.phone}"))
                    context.startActivity(intent)
                }
                is CustomerDataAction.WhatsAppCustomer -> {
                    val cleaned = action.phone.replace(Regex("[^0-9]"), "")
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/91$cleaned"))
                    context.startActivity(intent)
                }
                is CustomerDataAction.LedgerFromDateChanged ->
                    viewModel.onIntent(CustomerDataIntent.LedgerFromDateChanged(action.date))
                is CustomerDataAction.LedgerToDateChanged ->
                    viewModel.onIntent(CustomerDataIntent.LedgerToDateChanged(action.date))
                CustomerDataAction.LoadLedger ->
                    viewModel.onIntent(CustomerDataIntent.LoadLedger)
                CustomerDataAction.LoadMobileSummary ->
                    viewModel.onIntent(CustomerDataIntent.LoadMobileSummary)
                is CustomerDataAction.LedgerDatePreset ->
                    viewModel.onIntent(CustomerDataIntent.LedgerDatePreset(action.label, action.fromDate, action.toDate))
                CustomerDataAction.Retry ->
                    viewModel.onIntent(CustomerDataIntent.Load)
                CustomerDataAction.Refresh ->
                    viewModel.onIntent(CustomerDataIntent.Refresh)
                CustomerDataAction.ShowPaymentSheet ->
                    viewModel.onIntent(CustomerDataIntent.ShowPaymentSheet)
                CustomerDataAction.DismissPaymentSheet ->
                    viewModel.onIntent(CustomerDataIntent.DismissPaymentSheet)
                is CustomerDataAction.PaymentFormInvoiceChanged ->
                    viewModel.onIntent(CustomerDataIntent.PaymentFormInvoiceChanged(action.invoiceId))
                is CustomerDataAction.PaymentFormAmountChanged ->
                    viewModel.onIntent(CustomerDataIntent.PaymentFormAmountChanged(action.amount))
                is CustomerDataAction.PaymentFormModeChanged ->
                    viewModel.onIntent(CustomerDataIntent.PaymentFormModeChanged(action.mode))
                is CustomerDataAction.PaymentFormDateChanged ->
                    viewModel.onIntent(CustomerDataIntent.PaymentFormDateChanged(action.date))
                is CustomerDataAction.PaymentFormReferenceChanged ->
                    viewModel.onIntent(CustomerDataIntent.PaymentFormReferenceChanged(action.reference))
                is CustomerDataAction.PaymentFormRemarksChanged ->
                    viewModel.onIntent(CustomerDataIntent.PaymentFormRemarksChanged(action.remarks))
                CustomerDataAction.SubmitPayment ->
                    viewModel.onIntent(CustomerDataIntent.SubmitPayment)
                CustomerDataAction.PaymentResultDismissed ->
                    viewModel.onIntent(CustomerDataIntent.PaymentResultDismissed)
            }
        }
    )
}
