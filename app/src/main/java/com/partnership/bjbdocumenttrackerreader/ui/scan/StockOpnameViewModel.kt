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
import com.partnership.bjbdocumenttrackerreader.data.model.PostStockOpname
import com.partnership.bjbdocumenttrackerreader.data.model.TagInfo
import com.partnership.bjbdocumenttrackerreader.reader.RFIDManager
import com.partnership.bjbdocumenttrackerreader.repository.RFIDRepositoryImpl
import com.partnership.bjbdocumenttrackerreader.util.RFIDUtils
import com.partnership.bjbdocumenttrackerreader.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
        val seconds = millis / 1000
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

    private val _isThereFilter = MutableStateFlow<Boolean?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagedAssets: Flow<PagingData<AssetEntity>> = _isThereFilter
        .flatMapLatest { isThere ->
            repository.getAssetsPagingFlow(isThere)
        }
        .cachedIn(viewModelScope)

    fun setIsThereFilter(status: Boolean?) {
        _isThereFilter.value = status
    }

    //get data from API

    private val _getSearch = MutableLiveData<ResultWrapper<BaseResponse<GetBulkDocument>>?>()
    val listSearch: LiveData<ResultWrapper<BaseResponse<GetBulkDocument>>?> get() = _getSearch

    fun getSearchDocument(isDocument: Boolean, query: String? = null) {
        viewModelScope.launch {
            val type = if (isDocument) "document" else "agunan"
            _getSearch.value = repository.searchAsset(
                type = type,
                search = query
            )
        }
    }

    fun clearSearch(){
        _getSearch.value = null
    }

    private val _getBulkDocument = SingleLiveEvent<ResultWrapper<BaseResponse<GetBulkDocument>>?>()
    val listBulkDocument: LiveData<ResultWrapper<BaseResponse<GetBulkDocument>>?> get() = _getBulkDocument

    suspend fun getBulkDocument(isDocument: Boolean) {

        if (isDocument) {
            _getBulkDocument.value = repository.getBulkDocument()
        } else {
            _getBulkDocument.value = repository.getBulkAgunan()
        }
    }

    fun clearBulkDocument(){
        _getBulkDocument.value = null
    }

    suspend fun postStockOpname(isDocument: Boolean): ResultWrapper<BaseResponse<Unit>> {
        val type = if (isDocument) "document" else "agunan"
        val dataBulk = repository.getStockOpnameItems()
        return repository.postStockOpname(type, PostStockOpname(dataBulk))
    }

    private val _scannedTags = MutableStateFlow<List<TagInfo>>(emptyList())
    val scannedTags: StateFlow<List<TagInfo>> get() = _scannedTags

    fun startScan() {
        reader.readTagAuto { newTag ->
            val tagInfo = TagInfo(
                epc = newTag.epc,
                rssi = newTag.rssi
            )
            insertTagOrdered(tagInfo)
        }
    }

    fun stopScan(){
        if (reader.isInventorying() == true){
            reader.stopReadTag()
        }
    }

    private fun insertTagOrdered(newTag: TagInfo) {
        val currentList = _scannedTags.value.toMutableList()
        val exists = booleanArrayOf(false)

        val index = RFIDUtils.getInsertIndex(currentList, newTag, exists)

        if (!exists[0]) {
            _soundBeep.postValue(true)
            currentList.add(index, newTag)
            _scannedTags.value = currentList
        }else{
            _soundBeep.postValue(false)
        }
    }

    fun clearScannedTags() {
        Log.d(TAG, "clearScannedTags() dipanggil, jumlah tag sebelum dihapus: ${_scannedTags.value.size}")
        _scannedTags.value = emptyList()
        Log.d(TAG, "clearScannedTags() selesai, jumlah tag setelah dihapus: ${_scannedTags.value.size}")
    }


    fun validateAllTags(onDone: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentTags = scannedTags.value

            currentTags.forEach { tag ->
                if (!repository.isAssetThere(tag.epc)) {
                    repository.updateIsThere(tag.epc, true)
                }
            }

            // Kembali ke Main Thread setelah selesai untuk update UI
            withContext(Dispatchers.Main) {
                onDone()
            }
        }
    }

    private val _setDataToSearchingDocument = MutableLiveData<Document>()
    val searchDocumentEpc : LiveData<Document> get() = _setDataToSearchingDocument

    fun setDataToSearchingDocument(document: Document){
        _setDataToSearchingDocument.value = document
    }
}