package com.crstlnz.komikchino.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.crstlnz.komikchino.data.database.readhistory.model.ReadHistoryDao
import com.crstlnz.komikchino.data.database.readhistory.model.ReadHistoryItem

@Database(entities = [ReadHistoryItem::class], version = 1, exportSchema = false)
abstract class KomikDatabase : RoomDatabase() {
    abstract fun getReadHistoryDao(): ReadHistoryDao

    companion object {
        private var INSTANCE: KomikDatabase? = null
        fun getInstance(context: Context): KomikDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        KomikDatabase::class.java,
                        "komik_database"
                    ).fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}