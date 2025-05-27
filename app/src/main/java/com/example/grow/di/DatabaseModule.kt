package com.example.grow.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.example.grow.data.AppDatabase
import com.example.grow.data.PertumbuhanDao
import com.example.grow.data.DetailPertumbuhanDao
import com.example.grow.data.AnakDao
import com.example.grow.data.JenisPertumbuhanDao
import com.example.grow.data.StandarPertumbuhanDao
import com.example.grow.data.UserDao
import com.example.grow.data.ResepDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        Log.d("AppDatabase", "Creating database instance")
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "grow_db"
        ).fallbackToDestructiveMigration() // nambahin ini aja
            .build()
    }

    @Provides
    fun providePertumbuhanDao(db: AppDatabase): PertumbuhanDao = db.pertumbuhanDao()

    @Provides
    fun provideAnakDao(db: AppDatabase): AnakDao = db.anakDao()

    @Provides
    fun provideDetailPertumbuhanDao(db: AppDatabase): DetailPertumbuhanDao = db.detailPertumbuhanDao()

    @Provides
    fun provideJenisPertumbuhanDao(db: AppDatabase): JenisPertumbuhanDao = db.jenisPertumbuhanDao()

    @Provides
    fun provideStandarPertumbuhanDao(db: AppDatabase): StandarPertumbuhanDao = db.standarPertumbuhanDao()

    @Provides
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()
}

    @Provides
    fun provideResepDao(db: AppDatabase): ResepDao = db.resepDao()
}