package com.example.grow.data.remote

import com.example.grow.data.model.Anak
import com.example.grow.data.model.AnakRequest
import com.example.grow.data.model.AnakResponse
import com.example.grow.data.model.AnakSingleResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.DELETE
import retrofit2.http.Path
import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.Part

interface AnakApiService {
    @GET("anak")
    suspend fun getAllAnak(): Response<AnakResponse>

    @GET("anak/{id}")
    suspend fun getAnakById(@Path("id") id: Int): Response<Anak>

    @POST("anak")
    suspend fun createAnak(@Body anak: HashMap<String, Any>): Response<AnakResponse>

    @POST("anak")
    suspend fun createAnak(@Body anakRequest: AnakRequest): Response<AnakSingleResponse>

    @PUT("anak/{id}")
    suspend fun updateAnak(@Path("id") id: Int, @Body anak: HashMap<String, Any>): Response<AnakSingleResponse>

    @PUT("anak/{id}")
    suspend fun updateAnak2(
        @Path("id") id: Int,
        @Body anakRequest: AnakRequest
    ): Response<AnakSingleResponse>

    @Multipart
    @PUT("anak/{id}")
    suspend fun updateAnak3(
        @Path("id") id: Int,
        @Part("id_user") idUser: RequestBody,
        @Part("nama_anak") namaAnak: RequestBody,
        @Part("jenis_kelamin") jenisKelamin: RequestBody,
        @Part("tanggal_lahir") tanggalLahir: RequestBody,
        @Part profilePhoto: MultipartBody.Part? = null
    ): Response<AnakSingleResponse>

    @DELETE("anak/{id}")
    suspend fun deleteAnak(@Path("id") id: Int): Response<AnakResponse>
}
