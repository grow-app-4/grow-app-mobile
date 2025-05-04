package com.example.grow.di

import com.example.grow.remote.AnakApiService
import com.example.grow.remote.MakananAnakApiService
import com.example.grow.remote.MakananApiService
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
            .baseUrl("https://85f2-180-244-132-62.ngrok-free.app/api/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideMakananApiService(retrofit: Retrofit): MakananApiService {
        return retrofit.create(MakananApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideAnakApiService(retrofit: Retrofit): AnakApiService {
        return retrofit.create(AnakApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideMakananAnakApiService(retrofit: Retrofit): MakananAnakApiService {
        return retrofit.create(MakananAnakApiService::class.java)
    }
}
