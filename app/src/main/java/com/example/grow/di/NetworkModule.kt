package com.example.grow.di

import com.example.grow.remote.AsupanApiService
import com.example.grow.remote.KehamilanApiService
import com.example.grow.remote.Makanan2ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://559d-180-244-129-220.ngrok-free.app/api/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideKehamilanApiService(retrofit: Retrofit): KehamilanApiService {
        return retrofit.create(KehamilanApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideAsupanApiService(retrofit: Retrofit): AsupanApiService {
        return retrofit.create(AsupanApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideMakanan2ApiService(retrofit: Retrofit): Makanan2ApiService {
        return retrofit.create(Makanan2ApiService::class.java)
    }

}
