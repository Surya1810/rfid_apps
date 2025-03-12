package com.partnership.bjbdocumenttrackerreader.data.model

data class StorageLocation(
    val id: Int,
    val rfidNumber: String,
    val room: String,
    val row: String,
    val rack: String,
    val box: String,
    val isThere: Boolean
)
