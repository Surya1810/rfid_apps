package com.partnership.bjbdocumenttrackerreader.reader

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat.getString
import com.partnership.bjbdocumenttrackerreader.R
import com.partnership.bjbdocumenttrackerreader.data.model.TagInfo
import com.rscja.deviceapi.RFIDWithUHFUART
import com.rscja.deviceapi.entity.UHFTAGInfo
import com.rscja.deviceapi.interfaces.ConnectionStatus
import com.rscja.deviceapi.interfaces.IUHF

class RFIDManager {
    private var rfid: RFIDWithUHFUART? = null

    fun initUHF(context: Context, onResult: (Boolean, String) -> Unit) {
        try {
            rfid = RFIDWithUHFUART.getInstance()
            val initStatus = rfid?.init(context) ?: false
            onResult(initStatus, "RFID Loaded")
            if (initStatus) {
                Log.d("initRFID", "initUHFUART: Loaded")
            } else {
                Log.e("initRFID", "initUHFUART: Failed To Load")
            }
        } catch (e: Exception) {
            Log.e("initRFID", "initUHF: ${e.message}")
            onResult(false, "Error: ${e.message}") // Perbaikan
        }
    }

    fun stopReadTag(): Boolean? {
        //menghentikan pembacaan tag
        return if (rfid?.isInventorying == true){
            rfid?.stopInventory()
        }else{
            false
        }
    }
    fun stopLocating(){
        rfid?.stopLocation()
    }

    fun readSingleTag(onTagRead: (TagInfo?) -> Unit) {
        rfid = RFIDWithUHFUART.getInstance()
        val newTagDetected = rfid?.inventorySingleTag()

        onTagRead(newTagDetected?.let {
            TagInfo(
                epc = it.epc,
                rssi = it.rssi
            )
        })
    }

    fun readTagAuto(onTagRead: (UHFTAGInfo) -> Unit){
        rfid = RFIDWithUHFUART.getInstance()
        rfid?.setInventoryCallback { tagInfo ->
            onTagRead(tagInfo)
        }
        if (rfid?.startInventoryTag() == true){
            Log.d(TAG, "readTagAuto: Loop Scanning started")
        }else{
            Log.e(TAG, "readTagAuto: Loop Scanning Error", )
        }
    }

    fun isInventorying():Boolean? {
        rfid = RFIDWithUHFUART.getInstance()
        return rfid?.isInventorying
    }

    fun startLocatingTag(context: Context,epc: String, onResult: (Int, Boolean) -> Unit){
        rfid?.startLocation(context,epc, IUHF.Bank_EPC,32){value, isValid ->
            onResult(value,isValid)
        }
    }

    fun setFrequency(modeName: String, context: Context, onResult: (Boolean, String) -> Unit) {
        val mode = getMode(modeName, context)
        try {
            val success = rfid?.setFrequencyMode(mode) ?: false
            if (success) {
                val successMessage = "Frequency set to ${getModeName(mode, context)}"
                Log.d("set Frequency", successMessage)
                onResult(true, successMessage)
            } else {
                val errorMessage = "Failed to set frequency"
                Log.e("set Frequency", errorMessage)
                onResult(false, errorMessage)
            }
        } catch (e: Exception) {
            val errorMessage = "Error: ${e.message}"
            Log.e("set Frequency", errorMessage)
            onResult(false, errorMessage)
        }
    }

    fun getCurrentFrequencyName(context: Context): String {
        val mode = rfid?.getFrequencyMode() ?: -1
        Log.e("RFID", "getFrequencyMode() = $mode")

        return if (mode != -1) {
            getModeName(mode, context) // Ambil nama frekuensi berdasarkan mode
        } else {
            "Failed to get frequency check connection RFID!" // Jika gagal mendapatkan mode
        }
    }

    fun setPower(power: Int): Boolean{
        rfid = RFIDWithUHFUART.getInstance()
        if (rfid?.setPower(power) == true){
            Log.i(TAG, "setPower: success")
            return true
        }else{
            Log.e(TAG, "setPower: failed", )
            return false
        }
    }

    fun getCurrentPower(): Int?{
        rfid = RFIDWithUHFUART.getInstance()
        return rfid?.power
    }

    fun setEpcFilter(epc: String, ptr: Int = 32): Boolean? {
        val len = epc.length * 4
        return rfid?.setFilter(RFIDWithUHFUART.Bank_EPC, ptr, len, epc)
    }

    fun disableFilter(): Boolean {
        val emptyData = ""
        return rfid?.setFilter(RFIDWithUHFUART.Bank_EPC, 0, 0, emptyData) == true &&
                rfid?.setFilter(RFIDWithUHFUART.Bank_TID, 0, 0, emptyData)!! &&
                rfid?.setFilter(RFIDWithUHFUART.Bank_USER, 0, 0, emptyData)!!
    }



    fun releaseRFID(){
        rfid?.free()
    }

    fun checkStatusRFID(): ConnectionStatus? {
        return  rfid?.connectStatus
    }

    private fun getMode(modeName: String,context: Context): Int {
        return when (modeName) {
            getString(context, R.string.China_Standard_840_845MHz) -> 0x01
            getString(context,R.string.China_Standard_920_925MHz) -> 0x02
            getString(context,R.string.ETSI_Standard) -> 0x04
            getString(context,R.string.United_States_Standard) -> 0x08
            getString(context,R.string.Korea) -> 0x16
            getString(context,R.string.Japan) -> 0x32
            getString(context,R.string.South_Africa_915_919MHz) -> 0x33
            getString(context,R.string.New_Zealand) -> 0x34
            getString(context,R.string.Morocco) -> 0x80
            else -> 0x08 // Default ke United States Standard
        }
    }

    private fun getModeName(mode: Int,context: Context): String {
        return when (mode) {
            0x01 -> getString(context,R.string.China_Standard_840_845MHz)
            0x02 -> getString(context,R.string.China_Standard_920_925MHz)
            0x04 -> getString(context,R.string.ETSI_Standard)
            0x08 -> getString(context,R.string.United_States_Standard)
            0x16 -> getString(context,R.string.Korea)
            0x32 -> getString(context,R.string.Japan)
            0x33 -> getString(context,R.string.South_Africa_915_919MHz)
            0x34 -> getString(context,R.string.New_Zealand)
            0x80 -> getString(context,R.string.Morocco)
            else -> getString(context,R.string.United_States_Standard)
        }
    }
}