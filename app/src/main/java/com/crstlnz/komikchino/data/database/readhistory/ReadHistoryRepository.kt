package com.crstlnz.komikchino.data.database.readhistory

import androidx.lifecycle.LiveData
import com.crstlnz.komikchino.data.database.readhistory.model.ReadHistoryDao
import com.crstlnz.komikchino.data.database.readhistory.model.ReadHistoryItem

class ReadHistoryRepository(private val readHistoryDao: ReadHistoryDao) {
    val readAllData: LiveData<List<ReadHistoryItem>> = readHistoryDao.getAll()
    fun readKomikHistory(id: Int): LiveData<List<ReadHistoryItem>> {
        return readHistoryDao.getKomikHistory(id.toLong())
    }

    suspend fun get(id: Int): ReadHistoryItem? {
        return readHistoryDao.getById(id.toLong())
    }

    suspend fun add(history: ReadHistoryItem) {
        readHistoryDao.insert(history.apply {
            createdAt = System.currentTimeMillis()
        })
    }

    suspend fun delete(history: ReadHistoryItem) {
        readHistoryDao.delete(history)
    }

    suspend fun deleteAll() {
        readHistoryDao.deleteAll()
    }
}