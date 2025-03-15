package com.partnership.bjbdocumenttrackerreader.data.model

data class GetDashboard(
    val overview: Overview,
    val totalDocuments: Int,
    val totalAgunan: Int,
    val dashboard: DashboardDetail
)

data class Overview(
    val lastTimeScan: String,
    val totalData: Int,
    val totalvalue: Int
)

data class DashboardDetail(
    val totalDocumentsFound: Int,
    val totalDocumentsLost: Int,
    val valueLostDocument: Double,
    val listDocumentLost: List<String>
)