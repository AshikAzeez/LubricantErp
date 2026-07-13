package com.havos.lubricerp.core.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class ThemeModeTest {

    @Test
    fun `enum has exactly three entries`() {
        assertEquals(3, ThemeMode.entries.size)
    }

    @Test
    fun `from returns SYSTEM for exact match`() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.from("SYSTEM"))
    }

    @Test
    fun `from returns LIGHT for exact match`() {
        assertEquals(ThemeMode.LIGHT, ThemeMode.from("LIGHT"))
    }

    @Test
    fun `from returns DARK for exact match`() {
        assertEquals(ThemeMode.DARK, ThemeMode.from("DARK"))
    }

    @Test
    fun `from is case insensitive`() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.from("system"))
        assertEquals(ThemeMode.LIGHT, ThemeMode.from("light"))
        assertEquals(ThemeMode.DARK, ThemeMode.from("dark"))
    }

    @Test
    fun `from is case insensitive with mixed case`() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.from("System"))
        assertEquals(ThemeMode.LIGHT, ThemeMode.from("Light"))
        assertEquals(ThemeMode.DARK, ThemeMode.from("Dark"))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.from("SyStEm"))
    }

    @Test
    fun `from trims whitespace`() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.from("  SYSTEM  "))
        assertEquals(ThemeMode.LIGHT, ThemeMode.from(" LIGHT "))
        assertEquals(ThemeMode.DARK, ThemeMode.from("DARK\t"))
    }

    @Test
    fun `from defaults to SYSTEM for unrecognized value`() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.from("INVALID"))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.from(""))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.from("   "))
    }

    @Test
    fun `from defaults to SYSTEM for empty string`() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.from(""))
    }

    @Test
    fun `from defaults to SYSTEM for blank string`() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.from("   "))
    }

    @Test
    fun `from defaults to SYSTEM for null-like strings`() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.from("null"))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.from("NULL"))
    }

    @Test
    fun `SYSTEM is the default fallback`() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.from("some_random_value"))
    }

    @Test
    fun `valueOf returns correct enum for exact name`() {
        assertSame(ThemeMode.SYSTEM, ThemeMode.valueOf("SYSTEM"))
        assertSame(ThemeMode.LIGHT, ThemeMode.valueOf("LIGHT"))
        assertSame(ThemeMode.DARK, ThemeMode.valueOf("DARK"))
    }
}
