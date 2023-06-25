package com.crstlnz.komikchino.data.database.chapterhistory

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "readhistory", indices = [Index(value = ["id"], unique = true)])
data class ChapterHistoryItem(
    @ColumnInfo(name = "_id") @PrimaryKey(autoGenerate = true) val data_id: Long = 0L,
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "manga_id") var mangaId: String,
    @ColumnInfo(name = "title") var title: String,
    @ColumnInfo(name = "slug") var slug: String,
    @ColumnInfo(name = "created_at") var createdAt: Long = System.currentTimeMillis()
)