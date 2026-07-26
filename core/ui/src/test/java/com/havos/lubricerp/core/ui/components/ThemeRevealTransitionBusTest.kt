package com.havos.lubricerp.core.ui.components

import androidx.compose.ui.geometry.Offset
import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ThemeRevealTransitionBusTest {

    @Before
    fun setup() {
        // Drain any leftover events from the singleton shared flow.
        // SharedFlow doesn't support draining via tryEmit/collect easily; we
        // rely on the fact that a subscriber created after emission won't see
        // past values (no replay).
    }

    @Test
    fun `subscriber receives emitted offset`() = runTest {
        val origin = Offset(100f, 200f)

        ThemeRevealTransitionBus.originEvents.test {
            ThemeRevealTransitionBus.emitOrigin(origin)
            assertEquals(origin, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `subscriber receives null offset`() = runTest {
        ThemeRevealTransitionBus.originEvents.test {
            ThemeRevealTransitionBus.emitOrigin(null)
            assertEquals(null, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `multiple emissions are received in order`() = runTest {
        val first = Offset(10f, 20f)
        val second = Offset(30f, 40f)
        val third = Offset.Zero

        ThemeRevealTransitionBus.originEvents.test {
            ThemeRevealTransitionBus.emitOrigin(first)
            ThemeRevealTransitionBus.emitOrigin(second)
            ThemeRevealTransitionBus.emitOrigin(third)

            assertEquals(first, awaitItem())
            assertEquals(second, awaitItem())
            assertEquals(third, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `late subscriber does not receive previously emitted values`() = runTest {
        ThemeRevealTransitionBus.emitOrigin(Offset(50f, 50f))

        ThemeRevealTransitionBus.originEvents.test {
            ThemeRevealTransitionBus.emitOrigin(Offset(99f, 99f))
            assertEquals(Offset(99f, 99f), awaitItem())
            // The first emission (50,50) should NOT be received
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `tryEmit does not throw when buffer is full`() = runTest {
        // With extraBufferCapacity=1, back-to-back emissions without a subscriber
        // should be handled without exception.
        ThemeRevealTransitionBus.emitOrigin(Offset(1f, 1f))
        ThemeRevealTransitionBus.emitOrigin(Offset(2f, 2f))
        // tryEmit returns false if buffer full, but it doesn't throw
        // Just verifying no crash
    }
}
