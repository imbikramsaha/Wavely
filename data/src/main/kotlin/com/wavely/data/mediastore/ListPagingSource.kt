package com.wavely.data.mediastore

import androidx.paging.PagingSource
import androidx.paging.PagingState

/**
 * Pages over an in-memory snapshot. The full library is held in memory (small
 * domain objects), but rows/art are realized lazily by Paging + Coil, which is
 * what keeps large libraries (10k+) off the main thread and out of one giant
 * list composition (design.md risk T-7).
 */
class ListPagingSource<T : Any>(
    private val items: List<T>,
) : PagingSource<Int, T>() {

    override fun getRefreshKey(state: PagingState<Int, T>): Int? =
        state.anchorPosition?.let { anchor ->
            val closest = state.closestPageToPosition(anchor)
            closest?.prevKey?.plus(1) ?: closest?.nextKey?.minus(1)
        }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        val start = params.key ?: 0
        val end = minOf(start + params.loadSize, items.size)
        val page = if (start in 0..items.size && start <= end) items.subList(start, end) else emptyList()
        return LoadResult.Page(
            data = page,
            prevKey = if (start == 0) null else maxOf(0, start - params.loadSize),
            nextKey = if (end >= items.size) null else end,
        )
    }
}
