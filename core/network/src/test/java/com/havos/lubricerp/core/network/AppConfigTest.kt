package com.havos.lubricerp.core.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppConfigTest {

    @Test
    fun `ResolvedNetworkConfig holds all properties correctly`() {
        val config = ResolvedNetworkConfig(
            environment = AppEnvironment.TEST,
            useMockEngine = true,
            baseUrl = "https://test.example.com/api"
        )

        assertEquals(AppEnvironment.TEST, config.environment)
        assertTrue(config.useMockEngine)
        assertEquals("https://test.example.com/api", config.baseUrl)
    }

    @Test
    fun `useMockEngine is false for production config`() {
        val config = ResolvedNetworkConfig(
            environment = AppEnvironment.PRODUCTION,
            useMockEngine = false,
            baseUrl = "https://api.example.com"
        )

        assertEquals(AppEnvironment.PRODUCTION, config.environment)
        assertFalse(config.useMockEngine)
    }

    @Test
    fun `copy creates new instance with modified properties`() {
        val original = ResolvedNetworkConfig(
            environment = AppEnvironment.TEST,
            useMockEngine = true,
            baseUrl = "https://test.example.com"
        )

        val modified = original.copy(
            environment = AppEnvironment.PRODUCTION,
            useMockEngine = false
        )

        assertEquals(AppEnvironment.PRODUCTION, modified.environment)
        assertFalse(modified.useMockEngine)
        assertEquals(original.baseUrl, modified.baseUrl)
    }

    @Test
    fun `equals returns true for identical configs`() {
        val a = ResolvedNetworkConfig(AppEnvironment.STAGE, true, "https://api.example.com")
        val b = ResolvedNetworkConfig(AppEnvironment.STAGE, true, "https://api.example.com")
        assertEquals(a, b)
    }

    @Test
    fun `equals returns false for different configs`() {
        val a = ResolvedNetworkConfig(AppEnvironment.TEST, true, "https://test.example.com")
        val b = ResolvedNetworkConfig(AppEnvironment.PRODUCTION, false, "https://api.example.com")
        assertNotEquals(a, b)
    }
}
