package com.crstlnz.komikchino.data.firebase.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.crstlnz.komikchino.config.CHAPTER
import com.crstlnz.komikchino.config.KOMIK
import com.crstlnz.komikchino.config.SERVER
import com.crstlnz.komikchino.data.api.KomikServer
import com.crstlnz.komikchino.data.firebase.model.ChapterHistoryItem
import com.crstlnz.komikchino.data.firebase.model.KomikHistoryItem
import com.crstlnz.komikchino.data.firebase.model.KomikReadHistory
import com.crstlnz.komikchino.data.util.BaseFirebaseRepository
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class KomikHistoryRepository(private val databaseKey: KomikServer) : BaseFirebaseRepository() {
    private val komikCollection =
        userData.collection(SERVER).document(databaseKey.value).collection(KOMIK)

    private val chapterCollection =
        userData.collection(SERVER).document(databaseKey.value).collection(CHAPTER)

    suspend fun getKomikHistories(): List<KomikHistoryItem> {
        val snapshot = komikCollection.get().await()
        if (snapshot.isEmpty) return emptyList()
        return snapshot.documents.mapNotNull {
            try {
                it.toObject(KomikHistoryItem::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }

    fun readHistories(): LiveData<List<KomikHistoryItem>> {
        val komikLiveData = MutableLiveData<List<KomikHistoryItem>>()
        val listenerRegistration = komikCollection.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                // Handle error
                // You might want to set an appropriate error state in LiveData here
                return@addSnapshotListener
            }
            val komikItems = snapshot?.documents?.mapNotNull { doc ->
                try {
                    doc.toObject(KomikHistoryItem::class.java)
                } catch (e: Exception) {
                    Log.d("FIREBASE PARSE ERROR", e.stackTraceToString())
                    null
                }
            } ?: emptyList()
            komikLiveData.value = komikItems
        }
        listeners.add(listenerRegistration)
        return komikLiveData
    }

    suspend fun getHistories(): List<KomikReadHistory> {
        val snapshot = komikCollection.get().await()
        val result = arrayListOf<KomikReadHistory>()
        if (snapshot.isEmpty) return emptyList()

        val komiks = snapshot.documents.mapNotNull {
            try {
                it.toObject(KomikHistoryItem::class.java)
            } catch (e: Exception) {
                null
            }
        }

        for (komik in komiks) {
            result.add(
                KomikReadHistory(
                    komik = komik,
                    chapter = try {
                        chapterCollection.whereEqualTo("mangaId", komik.id)
                            .orderBy("updatedAt", Query.Direction.DESCENDING).limit(1)
                            .get().await().documents.getOrNull(0)
                            ?.toObject(ChapterHistoryItem::class.java)
                    } catch (e: Exception) {
                        Log.d("FIREBASE PARSE ERROR", e.stackTraceToString())
                        null
                    }
                )
            )
        }
        return result
    }

    suspend fun add(komikHistory: KomikHistoryItem) {
        try {
            val snapshot = komikCollection.document(komikHistory.id).get()
            if (snapshot.isSuccessful && snapshot.result.exists()) {
                try {
                    val komik = snapshot.result.toObject(KomikHistoryItem::class.java)
                    if (komik != null) {
                        komikCollection.document(komikHistory.id)
                            .set(komikHistory.copy(createdAt = komik.createdAt)).await()
                        return
                    }
                } catch (e: Exception) {
                    Log.d("FIREBASE ERROR", e.stackTraceToString())
                }
            }
            komikCollection.document(komikHistory.id).set(komikHistory).await()
        } catch (e: Exception) {
            Log.d("FIREBASE ERROR", e.stackTraceToString())
        }
    }

    suspend fun delete(id: String) {
        try {
            komikCollection.document(id).delete().await()
        } catch (e: Exception) {
            Log.d("FIREBASE ERROR", e.stackTraceToString())
        }
    }

}