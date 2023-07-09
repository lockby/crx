package com.crstlnz.komikchino.data.database.model

import com.crstlnz.komikchino.data.util.getCurrentDateString
import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId val id: String = "0",
    val email: String = "",
    val name: String = "",
    val img: String = "",
    val appVersion: String = "",
    val created_at: Long = System.currentTimeMillis(),
    val last_online: String = getCurrentDateString(),
)