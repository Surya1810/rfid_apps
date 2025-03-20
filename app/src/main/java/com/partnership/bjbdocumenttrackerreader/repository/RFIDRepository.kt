package com.partnership.bjbdocumenttrackerreader.repository

import com.partnership.bjbdocumenttrackerreader.data.ResultWrapper
import com.partnership.bjbdocumenttrackerreader.data.model.GetDashboard
import com.partnership.bjbdocumenttrackerreader.data.model.BaseResponse

interface RFIDRepository {
    suspend fun uploadDataDocument(data: String): ResultWrapper<BaseResponse<Unit>>
    suspend fun uploadDataAgunan(data: String): ResultWrapper<BaseResponse<Unit>>

    suspend fun getDashboard(): ResultWrapper<BaseResponse<GetDashboard>>
}