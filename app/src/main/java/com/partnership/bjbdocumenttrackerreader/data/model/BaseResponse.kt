package com.partnership.bjbdocumenttrackerreader.data.model


data class BaseResponse<T>(
    val status: String? = null,
    val message: String? = null,
    val data: T? = null
)
