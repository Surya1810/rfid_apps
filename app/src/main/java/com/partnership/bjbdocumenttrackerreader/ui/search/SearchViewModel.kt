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
import com.partnership.bjbdocumenttrackerreader.data.model.Document
import com.partnership.bjbdocumenttrackerreader.data.model.PostLostDocument
import com.partnership.bjbdocumenttrackerreader.repository.RFIDRepositoryImpl
import com.partnership.rfid.data.model.UploadData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SearchViewModel @Inject constructor(private val repository: RFIDRepositoryImpl, private val reader: RFIDManager): ViewModel() {

    private val _isScanning = MutableLiveData<Boolean>()
    val isScanning: LiveData<Boolean> get() = _isScanning

    private val _postMessage = MutableLiveData<ResultWrapper<BaseResponse<Unit>>>()
    val postMessage : LiveData<ResultWrapper<BaseResponse<Unit>>> get() = _postMessage

    private val _isFound = MutableLiveData<Boolean>()
    val isFound : LiveData<Boolean> get() = _isFound

    suspend fun postLostDocument(postLostDocument: PostLostDocument): ResultWrapper<BaseResponse<Unit>> {
        return repository.postLostDocument(postLostDocument)
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

    fun stopReadTag(){
        _isScanning.value = false
        reader.stopReadTag()
    }

}