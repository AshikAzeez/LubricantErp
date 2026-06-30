package com.havos.lubricerp.core.common

data class PagedResult<T>(
    val items: List<T>,
    val totalCount: Int,
    val skip: Int,
    val take: Int,
    val hasMore: Boolean
)
