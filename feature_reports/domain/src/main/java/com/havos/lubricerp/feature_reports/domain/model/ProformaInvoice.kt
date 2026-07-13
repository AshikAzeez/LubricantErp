package com.havos.lubricerp.feature_reports.domain.model

data class ProformaInvoice(
    val id: Long,
    val proformaNumber: String,
    val date: String,
    val validUntilDate: String,
    val customerName: String,
    val customerCode: String,
    val status: String,
    val totalAmount: Double,
    val lineCount: Int,
    val soNumber: String?,
    val salesOrderId: Long?,
    val isInterState: Boolean
)

data class ProformaInvoiceDetail(
    val id: Long,
    val proformaNumber: String,
    val date: String,
    val validUntilDate: String,
    val customerId: Long,
    val customerName: String,
    val customerCode: String,
    val customerGST: String?,
    val customerAddress: String,
    val customerState: String?,
    val customerStateCode: String?,
    val status: String,
    val isInterState: Boolean,
    val remarks: String?,
    val termsAndConditions: String?,
    val subTotal: Double,
    val cgstAmount: Double,
    val sgstAmount: Double,
    val igstAmount: Double,
    val roundOffAmount: Double,
    val totalAmount: Double,
    val salesOrderId: Long?,
    val salesOrderNumber: String?,
    val lines: List<ProformaInvoiceLine>
)

data class ProformaInvoiceLine(
    val id: Long,
    val deliveryType: String,
    val productGradeId: Int,
    val productGradeName: String,
    val productSKUId: Int,
    val productSKUName: String,
    val hsnCode: String,
    val description: String?,
    val quantity: Double,
    val unitOfMeasurement: String? = null,
    val unitPrice: Double,
    val discountPercent: Double,
    val taxRate: Double,
    val lineSubTotal: Double,
    val taxAmount: Double,
    val lineTotal: Double
)

data class CreateProformaInvoiceLine(
    val deliveryType: String,
    val productGradeId: Int,
    val productSKUId: Int? = null,
    val hsnCode: String,
    val quantity: Int,
    val unitPrice: Double,
    val taxRate: Double,
    val discountPercent: Double
)

data class CreateProformaInvoiceRequest(
    val customerId: Long,
    val proformaDate: String,
    val validUntilDate: String,
    val remarks: String?,
    val termsAndConditions: String?,
    val lines: List<CreateProformaInvoiceLine>
)

data class CreateProformaInvoiceResponse(
    val id: Long,
    val proformaNumber: String
)

data class ProductSku(
    val id: Int,
    val name: String,
    val code: String,
    val productGradeId: Int,
    val productGrade: String,
    val productFamily: String,
    val hsnCode: String,
    val packSizeLiters: Double,
    val packSizeLabel: String,
    val unitOfMeasureId: Int
)
