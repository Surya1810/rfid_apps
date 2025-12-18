package com.partnership.bjbdocumenttrackerreader.data.network

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
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("api/dashboard")
    suspend fun getDashboard( ): Response<BaseResponse<GetDashboard>>

    @GET("/api/scan-history")
    suspend fun getHistoriesSO(
        @Query("page") page: Int,
        @Query("category") type: String
    ): Response<BaseResponse<GetHistoriesResponse>>

    @GET("api/stock-opname/document")
    suspend fun getBulkDocument(
    ): Response<BaseResponse<GetBulkDocument>>


    @GET("api/stock-opname/agunan")
    suspend fun getBulkAgunan(): Response<BaseResponse<GetBulkDocument>>

    @POST("api/stock-opname/{type}")
    suspend fun postStockOpname(
        @Path("type")type: String,
        @Body stockOpname: PostStockOpname
    ): Response<BaseResponse<Unit>>

    @GET("api/segments")
    suspend fun getSegments(): Response<BaseResponse<List<GetListSegments>>>

    @GET("api/search")
    suspend fun getSearch(
        @Query("search") search: String? = null,
        @Query("type") type: String,
        @Query("segment") segment: String? = null,
        @Query("status") status: String? = null,
    ): Response<BaseResponse<GetBulkDocument>>

    @POST("api/search")
    suspend fun postLostDocument(
        @Body postLostDocument: PostLostDocument
    ): Response<BaseResponse<Unit>>

    @GET("api/manuals/videos")
    suspend fun getListTutorialVideo(): Response<BaseResponse<GetListTutorialVideo>>

    //lending
    @Multipart
    @POST("api/borrowed-documents")
    suspend fun borrowDocument(
        @Part("documentId") documentId: RequestBody,
        @Part("borrowerName") borrowerName: RequestBody,
        @Part("returnDate") returnDate: RequestBody,
        @Part signature: MultipartBody.Part
    ): Response<BaseResponse<Unit>>

    @Multipart
    @POST("api/borrowed-documents/{documentId}")
    suspend fun returnDocument(
        @Path("documentId") documentId: Int,
        @Part signature: MultipartBody.Part
    ): Response<BaseResponse<Unit>>

    @GET("api/borrowed-documents/{documentId}")
    suspend fun getDetailBorrowed(
        @Path("documentId") documentId: Int
    ): Response<BaseResponse<GetDetailBorrowed>>

    @GET("api/borrowed-documents/{idDocument}/history")
    suspend fun getBorrowHistory(
        @Path("idDocument") idDocument: Int
    ): Response<BaseResponse<GetHistoryBorrow>>
}