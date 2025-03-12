package com.partnership.rfid.repository

import com.partnership.bjbdocumenttrackerreader.data.ResultWrapper
import com.partnership.bjbdocumenttrackerreader.data.network.ApiService
import com.partnership.rfid.data.model.BaseResponse
import com.partnership.rfid.data.model.GetLastScan
import com.partnership.rfid.data.model.UploadData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RFIDRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : RFIDRepository {
    override suspend fun uploadData(data: String): ResultWrapper<BaseResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.uploadData(UploadData(data))
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        ResultWrapper.Success(body)
                    } else {
                        ResultWrapper.Error("Response body is null")
                    }
                } else {
                    ResultWrapper.Error("Error: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                ResultWrapper.Error(e.message ?: "Unknown Error")
            }
        }
    }

    override suspend fun getLastScan(): ResultWrapper<GetLastScan> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getLastScan()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        ResultWrapper.Success(body)
                    } else {
                        ResultWrapper.Error("Response body is null")
                    }
                } else {
                    ResultWrapper.Error("Error: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                ResultWrapper.Error(e.message ?: "Unknown Error")
            }
        }
    }
}