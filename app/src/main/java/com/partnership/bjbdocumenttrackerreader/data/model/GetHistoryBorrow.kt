package com.partnership.bjbdocumenttrackerreader.data.model

data class GetHistoryBorrow(
    val borrowed: List<Borrowed>,
)

data class Borrowed(
    val id: Int,
    val documentId: Int,
    val borrowerName: String,
    val estimatedReturnDate: String? = null,
    val borrowedAt: String? = null,
    val returnedAt: String? = null
)
