package com.partnership.bjbdocumenttrackerreader.data.model

import com.partnership.bjbdocumenttrackerreader.data.local.entity.AssetEntity

data class GetBulkDocument(
    val documents: List<Document>
)

data class Document(
    val id: Int,
    val noDoc: String,
    val rfid: String,
    val cif: String? = null,
    val name: String,
    val segment: String? = null,
    val amountAgunan: Int? = null,
    val location: Location? = null,
    val isThere: Boolean
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
            id = document.id, // convert Int ke String karena AssetEntity pakai String
            noDoc = document.noDoc,
            rfid = document.rfid,
            cif = document.cif,
            name = document.name,
            segment = document.segment,
            amountAgunan = document.amountAgunan,
            location = document.location, // langsung bisa dipakai karena strukturnya sama
            isThere = document.isThere
        )
    }
}

