package com.partnership.bjbdocumenttrackerreader.util

import com.partnership.bjbdocumenttrackerreader.data.model.TagInfo

object RFIDUtils {
    fun getInsertIndex(tagList: List<TagInfo>, newTag: TagInfo, exists: BooleanArray): Int {
        var startIndex = 0
        var endIndex = tagList.size - 1
        var judgeIndex: Int
        var ret: Int

        if (tagList.isEmpty()) {
            exists[0] = false
            return 0
        }

        while (startIndex <= endIndex) {
            judgeIndex = (startIndex + endIndex) / 2
            ret = compareBytes(
                newTag.epc.toByteArray(),
                tagList[judgeIndex].epc.toByteArray()
            )

            when {
                ret > 0 -> {
                    startIndex = judgeIndex + 1
                }
                ret < 0 -> {
                    endIndex = judgeIndex - 1
                }
                else -> {
                    exists[0] = true
                    return judgeIndex
                }
            }
        }

        exists[0] = false
        return startIndex
    }


    /**
     * Membandingkan dua array byte dan mengembalikan hasil perbandingan:
     * - `1` atau `2` jika `b1` lebih besar dari `b2`
     * - `-1` atau `-2` jika `b1` lebih kecil dari `b2`
     * - `0` jika `b1` sama dengan `b2`
     */
    private fun compareBytes(b1: ByteArray, b2: ByteArray): Int {
        val len = minOf(b1.size, b2.size)

        for (i in 0 until len) {
            val value1 = b1[i].toInt() and 0xFF
            val value2 = b2[i].toInt() and 0xFF
            when {
                value1 > value2 -> return 1
                value1 < value2 -> return -1
            }
        }
        return when {
            b1.size > b2.size -> 2
            b1.size < b2.size -> -2
            else -> 0
        }
    }
}