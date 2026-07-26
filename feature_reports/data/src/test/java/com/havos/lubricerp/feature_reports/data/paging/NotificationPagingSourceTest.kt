package com.havos.lubricerp.feature_reports.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.havos.lubricerp.core.common.ResultState
import com.havos.lubricerp.feature_reports.data.dto.NotificationItemDto
import com.havos.lubricerp.feature_reports.data.dto.NotificationPageDataDto
import com.havos.lubricerp.feature_reports.data.remote.notifications.NotificationRemoteDataSource
import com.havos.lubricerp.feature_reports.domain.model.NotificationItem
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class NotificationPagingSourceTest {

    private val remoteDataSource = mock<NotificationRemoteDataSource>()
    private val pagingSource = NotificationPagingSource(remoteDataSource)

    private fun mockNotifications(count: Int, startId: Long = 1): List<NotificationItemDto> =
        (startId..<(startId + count)).map { i ->
            NotificationItemDto(
                id = i,
                title = "Notification $i",
                message = "Body $i",
                isRead = false,
                createdAt = "2024-01-${(15 + i % 10).toString().padStart(2, '0')}",
                timeAgo = "${i}h ago"
            )
        }

    @Test
    fun `load returns success page for first page`() = runTest {
        val items = mockNotifications(20)
        whenever(remoteDataSource.getNotifications(eq(1), eq(20)))
            .thenReturn(ResultState.Success(NotificationPageDataDto(unreadCount = 5, items = items)))

        val result = pagingSource.load(PagingSource.LoadParams.Refresh(1, 20, false))

        assertTrue(result is PagingSource.LoadResult.Page)
        val page = result as PagingSource.LoadResult.Page
        assertEquals(20, page.data.size)
        assertEquals("Notification 1", page.data.first().title)
        assertNull(page.prevKey)
        assertNotNull(page.nextKey)
        assertEquals(2, page.nextKey)
    }

    @Test
    fun `load returns success page for second page`() = runTest {
        val items = mockNotifications(20, startId = 21)
        whenever(remoteDataSource.getNotifications(eq(2), eq(20)))
            .thenReturn(ResultState.Success(NotificationPageDataDto(unreadCount = 5, items = items)))

        val result = pagingSource.load(PagingSource.LoadParams.Append(2, 20, false))

        assertTrue(result is PagingSource.LoadResult.Page)
        val page = result as PagingSource.LoadResult.Page
        assertEquals(20, page.data.size)
        assertEquals(21L, page.data.first().id)
        assertEquals(1, page.prevKey)
        assertEquals(3, page.nextKey)
    }

    @Test
    fun `load returns last page with null nextKey when fewer than page size`() = runTest {
        val items = mockNotifications(7)
        whenever(remoteDataSource.getNotifications(eq(1), eq(20)))
            .thenReturn(ResultState.Success(NotificationPageDataDto(unreadCount = 1, items = items)))

        val result = pagingSource.load(PagingSource.LoadParams.Refresh(1, 20, false))

        assertTrue(result is PagingSource.LoadResult.Page)
        val page = result as PagingSource.LoadResult.Page
        assertEquals(7, page.data.size)
        assertNull(page.nextKey)
    }

    @Test
    fun `load returns error when remote data source fails`() = runTest {
        whenever(remoteDataSource.getNotifications(any(), any()))
            .thenReturn(ResultState.Error("Network failure"))

        val result = pagingSource.load(PagingSource.LoadParams.Refresh(1, 20, false))

        assertTrue(result is PagingSource.LoadResult.Error)
        val error = result as PagingSource.LoadResult.Error
        assertTrue(error.throwable.message!!.contains("Network failure"))
    }

    @Test
    fun `load returns error for Loading state`() = runTest {
        whenever(remoteDataSource.getNotifications(any(), any()))
            .thenReturn(ResultState.Loading)

        val result = pagingSource.load(PagingSource.LoadParams.Refresh(1, 20, false))

        assertTrue(result is PagingSource.LoadResult.Error)
        val error = result as PagingSource.LoadResult.Error
        assertTrue(error.throwable.message!!.contains("Unexpected"))
    }

    @OptIn(ExperimentalPagingApi::class)
    @Test
    fun `getRefreshKey returns null for null anchor`() {
        val state = PagingState<Int, NotificationItem>(
            pages = listOf(),
            anchorPosition = null,
            config = PagingConfig(20),
            leadingPlaceholderCount = 0
        )
        assertNull(pagingSource.getRefreshKey(state))
    }
}
