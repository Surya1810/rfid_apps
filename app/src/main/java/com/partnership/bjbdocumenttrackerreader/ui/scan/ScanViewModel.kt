package com.partnership.bjbdocumenttrackerreader.ui.scan

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.partnership.bjbdocumenttrackerreader.data.ResultWrapper
import com.partnership.bjbdocumenttrackerreader.data.model.GetDashboard
import com.partnership.bjbdocumenttrackerreader.data.model.TagInfo
import com.partnership.rfid.data.model.BaseResponse
import com.partnership.bjbdocumenttrackerreader.repository.RFIDRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScanViewModel @Inject constructor(private val repository: RFIDRepositoryImpl) : ViewModel() {

    private val _uploadData = MutableLiveData<ResultWrapper<BaseResponse<Unit>>>()
    val resultUploadData: MutableLiveData<ResultWrapper<BaseResponse<Unit>>> get() = _uploadData



    fun uploadData(epcList:List<TagInfo>,isDocument: Boolean){
        val epcData = epcList.joinToString(separator = ",") { it.epc }
        viewModelScope.launch {
            if (isDocument){
                _uploadData.value = repository.uploadDataDocument(epcData)
            }else{
                _uploadData.value = repository.uploadDataAgunan(epcData)
            }

        }
    }



}