package com.partnership.bjbdocumenttrackerreader.ui.book

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.partnership.bjbdocumenttrackerreader.data.ResultWrapper
import com.partnership.bjbdocumenttrackerreader.data.model.GetListTutorialVideo
import com.partnership.bjbdocumenttrackerreader.data.model.TutorialVideo
import com.partnership.bjbdocumenttrackerreader.repository.RFIDRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManualViewModel @Inject constructor(private val repository: RFIDRepositoryImpl): ViewModel() {

    private val _videoList = MutableLiveData<List<TutorialVideo>>()
    val videoList: MutableLiveData<List<TutorialVideo>> get() = _videoList

    fun getListTutorial(){
        viewModelScope.launch {
            when(val response = repository.getListTutorialVideo()){
                is ResultWrapper.Error<*> -> {

                }
                is ResultWrapper.ErrorResponse<*> -> {

                }
                ResultWrapper.Loading -> {

                }
                is ResultWrapper.NetworkError<*> -> {

                }
                is ResultWrapper.Success ->{
                    val data = response.data.data?.videos
                    if (data != null){
                        _videoList.value = data
                    }
                }
            }
        }
    }
}