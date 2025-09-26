package com.partnership.bjbdocumenttrackerreader.repository

import com.partnership.bjbdocumenttrackerreader.data.ResultWrapper
import com.partnership.bjbdocumenttrackerreader.data.model.BaseResponse
import com.partnership.bjbdocumenttrackerreader.data.model.GetBulkDocument
import com.partnership.bjbdocumenttrackerreader.data.model.GetDashboard
import com.partnership.bjbdocumenttrackerreader.data.model.GetListTutorialVideo
import com.partnership.bjbdocumenttrackerreader.data.model.PostLostDocument
import com.partnership.bjbdocumenttrackerreader.data.model.PostStockOpname

interface RFIDRepository {
    suspend fun getDashboard(): ResultWrapper<BaseResponse<GetDashboard>>
    suspend fun getListLostDocument(page: Int): ResultWrapper<BaseResponse<List<String>>>

    suspend fun getBulkDocument(): ResultWrapper<BaseResponse<GetBulkDocument>>
    suspend fun getBulkAgunan(): ResultWrapper<BaseResponse<GetBulkDocument>>
    suspend fun postStockOpname(type:String,stockOpname: PostStockOpname): ResultWrapper<BaseResponse<Unit>>
    suspend fun searchAsset(type:String,search:String? = null):ResultWrapper<BaseResponse<GetBulkDocument>>
    suspend fun postLostDocument(postLostDocument: PostLostDocument): ResultWrapper<BaseResponse<Unit>>
    suspend fun getListTutorialVideo(): ResultWrapper<BaseResponse<GetListTutorialVideo>>
}