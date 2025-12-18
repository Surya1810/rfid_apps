package com.partnership.bjbdocumenttrackerreader.ui.lending

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.partnership.bjbdocumenttrackerreader.data.ResultWrapper
import com.partnership.bjbdocumenttrackerreader.data.model.BaseResponse
import com.partnership.bjbdocumenttrackerreader.data.model.Document
import com.partnership.bjbdocumenttrackerreader.data.model.GetDetailBorrowed
import com.partnership.bjbdocumenttrackerreader.data.model.GetHistoryBorrow
import com.partnership.bjbdocumenttrackerreader.repository.RFIDRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class BorrowingViewModel @Inject constructor(private val repository: RFIDRepositoryImpl) :
    ViewModel() {

    private var _documentSelected = MutableLiveData<Document>()
    val documentSelected: LiveData<Document> get() = _documentSelected

    fun setDocument(document: Document) {
        _documentSelected.value = document
    }

    private var _isCreteBorrow = MutableLiveData<Boolean>()
    val isCreteBorrow: LiveData<Boolean> get() = _isCreteBorrow

    fun setIsCreateBorrow(isCreate: Boolean) {
        _isCreteBorrow.value = isCreate
    }

    private var _signatureBorrowed = MutableLiveData<File>()
    val signatureBorrowed: LiveData<File> get() = _signatureBorrowed

    fun setSignatureBorrowed(signature: File) {
        _signatureBorrowed.value = signature
    }

    private var _signatureReturned = MutableLiveData<File>()
    val signatureReturned: LiveData<File> get() = _signatureReturned

    fun setSignatureReturned(signature: File) {
        _signatureReturned.value = signature
    }

    fun clearSignature() {
        _signatureBorrowed.value = null
        _signatureReturned.value = null
    }

    fun clearDocument() {
        _documentSelected.value = null
    }

    suspend fun borrowDocument(
        documentId: Int,
        borrowerName: String,
        returnDate: String,
        signature: File
    ): ResultWrapper<BaseResponse<Unit>> {
        return repository.borrowDocument(documentId, borrowerName, returnDate, signature)
    }

    suspend fun returnDocument(
        documentId: Int,
        signature: File
    ): ResultWrapper<BaseResponse<Unit>> {
        return repository.returnDocument(documentId, signature)
    }

    private var _detailBorrowed = MutableLiveData<ResultWrapper<BaseResponse<GetDetailBorrowed>>>()
    val detailBorrowed: LiveData<ResultWrapper<BaseResponse<GetDetailBorrowed>>> get() = _detailBorrowed

    fun getDetailBorrowed(documentId: Int) {
        _detailBorrowed.value = ResultWrapper.Loading
        viewModelScope.launch {
            _detailBorrowed.value = repository.getDetailBorrowed(documentId)
        }
    }

    private var _historyBorrow = MutableLiveData<ResultWrapper<BaseResponse<GetHistoryBorrow>>>()
    val historyBorrow: LiveData<ResultWrapper<BaseResponse<GetHistoryBorrow>>> get() = _historyBorrow

    fun getHistoryBorrow(idDocument: Int) {
        _historyBorrow.value = ResultWrapper.Loading
        viewModelScope.launch {
            _historyBorrow.value = repository.getHistoryBorrow(idDocument)
        }
    }
}
