package com.havos.lubricerp.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MockAssetResponseProviderTest {

    private lateinit var client: HttpClient
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    @Before
    fun setup() {
        val provider = MockAssetResponseProvider(RuntimeEnvironment.getApplication())
        val engine = MockEngine { request ->
            with(provider) { respondFor(request) }
        }
        client = HttpClient(engine)
    }

    @Test
    fun `POST auth login returns login success JSON`() = runTest {
        val response = client.post("https://mock.test/api/auth/login") {
            setBody("{\"email\":\"test\",\"password\":\"test\"}")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertTrue(body["success"]!!.jsonPrimitive.content.toBoolean())
    }

    @Test
    fun `POST auth logout returns logout success JSON`() = runTest {
        val response = client.post("https://mock.test/api/auth/logout")
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `POST auth refresh returns refresh success JSON`() = runTest {
        val response = client.post("https://mock.test/api/auth/refresh")
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `GET auth profile returns profile JSON`() = runTest {
        val response = client.get("https://mock.test/api/auth/profile")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
        val data = body["data"]!!.jsonObject
        assertEquals("test@example.com", data["email"]!!.jsonPrimitive.content)
    }

    @Test
    fun `GET dashboard returns dashboard JSON`() = runTest {
        val response = client.get("https://mock.test/api/dashboard")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
        val data = body["data"]!!.jsonObject
        val dashboard = data["dashboard"]!!.jsonObject
        val stats = dashboard["stats"]!!.jsonObject
        assertEquals(100, stats["total_sales"]!!.jsonPrimitive.int)
    }

    @Test
    fun `GET customers returns customers JSON`() = runTest {
        val response = client.get("https://mock.test/api/customers")
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `GET notifications returns paged response`() = runTest {
        val response = client.get("https://mock.test/api/notifications?page=1&pageSize=5")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
        val data = body["data"]!!.jsonObject
        assertEquals(10, data["unreadCount"]!!.jsonPrimitive.int)
        val items = data["items"]!!.jsonArray
        assertEquals(5, items.size)
    }

    @Test
    fun `GET notifications page 2 returns next page of items`() = runTest {
        val response = client.get("https://mock.test/api/notifications?page=2&pageSize=7")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
        val items = body["data"]!!.jsonObject["items"]!!.jsonArray
        assertEquals(7, items.size)
    }

    @Test
    fun `GET notifications page beyond range returns empty items`() = runTest {
        val response = client.get("https://mock.test/api/notifications?page=99&pageSize=20")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
        val items = body["data"]!!.jsonObject["items"]!!.jsonArray
        assertEquals(0, items.size)
    }

    @Test
    fun `GET notifications unread count returns unread count JSON`() = runTest {
        val response = client.get("https://mock.test/api/notifications/unread-count")
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `GET sales orders with Confirmed status returns pending orders`() = runTest {
        val response = client.get("https://mock.test/api/sales-orders?status=Confirmed")
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `GET sales orders with Dispatched status returns dispatched orders`() = runTest {
        val response = client.get("https://mock.test/api/sales-orders?status=Dispatched")
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `GET specific sales order returns detail JSON`() = runTest {
        val response = client.get("https://mock.test/api/sales-orders/10")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals(10, body["data"]!!.jsonObject["order"]!!.jsonObject["id"]!!.jsonPrimitive.int)
    }

    @Test
    fun `GET payments received returns payments JSON`() = runTest {
        val response = client.get("https://mock.test/api/payments/received")
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `GET sales invoices returns invoices JSON`() = runTest {
        val response = client.get("https://mock.test/api/sales-invoices")
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `GET proforma invoices returns proforma JSON`() = runTest {
        val response = client.get("https://mock.test/api/proforma-invoices")
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `GET reports sales summary returns report JSON`() = runTest {
        val response = client.get("https://mock.test/api/reports/sales-summary")
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `POST payments record returns record payment JSON`() = runTest {
        val response = client.post("https://mock.test/api/payments/record")
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `unknown route returns 404 with error message`() = runTest {
        val response = client.get("https://mock.test/api/unknown/endpoint")
        assertEquals(HttpStatusCode.NotFound, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("route not found", ignoreCase = true))
    }

    @Test
    fun `GET customer mobile summary returns specific response`() = runTest {
        val response = client.get("https://mock.test/api/customers/1/mobile-summary")
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `GET sales invoices with trailing slash works`() = runTest {
        val response = client.get("https://mock.test/api/sales-invoices/")
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `route matching is case sensitive on path`() = runTest {
        val response = client.get("https://mock.test/api/DASHBOARD")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }
}
