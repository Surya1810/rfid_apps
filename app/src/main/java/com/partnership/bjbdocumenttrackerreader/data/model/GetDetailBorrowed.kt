package com.partnership.bjbdocumenttrackerreader.data.model

data class GetDetailBorrowed(
    val borrowed: BorrowedDetail
)

data class BorrowedDetail(
    val id: Int,
    val documentId: Int,
    val borrowerName: String,
    val estimatedReturnDate: String,
    val borrowedAt: String,
    val returnedAt: String? = null,
    val firstSignature: String,
    val lastSignature: String? = null
)
