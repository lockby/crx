package com.crstlnz.komikchino.data.firebase.model

import com.google.firebase.firestore.DocumentId

data class FavoriteKomikItem(
    @DocumentId var id: String = "0",
    var title: String = "",
    var img: String = "",
    var slug: String = "",
    var description: String = "",
    var type: String = "",
    var createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis()
)