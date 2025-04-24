package com.partnership.bjbdocumenttrackerreader.util

object helper {
    fun String.hexToByteArray(): ByteArray {
        return chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    }

    fun ByteArray.toHexString(): String {
        return joinToString("") { "%02X".format(it) }
    }
}