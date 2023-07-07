package com.crstlnz.komikchino.data.database.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.crstlnz.komikchino.config.CHAPTER
import com.crstlnz.komikchino.config.KOMIK
import com.crstlnz.komikchino.config.SERVER
import com.crstlnz.komikchino.data.api.KomikServer
import com.crstlnz.komikchino.data.database.model.ChapterHistoryItem
import com.crstlnz.komikchino.data.database.model.KomikHistoryItem
import com.crstlnz.komikchino.data.database.model.KomikReadHistory
import com.crstlnz.komikchino.data.util.BaseRepository
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class KomikHistoryRepository(private val databaseKey: KomikServer) : BaseRepository() {
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

    fun readHistories(): LiveData<List<KomikReadHistory>> {
        val komikItemsLiveData = MutableLiveData<List<KomikReadHistory>>()
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
            komikItemsLiveData.value =
                komikItems.map { KomikReadHistory(komik = it, chapter = null) }
        }
        listeners.add(listenerRegistration)
        return komikItemsLiveData
    }

    suspend fun add(komikHistory: KomikHistoryItem) {
        try {
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