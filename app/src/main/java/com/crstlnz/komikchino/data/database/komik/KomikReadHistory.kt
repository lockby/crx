package com.crstlnz.komikchino.data.database.komik

import androidx.room.Embedded
import androidx.room.Relation
import com.crstlnz.komikchino.data.database.chapterhistory.ChapterHistoryItem

data class KomikReadHistory(
    @Embedded val komik: KomikHistoryItem,
    @Relation(
        parentColumn = "id",
        entityColumn = "manga_id"
    )
    val chapter: ChapterHistoryItem?,
)