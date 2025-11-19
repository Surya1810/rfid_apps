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
import com.partnership.bjbdocumenttrackerreader.data.model.GetListSegments
import com.partnership.bjbdocumenttrackerreader.data.model.PostStockOpname
import com.partnership.bjbdocumenttrackerreader.data.model.TagInfo
import com.partnership.bjbdocumenttrackerreader.reader.RFIDManager
import com.partnership.bjbdocumenttrackerreader.repository.RFIDRepositoryImpl
import com.partnership.bjbdocumenttrackerreader.util.RFIDUtils
import com.partnership.bjbdocumenttrackerreader.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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

    // simpan state filter & query
    private val _listSegment = MutableLiveData<List<GetListSegments>>()
    val listSegment: LiveData<List<GetListSegments>> get() = _listSegment

    // null = "Semua"
    private val _selectedSegment = MutableLiveData<String?>(null)
    val selectedSegment: LiveData<String?> get() = _selectedSegment

    // state internal untuk search
    private var lastIsDocument: Boolean = true
    private var lastQuery: String? = null

    // --- API search utama, dipanggil dari fragment ---
    fun getSearchDocument(isDocument: Boolean, query: String? = null) {
        lastIsDocument = isDocument
        lastQuery = query
        searchWithCurrentFilter()
    }

    // fungsi helper: selalu pakai kombinasi terakhir isDocument + query + segment
    private fun searchWithCurrentFilter() {
        viewModelScope.launch {
            val type = if (lastIsDocument) "document" else "agunan"
            val segment = _selectedSegment.value
            val query = lastQuery

            _getSearch.value = ResultWrapper.Loading

            _getSearch.value = repository.searchAsset(
                type = type,
                search = query,
                segment = segment
            )
        }
    }

    // --- Segment filter ---

    suspend fun getListFilterSegment(): ResultWrapper<BaseResponse<List<GetListSegments>>> {
        return repository.getListSegment()
    }

    fun setListSegment(list: List<GetListSegments>) {
        _listSegment.value = list
    }

    // dipanggil ketika user pilih filter di bottom sheet
    fun setSelectedSegment(segment: String?) {
        _selectedSegment.value = segment
        // setiap ganti filter -> ulang search dengan query terakhir
        searchWithCurrentFilter()
    }

    fun clearSearch() {
        _getSearch.value = null
        lastQuery = null
        // kalau mau, bisa reset filter juga:
        // _selectedSegment.value = null
    }
    private val _getBulkDocument = SingleLiveEvent<ResultWrapper<BaseResponse<GetBulkDocument>>?>()
    val listBulkDocument: LiveData<ResultWrapper<BaseResponse<GetBulkDocument>>?> get() = _getBulkDocument

    suspend fun getBulkDocument(isDocument: Boolean) {
        _getBulkDocument.value = ResultWrapper.Loading
        if (isDocument) {
            _getBulkDocument.value = repository.getBulkDocument()
        } else {
            _getBulkDocument.value = repository.getBulkAgunan()
        }
    }

    fun clearBulkDocument() {
        _getBulkDocument.value = null
    }

    data class UploadProgress(val current: Int, val total: Int) {
        val percent: Int get() = if (total == 0) 0 else ((current * 100f) / total).toInt()
    }

    // bisa dipakai di Compose via collectAsState()
    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading

    private val _progress = MutableStateFlow(UploadProgress(0, 0))
    val progress: StateFlow<UploadProgress> = _progress

    private val _uploadResult =
        MutableStateFlow<ResultWrapper<BaseResponse<Unit>>?>(null)
    val uploadResult: StateFlow<ResultWrapper<BaseResponse<Unit>>?> = _uploadResult
    private var uploadJob: Job? = null
    fun postStockOpnameChunked(
        isDocument: Boolean,
        chunkSize: Int = 500
    ) {
        uploadJob?.cancel()
        uploadJob = viewModelScope.launch {
            _isUploading.value = true
            _progress.value = UploadProgress(0, 0)
            _uploadResult.value = null

            val type = if (isDocument) "document" else "agunan"
            val allStatuses = repository.getStockOpnameItems()

            if (allStatuses.isEmpty()) {
                _uploadResult.value = ResultWrapper.Error("Data stock opname kosong")
                _isUploading.value = false
                return@launch
            }

            val result = repository.postStockOpnameChunked(
                type = type,
                allStatuses = allStatuses,
                chunkSize = chunkSize
            ) { current, total ->
                _progress.value = UploadProgress(current, total)
            }

            _uploadResult.value = result
            _isUploading.value = false
        }
    }

    fun clearResultStockOpname() {
        _uploadResult.value = null
    }

    private val _scannedTags = MutableStateFlow<List<TagInfo>>(emptyList())
    val scannedTags: StateFlow<List<TagInfo>> get() = _scannedTags

    @Volatile private var isReading = false
    private var _isScanning: MutableLiveData<Boolean> = MutableLiveData(false)
    val isScanning: LiveData<Boolean> get() = _isScanning

    @OptIn(ExperimentalStdlibApi::class)
    fun startScan() {
        if (isReading) return
        isReading = true
        _isScanning.value = true
        startTimer()

        isReading = true
        recentlyScanned.clear()
        reader.readTagAuto { uhfTagInfo ->
            if (!isReading) return@readTagAuto

            CoroutineScope(Dispatchers.Main).launch {
                val scannedBytes = uhfTagInfo.epcBytes
                val scannedHex = scannedBytes.toHexString().lowercase()

                val matched = cacheLock.withLock {
                    cacheValidEpcs.any { valid -> scannedBytes.contentEquals(valid) }
                }

                if (matched && !isRecentlyScanned(scannedHex)) {
                    markScanned(scannedHex)
                    withContext(Dispatchers.IO) {
                        updateIsThere(scannedHex)
                    }
                }
            }
        }
    }

    fun setSoundBeepToFalse(){
        _soundBeep.postValue(false)
    }

    private val cacheValidEpcs = mutableSetOf<ByteArray>()
    private val cacheValidEpcStrings = mutableSetOf<String>()
    private val cacheLock = Mutex()
    private val recentlyScanned = mutableSetOf<String>()

    @OptIn(ExperimentalStdlibApi::class)
    fun cacheAllValidEpcs() {
        viewModelScope.launch {
            val allEpc = repository.getValidEpc()
            cacheLock.withLock {
                cacheValidEpcs.clear()
                cacheValidEpcStrings.clear()

                allEpc.forEach { epc ->
                    val normalized = epc.lowercase().padStart(8, '0')
                    cacheValidEpcStrings.add(normalized)

                    try {
                        if (normalized.length % 2 == 0) {
                            cacheValidEpcs.add(normalized.hexToByteArray())
                        }
                    } catch (e: Exception) {
                        Log.e("EPC_PARSE", "Invalid hex: $normalized", e)
                    }
                }
            }
        }
    }

    private fun isRecentlyScanned(hex: String): Boolean {
        return recentlyScanned.contains(hex)
    }


    private fun markScanned(hex: String) {
        recentlyScanned.add(hex)
    }

    private fun updateIsThere(hex: String) {
        if (!repository.isAssetThere(hex)) {
            repository.updateIsThere(hex, true)
            _soundBeep.postValue(true)
        }
    }


    fun clearScannedTags() {
        _scannedTags.value = emptyList()
    }

    fun stopScan() {
        if (reader.isInventorying() == true) {
            reader.stopReadTag()
            stopTimer()
            _isScanning.value = false
            isReading = false
            setSoundBeepToFalse()
        }
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
    val searchDocumentEpc: LiveData<Document> get() = _setDataToSearchingDocument

    fun setDataToSearchingDocument(document: Document) {
        _setDataToSearchingDocument.value = document
    }
}