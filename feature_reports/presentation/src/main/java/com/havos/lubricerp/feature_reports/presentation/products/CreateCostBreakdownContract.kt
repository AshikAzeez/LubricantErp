package com.havos.lubricerp.feature_reports.presentation.products

import androidx.compose.runtime.Stable
import com.havos.lubricerp.core.common.UiIntent
import com.havos.lubricerp.core.common.UiState
import com.havos.lubricerp.feature_reports.domain.model.ProductSku
import com.havos.lubricerp.feature_reports.domain.model.RawMaterialStockItem

/** A raw material line entered by the user in the create form. */
@Stable
data class RawMaterialLineInput(
    val rawMaterialId: Long,
    val rawMaterialCode: String,
    val rawMaterialName: String,
    val uom: String,
    val isBaseOil: Boolean,
    val quantity: Double,
    val rate: Double
) {
    val amount: Double get() = quantity * rate
}

sealed interface CreateCostBreakdownIntent : UiIntent {
    data object LoadLookups : CreateCostBreakdownIntent
    data class LoadSheetForEdit(val sheetId: Long) : CreateCostBreakdownIntent
    data class FamilySelected(val family: String) : CreateCostBreakdownIntent
    data class GradeSelected(val grade: String) : CreateCostBreakdownIntent
    data class SkuSelected(val sku: ProductSku) : CreateCostBreakdownIntent
    data class EffectiveFromChanged(val date: String) : CreateCostBreakdownIntent
    data class EffectiveToChanged(val date: String) : CreateCostBreakdownIntent
    data class RemarksChanged(val remarks: String) : CreateCostBreakdownIntent
    data class AddLine(val line: RawMaterialLineInput) : CreateCostBreakdownIntent
    data class UpdateLine(val index: Int, val line: RawMaterialLineInput) : CreateCostBreakdownIntent
    data class RemoveLine(val index: Int) : CreateCostBreakdownIntent
    data class PackageCostChanged(val value: String) : CreateCostBreakdownIntent
    data class MarginChanged(val value: String) : CreateCostBreakdownIntent
    data class TransportChanged(val value: String) : CreateCostBreakdownIntent
    data object Submit : CreateCostBreakdownIntent
    data object CancelClicked : CreateCostBreakdownIntent
}

@Stable
data class CreateCostBreakdownUiState(
    // Lookup data
    val isLoadingLookups: Boolean = true,
    val lookupError: String? = null,
    val skus: List<ProductSku> = emptyList(),
    val rawMaterials: List<RawMaterialStockItem> = emptyList(),
    // Edit mode – non-null when an existing sheet is being edited
    val editingSheetId: Long? = null,
    val isPrefilling: Boolean = false,
    // Section 1 – Basic Information
    val selectedFamily: String? = null,
    val selectedGrade: String? = null,
    val selectedSku: ProductSku? = null,
    val effectiveFrom: String = "",
    val effectiveTo: String = "",
    val remarks: String = "",
    // Section 2 – Raw Materials
    val lines: List<RawMaterialLineInput> = emptyList(),
    // Section 3 – Additional Costs (per liter)
    val packageCostStr: String = "",
    val marginStr: String = "",
    val transportStr: String = "",
    // Validation errors (null = valid)
    val familyError: String? = null,
    val gradeError: String? = null,
    val skuError: String? = null,
    val effectiveFromError: String? = null,
    val effectiveToError: String? = null,
    val linesError: String? = null,
    val packageCostError: String? = null,
    val marginError: String? = null,
    val transportError: String? = null,
    // Submission
    val isSubmitting: Boolean = false
) : UiState {

    val families: List<String>
        get() = skus.map { it.productFamily }.distinct().sorted()

    val grades: List<String>
        get() = skus.filter { it.productFamily == selectedFamily }
            .map { it.productGrade }.distinct().sorted()

    val skusForSelection: List<ProductSku>
        get() = skus.filter { it.productFamily == selectedFamily && it.productGrade == selectedGrade }
            .sortedBy { it.name }

    // ── Section 4 – Cost summary (derived) ─────────────────────────────────
    val rmSubtotal: Double get() = lines.sumOf { it.amount }

    /** Total batch volume in liters, derived from the entered line quantities. */
    val batchLiters: Double get() = lines.sumOf { it.quantity }

    val perLiterCostAllRm: Double
        get() = if (batchLiters > 0) rmSubtotal / batchLiters else 0.0

    val baseOilPerLiter: Double
        get() = if (batchLiters > 0) lines.filter { it.isBaseOil }.sumOf { it.amount } / batchLiters else 0.0

    val additionalPerLiter: Double
        get() = (packageCostStr.toDoubleOrNull() ?: 0.0) +
            (marginStr.toDoubleOrNull() ?: 0.0) +
            (transportStr.toDoubleOrNull() ?: 0.0)

    val subTotalPerLiter: Double get() = perLiterCostAllRm + additionalPerLiter

    val gstAmount: Double get() = subTotalPerLiter * GST_RATE

    val finalPricePerLiter: Double get() = subTotalPerLiter + gstAmount

    companion object {
        const val GST_RATE = 0.18
    }
}

sealed interface CreateCostBreakdownEffect {
    data class Toast(val message: String) : CreateCostBreakdownEffect
    data object Created : CreateCostBreakdownEffect
    data object Updated : CreateCostBreakdownEffect
    data object NavigateBack : CreateCostBreakdownEffect
}
