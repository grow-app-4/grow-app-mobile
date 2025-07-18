package com.example.grow.di

import android.content.Context
import android.content.SharedPreferences
import com.example.grow.data.api.PertumbuhanApiService
import com.example.grow.data.remote.AnakApiService
import com.example.grow.data.remote.AuthApiService
import com.example.grow.data.remote.AsupanApiService
import com.example.grow.data.remote.CatatanKehamilanApiService
import com.example.grow.data.remote.KehamilanApiService
import com.example.grow.data.remote.MakananApiService
import com.example.grow.data.remote.UserApiService
import com.example.grow.data.remote.ResepApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
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
            .baseUrl("https://ec23-103-147-8-80.ngrok-free.app/api/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAnakApiService(retrofit: Retrofit): AnakApiService {
        return retrofit.create(AnakApiService::class.java)
    }

    @Provides
    @Singleton
    fun providePertumbuhanApiService(retrofit: Retrofit): PertumbuhanApiService {
        return retrofit.create(PertumbuhanApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideKehamilanApiService(retrofit: Retrofit): KehamilanApiService {
        return retrofit.create(KehamilanApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideCatatanKehamilanApiService(retrofit: Retrofit): CatatanKehamilanApiService {
        return retrofit.create(CatatanKehamilanApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideUserApiService(retrofit: Retrofit): UserApiService {
        return retrofit.create(UserApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideAsupanApiService(retrofit: Retrofit): AsupanApiService {
        return retrofit.create(AsupanApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideMakananApiService(retrofit: Retrofit): MakananApiService {
        return retrofit.create(MakananApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideResepApiService(retrofit: Retrofit): ResepApiService {
        return retrofit.create(ResepApiService::class.java)
    }

    @Provides
    @Singleton
    @Named("BookmarkPrefs")
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("bookmark_prefs", Context.MODE_PRIVATE)
    }

}