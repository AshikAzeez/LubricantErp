package com.havos.lubricerp.feature_reports.presentation.reports

/**
 * Display label for a raw material's `unitOfMeasureId`.
 *
 * The `api/raw-materials` endpoint returns only the numeric unit-of-measure id, so the label is
 * resolved locally. Ids follow the master data used by the backend: 1 = Liters, 2 = Kilograms,
 * 3 = Numbers/pieces. Unknown ids fall back to a neutral label.
 */
internal fun uomLabel(unitOfMeasureId: Int): String = when (unitOfMeasureId) {
    1 -> "L"
    2 -> "Kg"
    3 -> "Nos"
    else -> "Unit"
}
