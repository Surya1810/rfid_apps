package com.partnership.bjbdocumenttrackerreader.data.model

data class PostStockOpname (
    val scanCode: String,
    val stockOpname: List<AssetStatus>
)
data class AssetStatus(
    val id:Int,
    val isThere: Boolean
)