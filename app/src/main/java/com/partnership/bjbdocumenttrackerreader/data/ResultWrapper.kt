package com.partnership.bjbdocumenttrackerreader.data

sealed class ResultWrapper<out T> {

    // Representasi Data berhasil diterima
    data class Success<out T>(val data: T) : ResultWrapper<T>()

    // Representasi error dari API
    data class ErrorResponse<out T>(val error: String) : ResultWrapper<T>()

    // Representasi error dari sistem (misalnya jaringan, parsing, dsb.)
    data class Error<out T>(val error: String) : ResultWrapper<T>()

    // Representasi proses yang sedang berjalan
    object Loading : ResultWrapper<Nothing>()

    data class NetworkError<out T>(val error: String) : ResultWrapper<T>()
}