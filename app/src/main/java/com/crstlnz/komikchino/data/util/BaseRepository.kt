package com.crstlnz.komikchino.data.util

import android.util.Log
import com.crstlnz.komikchino.config.USER_DATA
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

open class BaseRepository {
    protected val listeners: MutableList<ListenerRegistration> = mutableListOf()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    protected val userData: DocumentReference =
        firestore.collection(USER_DATA).document(FirebaseAuth.getInstance().currentUser?.uid ?: "0")

    fun close() {
        for (listener in listeners) {
            try {
                listener.remove()
            } catch (e: Exception) {
                Log.d("CLOSING REPOSITORY ERROR", e.stackTraceToString())
            }
        }
    }
}