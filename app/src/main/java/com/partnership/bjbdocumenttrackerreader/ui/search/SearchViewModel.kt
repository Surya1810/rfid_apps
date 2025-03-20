package com.partnership.bjbdocumenttrackerreader.ui.search

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.partnership.bjbdocumenttrackerreader.data.ResultWrapper
import com.partnership.bjbdocumenttrackerreader.data.model.DocumentDetail
import com.partnership.bjbdocumenttrackerreader.data.model.GetSearchAgunan
import com.partnership.bjbdocumenttrackerreader.data.model.GetSearchDocument
import com.partnership.bjbdocumenttrackerreader.data.model.ItemStatus
import com.partnership.bjbdocumenttrackerreader.reader.RFIDManager
import com.partnership.bjbdocumenttrackerreader.repository.SearchRepositoryImpl
import com.partnership.bjbdocumenttrackerreader.data.model.BaseResponse
import com.partnership.bjbdocumenttrackerreader.data.model.DetailAgunan
import com.partnership.rfid.data.model.UploadData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SearchViewModel @Inject constructor(private val repository: SearchRepositoryImpl, private val reader: RFIDManager): ViewModel() {

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

    private val _setDataToSearchingAgunan = MutableLiveData<DetailAgunan>()
    val searchAgunanEpc: LiveData<DetailAgunan> get() = _setDataToSearchingAgunan

    private val _setDataToSearchingDocument = MutableLiveData<DocumentDetail>()
    val searchDocumentEpc : LiveData<DocumentDetail> get() = _setDataToSearchingDocument

    private val _getLostDocument = MutableLiveData<ResultWrapper<BaseResponse<List<ItemStatus>>>>()
    val getLostEpc : LiveData<ResultWrapper<BaseResponse<List<ItemStatus>>>> get() = _getLostDocument

    private val _isScanning = MutableLiveData<Boolean>()
    val isScanning: LiveData<Boolean> get() = _isScanning

    private val _displayedList = MutableLiveData<List<ItemStatus>>()
    val displayedList: LiveData<List<ItemStatus>> get() = _displayedList

    private val _listSearchDocument = MutableLiveData<ResultWrapper<BaseResponse<List<DocumentDetail>>>>()
    val listSearchDocument : LiveData<ResultWrapper<BaseResponse<List<DocumentDetail>>>> get() = _listSearchDocument

    private val _listSearchAgunan = MutableLiveData<ResultWrapper<BaseResponse<List<DetailAgunan>>>>()
    val listSearchAgunan : LiveData<ResultWrapper<BaseResponse<List<DetailAgunan>>>> get() = _listSearchAgunan

    private val _postMessage = MutableLiveData<ResultWrapper<BaseResponse<Unit>>>()
    val postMessage : LiveData<ResultWrapper<BaseResponse<Unit>>> get() = _postMessage

    private val _isFound = MutableLiveData<Boolean>()
    val isFound : LiveData<Boolean> get() = _isFound

    init {
        _isFound.value = false
    }

    fun getListSearchDocument(search: String){
        viewModelScope.launch {
            _listSearchDocument.value = repository.getSearchDocument(search)
        }
    }

    fun getListSearchAgunan(search: String){
        viewModelScope.launch {
            _listSearchAgunan.value = repository.getSearchAgunan(search)
        }
    }

    fun setEpcFilter(data: String): Boolean{
        reader.setEpcFilter(data)
        return true
    }

    fun setDataDocumentSearch(documentDetail: DocumentDetail){
        _setDataToSearchingDocument.value = documentDetail
    }

    fun setDataAgunanSearch(agunanDetail: DetailAgunan){
        _setDataToSearchingAgunan.value = agunanDetail
    }
    fun sendDataLost(lostTags: List<ItemStatus>,isDocument: Boolean) {
        val foundTagsList = lostTags
            .filter { it.isThere }
            .joinToString(separator = ",") { it.epc }
        if (isDocument){
            viewModelScope.launch {
                repository.postLostDocument(UploadData(foundTagsList))
            }
        }else{
            viewModelScope.launch {
                repository.postLostAgunan(UploadData(foundTagsList))
            }
        }
    }

    fun sendDataSearch(epc: String, isDocument: Boolean) {
        if (isDocument){
            viewModelScope.launch {
                val result = repository.postSearchDocument(UploadData(epc))
                _postMessage.value = result
                Log.d("SendSearch", "Document Result: $result")
            }
        } else {
            viewModelScope.launch {
                val result = repository.postSearchAgunan(UploadData(epc))
                _postMessage.value = result
                Log.d("SendSearch", "Agunan Result: $result")
            }
        }
    }

    fun clearFilterReader(){
        reader.disableFilter()
    }

    val foundCount: LiveData<Int> = MediatorLiveData<Int>().apply {
        addSource(_displayedList) { list ->
            value = list.count { it.isThere }
        }
    }

    val lostCount: LiveData<Int> = MediatorLiveData<Int>().apply {
        addSource(_displayedList) { list ->
            value = list.count { !it.isThere }
        }
    }

    fun addLostTag(lostTags : List<ItemStatus>){
        _displayedList.value = lostTags
    }

    fun searchSingleTag(epc: String){
        _isScanning.value = true
        reader.readTagAuto {uhftagInfo ->
            if (uhftagInfo.epc == epc){
                _isFound.postValue(true)
                _isScanning.postValue(false)
            }
        }
    }

    fun readTagAuto(){
        _isScanning.value = true
        startTimer()
        reader.readTagAuto {uhftagInfo ->
            val newTag = ItemStatus(
                epc = uhftagInfo.epc,
                isThere = true
            )
            updateTagList(newTag)
        }
    }

    fun getLostEpc(){
        viewModelScope.launch {
            _getLostDocument.value = repository.getLostDocument()
        }
    }

    fun stopReadTag(){
        _isScanning.value = false
        stopTimer()
        reader.stopReadTag()
    }

    private fun updateTagList(scannedTag: ItemStatus) {
        val currentList = _displayedList.value?.toMutableList() ?: mutableListOf()

        val index = currentList.indexOfFirst { it.epc == scannedTag.epc }

        if (index != -1) {
            val item = currentList[index]
            if (!item.isThere) {
                currentList[index] = item.copy(isThere = true)
                _displayedList.postValue(currentList)
            }
        }
    }

    private fun startTimer() {
        if (!isRunning) {
            isRunning = true
            startTime = System.currentTimeMillis()
            handler.post(timerRunnable)
        }
    }

    private fun formatTime(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / 1000) / 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun stopTimer() {
        isRunning = false
        handler.removeCallbacks(timerRunnable)
    }
}