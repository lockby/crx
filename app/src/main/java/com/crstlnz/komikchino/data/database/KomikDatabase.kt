package com.crstlnz.komikchino.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.crstlnz.komikchino.data.database.chapterhistory.ChapterHistoryDao
import com.crstlnz.komikchino.data.database.chapterhistory.ChapterHistoryItem
import com.crstlnz.komikchino.data.database.favorite.FavoriteKomikDao
import com.crstlnz.komikchino.data.database.favorite.FavoriteKomikItem
import com.crstlnz.komikchino.data.database.komik.KomikHistoryDao
import com.crstlnz.komikchino.data.database.komik.KomikHistoryItem
import com.crstlnz.komikchino.data.datastore.KomikServer

@Database(
    entities = [ChapterHistoryItem::class, KomikHistoryItem::class, FavoriteKomikItem::class],
    version = 1,
    exportSchema = true
)
abstract class KomikDatabase() : RoomDatabase() {
    abstract fun getChapterHistoryDao(): ChapterHistoryDao
    abstract fun getKomikHistoryDao(): KomikHistoryDao
    abstract fun getFavoriteKomikDao(): FavoriteKomikDao

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
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}


