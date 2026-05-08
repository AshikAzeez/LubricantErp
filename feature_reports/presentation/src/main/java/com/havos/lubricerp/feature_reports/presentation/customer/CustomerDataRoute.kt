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
            }
        }
    )
}
