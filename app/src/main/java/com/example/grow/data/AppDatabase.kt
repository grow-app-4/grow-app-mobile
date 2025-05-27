package com.example.grow.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.grow.data.model.*

@Database(entities = [
    AnakEntity::class,
    PertumbuhanEntity::class,
    DetailPertumbuhanEntity::class,
    JenisPertumbuhanEntity::class,
    StandarPertumbuhanEntity::class,
    UserEntity::class,
    ResepEntity::class,
    BahanEntity::class,
    ResepBahanEntity::class,
    LangkahPembuatanEntity::class,
    NutrisiEntity::class,
    ResepNutrisiEntity::class], version = 11, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun anakDao(): AnakDao
    abstract fun pertumbuhanDao(): PertumbuhanDao
    abstract fun detailPertumbuhanDao(): DetailPertumbuhanDao
    abstract fun jenisPertumbuhanDao(): JenisPertumbuhanDao
    abstract fun standarPertumbuhanDao(): StandarPertumbuhanDao
    abstract fun resepDao(): ResepDao
}
