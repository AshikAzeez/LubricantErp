package com.havos.lubricerp.feature_reports.domain.model

data class NotificationItem(
    val id: Long,
    val title: String,
    val message: String,
    val type: String,
    val linkUrl: String,
    val isRead: Boolean,
    val readAt: String?,
    val createdAt: String,
    val timeAgo: String
)

data class NotificationPage(
    val unreadCount: Int,
    val items: List<NotificationItem>
)
