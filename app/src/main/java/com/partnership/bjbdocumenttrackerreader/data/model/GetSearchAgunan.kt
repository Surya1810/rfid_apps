package com.partnership.bjbdocumenttrackerreader.data.model

data class GetSearchAgunan (
    val agunan : List<DetailAgunan>
)
data class DetailAgunan(
    val rfidNumber: String,
    val typeAgunan: String,
    val nomorAgunan: String
)