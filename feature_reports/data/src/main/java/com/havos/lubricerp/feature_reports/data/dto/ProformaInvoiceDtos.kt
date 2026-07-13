package com.havos.lubricerp.feature_reports.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProformaInvoiceDto(
    val id: Long = 0L,
    val proformaNumber: String = "",
    val date: String = "",
    val validUntilDate: String = "",
    val customerName: String = "",
    val customerCode: String = "",
    val status: String = "",
    val totalAmount: Double = 0.0,
    val lineCount: Int = 0,
    val soNumber: String? = null,
    val salesOrderId: Long? = null,
    val isInterState: Boolean = false
)

@Serializable
data class ProformaInvoiceListApiResponseDto(
    val success: Boolean = false,
    val data: ProformaInvoicePagedDataDto? = null,
    val message: String? = null,
    val errors: List<String>? = null
)

@Serializable
data class ProformaInvoicePagedDataDto(
    val items: List<ProformaInvoiceDto> = emptyList(),
    val totalCount: Int = 0,
    val skip: Int = 0,
    val take: Int = 20,
    val hasMore: Boolean = false
)

@Serializable
data class ProformaInvoiceLineDto(
    val id: Long = 0L,
    val deliveryType: String = "",
    val productGradeId: Int = 0,
    val productGradeName: String = "",
    val productSKUId: Int = 0,
    val productSKUName: String = "",
    // Real API alternative field names
    val productGrade: String? = null,
    val productFamily: String? = null,
    val sku: String? = null,
    val hsnCode: String = "",
    val description: String? = null,
    val quantity: Double = 0.0,
    val unitOfMeasurement: String? = null,
    val unitPrice: Double = 0.0,
    val discountPercent: Double? = null,
    val taxRate: Double = 0.0,
    val lineSubTotal: Double = 0.0,
    val taxAmount: Double = 0.0,
    val lineTotal: Double = 0.0
)

@Serializable
data class ProformaInvoiceDetailDto(
    val id: Long = 0L,
    val proformaNumber: String = "",
    val date: String = "",
    val validUntilDate: String = "",
    val customerId: Long = 0L,
    val customerName: String = "",
    val customerCode: String = "",
    val customerGST: String? = null,
    // Real API uses customerGSTNumber instead of customerGST
    val customerGSTNumber: String? = null,
    val customerAddress: String = "",
    val customerState: String? = null,
    val customerStateCode: String? = null,
    val status: String = "",
    val isInterState: Boolean = false,
    val remarks: String? = null,
    val termsAndConditions: String? = null,
    val subTotal: Double = 0.0,
    val totalTaxAmount: Double = 0.0,
    val cgstAmount: Double = 0.0,
    val sgstAmount: Double = 0.0,
    val igstAmount: Double = 0.0,
    val roundOffAmount: Double = 0.0,
    val totalAmount: Double = 0.0,
    val salesOrderId: Long? = null,
    val salesOrderNumber: String? = null,
    val lines: List<ProformaInvoiceLineDto> = emptyList()
)

@Serializable
data class ProformaInvoiceDetailApiResponseDto(
    val success: Boolean = false,
    val data: ProformaInvoiceDetailDto? = null,
    val message: String? = null,
    val errors: List<String>? = null
)

@OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
@Serializable
data class CreateProformaInvoiceLineDto(
    val deliveryType: String,
    val productGradeId: Int,
    @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.NEVER)
    val productSKUId: Int? = null,
    val hsnCode: String,
    val quantity: Int,
    val unitPrice: Double,
    val taxRate: Double,
    val discountPercent: Double
)

@Serializable
data class CreateProformaInvoiceRequestDto(
    val customerId: Long,
    val proformaDate: String,
    val validUntilDate: String,
    val remarks: String?,
    val termsAndConditions: String?,
    val lines: List<CreateProformaInvoiceLineDto>
)

@Serializable
data class CreateProformaInvoiceResponseDto(
    val id: Long,
    val proformaNumber: String
)

@Serializable
data class CreateProformaInvoiceApiResponseDto(
    val success: Boolean = false,
    val data: ProformaInvoiceDetailDto? = null,
    val message: String? = null,
    val errors: List<String>? = null
)

@Serializable
data class ProductSkuDto(
    val id: Int = 0,
    val name: String = "",
    val code: String = "",
    val productGradeId: Int = 0,
    val productGrade: String = "",
    val productFamily: String = "",
    val hsnCode: String = "",
    val packSizeLiters: Double = 0.0,
    val packSizeLabel: String = "",
    val unitOfMeasureId: Int = 0
)

@Serializable
data class ProductSkuListApiResponseDto(
    val success: Boolean = false,
    val data: List<ProductSkuDto> = emptyList(),
    val message: String? = null,
    val errors: List<String>? = null
)

@Serializable
data class UpdateProformaInvoiceApiResponseDto(
    val success: Boolean = false,
    val message: String? = null,
    val errors: List<String>? = null
)
