package com.havos.lubricerp.feature_reports.data.mapper

import com.havos.lubricerp.feature_reports.data.dto.CustomerDto
import com.havos.lubricerp.feature_reports.data.dto.LoginResponseDto
import com.havos.lubricerp.feature_reports.data.dto.NotificationItemDto
import com.havos.lubricerp.feature_reports.data.dto.NotificationPageDataDto
import com.havos.lubricerp.feature_reports.data.dto.PaymentReceivedItemDto
import com.havos.lubricerp.feature_reports.data.dto.PaymentReceivedPagedDataDto
import com.havos.lubricerp.feature_reports.data.dto.ProfileDataDto
import com.havos.lubricerp.feature_reports.data.dto.SalesInvoiceItemDto
import com.havos.lubricerp.feature_reports.data.dto.SalesOrderItemDto
import com.havos.lubricerp.feature_reports.data.dto.SalesOrderPagedDataDto
import com.havos.lubricerp.core.common.PagedResult
import com.havos.lubricerp.feature_reports.domain.model.AuthSession
import com.havos.lubricerp.feature_reports.domain.model.Customer
import com.havos.lubricerp.feature_reports.domain.model.NotificationItem
import com.havos.lubricerp.feature_reports.domain.model.SalesInvoiceItem
import com.havos.lubricerp.feature_reports.domain.model.SalesOrderItem
import com.havos.lubricerp.feature_reports.domain.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DomainMappersTest {

    @Test
    fun `LoginResponseDto maps to AuthSession correctly`() {
        val dto = LoginResponseDto(
            username = "john_doe",
            token = "access_token_123",
            refreshToken = "refresh_token_456"
        )

        val result = dto.toDomain()

        assertEquals("john_doe", result.username)
        assertEquals("access_token_123", result.token)
        assertEquals("refresh_token_456", result.refreshToken)
    }

    @Test
    fun `ProfileDataDto maps to UserProfile correctly`() {
        val dto = ProfileDataDto(
            id = 42L,
            email = "user@example.com",
            fullName = "John Doe",
            branchId = 7L,
            roles = listOf("ADMIN", "SALES")
        )

        val result = dto.toDomain()

        assertEquals(42L, result.id)
        assertEquals("user@example.com", result.email)
        assertEquals("John Doe", result.fullName)
        assertEquals(7L, result.branchId)
        assertEquals(listOf("ADMIN", "SALES"), result.roles)
    }

    @Test
    fun `CustomerDto maps to Customer correctly`() {
        val dto = CustomerDto(
            id = 1L,
            name = "Acme Corp",
            code = "C001",
            phone = "1234567890",
            email = "acme@example.com",
            address = "123 Main St",
            gSTNumber = "GST123",
            state = "MH"
        )

        val result = dto.toDomain()

        assertEquals(1L, result.id)
        assertEquals("Acme Corp", result.name)
        assertEquals("C001", result.code)
        assertEquals("1234567890", result.phone)
        assertEquals("acme@example.com", result.email)
        assertEquals("123 Main St", result.address)
        assertEquals("GST123", result.gstNumber)
        assertEquals("MH", result.state)
    }

    @Test
    fun `NotificationItemDto maps to NotificationItem correctly`() {
        val dto = NotificationItemDto(
            id = 1L,
            title = "New Order",
            message = "You have a new order #123",
            type = "order",
            linkUrl = "/orders/123",
            isRead = false,
            readAt = null,
            createdAt = "2024-06-15T10:00:00",
            timeAgo = "2 hours ago"
        )

        val result = dto.toDomain()

        assertEquals(1L, result.id)
        assertEquals("New Order", result.title)
        assertEquals("You have a new order #123", result.message)
        assertEquals("order", result.type)
        assertEquals("/orders/123", result.linkUrl)
        assertFalse(result.isRead)
        assertEquals("2024-06-15T10:00:00", result.createdAt)
        assertEquals("2 hours ago", result.timeAgo)
    }

    @Test
    fun `SalesOrderItemDto maps to SalesOrderItem correctly`() {
        val dto = SalesOrderItemDto(
            id = 100L,
            soNumber = "SO-2024-001",
            soDate = "2024-06-10",
            customerName = "Acme Corp",
            status = "Confirmed",
            totalAmount = 15000.0,
            expectedDeliveryDate = "2024-06-20",
            lineCount = 3,
            deliveredPercentage = 0.0,
            salesmanName = "Agent Smith"
        )

        val result = dto.toDomain()

        assertEquals(100L, result.id)
        assertEquals("SO-2024-001", result.soNumber)
        assertEquals("Confirmed", result.status)
        assertEquals(15000.0, result.totalAmount, 0.001)
        assertEquals("Agent Smith", result.salesmanName)
    }

    @Test
    fun `SalesInvoiceItemDto maps with nullable dueDate fallback`() {
        val dto = SalesInvoiceItemDto(
            id = 1L,
            invoiceNumber = "INV-001",
            invoiceDate = "2024-01-01",
            customerName = "Cust",
            customerCode = "C001",
            dnNumber = "DN-001",
            totalAmount = 1000.0,
            paymentStatus = "Paid",
            paidAmount = 1000.0,
            balanceAmount = 0.0,
            dueDate = null,
            isOverdue = false,
            isInterState = false
        )

        val result = dto.toDomain()

        assertEquals("", result.dueDate)
        assertFalse(result.isOverdue)
    }

    @Test
    fun `PaymentReceivedPagedDataDto maps to PagedResult correctly`() {
        val items = listOf(
            PaymentReceivedItemDto(id = 1, receiptNumber = "R001", paymentDate = "2024-01-01",
                customerName = "Cust", invoiceNumber = "INV1", amount = 1000.0,
                paymentMode = "Cash", reference = "", remarks = ""),
            PaymentReceivedItemDto(id = 2, receiptNumber = "R002", paymentDate = "2024-01-02",
                customerName = "Cust2", invoiceNumber = "INV2", amount = 2000.0,
                paymentMode = "UPI", reference = "", remarks = "")
        )
        val pagedDto = PaymentReceivedPagedDataDto(
            items = items,
            totalCount = 50,
            skip = 0,
            take = 20,
            hasMore = true
        )

        val result: PagedResult<*> = pagedDto.toDomain()

        assertEquals(2, result.items.size)
        assertEquals(50, result.totalCount)
        assertEquals(0, result.skip)
        assertEquals(20, result.take)
        assertTrue(result.hasMore)
    }
}
