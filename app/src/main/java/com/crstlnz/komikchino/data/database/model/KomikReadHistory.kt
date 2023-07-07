package com.crstlnz.komikchino.data.database.model

data class KomikReadHistory(
    val komik: KomikHistoryItem,
    val chapter: ChapterHistoryItem?,
)