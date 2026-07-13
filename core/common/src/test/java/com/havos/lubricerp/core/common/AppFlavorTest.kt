package com.havos.lubricerp.core.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppFlavorTest {

    @Test
    fun `DEMO has displaySuffix (D)`() {
        assertEquals(" (D)", AppFlavor.DEMO.displaySuffix)
    }

    @Test
    fun `STAGE has displaySuffix (S)`() {
        assertEquals(" (S)", AppFlavor.STAGE.displaySuffix)
    }

    @Test
    fun `PRODUCTION has empty displaySuffix`() {
        assertEquals("", AppFlavor.PRODUCTION.displaySuffix)
    }

    @Test
    fun `enum has exactly three entries`() {
        assertEquals(3, AppFlavor.entries.size)
    }

    @Test
    fun `DEMO valueOf returns correct enum`() {
        assertEquals(AppFlavor.DEMO, AppFlavor.valueOf("DEMO"))
    }

    @Test
    fun `STAGE valueOf returns correct enum`() {
        assertEquals(AppFlavor.STAGE, AppFlavor.valueOf("STAGE"))
    }

    @Test
    fun `PRODUCTION valueOf returns correct enum`() {
        assertEquals(AppFlavor.PRODUCTION, AppFlavor.valueOf("PRODUCTION"))
    }

    @Test
    fun `all entries have distinct displaySuffixes for non-production`() {
        val suffixes = AppFlavor.entries.map { it.displaySuffix }
        assertTrue(suffixes.any { it.isNotEmpty() })
        assertTrue(suffixes.any { it.isEmpty() })
    }
}
