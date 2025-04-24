package com.partnership.bjbdocumenttrackerreader.ui.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.partnership.bjbdocumenttrackerreader.data.ResultWrapper
import com.partnership.bjbdocumenttrackerreader.repository.RFIDRepository

class LostDocumentPagingSource(
    private val repository: RFIDRepository
) : PagingSource<Int, String>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, String> {
        return try {
            val currentPage = params.key ?: 1
            val result = repository.getListLostDocument(currentPage)

            when (result) {
                is ResultWrapper.Success -> {
                    val dataList = result.data.data ?: emptyList()
                    val paging = result.data.paging

                    val nextPage = if (paging != null && paging.currentPage < paging.totalPages) {
                        currentPage + 1
                    } else {
                        null
                    }

                    val prevPage = if (currentPage == 1) null else currentPage - 1

                    LoadResult.Page(
                        data = dataList,
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

    override fun getRefreshKey(state: PagingState<Int, String>): Int? {
        return state.anchorPosition?.let { pos ->
            state.closestPageToPosition(pos)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(pos)?.nextKey?.minus(1)
        }
    }
}


