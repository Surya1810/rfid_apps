package com.partnership.bjbdocumenttrackerreader.data.network

import com.partnership.rfid.data.model.BaseResponse
import com.partnership.rfid.data.model.GetLastScan
import com.partnership.rfid.data.model.UploadData
import okhttp3.Request
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @POST("api/data")
    suspend fun uploadData(@Body request: UploadData): Response<BaseResponse>

    @GET("api/last-scan")
    suspend fun getLastScan(): Response<GetLastScan>
}