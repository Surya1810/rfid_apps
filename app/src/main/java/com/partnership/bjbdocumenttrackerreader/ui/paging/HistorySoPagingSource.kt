package com.partnership.bjbdocumenttrackerreader.ui.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.partnership.bjbdocumenttrackerreader.data.ResultWrapper
import com.partnership.bjbdocumenttrackerreader.data.model.ScanItem
import com.partnership.bjbdocumenttrackerreader.repository.RFIDRepository
import kotlin.collections.emptyList

class HistorySoPagingSource(
    private val repository: RFIDRepository,
    private val category : String
) : PagingSource<Int, ScanItem>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ScanItem> {
        return try {
            val currentPage = params.key ?: 1
            when (val result = repository.getHistoriesSo(currentPage, category)) {
                is ResultWrapper.Success -> {
                    val dataList = result.data.data?.scans
                    val paging = result.data.meta

                    val nextPage = if (paging != null && paging.currentPage < paging.lastPage) {
                        currentPage + 1
                    } else {
                        null
                    }

                    val prevPage = if (currentPage == 1) null else currentPage - 1

                    LoadResult.Page(
                        data = dataList ?: emptyList(),
                        prevKey = prevPage,
                        nextKey = nextPage
                    )
                }

                is ResultWrapper.ErrorResponse -> LoadResult.Error(Exception("API Error: ${result.error}"))
                is ResultWrapper.Error -> LoadResult.Error(Exception("System Error: ${result.error}"))
                is ResultWrapper.NetworkError -> LoadResult.Error(Exception("Network Error: ${result.error}"))
                is ResultWrapper.Loading -> LoadResult.Invalid()
            }

        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, ScanItem>): Int? {
        return state.anchorPosition?.let { pos ->
            state.closestPageToPosition(pos)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(pos)?.nextKey?.minus(1)
        }
    }
}


