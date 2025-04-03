package com.partnership.bjbdocumenttrackerreader.repository

import com.partnership.bjbdocumenttrackerreader.data.ResultWrapper
import com.partnership.bjbdocumenttrackerreader.data.model.GetSearchAgunan
import com.partnership.bjbdocumenttrackerreader.data.model.GetSearchDocument
import com.partnership.bjbdocumenttrackerreader.data.model.ItemStatus
import com.partnership.bjbdocumenttrackerreader.data.network.ApiService
import com.partnership.bjbdocumenttrackerreader.data.model.BaseResponse
import com.partnership.bjbdocumenttrackerreader.data.model.Data
import com.partnership.bjbdocumenttrackerreader.data.model.DetailAgunan
import com.partnership.bjbdocumenttrackerreader.data.model.DocumentDetail
import com.partnership.rfid.data.model.UploadData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

class SearchRepositoryImpl @Inject constructor(private val apiService: ApiService) :
    SearchRepository {
    override suspend fun getLostDocument(): ResultWrapper<BaseResponse<List<ItemStatus>>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getDocumentLost()
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
            } catch (e: IOException) {
                ResultWrapper.NetworkError(e.message ?: "Network Error")
            }
        }
    }

    override suspend fun postLostDocument(uploadData: Data): ResultWrapper<BaseResponse<Unit>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.postDocumentLost(uploadData)
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
            } catch (e: IOException) {
                ResultWrapper.NetworkError(e.message ?: "Network Error")
            }
        }
    }

    override suspend fun getSearchDocument(
        search: String
    ): ResultWrapper<BaseResponse<List<DocumentDetail>>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getSearchDocument(search)
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
            } catch (e: IOException) {
                ResultWrapper.NetworkError(e.message ?: "Network Error")
            }
        }
    }

    override suspend fun postSearchDocument(uploadData: Data): ResultWrapper<BaseResponse<Unit>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.postSearchDocument(uploadData)
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
            } catch (e: IOException){
                ResultWrapper.NetworkError(e.message ?:"Network Error")
            }
        }
    }

    override suspend fun getLostAgunan(): ResultWrapper<BaseResponse<List<ItemStatus>>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAgunanLost()
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
            } catch (e: IOException) {
                ResultWrapper.NetworkError(e.message ?: "Network Error")
            }
        }
    }

    override suspend fun postLostAgunan(uploadData: Data): ResultWrapper<BaseResponse<Unit>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.postAgunanLost(uploadData)
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
            } catch (e: IOException){
                ResultWrapper.NetworkError(e.message ?:"Network Error")
            }
        }
    }

    override suspend fun getSearchAgunan(search: String): ResultWrapper<BaseResponse<List<DetailAgunan>>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getSearchAgunan(search)
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
            } catch (e: IOException) {
                ResultWrapper.NetworkError(e.message ?: "Network Error")
            }
        }
    }

    override suspend fun postSearchAgunan(uploadData: Data): ResultWrapper<BaseResponse<Unit>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.postSearchAgunan(uploadData)
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
            } catch (e: IOException){
                ResultWrapper.NetworkError(e.message ?:"Network Error")
            }
        }
    }

}