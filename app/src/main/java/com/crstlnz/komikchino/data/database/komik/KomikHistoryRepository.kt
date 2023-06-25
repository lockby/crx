package com.crstlnz.komikchino.data.database.komik

import androidx.lifecycle.LiveData

class KomikHistoryRepository(private val komikDao: KomikHistoryDao) {
    val histories: LiveData<List<KomikReadHistory>> = komikDao.readHistory()
    suspend fun add(komikHistory: KomikHistoryItem) {
        komikDao.insert(komikHistory)
    }
}