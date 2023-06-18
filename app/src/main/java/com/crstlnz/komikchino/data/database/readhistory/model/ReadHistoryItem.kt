package com.crstlnz.komikchino.data.database.readhistory.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "readhistory")
data class ReadHistoryItem(
    @ColumnInfo(name = "chapter_id") @PrimaryKey(autoGenerate = false) val chapterId: Int,
    @ColumnInfo(name = "manga_id") var mangaId: Int,
    @ColumnInfo(name = "visible_index") var firstVisibleItemIndex: Int,
    @ColumnInfo(name = "visible_offset") var firstVisibleItemScrollOffset: Int,
    @ColumnInfo(name = "created_at") var createdAt: Long = System.currentTimeMillis()
)