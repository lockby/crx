package com.crstlnz.komikchino.data.firebase.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.crstlnz.komikchino.config.CHAPTER
import com.crstlnz.komikchino.config.SERVER
import com.crstlnz.komikchino.data.api.KomikServer
import com.crstlnz.komikchino.data.firebase.model.ChapterHistoryItem
import com.crstlnz.komikchino.data.firebase.model.KomikHistoryItem
import com.crstlnz.komikchino.data.util.BaseFirebaseRepository
import kotlinx.coroutines.tasks.await

class ChapterHistoryRepository(private val databaseKey: KomikServer) : BaseFirebaseRepository() {
    private val chapterCollection =
        userData.collection(SERVER).document(databaseKey.value).collection(CHAPTER)

    fun readChapterHistory(id: String): LiveData<List<ChapterHistoryItem>> {
        val komikItemsLiveData = MutableLiveData<List<ChapterHistoryItem>>()
        val listenerRegistration = chapterCollection.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                // Handle error
                // You might want to set an appropriate error state in LiveData here
                return@addSnapshotListener
            }
            val komikItems = snapshot?.documents?.mapNotNull { doc ->
                try {
                    doc.toObject(ChapterHistoryItem::class.java)
                } catch (e: Exception) {
                    Log.d("FIREBASE PARSE ERROR", e.stackTraceToString())
                    null
                }
            } ?: emptyList()
            komikItemsLiveData.value = komikItems
        }
        listeners.add(listenerRegistration)
        return komikItemsLiveData
    }

    suspend fun get(id: String): ChapterHistoryItem? {
        return try {
            chapterCollection.document(id.ifEmpty { "0" }).get().await()
                .toObject(ChapterHistoryItem::class.java)
        } catch (e: Exception) {
            Log.d("FIREBASE ERROR", e.stackTraceToString())
            null
        }
    }

    suspend fun delete(id: String) {
        try {
            chapterCollection.document(id).delete().await()
        } catch (e: Exception) {
            Log.d("FIREBASE ERROR", e.stackTraceToString())
        }
    }

    suspend fun add(history: ChapterHistoryItem) {
        try {
            val snapshot = chapterCollection.document(history.id).get()
            if (snapshot.isSuccessful && snapshot.result.exists()) {
                try {
                    val komik = snapshot.result.toObject(KomikHistoryItem::class.java)
                    if (komik != null) {
                        chapterCollection.document(history.id)
                            .set(history.copy(createdAt = komik.createdAt)).await()
                        return
                    }
                } catch (e: Exception) {
                    Log.d("FIREBASE ERROR", e.stackTraceToString())
                }
            }
            chapterCollection.document(history.id).set(history).await()
        } catch (e: Exception) {
            Log.d("FIREBASE ERROR", e.stackTraceToString())
        }
    }

}