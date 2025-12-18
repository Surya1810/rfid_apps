package com.partnership.bjbdocumenttrackerreader.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.partnership.bjbdocumenttrackerreader.data.ResultWrapper
import com.partnership.bjbdocumenttrackerreader.data.local.dao.AssetDao
import com.partnership.bjbdocumenttrackerreader.data.local.entity.AssetEntity
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
import com.partnership.bjbdocumenttrackerreader.data.model.toEntityList
import com.partnership.bjbdocumenttrackerreader.data.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import retrofit2.HttpException
import java.io.File
import javax.inject.Inject

class RFIDRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val assetDao: AssetDao
) : RFIDRepository {

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

    override suspend fun getHistoriesSo(
        page: Int,
        category: String
    ): ResultWrapper<BaseResponse<GetHistoriesResponse>> {
        return try {
            val response = apiService.getHistoriesSO(
                page = page,
                type = category
            )

            if (response.isSuccessful) {
                val body = response.body()

                if (body != null) {
                    ResultWrapper.Success(body)
                } else {
                    ResultWrapper.ErrorResponse("Response body is null")
                }

            } else {
                ResultWrapper.ErrorResponse(
                    "HTTP ${response.code()} : ${response.message()}"
                )
            }

        } catch (e: IOException) {
            ResultWrapper.NetworkError(e.message ?: "Network error")
        } catch (e: Exception) {
            ResultWrapper.Error(e.message ?: "Unexpected error")
        }
    }


    override suspend fun getBulkDocument(): ResultWrapper<BaseResponse<GetBulkDocument>> {
        return try {
            val response = apiService.getBulkDocument()

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.status == "success" && body.data != null) {
                    val entities = body.data.toEntityList()
                    assetDao.deleteAllAssets()
                    assetDao.insertAll(entities)
                    ResultWrapper.Success(body)
                } else {
                    ResultWrapper.ErrorResponse("Gagal: ${body?.message ?: "Unknown"}")
                }
            } else {
                ResultWrapper.ErrorResponse("HTTP error: ${response.code()}")
            }

        } catch (e: IOException) {
            ResultWrapper.NetworkError("Network error: ${e.localizedMessage}")
        } catch (e: HttpException) {
            ResultWrapper.ErrorResponse("HTTP error: ${e.message()}")
        } catch (e: Exception) {
            ResultWrapper.Error("Terjadi kesalahan: ${e.localizedMessage}")
        }
    }

    override suspend fun getBulkAgunan(): ResultWrapper<BaseResponse<GetBulkDocument>> {
        return try {
            assetDao.deleteAllAssets()
            val response = apiService.getBulkAgunan()

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.status == "success" && body.data != null) {
                    val entities = body.data.toEntityList()
                    assetDao.insertAll(entities)
                    ResultWrapper.Success(body)
                } else {
                    ResultWrapper.ErrorResponse("Gagal: ${body?.message ?: "Unknown"}")
                }
            } else {
                ResultWrapper.ErrorResponse("HTTP error: ${response.code()}")
            }

        } catch (e: IOException) {
            ResultWrapper.NetworkError("Network error: ${e.localizedMessage}")
        } catch (e: HttpException) {
            ResultWrapper.ErrorResponse("HTTP error: ${e.message()}")
        } catch (e: Exception) {
            ResultWrapper.Error("Terjadi kesalahan: ${e.localizedMessage}")
        }
    }

    override suspend fun postStockOpname(
        type: String,
        stockOpname: PostStockOpname
    ): ResultWrapper<BaseResponse<Unit>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.postStockOpname(type,stockOpname)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        ResultWrapper.Success(body)
                    } else {
                        ResultWrapper.Error("Response body is null")
                    }
                } else {
                    ResultWrapper.ErrorResponse("Error response: ${response.errorBody()?.string() ?: "Unknown error"}")
                }
            } catch (e: IOException) {
                ResultWrapper.NetworkError("Network Error: ${e.localizedMessage}")
            } catch (e: Exception) {
                ResultWrapper.Error("System Error: ${e.localizedMessage}")
            }
        }
    }

    override suspend fun searchAsset(
        type: String,
        search: String?,
        segment: String?,
        status: String?
    ): ResultWrapper<BaseResponse<GetBulkDocument>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getSearch(search,type,segment, status)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        ResultWrapper.Success(body)
                    } else {
                        ResultWrapper.Error("Response body is null")
                    }
                } else {
                    ResultWrapper.ErrorResponse("Error response: ${response.errorBody()?.string() ?: "Unknown error"}")
                }
            } catch (e: IOException) {
                ResultWrapper.NetworkError("Network Error: ${e.localizedMessage}")
            } catch (e: Exception) {
                ResultWrapper.Error("System Error: ${e.localizedMessage}")
            }
        }
    }

    override suspend fun postLostDocument(postLostDocument: PostLostDocument): ResultWrapper<BaseResponse<Unit>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.postLostDocument(postLostDocument)
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

    override suspend fun getListTutorialVideo(): ResultWrapper<BaseResponse<GetListTutorialVideo>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getListTutorialVideo()
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

    override suspend fun getListSegment(): ResultWrapper<BaseResponse<List<GetListSegments>>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getSegments()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        ResultWrapper.Success(body)
                    } else {
                        ResultWrapper.Error("Response body is null")
                    }
                }
                else {
                    ResultWrapper.Error("Error: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                ResultWrapper.Error(e.message ?: "Unknown Error")
            }
        }
    }

    override suspend fun borrowDocument(
        documentId: Int,
        borrowerName: String,
        returnDate: String,
        signature: File
    ): ResultWrapper<BaseResponse<Unit>> {
        return try {
            val documentIdBody =
                documentId.toString().toRequestBody("text/plain".toMediaType())
            val borrowerName =
                borrowerName.toRequestBody("text/plain".toMediaType())
            val returnDateBody =
                returnDate.toRequestBody("text/plain".toMediaType())

            val signatureRequest =
                signature.asRequestBody("image/png".toMediaType())

            val signaturePart = MultipartBody.Part.createFormData(
                name = "signature",
                filename = signature.name,
                body = signatureRequest
            )

            val response = apiService.borrowDocument(
                documentId = documentIdBody,
                borrowerName = borrowerName,
                returnDate = returnDateBody,
                signature = signaturePart
            )

            if (response.isSuccessful && response.body() != null) {
                ResultWrapper.Success(response.body()!!)
            } else {
                ResultWrapper.Error(
                    error = response.errorBody()?.string() ?: "Peminjaman dokumen gagal"
                )
            }
        } catch (e: Exception) {
            ResultWrapper.Error(
                error = e.localizedMessage ?: "Unexpected error, congrats"
            )
        }
    }

    override suspend fun returnDocument(
        documentId: Int,
        signature: File
    ): ResultWrapper<BaseResponse<Unit>> {
        val signatureRequest =
            signature.asRequestBody("image/png".toMediaType())

        val signaturePart = MultipartBody.Part.createFormData(
            name = "signature",
            filename = signature.name,
            body = signatureRequest
        )

        val response = apiService.returnDocument(
            documentId = documentId,
            signature = signaturePart
        )

        if (response.isSuccessful && response.body() != null) {
            return ResultWrapper.Success(response.body()!!)
        } else {
            return ResultWrapper.Error(
                error = response.errorBody()?.string() ?: "Pengembalian dokumen gagal"
            )
        }
    }

    override suspend fun getDetailBorrowed(documentId: Int): ResultWrapper<BaseResponse<GetDetailBorrowed>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getDetailBorrowed(documentId)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        ResultWrapper.Success(body)
                    } else {
                        ResultWrapper.Error("Response body is null")
                    }
                }
                else {
                    ResultWrapper.Error("Error: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                ResultWrapper.Error(e.message ?: "Unknown Error")
            }
        }
    }

    override suspend fun getHistoryBorrow(idDocument: Int): ResultWrapper<BaseResponse<GetHistoryBorrow>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getBorrowHistory(idDocument)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        ResultWrapper.Success(body)
                    } else {
                        ResultWrapper.Error("Response body is null")
                    }
                }
                else {
                    ResultWrapper.Error("Error: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                ResultWrapper.Error(e.message ?: "Unknown Error")
            }
        }

    }


    override suspend fun postStockOpnameChunked(
        type: String,
        allStatuses: List<AssetStatus>,
        chunkSize: Int,
        onProgress: (Int, Int) -> Unit
    ): ResultWrapper<BaseResponse<Unit>> = withContext(Dispatchers.IO) {
        if (allStatuses.isEmpty()) {
            return@withContext ResultWrapper.Error("List stockOpname kosong")
        }
        if (chunkSize <= 0) {
            return@withContext ResultWrapper.Error("chunkSize harus > 0")
        }

        val chunks: List<List<AssetStatus>> = allStatuses.chunked(chunkSize)
        val total = chunks.size
        var lastSuccessBody: BaseResponse<Unit>? = null

        val scanCode = getScanCode()

        for ((index, part) in chunks.withIndex()) {
            onProgress(index + 1, total)

            // panggil API untuk chunk ini
            val result = try {
                val resp = apiService.postStockOpname(type, PostStockOpname(scanCode,part))
                if (resp.isSuccessful) {
                    val body = resp.body()
                    if (body != null) {
                        lastSuccessBody = body
                        null // null artinya tidak ada error
                    } else {
                        "Response body is null (chunk ${index + 1}/$total, size=${part.size})"
                    }
                } else {
                    val msg = resp.errorBody()?.string()?.take(500) ?: "Unknown error"
                    "HTTP ${resp.code()} - $msg (chunk ${index + 1}/$total, size=${part.size})"
                }
            } catch (e: IOException) {
                "Network Error: ${e.localizedMessage} (chunk ${index + 1}/$total, size=${part.size})"
            } catch (e: Exception) {
                "System Error: ${e.localizedMessage} (chunk ${index + 1}/$total, size=${part.size})"
            }

            if (result != null) {
                // berhenti di sini kalau ada error di chunk ke-(index+1)
                return@withContext ResultWrapper.Error(result)
            }
        }

        // semua chunk sukses
        if (lastSuccessBody != null) {
            ResultWrapper.Success(lastSuccessBody)
        } else {
            // sangat kecil kemungkinan terjadi (kalau server selalu null body)
            ResultWrapper.Error("Semua chunk terkirim tapi tidak ada body sukses dari server")
        }
    }

    suspend fun getScanCode(): String {
        return assetDao.getScanCode()
    }


    fun updateIsThere(rfidNumber: String, status: Boolean) {
        assetDao.updateIsThere(rfidNumber, status)
    }

    fun observeDetectedAssets(): Flow<Int> {
        return assetDao.observeDetectedAssets()
    }

    fun observeAllAssets(): Flow<Int> {
        return assetDao.observeAllAssets()
    }

    fun getAssetsPagingFlow(isThere: Boolean?): Flow<PagingData<AssetEntity>> {
        return Pager(
            config = PagingConfig(pageSize = 10),
            pagingSourceFactory = { assetDao.getFilteredAssets(isThere) }
        ).flow
    }

    suspend fun getStockOpnameItems(): List<AssetStatus> {
        return assetDao.getStockOpnameItems()
    }

    fun isAssetThere(epc:String):Boolean{
        return  assetDao.isAssetThere(epc)
    }

    suspend fun getValidEpc(): List<String> {
        return assetDao.getValidEpc()
    }
}