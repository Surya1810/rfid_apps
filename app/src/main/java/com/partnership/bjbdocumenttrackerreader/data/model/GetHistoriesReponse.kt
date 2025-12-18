package com.partnership.bjbdocumenttrackerreader.data.model

data class GetHistoriesResponse (
    val lost: LostInfo,
    val scans: List<ScanItem>
)
data class ScanItem(
    val id: Int,
    val category: String,
    val totalFound: Int,
    val totalMissing: Int,
    val totalItems : Int,
    val createdAt: String,
    val updatedAt: String
)

data class LostInfo(
    val countDocument: Int,
    val countAgunan: Int,
    val value: Double
)