package com.partnership.bjbdocumenttrackerreader.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.partnership.bjbdocumenttrackerreader.data.ResultWrapper
import com.partnership.bjbdocumenttrackerreader.data.local.dao.AssetDao
import com.partnership.bjbdocumenttrackerreader.data.model.GetDashboard
import com.partnership.bjbdocumenttrackerreader.repository.RFIDRepositoryImpl
import com.partnership.bjbdocumenttrackerreader.data.model.BaseResponse
import com.partnership.bjbdocumenttrackerreader.ui.paging.LostDocumentPagingSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(private val repository: RFIDRepositoryImpl, private val assetDao: AssetDao): ViewModel() {
    private val _getDashboard = MutableLiveData<ResultWrapper<BaseResponse<GetDashboard>>>()
    val dataDashboard: LiveData<ResultWrapper<BaseResponse<GetDashboard>>>
        get() = _getDashboard


    val lostDocumentPagingData: Flow<PagingData<String>> = Pager(
        config = PagingConfig(pageSize = 10),
        pagingSourceFactory = { LostDocumentPagingSource(repository) }
    ).flow.cachedIn(viewModelScope)

    fun getDashboard() {
        viewModelScope.launch(Dispatchers.IO) { // Pindahkan ke IO Thread
            val result = repository.getDashboard()
            withContext(Dispatchers.Main) {
                _getDashboard.value = result
            }
        }
    }

    private val _isDocumentSelected =  MutableLiveData<Boolean>()
    val isDocumentSelected : LiveData<Boolean> get() = _isDocumentSelected

    fun setIsDocument(state: Boolean){
        _isDocumentSelected.value = state
    }

}