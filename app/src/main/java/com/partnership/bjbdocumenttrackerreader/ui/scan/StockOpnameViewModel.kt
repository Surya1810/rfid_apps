package com.partnership.bjbdocumenttrackerreader.ui.scan

import android.content.ContentValues.TAG
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.partnership.bjbdocumenttrackerreader.data.ResultWrapper
import com.partnership.bjbdocumenttrackerreader.data.local.entity.AssetEntity
import com.partnership.bjbdocumenttrackerreader.data.model.BaseResponse
import com.partnership.bjbdocumenttrackerreader.data.model.Document
import com.partnership.bjbdocumenttrackerreader.data.model.GetBulkDocument
import com.partnership.bjbdocumenttrackerreader.data.model.PostLostDocument
import com.partnership.bjbdocumenttrackerreader.data.model.PostStockOpname
import com.partnership.bjbdocumenttrackerreader.data.model.TagInfo
import com.partnership.bjbdocumenttrackerreader.reader.RFIDManager
import com.partnership.bjbdocumenttrackerreader.repository.RFIDRepositoryImpl
import com.partnership.bjbdocumenttrackerreader.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StockOpnameViewModel @Inject constructor(
    private val reader: RFIDManager,
    private val repository: RFIDRepositoryImpl
) : ViewModel() {
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
                handler.postDelayed(this, 10)
            }
        }
    }

    private val _isReaderInit = MutableLiveData(false)
    val isReaderInit: LiveData<Boolean> get() = _isReaderInit

    private val _messageReader = SingleLiveEvent<String>()
    val messageReader: LiveData<String> get() = _messageReader

    private val _soundBeep = MutableLiveData<Boolean>()
    val soundBeep: LiveData<Boolean> get() = _soundBeep

    private var hasInitReader = false

    fun setSoundToFalse() {
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

    fun stopReadTag() {
        stopTimer()
        reader.stopReadTag()
    }

    fun releaseReader() {
        reader.releaseRFID()
    }

    private fun formatTime(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val milliseconds = (millis % 1000) / 10
        return String.format("%02d.%02d", seconds, milliseconds)
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

    val assetStatusInfo: StateFlow<Pair<Int, Int>> = combine(
        repository.observeDetectedAssets(),
        repository.observeAllAssets()
    ) { detected, total ->
        detected to total
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0 to 0)

    val pagedAssets: Flow<PagingData<AssetEntity>> =
        repository.getAssetsPagingFlow()
            .cachedIn(viewModelScope)

    private fun updateIsThere(rfidNumber: String, isThere: Boolean) {
        viewModelScope.launch {
            repository.updateIsThere(rfidNumber, isThere)
        }
    }

    private val scannedTags = MutableSharedFlow<String>(extraBufferCapacity = 100)

    private val validEpcMap = mutableMapOf<String, ByteArray>()

    private fun hexStringToByteArray(hex: String): ByteArray {
        val result = ByteArray(hex.length / 2)
        for (i in result.indices) {
            val index = i * 2
            val byte = hex.substring(index, index + 2).toInt(16)
            result[i] = byte.toByte()
        }
        return result
    }

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

    //get data from API

    private val _getSearch = MutableLiveData<ResultWrapper<BaseResponse<GetBulkDocument>>>()
    val listSearch: LiveData<ResultWrapper<BaseResponse<GetBulkDocument>>> get() = _getSearch

    fun getSearchDocument(isDocument: Boolean, query: String? = null) {
        viewModelScope.launch {
            val type = if (isDocument) "document" else "agunan"
            _getSearch.value = repository.searchAsset(
                type = type,
                search = query
            )
        }
    }

    suspend fun getBulkDocument(isDocument: Boolean): ResultWrapper<BaseResponse<GetBulkDocument>> {
        Log.w(TAG, "getBulkDocument: $isDocument")
        return if (isDocument) {
            repository.getBulkDocument()
        } else {
            repository.getBulkAgunan()
        }
    }

    suspend fun postStockOpname(isDocument: Boolean): ResultWrapper<BaseResponse<Unit>> {
        val type = if (isDocument) "document" else "agunan"
        val dataBulk = repository.getStockOpnameItems()
        return repository.postStockOpname(type, PostStockOpname(dataBulk))
    }

    //new method
    private val cacheValidEpcs = mutableListOf<ByteArray>()
    private val recentlyScanned = mutableMapOf<String, Long>()
    private val cooldownMillis = 5000L // 5 detik

    @OptIn(ExperimentalStdlibApi::class)
    fun cacheAllValidEpcs() {
        viewModelScope.launch {
            val allEpc = repository.getAllValidEpcs()
            cacheValidEpcs.clear()
            cacheValidEpcs.addAll(allEpc.map { it.hexToByteArray() })
            Log.w(TAG, "cacheAllValidEpcs: $allEpc")
        }
    }


    @OptIn(ExperimentalStdlibApi::class)
    fun readTagAuto2() {
        startTimer()
        reader.readTagAuto { uhfTagInfo ->
            val scanned = uhfTagInfo.epcBytes
            val matched = cacheValidEpcs.any { valid ->
                scanned.contentEquals(valid)
            }

            if (matched) {
                val hex = scanned.toHexString()
                if (!isRecentlyScanned(hex)) {
                    markScanned(hex)
                    updateIsThere(hex)
                }
            }
        }
    }

    private fun isRecentlyScanned(hex: String): Boolean {
        val currentTime = System.currentTimeMillis()
        val lastScanned = recentlyScanned[hex] ?: 0L
        return currentTime - lastScanned < cooldownMillis
    }

    private fun markScanned(hex: String) {
        recentlyScanned[hex] = System.currentTimeMillis()
    }

    private fun updateIsThere(hex: String) {

        if (!repository.isAssetThere(hex)) {
            repository.updateIsThere(hex, true)
            _soundBeep.postValue(true)

        }
    }
    private val _setDataToSearchingDocument = MutableLiveData<Document>()
    val searchDocumentEpc : LiveData<Document> get() = _setDataToSearchingDocument

    fun setDataToSearchingDocument(document: Document){
        _setDataToSearchingDocument.value = document
    }

}