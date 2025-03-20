package com.partnership.bjbdocumenttrackerreader.data.model

data class GetSearchDocument (
    val data : List<DocumentDetail>
)
data class DocumentDetail(
    val rfidNumber: String,
    val cif: String,
    val namaNasabah: String
)