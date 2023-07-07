package com.crstlnz.komikchino.data.database.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.crstlnz.komikchino.config.FAVORITES
import com.crstlnz.komikchino.config.SERVER
import com.crstlnz.komikchino.data.api.KomikServer
import com.crstlnz.komikchino.data.database.model.FavoriteKomikItem
import com.crstlnz.komikchino.data.util.BaseRepository
import kotlinx.coroutines.tasks.await

class FavoriteKomikRepository(
    val databaseKey: KomikServer,
) : BaseRepository() {
    private val favoriteCollection =
        userData.collection(SERVER).document(databaseKey.value).collection(FAVORITES)

    suspend fun get(id: String): FavoriteKomikItem? {
        return try {
            favoriteCollection.document(id).get().await()
                .toObject(FavoriteKomikItem::class.java)
        } catch (e: Exception) {
            Log.d("FIREBASE ERROR", e.stackTraceToString())
            null
        }
    }

    suspend fun add(favorite: FavoriteKomikItem) {
        try {
            favoriteCollection.document(favorite.id).set(favorite).await()
        } catch (e: Exception) {
            Log.d("FIREBASE ERROR", e.stackTraceToString())
        }
    }

    suspend fun delete(id: String) {
        try {
            favoriteCollection.document(id).delete().await()
        } catch (e: Exception) {
            Log.d("FIREBASE ERROR", e.stackTraceToString())
        }
    }

    fun isFavorite(id: String): LiveData<Int> {
        val isFavorite = MutableLiveData<Int>()
        val listenerRegistration =
            favoriteCollection.document(id.ifEmpty { "0" })
                .addSnapshotListener { snapshot, exception ->
                    if (exception != null) {
                        isFavorite.value = 0
                        return@addSnapshotListener
                    }
                    if (snapshot?.exists() == true) {
                        isFavorite.value = 1
                    } else {
                        isFavorite.value = 0
                    }
                }

        listeners.add(listenerRegistration)
        return isFavorite
    }

    fun getAll(): LiveData<List<FavoriteKomikItem>> {
        val favoriteLiveData = MutableLiveData<List<FavoriteKomikItem>>()
        val listenerRegistration = favoriteCollection.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                // Handle error
                // You might want to set an appropriate error state in LiveData here
                return@addSnapshotListener
            }
            val komikItems = snapshot?.documents?.mapNotNull { doc ->
                try {
                    doc.toObject(FavoriteKomikItem::class.java)
                } catch (e: Exception) {
                    Log.d("FIREBASE PARSE ERROR", e.stackTraceToString())
                    null
                }
            } ?: emptyList()
            favoriteLiveData.value = komikItems
        }
        listeners.add(listenerRegistration)
        return favoriteLiveData
    }
}