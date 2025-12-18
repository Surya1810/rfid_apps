package com.partnership.bjbdocumenttrackerreader.data.model

import com.partnership.bjbdocumenttrackerreader.data.local.entity.AssetEntity

data class GetBulkDocument(
    val scanCode: String,
    val documents: List<Document>
)

data class Document(
    val id: Int,
    val noDoc: String? = "-",
    val rfid: String,
    val cif: String? = null,
    val noRef: String? = null,
    val name: String,
    val segment: String? = null,
    val amountAgunan: Int? = null,
    val location: Location? = null,
    val isThere: Boolean,
    val isBorrowed: Boolean
)

data class Location(
    val room: String,
    val row: String,
    val rack: String,
    val box: String
)

fun GetBulkDocument.toEntityList(): List<AssetEntity> {
    return documents.map { document ->
        AssetEntity(
            id = document.id,
            noDoc =if (document.noDoc.isNullOrEmpty()) "-" else document.noDoc,
            rfid = document.rfid,
            cif = document.cif,
            name = document.name,
            segment = document.segment,
            amountAgunan = document.amountAgunan,
            location = document.location,
            isThere = document.isThere,
            noRef = document.noRef,
            scanCode = scanCode
        )
    }
}

