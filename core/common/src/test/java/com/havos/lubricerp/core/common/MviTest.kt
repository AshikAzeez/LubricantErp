package com.havos.lubricerp.core.common

import org.junit.Assert.assertEquals
import org.junit.Test

class MviTest {

    private data class TestState(val name: String, val count: Int) : UiState

    private sealed interface TestIntent : UiIntent {
        data class UpdateName(val name: String) : TestIntent
        data object Increment : TestIntent
    }

    @Test
    fun `UiState can be implemented by data classes`() {
        val state = TestState(name = "initial", count = 0)
        assertEquals("initial", state.name)
        assertEquals(0, state.count)
    }

    @Test
    fun `UiIntent can be implemented by sealed interfaces`() {
        val intent: UiIntent = TestIntent.UpdateName("test")
        assertEquals("test", (intent as TestIntent.UpdateName).name)
    }

    @Test
    fun `reduce returns new state from reducer lambda`() {
        val state = TestState(name = "initial", count = 0)
        val result = state.reduce {
            copy(name = "updated", count = 1)
        }
        assertEquals(TestState(name = "updated", count = 1), result)
    }

    @Test
    fun `reduce does not modify original state`() {
        val state = TestState(name = "initial", count = 0)
        state.reduce {
            copy(name = "updated", count = 1)
        }
        assertEquals(TestState(name = "initial", count = 0), state)
    }

    @Test
    fun `reduce preserves state when reducer returns same state`() {
        val state = TestState(name = "initial", count = 0)
        val result = state.reduce { this }
        assertEquals(state, result)
    }

    @Test
    fun `reduce works with chained calls`() {
        val state = TestState(name = "initial", count = 0)
        val result = state
            .reduce { copy(name = "updated") }
            .reduce { copy(count = count + 1) }
        assertEquals(TestState(name = "updated", count = 1), result)
    }
}
