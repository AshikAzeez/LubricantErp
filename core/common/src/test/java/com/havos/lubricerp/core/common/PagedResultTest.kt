package com.havos.lubricerp.core.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PagedResultTest {

    @Test
    fun `PagedResult holds all properties correctly`() {
        val items = listOf("a", "b", "c")
        val result = PagedResult(
            items = items,
            totalCount = 100,
            skip = 0,
            take = 10,
            hasMore = true
        )

        assertEquals(items, result.items)
        assertEquals(100, result.totalCount)
        assertEquals(0, result.skip)
        assertEquals(10, result.take)
        assertTrue(result.hasMore)
    }

    @Test
    fun `hasMore is false when totalCount equals items size`() {
        val result = PagedResult(
            items = listOf("a", "b", "c"),
            totalCount = 3,
            skip = 0,
            take = 10,
            hasMore = false
        )

        assertFalse(result.hasMore)
        assertEquals(3, result.totalCount)
    }

    @Test
    fun `skip reflects offset of fetched data`() {
        val result = PagedResult(
            items = listOf("d", "e"),
            totalCount = 10,
            skip = 3,
            take = 2,
            hasMore = true
        )

        assertEquals(3, result.skip)
        assertEquals(2, result.take)
    }

    @Test
    fun `copy creates new instance with modified properties`() {
        val original = PagedResult(
            items = listOf("a"),
            totalCount = 10,
            skip = 0,
            take = 5,
            hasMore = true
        )

        val modified = original.copy(skip = 5, items = listOf("b"))

        assertEquals(5, modified.skip)
        assertEquals(listOf("b"), modified.items)
        assertEquals(original.totalCount, modified.totalCount)
        assertEquals(original.take, modified.take)
    }

    @Test
    fun `equals returns true for same data`() {
        val a = PagedResult(listOf("x"), 1, 0, 1, false)
        val b = PagedResult(listOf("x"), 1, 0, 1, false)

        assertEquals(a, b)
    }

    @Test
    fun `equals returns false for different data`() {
        val a = PagedResult(listOf("x"), 1, 0, 1, false)
        val b = PagedResult(listOf("y"), 1, 0, 1, false)

        assertNotEquals(a, b)
    }

    @Test
    fun `empty items list is allowed`() {
        val result = PagedResult(
            items = emptyList<String>(),
            totalCount = 0,
            skip = 0,
            take = 10,
            hasMore = false
        )

        assertTrue(result.items.isEmpty())
        assertEquals(0, result.totalCount)
    }

    @Test
    fun `totalCount is greater than items size when hasMore is true`() {
        val result = PagedResult(
            items = listOf("a", "b"),
            totalCount = 10,
            skip = 0,
            take = 2,
            hasMore = true
        )

        assertTrue(result.totalCount > result.items.size)
        assertTrue(result.hasMore)
    }
}
