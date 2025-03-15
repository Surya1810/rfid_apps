package com.partnership.bjbdocumenttrackerreader.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.viewModelScope
import com.partnership.bjbdocumenttrackerreader.data.ResultWrapper
import com.partnership.bjbdocumenttrackerreader.data.model.GetDashboard
import com.partnership.bjbdocumenttrackerreader.repository.RFIDRepositoryImpl
import com.partnership.rfid.data.model.BaseResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(private val repository: RFIDRepositoryImpl): ViewModel() {
    private val _getDashboard = MutableLiveData<ResultWrapper<BaseResponse<GetDashboard>>>()
    val dataDashboard: LiveData<ResultWrapper<BaseResponse<GetDashboard>>>
        get() = _getDashboard.distinctUntilChanged()


    fun getDashboard() {
        viewModelScope.launch(Dispatchers.IO) { // Pindahkan ke IO Thread
            val result = repository.getDashboard()
            withContext(Dispatchers.Main) {
                _getDashboard.value = result
            }
        }
    }



}