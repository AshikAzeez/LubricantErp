package com.havos.lubricerp.feature_reports.presentation.reports

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.havos.lubricerp.core.ui.components.ReportListShimmer
import com.havos.lubricerp.core.ui.components.StatCard
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@Composable
internal fun TwoMetricReportScreen(
    modifier: Modifier,
    headline: String,
    state: ReportDetailUiState,
    onAction: (ReportDetailAction) -> Unit,
    primaryMetric: String,
    primaryValue: String,
    secondaryMetric: String,
    secondaryValue: String,
    headers: List<String>,
    rows: List<List<String>>
) {
    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text(headline) }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = state.fromDate,
                        onValueChange = { onAction(ReportDetailAction.FromDateChanged(it)) },
                        label = { Text("From Date") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = state.toDate,
                        onValueChange = { onAction(ReportDetailAction.ToDateChanged(it)) },
                        label = { Text("To Date") },
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = { onAction(ReportDetailAction.ApplyFilter) },
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) { Text("Apply") }
                }
            }
        }
        item {
            AdaptiveStatRow {
                StatCard(primaryMetric, primaryValue, Modifier.weight(1f))
                StatCard(secondaryMetric, secondaryValue, Modifier.weight(1f))
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = headers.joinToString("  |  "),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    rows.forEach { row ->
                        Text(
                            text = row.joinToString("  |  "),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AdaptiveStatRow(content: @Composable RowScope.() -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            content = content
        )
    }
}

@Composable
internal fun ReportLoadingScreen(modifier: Modifier = Modifier) {
    ReportListShimmer(modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DatePickerButton(
    label: String,
    value: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    minDateMillis: Long? = null,
    maxDateMillis: Long? = null,
    borderColor: Color? = null
) {
    val utcFmt = remember {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }

    val todayEndOfDay = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    val effectiveMax = maxDateMillis ?: todayEndOfDay
    val effectiveMin = minDateMillis

    val initialMillis = remember(value) {
        runCatching { utcFmt.parse(value)?.time }.getOrNull()
    }

    val selectableDates = remember(effectiveMin, effectiveMax) {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val afterMin = effectiveMin == null || utcTimeMillis >= effectiveMin
                val beforeMax = utcTimeMillis <= effectiveMax
                return afterMin && beforeMax
            }
        }
    }

    var showPicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis,
        selectableDates = selectableDates
    )

    val isError = remember(value, effectiveMin, effectiveMax) {
        val millis = runCatching { utcFmt.parse(value)?.time }.getOrNull()
        millis != null && (millis > effectiveMax || (effectiveMin != null && millis < effectiveMin))
    }

    val shape = RoundedCornerShape(6.dp)
    val borderColor = borderColor ?: if (isError) MaterialTheme.colorScheme.error
                      else MaterialTheme.colorScheme.outlineVariant

    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (isError) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .border(1.dp, borderColor, shape)
                .clickable { showPicker = true }
                .padding(horizontal = 8.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = value.ifBlank { "Select date" },
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    isError -> MaterialTheme.colorScheme.error
                    value.isBlank() -> MaterialTheme.colorScheme.onSurfaceVariant
                    else -> MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Filled.CalendarMonth,
                contentDescription = null,
                tint = if (isError) MaterialTheme.colorScheme.error
                       else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
        if (isError) {
            Text(
                text = when {
                    effectiveMin != null && (runCatching { utcFmt.parse(value)?.time }.getOrNull() ?: 0L) < effectiveMin ->
                        "Must be on or after From date"
                    else -> "Future dates not allowed"
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 2.dp, start = 2.dp)
            )
        }
    }

    if (showPicker) {
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showPicker = false
                    datePickerState.selectedDateMillis?.let { millis ->
                        onDateSelected(utcFmt.format(millis))
                    }
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
