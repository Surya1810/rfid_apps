package com.partnership.bjbdocumenttrackerreader.data.network

import com.partnership.bjbdocumenttrackerreader.data.model.GetDashboard
import com.partnership.bjbdocumenttrackerreader.data.model.GetSearchAgunan
import com.partnership.bjbdocumenttrackerreader.data.model.GetSearchDocument
import com.partnership.bjbdocumenttrackerreader.data.model.ItemStatus
import com.partnership.bjbdocumenttrackerreader.data.model.BaseResponse
import com.partnership.bjbdocumenttrackerreader.data.model.Data
import com.partnership.bjbdocumenttrackerreader.data.model.DetailAgunan
import com.partnership.bjbdocumenttrackerreader.data.model.DocumentDetail
import com.partnership.rfid.data.model.UploadData
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @POST("api/scan-document")
    suspend fun uploadDataDocument(@Body request: UploadData): Response<BaseResponse<Unit>>

    @POST("api/scan-agunan")
    suspend fun uploadDataAgunan(@Body request: UploadData): Response<BaseResponse<Unit>>

    @GET("api/dashboard")
    suspend fun getDashboard( ): Response<BaseResponse<GetDashboard>>

    @GET("api/list-lost-document")
    suspend fun getListLostDocument(@Query("page") page: Int):Response<BaseResponse<List<String>>>

    @GET("api/document-lost")
    suspend fun getDocumentLost(): Response<BaseResponse<List<ItemStatus>>>

    @POST("api/document-lost")
    suspend fun postDocumentLost(@Body request: Data): Response<BaseResponse<Unit>>

    @GET("api/agunan-lost")
    suspend fun getAgunanLost(): Response<BaseResponse<List<ItemStatus>>>

    @POST("api/agunan-lost")
    suspend fun postAgunanLost(@Body request: Data): Response<BaseResponse<Unit>>

    @GET("api/search-document")
    suspend fun getSearchDocument(
        @Query("search") search: String?
    ): Response<BaseResponse<List<DocumentDetail>>>

    @POST("api/search-document")
    suspend fun postSearchDocument(@Body request: Data): Response<BaseResponse<Unit>>

    @GET("api/search-agunan")
    suspend fun getSearchAgunan(
        @Query("search") search: String?
    ): Response<BaseResponse<List<DetailAgunan>>>

    @POST("api/search-agunan")
    suspend fun postSearchAgunan(@Body request: Data): Response<BaseResponse<Unit>>

}