package com.havos.lubricerp.core.database

import app.cash.turbine.test
import com.havos.lubricerp.core.common.ThemeMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SecureSessionStoreTest {

    private lateinit var store: SecureSessionStore

    @Before
    fun setup() = runTest {
        store = SecureSessionStoreImpl(RuntimeEnvironment.getApplication(), FakeCryptoManager())
        // Ensure every test starts from a clean state
        store.clearSession()
        store.clearRememberedUsername()
        store.setRememberMeEnabled(false)
        store.setThemeMode(ThemeMode.SYSTEM)
    }

    @Test
    fun `sessionFlow emits null when no session stored`() = runTest {
        store.sessionFlow.test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `saveSession then sessionFlow emits decrypted session data`() = runTest {
        val session = SessionData(
            username = "john.doe",
            token = "access-token-123",
            refreshToken = "refresh-token-456"
        )

        store.sessionFlow.test {
            assertNull(awaitItem())

            store.saveSession(session)

            val emitted = awaitItem()
            assertNotNull(emitted)
            assertEquals(session.username, emitted!!.username)
            assertEquals(session.token, emitted.token)
            assertEquals(session.refreshToken, emitted.refreshToken)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `saveSession without refresh token emits blank refresh token`() = runTest {
        store.sessionFlow.test {
            assertNull(awaitItem())

            store.saveSession(SessionData(username = "user", token = "token"))

            val emitted = awaitItem()
            assertNotNull(emitted)
            assertEquals("user", emitted!!.username)
            assertEquals("token", emitted.token)
            assertEquals("", emitted.refreshToken)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clearSession removes stored session`() = runTest {
        store.saveSession(SessionData("user", "token", "refresh"))

        store.sessionFlow.test {
            assertNotNull(awaitItem())

            store.clearSession()

            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clearSession also clears sales filter`() = runTest {
        store.saveSalesFilter("2024-01-01", "2024-01-31")

        store.salesFilterFlow.test {
            assertEquals(Pair("2024-01-01", "2024-01-31"), awaitItem())

            store.clearSession()

            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `rememberedUsernameFlow emits empty string when nothing stored`() = runTest {
        store.rememberedUsernameFlow.test {
            assertEquals("", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `saveRememberedUsername stores and emits decrypted username`() = runTest {
        store.rememberedUsernameFlow.test {
            assertEquals("", awaitItem())

            store.saveRememberedUsername("remembered_user")

            assertEquals("remembered_user", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `saveRememberedUsername with blank value removes stored username`() = runTest {
        store.saveRememberedUsername("some_user")

        store.rememberedUsernameFlow.test {
            assertEquals("some_user", awaitItem())

            store.saveRememberedUsername("   ")

            assertEquals("", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clearRememberedUsername removes stored username`() = runTest {
        store.saveRememberedUsername("some_user")

        store.rememberedUsernameFlow.test {
            assertEquals("some_user", awaitItem())

            store.clearRememberedUsername()

            assertEquals("", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `rememberMeEnabledFlow defaults to false`() = runTest {
        store.rememberMeEnabledFlow.test {
            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setRememberMeEnabled updates flow`() = runTest {
        store.rememberMeEnabledFlow.test {
            assertFalse(awaitItem())

            store.setRememberMeEnabled(true)
            assertTrue(awaitItem())

            store.setRememberMeEnabled(false)
            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `themeModeFlow defaults to SYSTEM`() = runTest {
        store.themeModeFlow.test {
            assertEquals(ThemeMode.SYSTEM, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setThemeMode stores and emits theme mode`() = runTest {
        store.themeModeFlow.test {
            assertEquals(ThemeMode.SYSTEM, awaitItem())

            store.setThemeMode(ThemeMode.DARK)
            assertEquals(ThemeMode.DARK, awaitItem())

            store.setThemeMode(ThemeMode.LIGHT)
            assertEquals(ThemeMode.LIGHT, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `salesFilterFlow emits null when no filter stored`() = runTest {
        store.salesFilterFlow.test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `saveSalesFilter stores and emits date range`() = runTest {
        store.salesFilterFlow.test {
            assertNull(awaitItem())

            store.saveSalesFilter("2024-03-01", "2024-03-31")

            assertEquals(Pair("2024-03-01", "2024-03-31"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clearSalesFilter removes stored filter`() = runTest {
        store.saveSalesFilter("2024-03-01", "2024-03-31")

        store.salesFilterFlow.test {
            assertEquals(Pair("2024-03-01", "2024-03-31"), awaitItem())

            store.clearSalesFilter()

            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `theme mode survives special characters via encryption round trip`() = runTest {
        store.setThemeMode(ThemeMode.DARK)

        store.themeModeFlow.test {
            assertEquals(ThemeMode.DARK, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
