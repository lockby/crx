package com.crstlnz.komikchino.data.database.model

import com.google.firebase.firestore.DocumentId

data class KomikHistoryItem(
    @DocumentId val id: String = "0", // pake string karna mau multi server
    var title: String = "",
    var img: String = "",
    var slug: String = "",
    var description: String = "",
    var type: String = "",
    var createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis()
)