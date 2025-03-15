package com.partnership.bjbdocumenttrackerreader.data.network

import com.partnership.bjbdocumenttrackerreader.data.model.GetDashboard
import com.partnership.rfid.data.model.BaseResponse
import com.partnership.rfid.data.model.GetLastScan
import com.partnership.rfid.data.model.UploadData
import okhttp3.Request
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @POST("api/scan-document")
    suspend fun uploadDataDocument(@Body request: UploadData): Response<BaseResponse<Unit>>

    @POST("api/scan-agunan")
    suspend fun uploadDataAgunan(@Body request: UploadData): Response<BaseResponse<Unit>>

    @GET("api/dashboard")
    suspend fun getDashboard(): Response<BaseResponse<GetDashboard>>
}