package com.havos.lubricerp.feature_reports.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.havos.lubricerp.core.common.ResultState
import com.havos.lubricerp.feature_reports.data.paging.NotificationPagingSource
import com.havos.lubricerp.feature_reports.data.remote.notifications.NotificationRemoteDataSource
import com.havos.lubricerp.feature_reports.domain.model.NotificationItem
import com.havos.lubricerp.feature_reports.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow

class NotificationRepositoryImpl(
    private val remoteDataSource: NotificationRemoteDataSource
) : NotificationRepository {

    override fun getNotificationsPaged(): Flow<PagingData<NotificationItem>> {
        return Pager(
            config = PagingConfig(pageSize = NotificationPagingSource.PAGE_SIZE, enablePlaceholders = false),
            pagingSourceFactory = { NotificationPagingSource(remoteDataSource) }
        ).flow
    }

    override suspend fun getUnreadCount(): ResultState<Int> =
        remoteDataSource.getUnreadCount()

    override suspend fun markAsRead(id: Long): ResultState<Unit> =
        remoteDataSource.markAsRead(id)

    override suspend fun markAllAsRead(): ResultState<Unit> =
        remoteDataSource.markAllAsRead()
}
