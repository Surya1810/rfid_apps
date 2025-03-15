package com.partnership.bjbdocumenttrackerreader.repository

import com.partnership.bjbdocumenttrackerreader.data.ResultWrapper
import com.partnership.bjbdocumenttrackerreader.data.model.GetDashboard
import com.partnership.rfid.data.model.BaseResponse
import com.partnership.rfid.data.model.GetLastScan

interface RFIDRepository {
    suspend fun uploadDataDocument(data: String): ResultWrapper<BaseResponse<Unit>>
    suspend fun uploadDataAgunan(data: String): ResultWrapper<BaseResponse<Unit>>

    suspend fun getDashboard(): ResultWrapper<BaseResponse<GetDashboard>>
}