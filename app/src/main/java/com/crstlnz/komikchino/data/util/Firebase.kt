package com.crstlnz.komikchino.data.util

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseInitializer {
    fun initialize(context: Context) {
        FirebaseApp.initializeApp(context)
    }

    fun getFirestoreInstance(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }
}