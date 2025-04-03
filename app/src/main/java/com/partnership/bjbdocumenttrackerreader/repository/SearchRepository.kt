package com.partnership.bjbdocumenttrackerreader.repository

import com.partnership.bjbdocumenttrackerreader.data.ResultWrapper
import com.partnership.bjbdocumenttrackerreader.data.model.BaseResponse
import com.partnership.bjbdocumenttrackerreader.data.model.Data
import com.partnership.bjbdocumenttrackerreader.data.model.DetailAgunan
import com.partnership.bjbdocumenttrackerreader.data.model.DocumentDetail
import com.partnership.bjbdocumenttrackerreader.data.model.GetSearchAgunan
import com.partnership.bjbdocumenttrackerreader.data.model.GetSearchDocument
import com.partnership.bjbdocumenttrackerreader.data.model.ItemStatus
import com.partnership.rfid.data.model.UploadData

interface SearchRepository {
    suspend fun getLostDocument(): ResultWrapper<BaseResponse<List<ItemStatus>>>
    suspend fun postLostDocument(uploadData: Data): ResultWrapper<BaseResponse<Unit>>
    suspend fun getSearchDocument(rfidNumber: String): ResultWrapper<BaseResponse<List<DocumentDetail>>>
    suspend fun postSearchDocument(uploadData: Data):ResultWrapper<BaseResponse<Unit>>

    suspend fun getLostAgunan(): ResultWrapper<BaseResponse<List<ItemStatus>>>
    suspend fun postLostAgunan(uploadData: Data): ResultWrapper<BaseResponse<Unit>>
    suspend fun getSearchAgunan(rfidNumber: String): ResultWrapper<BaseResponse<List<DetailAgunan>>>
    suspend fun postSearchAgunan(uploadData: Data):ResultWrapper<BaseResponse<Unit>>
}