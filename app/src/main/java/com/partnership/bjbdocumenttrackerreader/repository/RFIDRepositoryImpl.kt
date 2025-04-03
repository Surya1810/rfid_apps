package com.partnership.bjbdocumenttrackerreader.repository

import com.partnership.bjbdocumenttrackerreader.data.ResultWrapper
import com.partnership.bjbdocumenttrackerreader.data.model.GetDashboard
import com.partnership.bjbdocumenttrackerreader.data.network.ApiService
import com.partnership.bjbdocumenttrackerreader.data.model.BaseResponse
import com.partnership.rfid.data.model.UploadData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.IOException
import javax.inject.Inject

class RFIDRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : RFIDRepository {
    override suspend fun uploadDataDocument(data: List<String>): ResultWrapper<BaseResponse<Unit>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.uploadDataDocument(UploadData(data))
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


    override suspend fun uploadDataAgunan(data: List<String>): ResultWrapper<BaseResponse<Unit>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.uploadDataAgunan(UploadData(data))
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

    override suspend fun getDashboard(): ResultWrapper<BaseResponse<GetDashboard>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getDashboard()
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

    override suspend fun getListLostDocument(page: Int): ResultWrapper<BaseResponse<List<String>>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getListLostDocument(page)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        ResultWrapper.Success(body)
                    } else {
                        ResultWrapper.Error("Response body is null")
                    }
                } else {
                    // Asumsikan ini adalah error dari API → ErrorResponse
                    ResultWrapper.ErrorResponse("Error response: ${response.errorBody()?.string() ?: "Unknown error"}")
                }
            } catch (e: IOException) {
                // Error jaringan → NetworkError
                ResultWrapper.NetworkError("Network Error: ${e.localizedMessage}")
            } catch (e: Exception) {
                // Error umum sistem → Error
                ResultWrapper.Error("System Error: ${e.localizedMessage}")
            }
        }
    }



}