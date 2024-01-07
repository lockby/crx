package com.crstlnz.komikchino.data.firebase.model

data class KomikReadHistory(
    val komik: KomikHistoryItem,
    val chapter: ChapterHistoryItem?,
)