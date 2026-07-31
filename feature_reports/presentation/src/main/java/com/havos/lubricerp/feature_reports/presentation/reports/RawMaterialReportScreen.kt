package com.havos.lubricerp.feature_reports.presentation.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun RawMaterialStockScreen(
    state: ReportDetailUiState,
    onAction: (ReportDetailAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val filtered = remember(state.rawMaterialItems, state.searchQuery) {
        state.rawMaterialItems.filter {
            state.searchQuery.isBlank() ||
                it.name.contains(state.searchQuery, ignoreCase = true) ||
                it.code.contains(state.searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text(text = "Raw material master with cost per unit") }
        item {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { onAction(ReportDetailAction.SearchChanged(it)) },
                label = { Text("Search") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filtered.forEach { item ->
                        Text("${item.code} - ${item.name} (${uomLabel(item.unitOfMeasureId)})")
                    }
                }
            }
        }
    }
}
