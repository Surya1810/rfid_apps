package com.partnership.bjbdocumenttrackerreader.ui.scan

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.partnership.bjbdocumenttrackerreader.data.model.TagInfo
import com.partnership.bjbdocumenttrackerreader.reader.RFIDManager
import com.partnership.bjbdocumenttrackerreader.util.RFIDUtils
import com.partnership.bjbdocumenttrackerreader.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RFIDViewModel @Inject constructor(private val reader: RFIDManager): ViewModel() {
    private val _elapsedTime = MutableLiveData<String>()
    val elapsedTime: LiveData<String> get() = _elapsedTime
    private var startTime: Long = 0L
    private var isRunning = false
    private val handler = Handler(Looper.getMainLooper())

    private val timerRunnable = object : Runnable {
        override fun run() {
            if (isRunning) {
                val elapsed = System.currentTimeMillis() - startTime
                _elapsedTime.postValue(formatTime(elapsed))
                handler.postDelayed(this, 1000) // Update tiap 1 detik
            }
        }
    }

    private val _isReaderInit = MutableLiveData(false)
    val isReaderInit: LiveData<Boolean> get() = _isReaderInit

    private val _messageReader = SingleLiveEvent<String>()
    val messageReader: LiveData<String> get() = _messageReader


    private val _isScanning = MutableLiveData<Boolean>()
    val isScanning: LiveData<Boolean> get() = _isScanning

    private val _tagList = MutableLiveData<MutableList<TagInfo>>(mutableListOf())
    val tagList: MutableLiveData<MutableList<TagInfo>> get() = _tagList

    private val _soundBeep = MutableLiveData<Boolean>()
    val soundBeep : LiveData<Boolean> get() = _soundBeep

    private var hasInitReader = false // <- tambahkan flag internal


    init {
        _isScanning.value = false
    }

    fun setSoundToFalse(){
        _soundBeep.value = false
    }

    fun initReader(context: Context) {
        if (hasInitReader) return
        hasInitReader = true

        viewModelScope.launch(Dispatchers.IO) {
            reader.initUHF(context) { isInited, message ->
                _isReaderInit.postValue(isInited)
                _messageReader.postValue(message)
            }
        }
    }


    fun getCurrentPower(): Int? {
        return reader.getCurrentPower()
    }

    fun setPowerReader(iPower: Int): Boolean {
        return reader.setPower(iPower)
    }

    fun readTagAuto(){
        _isScanning.value = true
        startTimer()
            reader.readTagAuto {uhftagInfo ->  
                val newTag = TagInfo(
                    epc = uhftagInfo.epc,
                    rssi = uhftagInfo.epc,
                    isThere = true
                )
                updateTagList(newTag)
            }

    }

    fun clearTagList() {
        _tagList.value = mutableListOf()
    }

    fun stopReadTag(){
        _isScanning.value = false
        stopTimer()
        reader.stopReadTag()
    }

    fun releaseReader(){
        reader.releaseRFID()
    }

    private fun updateTagList(newTag: TagInfo) {
        val currentList = _tagList.value ?: mutableListOf()
        val exists = BooleanArray(1)

        val insertIndex = RFIDUtils.getInsertIndex(currentList, newTag, exists)

        if (exists[0]) {
            // Jika tag sudah ada, kita cek dulu apakah RSSI berubah (jika memang masih mau simpan RSSI)
            val oldTag = currentList[insertIndex]
            if (oldTag.rssi != newTag.rssi) {
                // Update hanya kalau benar-benar ada perbedaan
                currentList[insertIndex] = oldTag.copy(rssi = newTag.rssi)
                _tagList.postValue(currentList)
            }
            // Jika RSSI sama, tidak perlu update apa-apa → hemat performa
        } else {
            // Tag baru, tambahkan dan update LiveData
            currentList.add(insertIndex, newTag)
            _tagList.postValue(currentList)
            _soundBeep.postValue(true)
        }
    }




    private fun formatTime(millis: Long): String {
        val minutes = (millis / 1000) / 60
        val seconds = (millis / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }


    override fun onCleared() {
        super.onCleared()
        handler.removeCallbacks(timerRunnable)
    }
    private fun startTimer() {
        if (!isRunning) {
            isRunning = true
            startTime = System.currentTimeMillis()
            handler.post(timerRunnable)
        }
    }

    private fun stopTimer() {
        isRunning = false
        handler.removeCallbacks(timerRunnable)
    }


}