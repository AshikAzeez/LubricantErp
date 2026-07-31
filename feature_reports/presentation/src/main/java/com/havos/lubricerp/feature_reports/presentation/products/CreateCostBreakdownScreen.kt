package com.havos.lubricerp.feature_reports.presentation.products

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.havos.lubricerp.feature_reports.domain.model.ProductSku
import com.havos.lubricerp.feature_reports.domain.model.RawMaterialStockItem
import com.havos.lubricerp.feature_reports.presentation.reports.DatePickerButton
import com.havos.lubricerp.feature_reports.presentation.reports.uomLabel
import org.koin.androidx.compose.koinViewModel
import java.util.Calendar

@Composable
fun CreateCostBreakdownRoute(
    sheetId: Long?,
    onBackClick: () -> Unit,
    onSaved: () -> Unit,
    viewModel: CreateCostBreakdownViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(sheetId) {
        if (sheetId != null) {
            viewModel.onIntent(CreateCostBreakdownIntent.LoadSheetForEdit(sheetId))
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is CreateCostBreakdownEffect.Toast -> snackbarHostState.showSnackbar(effect.message)
                CreateCostBreakdownEffect.Created -> onSaved()
                CreateCostBreakdownEffect.Updated -> onSaved()
                CreateCostBreakdownEffect.NavigateBack -> onBackClick()
            }
        }
    }

    CreateCostBreakdownScreen(
        state = state,
        onBackClick = onBackClick,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCostBreakdownScreen(
    state: CreateCostBreakdownUiState,
    onBackClick: () -> Unit,
    onIntent: (CreateCostBreakdownIntent) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    var showLineSheet by remember { mutableStateOf(false) }
    var editingLineIndex by remember { mutableIntStateOf(-1) }
    val isEditMode = state.editingSheetId != null

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditMode) "Edit Cost Breakdown" else "Create Cost Breakdown",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            state.isLoadingLookups || state.isPrefilling -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }
            state.lookupError != null -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = state.lookupError,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { onIntent(CreateCostBreakdownIntent.LoadLookups) }) {
                        Text("Retry")
                    }
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    BasicInformationSection(state, onIntent)
                    RawMaterialsSection(
                        state = state,
                        onAddClick = {
                            editingLineIndex = -1
                            showLineSheet = true
                        },
                        onEditClick = { index ->
                            editingLineIndex = index
                            showLineSheet = true
                        },
                        onRemoveClick = { index -> onIntent(CreateCostBreakdownIntent.RemoveLine(index)) }
                    )
                    AdditionalCostsSection(state, onIntent)
                    CostSummarySection(state)

                    Button(
                        onClick = { onIntent(CreateCostBreakdownIntent.Submit) },
                        enabled = !state.isSubmitting,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (state.isSubmitting) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text(
                                text = if (isEditMode) "Save Changes" else "Create Cost Breakdown",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = { onIntent(CreateCostBreakdownIntent.CancelClicked) },
                        enabled = !state.isSubmitting,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }

    if (showLineSheet) {
        AddRawMaterialBottomSheet(
            materials = state.rawMaterials,
            initialLine = state.lines.getOrNull(editingLineIndex),
            onDismiss = { showLineSheet = false },
            onConfirm = { line ->
                if (editingLineIndex >= 0) {
                    onIntent(CreateCostBreakdownIntent.UpdateLine(editingLineIndex, line))
                } else {
                    onIntent(CreateCostBreakdownIntent.AddLine(line))
                }
                showLineSheet = false
            }
        )
    }
}

// ── Section 1 – Basic Information ────────────────────────────────────────────

