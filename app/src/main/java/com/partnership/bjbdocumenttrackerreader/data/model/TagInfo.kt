package com.partnership.bjbdocumenttrackerreader.data.model

import java.util.UUID

data class TagInfo (
    val epc: String,
    val rssi: String,
    val isThere: Boolean,
    val id: String = UUID.randomUUID().toString()
)