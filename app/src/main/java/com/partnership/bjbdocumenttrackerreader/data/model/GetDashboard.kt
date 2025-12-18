package com.partnership.bjbdocumenttrackerreader.data.model

data class GetDashboard(
    val overview: Overview,
    val totalAgunan: Int,
    val totalDocuments: Int,
    val totalBorrowedDocuments: Int,
    val lastStockOpname: LastStockOpname
)


data class Overview(
    val lastTimeScan: String,
    val totalData: Int,
    val totalValue: Double
)

data class LastStockOpname(
    val document: StockOpnameDetail,
    val agunan: StockOpnameDetail
)

data class StockOpnameDetail(
    val lastTimeScan: String?,
    val totalFound: Int,
    val totalMissing: Int,
    val totalItems: Int
)