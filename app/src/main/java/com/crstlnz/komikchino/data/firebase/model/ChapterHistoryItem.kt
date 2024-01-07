package com.crstlnz.komikchino.data.firebase.model

import com.google.firebase.firestore.DocumentId

data class ChapterHistoryItem(
    @DocumentId val id: String = "0",
    var mangaId: String = "",
    var title: String = "",
    var slug: String = "",
    var createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis()
)