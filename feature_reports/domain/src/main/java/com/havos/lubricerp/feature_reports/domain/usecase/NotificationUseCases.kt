package com.havos.lubricerp.feature_reports.domain.usecase

import androidx.paging.PagingData
import com.havos.lubricerp.core.common.ResultState
import com.havos.lubricerp.feature_reports.domain.model.NotificationItem
import com.havos.lubricerp.feature_reports.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow

class GetNotificationsPagedUseCase(
    private val repository: NotificationRepository
) {
    operator fun invoke(): Flow<PagingData<NotificationItem>> = repository.getNotificationsPaged()
}

class GetUnreadNotificationCountUseCase(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(): ResultState<Int> = repository.getUnreadCount()
}

class MarkNotificationAsReadUseCase(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(id: Long): ResultState<Unit> = repository.markAsRead(id)
}

class MarkAllNotificationsReadUseCase(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(): ResultState<Unit> = repository.markAllAsRead()
}
