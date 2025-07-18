package com.example.grow.data.remote

import com.example.grow.data.model.AnalisisAsupanRequest
import com.example.grow.data.model.CheckAsupanResponse
import com.example.grow.data.model.MakananIbu
import com.example.grow.data.model.AsupanAsi
import com.example.grow.data.model.NutrisiAnalisisResponse
import com.example.grow.data.model.ResepRekomendasiResponse
import com.example.grow.data.model.StandarNutrisi
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.Call

interface AsupanApiService {
    @POST("analisis-asupan")
    suspend fun analisisAsupan(
        @Body request: AnalisisAsupanRequest
    ): NutrisiAnalisisResponse

    @GET("makanan-ibu/{id_user}")
    suspend fun getMakananIbu(
        @Path("id_user") userId: Int,
        @Query("tanggal") tanggal: String
    ): List<MakananIbu>

    @GET("standar-nutrisi-by-rentang")
    suspend fun getStandarNutrisiByRentang(
        @Query("rentang") rentang: String,
        @Query("kategori") kategori: String
    ): List<StandarNutrisi>

    @GET("asupan/check")
    suspend fun checkAsupan(
        @Query("userId") userId: Int,
        @Query("tanggal") tanggal: String
    ): CheckAsupanResponse

    @POST("makanan-anak/inputAsupan")
    suspend fun inputAsupanAsi(
        @Body asupanAsi: AsupanAsi
    ): Response<Unit>

    @GET("makanan-anak/{idAnak}")
    suspend fun getAsupanByAnakAndTanggal(
        @Path("idAnak") idAnak: Int,
        @Query("tanggal") tanggal: String
    ): Response<AsupanAsi?>

    @POST("makanan-anak/getResep")
    fun getResep(@Body body: Map<String, String>): Call<ResepRekomendasiResponse>
}