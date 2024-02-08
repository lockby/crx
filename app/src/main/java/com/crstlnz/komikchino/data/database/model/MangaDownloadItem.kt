package com.crstlnz.komikchino.data.database.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "mangaDownload", indices = [Index(value = ["id"], unique = true)])
data class MangaDownloadItem(
    @PrimaryKey(autoGenerate = false) val id: String = "0",
    var title: String = "",
    var img: String = "",
    var slug: String = "0",
    var description: String = "",
    var type: String = "",
    var createdAt: Long = System.currentTimeMillis(),
)

