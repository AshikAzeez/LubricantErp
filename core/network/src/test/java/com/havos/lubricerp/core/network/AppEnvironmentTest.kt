package com.havos.lubricerp.core.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class AppEnvironmentTest {

    @Test
    fun `enum has exactly three entries`() {
        assertEquals(3, AppEnvironment.entries.size)
    }

    @Test
    fun `from returns TEST for exact match`() {
        assertEquals(AppEnvironment.TEST, AppEnvironment.from("TEST"))
    }

    @Test
    fun `from returns STAGE for exact match`() {
        assertEquals(AppEnvironment.STAGE, AppEnvironment.from("STAGE"))
    }

    @Test
    fun `from returns PRODUCTION for exact match`() {
        assertEquals(AppEnvironment.PRODUCTION, AppEnvironment.from("PRODUCTION"))
    }

    @Test
    fun `from is case insensitive`() {
        assertEquals(AppEnvironment.TEST, AppEnvironment.from("test"))
        assertEquals(AppEnvironment.STAGE, AppEnvironment.from("stage"))
        assertEquals(AppEnvironment.PRODUCTION, AppEnvironment.from("production"))
    }

    @Test
    fun `from is case insensitive with mixed case`() {
        assertEquals(AppEnvironment.TEST, AppEnvironment.from("Test"))
        assertEquals(AppEnvironment.STAGE, AppEnvironment.from("Stage"))
        assertEquals(AppEnvironment.PRODUCTION, AppEnvironment.from("Production"))
    }

    @Test
    fun `from trims whitespace`() {
        assertEquals(AppEnvironment.TEST, AppEnvironment.from("  TEST  "))
        assertEquals(AppEnvironment.STAGE, AppEnvironment.from(" STAGE "))
        assertEquals(AppEnvironment.PRODUCTION, AppEnvironment.from("PRODUCTION\t"))
    }

    @Test
    fun `from defaults to TEST for unrecognized value`() {
        assertEquals(AppEnvironment.TEST, AppEnvironment.from("INVALID"))
    }

    @Test
    fun `from defaults to TEST for empty string`() {
        assertEquals(AppEnvironment.TEST, AppEnvironment.from(""))
    }

    @Test
    fun `from defaults to TEST for blank string`() {
        assertEquals(AppEnvironment.TEST, AppEnvironment.from("   "))
    }

    @Test
    fun `valueOf returns correct enum for exact name`() {
        assertSame(AppEnvironment.TEST, AppEnvironment.valueOf("TEST"))
        assertSame(AppEnvironment.STAGE, AppEnvironment.valueOf("STAGE"))
        assertSame(AppEnvironment.PRODUCTION, AppEnvironment.valueOf("PRODUCTION"))
    }
}
