package com.partnership.bjbdocumenttrackerreader.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.partnership.bjbdocumenttrackerreader.data.local.dao.AssetDao
import com.partnership.bjbdocumenttrackerreader.data.local.entity.AssetEntity


@Database(entities = [AssetEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun assetDao(): AssetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "asset_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}