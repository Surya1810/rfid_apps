package com.partnership.bjbdocumenttrackerreader.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.partnership.bjbdocumenttrackerreader.data.model.Location

@Entity(tableName = "assets")
data class AssetEntity(
    @PrimaryKey val id: Int,
    val noDoc: String = "-",
    val scanCode: String,
    val rfid: String,
    val noRef: String? = null,
    val cif: String? = null,
    val name: String,
    val segment: String? = null,
    val amountAgunan: Int? = null,
    @Embedded val location: Location? = null,
    val isThere: Boolean
)