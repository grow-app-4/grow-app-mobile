package com.example.grow.data.remote

import com.example.grow.data.model.ApiResponse
import com.example.grow.data.model.CatatanKehamilan
import com.example.grow.data.model.KehamilanWithCatatan
import com.example.grow.data.model.StatusUpdateRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface CatatanKehamilanApiService {
    @POST("catatan-kehamilan")
    suspend fun postCatatanKehamilan(@Body catatan: CatatanKehamilan): Response<ResponseBody>

    @GET("catatan-kehamilan/user/{user_id}")
    suspend fun getKehamilanByUserId(@Path("user_id") userId: Int): ApiResponse<KehamilanWithCatatan>

    @DELETE("catatan-kehamilan/{id_kehamilan}/{tanggal}")
    suspend fun deleteCatatanKehamilan(
        @Path("id_kehamilan") idKehamilan: Int,
        @Path("tanggal") tanggal: String
    ): Response<ResponseBody>

    @PUT("catatan-kehamilan/{id_kehamilan}/{tanggal}")
    suspend fun updateCatatanKehamilan(
        @Path("id_kehamilan") idKehamilan: Int,
        @Path("tanggal") tanggal: String,
        @Body catatan: CatatanKehamilan
    ): Response<ApiResponse<CatatanKehamilan>>

    @PUT("kehamilan/{id}/status")
    suspend fun updateStatusKehamilan(
        @Path("id") id: Int,
        @Body statusBody: StatusUpdateRequest
    ): Response<ResponseBody>
}
