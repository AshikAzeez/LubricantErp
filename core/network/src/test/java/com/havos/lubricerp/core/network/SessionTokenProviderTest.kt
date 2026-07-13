package com.havos.lubricerp.core.network

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SessionTokenProviderTest {

    private class FakeTokenProvider : SessionTokenProvider {
        private var accessToken: String? = "access-token"
        private var refreshToken: String? = "refresh-token"
        private var cleared = false
        private var refreshCalled = false

        override suspend fun getAccessToken(): String? = accessToken

        override suspend fun getRefreshToken(): String? = refreshToken

        override suspend fun refreshAndSave(): String? {
            refreshCalled = true
            accessToken = "new-access-token"
            return accessToken
        }

        override suspend fun clearSession() {
            cleared = true
            accessToken = null
            refreshToken = null
        }

        fun wasCleared() = cleared
        fun wasRefreshed() = refreshCalled
    }

    @Test
    fun `SessionTokenProvider can be implemented`() = runTest {
        val provider = FakeTokenProvider()

        assertEquals("access-token", provider.getAccessToken())
        assertEquals("refresh-token", provider.getRefreshToken())
    }

    @Test
    fun `refreshAndSave returns new token`() = runTest {
        val provider = FakeTokenProvider()

        val newToken = provider.refreshAndSave()

        assertEquals("new-access-token", newToken)
        assertEquals("new-access-token", provider.getAccessToken())
        assert(provider.wasRefreshed())
    }

    @Test
    fun `clearSession nullifies tokens`() = runTest {
        val provider = FakeTokenProvider()

        provider.clearSession()

        assertNull(provider.getAccessToken())
        assertNull(provider.getRefreshToken())
        assert(provider.wasCleared())
    }
}
