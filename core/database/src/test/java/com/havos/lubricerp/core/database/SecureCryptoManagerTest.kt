package com.havos.lubricerp.core.database

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for [SecureCryptoManager] paths that do not require the
 * AndroidKeyStore. Full encrypt/decrypt round-trip tests must run as
 * instrumented tests because AndroidKeyStore is not available on the JVM
 * (not supported by Robolectric either).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SecureCryptoManagerTest {

    private val cryptoManager = SecureCryptoManager()

    @Test
    fun `encrypt returns empty string for empty input without touching keystore`() {
        assertEquals("", cryptoManager.encrypt(""))
    }

    @Test
    fun `encrypt returns blank input unchanged without touching keystore`() {
        assertEquals("   ", cryptoManager.encrypt("   "))
    }

    @Test
    fun `decrypt returns empty string for empty input without touching keystore`() {
        assertEquals("", cryptoManager.decrypt(""))
    }

    @Test
    fun `decrypt returns input unchanged when there is no iv separator`() {
        assertEquals("not_encrypted_text", cryptoManager.decrypt("not_encrypted_text"))
    }

    @Test
    fun `decrypt returns blank input unchanged`() {
        assertEquals("   ", cryptoManager.decrypt("   "))
    }

    @Test
    fun `FakeCryptoManager round trips values for store tests`() {
        val fake = FakeCryptoManager()
        val values = listOf(
            "simple",
            "user@example.com",
            "with spaces and \n newlines",
            "unicode 世界 🌍",
            "1234567890",
            "special !@#$%^&*() chars"
        )
        values.forEach { value ->
            assertEquals(value, fake.decrypt(fake.encrypt(value)))
        }
    }

    @Test
    fun `FakeCryptoManager encrypted value differs from plain text`() {
        val fake = FakeCryptoManager()
        val encrypted = fake.encrypt("secret")
        assert(encrypted != "secret")
        assert(encrypted.startsWith("fake:"))
    }
}
