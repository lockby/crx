package com.crstlnz.komikchino.data.database.chapterhistory

import androidx.lifecycle.LiveData

class ChapterHistoryRepository(private val chapterHistoryDao: ChapterHistoryDao) {
    fun readChapterHistory(id: String): LiveData<List<ChapterHistoryItem>> {
        return chapterHistoryDao.readChapterHistory(id)
    }

    suspend fun get(id: String): ChapterHistoryItem? {
        return chapterHistoryDao.getById(id)
    }

    suspend fun add(history: ChapterHistoryItem) {
        chapterHistoryDao.insert(history.apply {
            createdAt = System.currentTimeMillis()
        })
    }
}