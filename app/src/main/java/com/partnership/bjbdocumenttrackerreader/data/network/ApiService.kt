package com.partnership.bjbdocumenttrackerreader.data.network

import com.partnership.bjbdocumenttrackerreader.data.model.BaseResponse
import com.partnership.bjbdocumenttrackerreader.data.model.GetBulkDocument
import com.partnership.bjbdocumenttrackerreader.data.model.GetDashboard
import com.partnership.bjbdocumenttrackerreader.data.model.GetListTutorialVideo
import com.partnership.bjbdocumenttrackerreader.data.model.PostLostDocument
import com.partnership.bjbdocumenttrackerreader.data.model.PostStockOpname
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("api/dashboard")
    suspend fun getDashboard( ): Response<BaseResponse<GetDashboard>>

    @GET("api/list-lost-document")
    suspend fun getListLostDocument(@Query("page") page: Int):Response<BaseResponse<List<String>>>

    @GET("api/stock-opname/document")
    suspend fun getBulkDocument(): Response<BaseResponse<GetBulkDocument>>

    @GET("api/stock-opname/agunan")
    suspend fun getBulkAgunan(): Response<BaseResponse<GetBulkDocument>>

    @POST("api/stock-opname/{type}")
    suspend fun postStockOpname(
        @Path("type")type: String,
        @Body stockOpname: PostStockOpname
    ): Response<BaseResponse<Unit>>

    @GET("api/search")
    suspend fun getSearch(
        @Query("search") search: String? = null,
        @Query("type") type: String
    ): Response<BaseResponse<GetBulkDocument>>

    @POST("api/search")
    suspend fun postLostDocument(
        @Body postLostDocument: PostLostDocument
    ): Response<BaseResponse<Unit>>

    @GET("api/manuals/videos")
    suspend fun getListTutorialVideo(): Response<BaseResponse<GetListTutorialVideo>>

}