package com.example.grow.data.remote

import com.example.grow.data.model.ApiResponse
import com.example.grow.data.model.CatatanKehamilan
import com.example.grow.data.model.KehamilanWithCatatan
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface CatatanKehamilanApiService {
    @POST("catatan-kehamilan")
    suspend fun postCatatanKehamilan(@Body catatan: CatatanKehamilan): Response<ResponseBody>

    @GET("catatan-kehamilan/user/{user_id}")
    suspend fun getKehamilanByUserId(@Path("user_id") userId: Int): ApiResponse<KehamilanWithCatatan>
}
