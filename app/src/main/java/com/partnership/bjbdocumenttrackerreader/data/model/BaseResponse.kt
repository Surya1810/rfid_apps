package com.partnership.rfid.data.model


data class BaseResponse<T>(
    val status: String? = null,
    val message: String,
    val data: T? = null
)
