package com.havos.lubricerp.feature_reports.presentation.proforma

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.havos.lubricerp.feature_reports.domain.model.Customer
import com.havos.lubricerp.feature_reports.domain.model.CreateProformaInvoiceLine
import com.havos.lubricerp.feature_reports.domain.model.ProductSku
import com.havos.lubricerp.feature_reports.presentation.reports.DatePickerButton
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat
import java.util.*

@Composable
fun CreateProformaInvoiceRoute(
    invoiceId: Long?,
    onBackClick: () -> Unit,
    viewModel: CreateProformaInvoiceViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(invoiceId) {
        if (invoiceId != null) {
            viewModel.onIntent(CreateProformaInvoiceIntent.LoadInvoiceForEdit(invoiceId))
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is CreateProformaInvoiceEffect.ShowToast -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                CreateProformaInvoiceEffect.NavigateBack -> {
                    onBackClick()
                }
            }
        }
    }

    CreateProformaInvoiceScreen(
        state = state,
        onBackClick = onBackClick,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProformaInvoiceScreen(
    state: CreateProformaInvoiceUiState,
    onBackClick: () -> Unit,
    onIntent: (CreateProformaInvoiceIntent) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    var showAddLineItemBottomSheet by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (state.editingInvoiceId != null) "Edit Proforma Invoice" else "Create Proforma Invoice",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)

        if (state.isLoading) {
            Box(modifier = contentModifier, contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = contentModifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Error Banner
            if (state.error != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = state.error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // 2. Customer Selection
            Text(
                text = "Customer Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            var customerDropdownExpanded by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { customerDropdownExpanded = true }
            ) {
                OutlinedTextField(
                    value = state.selectedCustomer?.let { "${it.name} (${it.code})" } ?: "Select Customer *",
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    trailingIcon = {
                        Icon(
                            imageVector = if (customerDropdownExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                DropdownMenu(
                    expanded = customerDropdownExpanded,
                    onDismissRequest = { customerDropdownExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    state.customers.forEach { customer ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(customer.name, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "Code: ${customer.code} | State: ${customer.state}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            onClick = {
                                onIntent(CreateProformaInvoiceIntent.CustomerSelected(customer))
                                customerDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // 3. Proforma Date & Valid Until Date (indicated with *)
            Text(
                text = "Invoice Dates",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val maxFutureMillis = System.currentTimeMillis() + 10 * 365 * 24 * 60 * 60 * 1000L

                DatePickerButton(
                    label = "Proforma Date *",
                    value = convertDateToDisplay(state.proformaDate),
                    onDateSelected = { onIntent(CreateProformaInvoiceIntent.ProformaDateChanged(convertDateToApi(it))) },
                    maxDateMillis = maxFutureMillis,
                    modifier = Modifier.weight(1f)
                )

                DatePickerButton(
                    label = "Valid Until *",
                    value = convertDateToDisplay(state.validUntilDate),
                    onDateSelected = { onIntent(CreateProformaInvoiceIntent.ValidUntilDateChanged(convertDateToApi(it))) },
                    maxDateMillis = maxFutureMillis,
                    modifier = Modifier.weight(1f)
                )
            }

            // 4. Remarks (max 100 char)
            OutlinedTextField(
                value = state.remarks,
                onValueChange = { onIntent(CreateProformaInvoiceIntent.RemarksChanged(it)) },
                label = { Text("Remarks (Max 100 chars)") },
                placeholder = { Text("E.g., As per inquiry dated 22-May-2026") },
                supportingText = {
                    Text(
                        text = "${state.remarks.length}/100",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // 5. Terms and Conditions (multi line max 500 char)
            OutlinedTextField(
                value = state.termsAndConditions,
                onValueChange = { onIntent(CreateProformaInvoiceIntent.TermsChanged(it)) },
                label = { Text("Terms & Conditions (Max 500 chars)") },
                placeholder = { Text("E.g., Payment within 14 days.") },
                minLines = 3,
                maxLines = 5,
                supportingText = {
                    Text(
                        text = "${state.termsAndConditions.length}/500",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // 6. Add line items option (+)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Line Items",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Button(
                    onClick = { showAddLineItemBottomSheet = true },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Item",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Item")
                }
            }

            if (state.lines.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No line items added. Click 'Add Item' to insert line items.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                state.lines.forEachIndexed { index, line ->
                    LineItemInputCard(
                        line = line,
                        index = index,
                        products = state.products,
                        onDeleteClick = { onIntent(CreateProformaInvoiceIntent.RemoveLineItem(index)) }
                    )
                }
            }

            // Summary Card
            if (state.lines.isNotEmpty()) {
                val subTotal = state.lines.sumOf { it.quantity * it.unitPrice * (1.0 - it.discountPercent / 100.0) }
                val totalTax = state.lines.sumOf { it.quantity * it.unitPrice * (1.0 - it.discountPercent / 100.0) * (it.taxRate / 100.0) }
                val totalAmount = subTotal + totalTax

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Summary",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Line Items", style = MaterialTheme.typography.bodyMedium)
                            Text("${state.lines.size}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Subtotal", style = MaterialTheme.typography.bodyMedium)
                            Text(formatCurrency(subTotal), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tax Amount", style = MaterialTheme.typography.bodyMedium)
                            Text(formatCurrency(totalTax), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                        
                        HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Amount", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                text = formatCurrency(totalAmount),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 7. Submit Button
            Button(
                onClick = { onIntent(CreateProformaInvoiceIntent.Submit) },
                enabled = !state.isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = if (state.editingInvoiceId != null) "Update Proforma Invoice" else "Create Proforma Invoice",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }

    if (showAddLineItemBottomSheet) {
        AddLineItemBottomSheet(
            products = state.products,
            onDismiss = { showAddLineItemBottomSheet = false },
            onConfirm = { line ->
                onIntent(CreateProformaInvoiceIntent.AddLineItem(line))
                showAddLineItemBottomSheet = false
            }
        )
    }
}

@Composable
fun LineItemInputCard(
    line: CreateProformaInvoiceLine,
    index: Int,
    products: List<ProductSku>,
    onDeleteClick: () -> Unit
) {
    val product = products.find { it.id == line.productSKUId }
    val productName = product?.name ?: "SKU: ${line.productSKUId}"
    val productGradeName = product?.productGrade ?: "Grade ID: ${line.productGradeId}"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Item #${index + 1} - $productName",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Grade: $productGradeName • HSN: ${line.hsnCode} • Type: ${line.deliveryType}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove Item",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
                }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${formatQuantity(line.quantity)} × ${formatCurrency(line.unitPrice)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val lineTotal = line.quantity * line.unitPrice * (1.0 - line.discountPercent / 100.0) * (1.0 + line.taxRate / 100.0)
                Text(
                    text = formatCurrency(lineTotal),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLineItemBottomSheet(
    products: List<ProductSku>,
    onDismiss: () -> Unit,
    onConfirm: (CreateProformaInvoiceLine) -> Unit
) {
    var deliveryType by remember { mutableStateOf("Packaged") }
    var selectedProduct by remember { mutableStateOf<ProductSku?>(null) }
    var selectedGradeId by remember { mutableStateOf<Int?>(null) }
    var selectedGradeHsn by remember { mutableStateOf<String>("") }
    var quantityStr by remember { mutableStateOf("1") }
    var unitPriceStr by remember { mutableStateOf("") }
    var taxRateStr by remember { mutableStateOf("18.0") }
    var discountPercentStr by remember { mutableStateOf("0.0") }

    var validationError by remember { mutableStateOf<String?>(null) }

    val sheetState = rememberModalBottomSheetState(
        confirmValueChange = { targetValue ->
            targetValue != SheetValue.Hidden
        }
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
                    text = "Add Line Item",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Bottom Sheet"
                    )
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

            // 1. Delivery Type Dropdown
            var deliveryDropdownExpanded by remember { mutableStateOf(false) }
            Text(
                text = "Delivery Type",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { deliveryDropdownExpanded = true }
            ) {
                OutlinedTextField(
                    value = deliveryType,
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    trailingIcon = {
                        Icon(
                            imageVector = if (deliveryDropdownExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                DropdownMenu(
                    expanded = deliveryDropdownExpanded,
                    onDismissRequest = { deliveryDropdownExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                        listOf("Packaged", "Bulk").forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    deliveryType = type
                                    selectedProduct = null
                                    selectedGradeId = null
                                    selectedGradeHsn = ""
                                    deliveryDropdownExpanded = false
                                }
                            )
                        }
                }
            }

            // 2. Product / Grade selection based on delivery type
            if (deliveryType == "Packaged") {
                var productDropdownExpanded by remember { mutableStateOf(false) }
                Text(
                    text = "Select Product *",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { productDropdownExpanded = true }
                ) {
                    OutlinedTextField(
                        value = selectedProduct?.let { "${it.name} (${it.code})" } ?: "Choose Product",
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = if (selectedProduct != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        trailingIcon = {
                            Icon(
                                imageVector = if (productDropdownExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    DropdownMenu(
                        expanded = productDropdownExpanded,
                        onDismissRequest = { productDropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        if (products.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No products available") },
                                onClick = { productDropdownExpanded = false },
                                enabled = false
                            )
                        } else {
                            products.forEach { product ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(product.name, fontWeight = FontWeight.Bold)
                                            Text(
                                                text = "Grade: ${product.productGrade} | Family: ${product.productFamily}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedProduct = product
                                        productDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                selectedProduct?.let { product ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Product Info (Auto-filled)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text("Grade: ${product.productGrade} (ID: ${product.productGradeId})", style = MaterialTheme.typography.bodySmall)
                            Text("SKU: ${product.code} (ID: ${product.id})", style = MaterialTheme.typography.bodySmall)
                            Text("HSN Code: ${product.hsnCode}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            } else {
                val distinctGrades = products
                    .distinctBy { it.productGradeId }
                    .sortedBy { it.productGrade }

                var gradeDropdownExpanded by remember { mutableStateOf(false) }
                Text(
                    text = "Select Grade *",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { gradeDropdownExpanded = true }
                ) {
                    OutlinedTextField(
                        value = selectedGradeId?.let { id ->
                            distinctGrades.find { g -> g.productGradeId == id }?.let { "${it.productGrade} (ID: ${it.productGradeId})" } ?: "Choose Grade"
                        } ?: "Choose Grade",
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = if (selectedGradeId != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        trailingIcon = {
                            Icon(
                                imageVector = if (gradeDropdownExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    DropdownMenu(
                        expanded = gradeDropdownExpanded,
                        onDismissRequest = { gradeDropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        if (distinctGrades.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No grades available") },
                                onClick = { gradeDropdownExpanded = false },
                                enabled = false
                            )
                        } else {
                            distinctGrades.forEach { grade ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(grade.productGrade, fontWeight = FontWeight.Bold)
                                            Text(
                                                text = "ID: ${grade.productGradeId} | HSN: ${grade.hsnCode}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedGradeId = grade.productGradeId
                                        selectedGradeHsn = grade.hsnCode
                                        gradeDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = quantityStr,
                    onValueChange = { quantityStr = it },
                    label = { Text("Quantity *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = unitPriceStr,
                    onValueChange = { unitPriceStr = it },
                    label = { Text("Unit Price *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = taxRateStr,
                    onValueChange = { taxRateStr = it },
                    label = { Text("Tax Rate (%) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = discountPercentStr,
                    onValueChange = { discountPercentStr = it },
                    label = { Text("Discount (%) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        val qty = quantityStr.toIntOrNull()
                        val price = unitPriceStr.toDoubleOrNull()
                        val tax = taxRateStr.toDoubleOrNull()
                        val discount = discountPercentStr.toDoubleOrNull()

                        if (qty == null || price == null || tax == null || discount == null) {
                            validationError = "Please fill all fields with valid numbers."
                            return@Button
                        }

                        if (deliveryType == "Packaged") {
                            val product = selectedProduct
                            if (product == null) {
                                validationError = "Please select a product first."
                                return@Button
                            }
                            onConfirm(
                                CreateProformaInvoiceLine(
                                    deliveryType = deliveryType,
                                    productGradeId = product.productGradeId,
                                    productSKUId = product.id,
                                    hsnCode = product.hsnCode,
                                    quantity = qty,
                                    unitPrice = price,
                                    taxRate = tax,
                                    discountPercent = discount
                                )
                            )
                        } else {
                            val gradeId = selectedGradeId
                            if (gradeId == null) {
                                validationError = "Please select a grade first."
                                return@Button
                            }
                            onConfirm(
                                CreateProformaInvoiceLine(
                                    deliveryType = deliveryType,
                                    productGradeId = gradeId,
                                    productSKUId = null,
                                    hsnCode = selectedGradeHsn,
                                    quantity = qty,
                                    unitPrice = price,
                                    taxRate = tax,
                                    discountPercent = discount
                                )
                            )
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Add Item")
                }
            }
        }
    }
}

private fun formatQuantity(qty: Int): String = qty.toString()

private fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    return formatter.format(amount)
}

private fun convertDateToDisplay(dateStr: String): String {
    if (dateStr.isBlank()) return ""
    return try {
        val date = java.time.LocalDate.parse(dateStr)
        date.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    } catch (_: Exception) {
        dateStr
    }
}

private fun convertDateToApi(dateStr: String): String {
    if (dateStr.isBlank()) return ""
    return try {
        val date = java.time.LocalDate.parse(dateStr, java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        date.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    } catch (_: Exception) {
        dateStr
    }
}
