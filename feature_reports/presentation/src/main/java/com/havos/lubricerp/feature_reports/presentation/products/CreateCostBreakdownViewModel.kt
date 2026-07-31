package com.havos.lubricerp.feature_reports.presentation.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.havos.lubricerp.core.common.ResultState
import com.havos.lubricerp.feature_reports.domain.model.CostBreakdownDetail
import com.havos.lubricerp.feature_reports.domain.model.CreateCostBreakdownLine
import com.havos.lubricerp.feature_reports.domain.model.CreateCostBreakdownRequest
import com.havos.lubricerp.feature_reports.domain.usecase.CreateCostBreakdownUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.GetCostBreakdownDetailUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.GetProductSkusUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.GetRawMaterialStockUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.ObserveSessionUseCase
import com.havos.lubricerp.feature_reports.domain.usecase.UpdateCostBreakdownUseCase
import com.havos.lubricerp.feature_reports.presentation.reports.uomLabel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class CreateCostBreakdownViewModel(
    private val observeSessionUseCase: ObserveSessionUseCase,
    private val getProductSkusUseCase: GetProductSkusUseCase,
    private val getRawMaterialStockUseCase: GetRawMaterialStockUseCase,
    private val createCostBreakdownUseCase: CreateCostBreakdownUseCase,
    private val getCostBreakdownDetailUseCase: GetCostBreakdownDetailUseCase,
    private val updateCostBreakdownUseCase: UpdateCostBreakdownUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CreateCostBreakdownUiState())
    val state: StateFlow<CreateCostBreakdownUiState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<CreateCostBreakdownEffect>()
    val effect: SharedFlow<CreateCostBreakdownEffect> = _effect.asSharedFlow()

    /** Kept so that edit-mode prefill can wait for the dropdown lookups to arrive. */
    private var lookupJob: Job = loadLookups()

    fun onIntent(intent: CreateCostBreakdownIntent) {
        when (intent) {
            is CreateCostBreakdownIntent.LoadLookups -> {
                lookupJob.cancel()
                lookupJob = loadLookups()
                // A retry in edit mode has to re-resolve the prefill against the fresh lookups.
                _state.value.editingSheetId?.let { loadSheetForEdit(it) }
            }
            is CreateCostBreakdownIntent.LoadSheetForEdit -> loadSheetForEdit(intent.sheetId)
            is CreateCostBreakdownIntent.FamilySelected -> _state.update {
                it.copy(
                    selectedFamily = intent.family,
                    selectedGrade = null,
                    selectedSku = null,
                    familyError = null,
                    gradeError = null,
                    skuError = null
                )
            }
            is CreateCostBreakdownIntent.GradeSelected -> _state.update {
                it.copy(
                    selectedGrade = intent.grade,
                    selectedSku = null,
                    gradeError = null,
                    skuError = null
                )
            }
            is CreateCostBreakdownIntent.SkuSelected -> _state.update {
                it.copy(selectedSku = intent.sku, skuError = null)
            }
            is CreateCostBreakdownIntent.EffectiveFromChanged -> _state.update {
                it.copy(effectiveFrom = intent.date, effectiveFromError = null, effectiveToError = null)
            }
            is CreateCostBreakdownIntent.EffectiveToChanged -> _state.update {
                it.copy(effectiveTo = intent.date, effectiveToError = null)
            }
            is CreateCostBreakdownIntent.RemarksChanged -> _state.update {
                it.copy(remarks = intent.remarks)
            }
            is CreateCostBreakdownIntent.AddLine -> _state.update {
                it.copy(lines = it.lines + intent.line, linesError = null)
            }
            is CreateCostBreakdownIntent.UpdateLine -> _state.update {
                it.copy(
                    lines = it.lines.mapIndexed { index, line ->
                        if (index == intent.index) intent.line else line
                    }
                )
            }
            is CreateCostBreakdownIntent.RemoveLine -> _state.update {
                it.copy(lines = it.lines.filterIndexed { index, _ -> index != intent.index })
            }
            is CreateCostBreakdownIntent.PackageCostChanged -> _state.update {
                it.copy(packageCostStr = intent.value, packageCostError = null)
            }
            is CreateCostBreakdownIntent.MarginChanged -> _state.update {
                it.copy(marginStr = intent.value, marginError = null)
            }
            is CreateCostBreakdownIntent.TransportChanged -> _state.update {
                it.copy(transportStr = intent.value, transportError = null)
            }
            is CreateCostBreakdownIntent.Submit -> submit()
            is CreateCostBreakdownIntent.CancelClicked -> viewModelScope.launch {
                _effect.emit(CreateCostBreakdownEffect.NavigateBack)
            }
        }
    }

    private fun loadLookups(): Job = viewModelScope.launch {
        _state.update { it.copy(isLoadingLookups = true, lookupError = null) }
        val token = runCatching { observeSessionUseCase().first()?.token.orEmpty() }.getOrElse { "" }
        val skusDeferred = async { getProductSkusUseCase(token) }
        val materialsDeferred = async { getRawMaterialStockUseCase(token) }
        val skusResult = skusDeferred.await()
        val materialsResult = materialsDeferred.await()

        if (skusResult is ResultState.Error) {
            _state.update { it.copy(isLoadingLookups = false, lookupError = skusResult.message) }
            return@launch
        }
        if (materialsResult is ResultState.Error) {
            _state.update { it.copy(isLoadingLookups = false, lookupError = materialsResult.message) }
            return@launch
        }
        _state.update {
            it.copy(
                isLoadingLookups = false,
                skus = (skusResult as? ResultState.Success)?.data ?: emptyList(),
                rawMaterials = (materialsResult as? ResultState.Success)?.data ?: emptyList()
            )
        }
    }

    private fun loadSheetForEdit(sheetId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(editingSheetId = sheetId, isPrefilling = true) }
            // The dropdowns resolve family/grade/UoM, so the lookups must be in place first.
            lookupJob.join()
            val token = runCatching { observeSessionUseCase().first()?.token.orEmpty() }.getOrElse { "" }
            when (val result = getCostBreakdownDetailUseCase(token, sheetId)) {
                is ResultState.Success -> prefill(result.data)
                is ResultState.Error -> _state.update {
                    it.copy(isPrefilling = false, lookupError = result.message)
                }
                ResultState.Loading -> Unit
            }
        }
    }

    private fun prefill(detail: CostBreakdownDetail) {
        val current = _state.value
        val sku = current.skus.firstOrNull { it.id.toLong() == detail.productSKUId }
        val lines = detail.lines.map { line ->
            val material = current.rawMaterials.firstOrNull { it.id == line.rawMaterialId }
            RawMaterialLineInput(
                rawMaterialId = line.rawMaterialId,
                rawMaterialCode = line.rawMaterialCode.ifBlank { material?.code.orEmpty() },
                rawMaterialName = line.rawMaterialName.ifBlank { material?.name.orEmpty() },
                uom = material?.let { uomLabel(it.unitOfMeasureId) } ?: "Unit",
                isBaseOil = material?.type.equals("BaseOil", ignoreCase = true),
                quantity = line.quantity,
                rate = line.rate
            )
        }
        _state.update {
            it.copy(
                isPrefilling = false,
                selectedFamily = sku?.productFamily ?: detail.productFamily.takeIf { f -> f.isNotBlank() },
                selectedGrade = sku?.productGrade ?: detail.productGrade.takeIf { g -> g.isNotBlank() },
                selectedSku = sku,
                effectiveFrom = toDisplayDate(detail.effectiveFrom),
                effectiveTo = detail.effectiveTo?.let { date -> toDisplayDate(date) }.orEmpty(),
                remarks = detail.remarks.orEmpty(),
                lines = lines,
                packageCostStr = toCostInput(detail.packageCost),
                marginStr = toCostInput(detail.margin),
                transportStr = toCostInput(detail.transportCost)
            )
        }
    }

    private fun validate(): Boolean {
        val current = _state.value
        val familyError = if (current.selectedFamily == null) "Product family is required" else null
        val gradeError = if (current.selectedGrade == null) "Product grade is required" else null
        val skuError = if (current.selectedSku == null) "Product SKU is required" else null
        val effectiveFromError = if (current.effectiveFrom.isBlank()) "Effective from date is required" else null
        val effectiveToError = if (
            current.effectiveTo.isNotBlank() && current.effectiveFrom.isNotBlank() &&
            (toIsoDate(current.effectiveTo) ?: "") < (toIsoDate(current.effectiveFrom) ?: "")
        ) "Must be on or after Effective From" else null
        val linesError = if (current.lines.isEmpty()) "Add at least one raw material" else null
        val packageCostError = validateCost(current.packageCostStr, "package cost")
        val marginError = validateCost(current.marginStr, "margin")
        val transportError = validateCost(current.transportStr, "transport cost")

        _state.update {
            it.copy(
                familyError = familyError,
                gradeError = gradeError,
                skuError = skuError,
                effectiveFromError = effectiveFromError,
                effectiveToError = effectiveToError,
                linesError = linesError,
                packageCostError = packageCostError,
                marginError = marginError,
                transportError = transportError
            )
        }
        return listOf(
            familyError, gradeError, skuError, effectiveFromError, effectiveToError,
            linesError, packageCostError, marginError, transportError
        ).all { it == null }
    }

    private fun validateCost(value: String, label: String): String? {
        if (value.isBlank()) return null // blank is treated as 0
        val parsed = value.toDoubleOrNull() ?: return "Enter a valid $label"
        return if (parsed < 0) "Cannot be negative" else null
    }

    private fun submit() {
        if (_state.value.isSubmitting || _state.value.isPrefilling) return
        if (!validate()) {
            viewModelScope.launch {
                _effect.emit(CreateCostBreakdownEffect.Toast("Please fix the highlighted errors"))
            }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true) }
            val current = _state.value
            val token = runCatching { observeSessionUseCase().first()?.token.orEmpty() }.getOrElse { "" }
            val request = CreateCostBreakdownRequest(
                productSKUId = current.selectedSku?.id?.toLong() ?: 0L,
                effectiveFrom = toIsoDate(current.effectiveFrom) ?: current.effectiveFrom,
                effectiveTo = current.effectiveTo.takeIf { it.isNotBlank() }?.let { toIsoDate(it) ?: it },
                remarks = current.remarks.takeIf { it.isNotBlank() },
                packageCost = current.packageCostStr.toDoubleOrNull() ?: 0.0,
                margin = current.marginStr.toDoubleOrNull() ?: 0.0,
                transportCost = current.transportStr.toDoubleOrNull() ?: 0.0,
                lines = current.lines.map {
                    CreateCostBreakdownLine(
                        rawMaterialId = it.rawMaterialId,
                        quantity = it.quantity,
                        rate = it.rate
                    )
                }
            )
            val editingSheetId = current.editingSheetId
            val result = if (editingSheetId != null) {
                updateCostBreakdownUseCase(token, editingSheetId, request)
            } else {
                createCostBreakdownUseCase(token, request)
            }
            when (result) {
                is ResultState.Success -> {
                    _state.update { it.copy(isSubmitting = false) }
                    if (editingSheetId != null) {
                        _effect.emit(CreateCostBreakdownEffect.Toast("Cost breakdown sheet updated"))
                        _effect.emit(CreateCostBreakdownEffect.Updated)
                    } else {
                        _effect.emit(CreateCostBreakdownEffect.Toast("Cost breakdown sheet created"))
                        _effect.emit(CreateCostBreakdownEffect.Created)
                    }
                }
                is ResultState.Error -> {
                    _state.update { it.copy(isSubmitting = false) }
                    _effect.emit(CreateCostBreakdownEffect.Toast(result.message))
                }
                ResultState.Loading -> Unit
            }
        }
    }

    /** Converts the dd/MM/yyyy value emitted by the date picker to yyyy-MM-dd for the API. */
    private fun toIsoDate(date: String): String? = runCatching {
        val parsed = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).parse(date)
        SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(parsed!!)
    }.getOrNull()

    /** Converts an API date (yyyy-MM-dd, optionally with a time part) to the picker's dd/MM/yyyy. */
    private fun toDisplayDate(date: String): String {
        val raw = date.substringBefore("T")
        return runCatching {
            val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(raw)
            SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).format(parsed!!)
        }.getOrDefault(raw)
    }

    /** Renders a cost for a text field, leaving it blank when the value is zero. */
    private fun toCostInput(value: Double): String = when {
        value == 0.0 -> ""
        value % 1.0 == 0.0 -> value.toLong().toString()
        else -> value.toString()
    }
}
