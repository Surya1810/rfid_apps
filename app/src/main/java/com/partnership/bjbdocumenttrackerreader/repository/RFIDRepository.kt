package com.partnership.bjbdocumenttrackerreader.repository

import com.partnership.bjbdocumenttrackerreader.data.ResultWrapper
import com.partnership.bjbdocumenttrackerreader.data.model.AssetStatus
import com.partnership.bjbdocumenttrackerreader.data.model.BaseResponse
import com.partnership.bjbdocumenttrackerreader.data.model.GetBulkDocument
import com.partnership.bjbdocumenttrackerreader.data.model.GetDashboard
import com.partnership.bjbdocumenttrackerreader.data.model.GetDetailBorrowed
import com.partnership.bjbdocumenttrackerreader.data.model.GetHistoriesResponse
import com.partnership.bjbdocumenttrackerreader.data.model.GetHistoryBorrow
import com.partnership.bjbdocumenttrackerreader.data.model.GetListSegments
import com.partnership.bjbdocumenttrackerreader.data.model.GetListTutorialVideo
import com.partnership.bjbdocumenttrackerreader.data.model.PostLostDocument
import com.partnership.bjbdocumenttrackerreader.data.model.PostStockOpname
import java.io.File

interface RFIDRepository {
    suspend fun getDashboard(): ResultWrapper<BaseResponse<GetDashboard>>
    suspend fun getHistoriesSo(page: Int, category: String): ResultWrapper<BaseResponse<GetHistoriesResponse>>
    suspend fun getBulkDocument(): ResultWrapper<BaseResponse<GetBulkDocument>>
    suspend fun getBulkAgunan(): ResultWrapper<BaseResponse<GetBulkDocument>>
    suspend fun postStockOpname(type:String,stockOpname: PostStockOpname): ResultWrapper<BaseResponse<Unit>>
    suspend fun searchAsset(type:String,search:String? = null, segment:String? = null, status: String? = null):ResultWrapper<BaseResponse<GetBulkDocument>>
    suspend fun postLostDocument(postLostDocument: PostLostDocument): ResultWrapper<BaseResponse<Unit>>
    suspend fun getListTutorialVideo(): ResultWrapper<BaseResponse<GetListTutorialVideo>>
    suspend fun getListSegment(): ResultWrapper<BaseResponse<List<GetListSegments>>>
    suspend fun borrowDocument(documentId: Int, borrowerName: String, returnDate: String, signature: File) : ResultWrapper<BaseResponse<Unit>>
    suspend fun returnDocument(documentId: Int, signature: File) : ResultWrapper<BaseResponse<Unit>>
    suspend fun getDetailBorrowed(documentId: Int) : ResultWrapper<BaseResponse<GetDetailBorrowed>>
    suspend fun getHistoryBorrow(idDocument: Int) : ResultWrapper<BaseResponse<GetHistoryBorrow>>

    suspend fun postStockOpnameChunked(
        type: String,
        allStatuses: List<AssetStatus>,
        chunkSize: Int = 500,
        onProgress: (currentChunk: Int, totalChunks: Int) -> Unit = { _, _ -> }
    ): ResultWrapper<BaseResponse<Unit>>
}