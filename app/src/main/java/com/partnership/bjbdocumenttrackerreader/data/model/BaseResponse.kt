package com.partnership.bjbdocumenttrackerreader.data.model


data class Meta (
    val currentPage: Int,
    val lastPage: Int,
    val perPage: Int,
    val totalItem: Int
)

data class BaseResponse<T>(
    val status: String? = null,
    val message: String? = null,
    val data: T? = null,
    val meta: Meta? = null
)
