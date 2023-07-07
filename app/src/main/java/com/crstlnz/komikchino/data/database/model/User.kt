package com.crstlnz.komikchino.data.database.model

import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId val id: String,
    val email: String,
    val name: String,
    val img: String,
)