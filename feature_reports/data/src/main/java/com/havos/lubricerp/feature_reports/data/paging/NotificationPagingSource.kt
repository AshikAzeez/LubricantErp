package com.havos.lubricerp.feature_reports.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.havos.lubricerp.core.common.ResultState
import com.havos.lubricerp.feature_reports.data.mapper.toDomain
import com.havos.lubricerp.feature_reports.data.remote.notifications.NotificationRemoteDataSource
import com.havos.lubricerp.feature_reports.domain.model.NotificationItem

class NotificationPagingSource(
    private val remoteDataSource: NotificationRemoteDataSource
) : PagingSource<Int, NotificationItem>() {

    override fun getRefreshKey(state: PagingState<Int, NotificationItem>): Int? {
        return state.anchorPosition?.let { anchor ->
            state.closestPageToPosition(anchor)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchor)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, NotificationItem> {
        val page = params.key ?: 1
        return when (val result = remoteDataSource.getNotifications(page = page, pageSize = PAGE_SIZE)) {
            is ResultState.Success -> {
                val items = result.data.items.map { it.toDomain() }
                LoadResult.Page(
                    data = items,
                    prevKey = if (page == 1) null else page - 1,
                    nextKey = if (items.size < PAGE_SIZE) null else page + 1
                )
            }
            is ResultState.Error -> LoadResult.Error(Exception(result.message))
            ResultState.Loading -> LoadResult.Error(Exception("Unexpected loading state"))
        }
    }

    companion object {
        const val PAGE_SIZE = 20
    }
}
