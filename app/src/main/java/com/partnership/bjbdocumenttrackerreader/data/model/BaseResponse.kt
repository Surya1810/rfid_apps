package com.partnership.bjbdocumenttrackerreader.data.model


data class Paging (
    val currentPage: Int,
    val limit: Int,
    val totalData: Int,
    val totalPages: Int
)

data class BaseResponse<T>(
    val status: String? = null,
    val message: String? = null,
    val data: T? = null,
    val paging: Paging? = null
)