@Composable
private fun BasicInformationSection(
    state: CreateCostBreakdownUiState,
    onIntent: (CreateCostBreakdownIntent) -> Unit
) {
    SectionCard(title = "Basic Information") {
        DropdownField(
            label = "Product Family *",
            value = state.selectedFamily,
            placeholder = "Choose Product Family",
            enabled = true,
            errorText = state.familyError,
            options = state.families,
            optionLabel = { it },
            onSelected = { onIntent(CreateCostBreakdownIntent.FamilySelected(it)) }
        )
        DropdownField(
            label = "Product Grade *",
            value = state.selectedGrade,
            placeholder = if (state.selectedFamily == null) "Select Product Family first" else "Choose Product Grade",
            enabled = state.selectedFamily != null,
            errorText = state.gradeError,
            options = state.grades,
            optionLabel = { it },
            onSelected = { onIntent(CreateCostBreakdownIntent.GradeSelected(it)) }
        )
        DropdownField(
            label = "Product SKU *",
            value = state.selectedSku?.let { "${it.name} (${it.code})" },
            placeholder = if (state.selectedGrade == null) "Select Product Grade first" else "Choose Product SKU",
            enabled = state.selectedFamily != null && state.selectedGrade != null,
            errorText = state.skuError,
            options = state.skusForSelection,
            optionLabel = { "${it.name} (${it.code})" },
            optionSupportText = { "Pack: ${it.packSizeLabel}" },
            onSelected = { onIntent(CreateCostBreakdownIntent.SkuSelected(it)) }
        )

        val farFutureMillis = remember {
            Calendar.getInstance().apply { add(Calendar.YEAR, 10) }.timeInMillis
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                DatePickerButton(
                    label = "Effective From *",
                    value = state.effectiveFrom,
                    onDateSelected = { onIntent(CreateCostBreakdownIntent.EffectiveFromChanged(it)) },
                    maxDateMillis = farFutureMillis
                )
                FieldError(state.effectiveFromError)
            }
            Column(modifier = Modifier.weight(1f)) {
                DatePickerButton(
                    label = "Effective To",
                    value = state.effectiveTo,
                    onDateSelected = { onIntent(CreateCostBreakdownIntent.EffectiveToChanged(it)) },
                    maxDateMillis = farFutureMillis
                )
                FieldError(state.effectiveToError)
            }
        }

        OutlinedTextField(
            value = state.remarks,
            onValueChange = { if (it.length <= 500) onIntent(CreateCostBreakdownIntent.RemarksChanged(it)) },
            label = { Text("Remarks") },
            minLines = 2,
            maxLines = 4,
            supportingText = { Text("${state.remarks.length}/500") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ── Section 2 – Raw Materials ────────────────────────────────────────────────

@Composable
private fun RawMaterialsSection(
    state: CreateCostBreakdownUiState,
    onAddClick: () -> Unit,
    onEditClick: (Int) -> Unit,
    onRemoveClick: (Int) -> Unit
) {
    SectionCard(title = "Raw Materials") {
        if (state.linesError != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = state.linesError,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        state.lines.forEachIndexed { index, line ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = line.rawMaterialName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${line.rawMaterialCode} • Qty: ${"%.4f".format(line.quantity)} ${line.uom} × ₹${"%.2f".format(line.rate)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "₹%,.2f".format(line.amount),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    IconButton(onClick = { onEditClick(index) }) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit ${line.rawMaterialName}",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { onRemoveClick(index) }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Remove ${line.rawMaterialName}",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        OutlinedButton(
            onClick = onAddClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Add Raw Material")
        }

        if (state.lines.isNotEmpty()) {
            HorizontalDivider()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Subtotal", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(
                    "₹%,.2f".format(state.rmSubtotal),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// ── Section 3 – Additional Costs (per liter) ─────────────────────────────────

@Composable
private fun AdditionalCostsSection(
    state: CreateCostBreakdownUiState,
    onIntent: (CreateCostBreakdownIntent) -> Unit
) {
    SectionCard(title = "Additional Costs (per liter)") {
        CostField(
            label = "Package Cost",
            value = state.packageCostStr,
            errorText = state.packageCostError,
            onValueChange = { onIntent(CreateCostBreakdownIntent.PackageCostChanged(it)) }
        )
        CostField(
            label = "Margin",
            value = state.marginStr,
            errorText = state.marginError,
            onValueChange = { onIntent(CreateCostBreakdownIntent.MarginChanged(it)) }
        )
        CostField(
            label = "Transport",
            value = state.transportStr,
            errorText = state.transportError,
            onValueChange = { onIntent(CreateCostBreakdownIntent.TransportChanged(it)) }
        )
    }
}

@Composable
private fun CostField(
    label: String,
    value: String,
    errorText: String?,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        prefix = { Text("₹") },
        isError = errorText != null,
        supportingText = errorText?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    )
}

// ── Section 4 – Cost Summary ─────────────────────────────────────────────────

@Composable
private fun CostSummarySection(state: CreateCostBreakdownUiState) {
    SectionCard(title = "Cost Summary") {
        SummaryRow("Per Liter Cost (All RM)", state.perLiterCostAllRm)
        SummaryRow("Base Oil Per Liter", state.baseOilPerLiter)
        SummaryRow("+ Package / Margin / Transport", state.additionalPerLiter)
        HorizontalDivider()
        SummaryRow("Sub Total (per liter)", state.subTotalPerLiter, bold = true)
        SummaryRow("+ GST (18%)", state.gstAmount)
        HorizontalDivider()
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Final Price (per liter)",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                "₹%,.2f".format(state.finalPricePerLiter),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: Double, bold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "₹%,.2f".format(value),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium
        )
    }
}

// ── Shared building blocks ───────────────────────────────────────────────────

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            content()
        }
    }
}

@Composable
private fun FieldError(errorText: String?) {
    if (errorText != null) {
        Text(
            text = errorText,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(top = 2.dp, start = 2.dp)
        )
    }
}

@Composable
private fun <T> DropdownField(
    label: String,
    value: String?,
    placeholder: String,
    enabled: Boolean,
    errorText: String?,
    options: List<T>,
    optionLabel: (T) -> String,
    onSelected: (T) -> Unit,
    optionSupportText: ((T) -> String)? = null
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = if (errorText != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled) { expanded = true }
        ) {
            OutlinedTextField(
                value = value ?: placeholder,
                onValueChange = {},
                readOnly = true,
                enabled = false,
                isError = errorText != null,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = if (value != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledBorderColor = if (errorText != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                trailingIcon = {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = null
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                if (options.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No options available") },
                        onClick = { expanded = false },
                        enabled = false
                    )
                } else {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(optionLabel(option), fontWeight = FontWeight.Bold)
                                    optionSupportText?.let {
                                        Text(
                                            text = it(option),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            },
                            onClick = {
                                onSelected(option)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
        FieldError(errorText)
    }
}

// ── Add / Edit Raw Material bottom sheet ─────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddRawMaterialBottomSheet(
    materials: List<RawMaterialStockItem>,
    initialLine: RawMaterialLineInput?,
    onDismiss: () -> Unit,
    onConfirm: (RawMaterialLineInput) -> Unit
) {
    var selectedMaterial by remember {
        mutableStateOf(materials.find { it.id == initialLine?.rawMaterialId })
    }
    var quantityStr by remember {
        mutableStateOf(initialLine?.quantity?.toString() ?: "")
    }
    var rateStr by remember {
        mutableStateOf(initialLine?.rate?.toString() ?: "")
    }
    var validationError by remember { mutableStateOf<String?>(null) }

    val quantity = quantityStr.toDoubleOrNull()
    val rate = rateStr.toDoubleOrNull()
    val amount = if (quantity != null && rate != null) quantity * rate else null

    val sheetState = rememberModalBottomSheetState(
        confirmValueChange = { targetValue -> targetValue != SheetValue.Hidden }
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (initialLine != null) "Edit Raw Material" else "Add Raw Material",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close Bottom Sheet")
                }
            }

            if (validationError != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = validationError!!,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            DropdownField(
                label = "Select Material *",
                value = selectedMaterial?.let { "${it.name} (${it.code})" },
                placeholder = "Choose Raw Material",
                enabled = true,
                errorText = null,
                options = materials,
                optionLabel = { "${it.name} (${it.code})" },
                optionSupportText = { "${it.type} • ${uomLabel(it.unitOfMeasureId)} • ₹${"%.2f".format(it.costPerUnit)}/unit" },
                onSelected = { material ->
                    selectedMaterial = material
                    // Prefill the rate with the material's current cost per unit.
                    if (rateStr.isBlank()) rateStr = material.costPerUnit.toString()
                }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = quantityStr,
                    onValueChange = { quantityStr = it },
                    label = { Text("Qty *") },
                    suffix = { selectedMaterial?.let { Text(uomLabel(it.unitOfMeasureId)) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = rateStr,
                    onValueChange = { rateStr = it },
                    label = { Text("Rate *") },
                    prefix = { Text("₹") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = amount?.let { "%,.2f".format(it) } ?: "",
                onValueChange = {},
                readOnly = true,
                enabled = false,
                label = { Text("Amount (Qty × Rate)") },
                prefix = { Text("₹") },
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledPrefixColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.material3.TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        val material = selectedMaterial
                        if (material == null) {
                            validationError = "Please select a raw material first."
                            return@Button
                        }
                        if (quantity == null || quantity <= 0) {
                            validationError = "Please enter a valid quantity greater than zero."
                            return@Button
                        }
                        if (rate == null || rate <= 0) {
                            validationError = "Please enter a valid rate greater than zero."
                            return@Button
                        }
                        onConfirm(
                            RawMaterialLineInput(
                                rawMaterialId = material.id,
                                rawMaterialCode = material.code,
                                rawMaterialName = material.name,
                                uom = uomLabel(material.unitOfMeasureId),
                                isBaseOil = material.type.equals("BaseOil", ignoreCase = true),
                                quantity = quantity,
                                rate = rate
                            )
                        )
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (initialLine != null) "Update" else "Add Material")
                }
            }
        }
    }
}
