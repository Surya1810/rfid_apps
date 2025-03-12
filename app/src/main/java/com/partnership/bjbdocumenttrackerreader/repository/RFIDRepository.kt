package com.partnership.rfid.repository

import com.partnership.bjbdocumenttrackerreader.data.ResultWrapper
import com.partnership.rfid.data.model.BaseResponse
import com.partnership.rfid.data.model.GetLastScan

interface RFIDRepository {
    suspend fun uploadData(data: String): ResultWrapper<BaseResponse>
    suspend fun getLastScan(): ResultWrapper<GetLastScan>
}