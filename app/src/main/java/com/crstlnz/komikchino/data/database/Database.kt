package com.crstlnz.komikchino.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.crstlnz.komikchino.data.api.KomikServer
import com.crstlnz.komikchino.data.database.dao.ChapterDownloadDao
import com.crstlnz.komikchino.data.database.dao.MangaDownloadDao
import com.crstlnz.komikchino.data.database.model.ChapterDownloadItem
import com.crstlnz.komikchino.data.database.model.ChapterImages
import com.crstlnz.komikchino.data.database.model.DisqusConfigConverter
import com.crstlnz.komikchino.data.database.model.MangaDownloadItem

@Database(
    entities = [MangaDownloadItem::class, ChapterDownloadItem::class, ChapterImages::class],
    version = 3,
    exportSchema = true
)
abstract class KomikDatabase() : RoomDatabase() {
    abstract fun getMangaDownloadDao(): MangaDownloadDao
    abstract fun getChapterDownloadDao(): ChapterDownloadDao

    companion object {
        private var INSTANCE: KomikDatabase? = null
        fun getInstance(context: Context, server: KomikServer): KomikDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        KomikDatabase::class.java,
                        "${server.value}-komik_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}