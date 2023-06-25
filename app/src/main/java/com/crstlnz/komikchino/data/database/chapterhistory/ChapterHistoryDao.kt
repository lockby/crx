package com.crstlnz.komikchino.data.database.chapterhistory

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ChapterHistoryDao {
    @Query("SELECT * from readhistory")
    fun getAll(): LiveData<List<ChapterHistoryItem>>

    @Query("SELECT * from readhistory where manga_id = :id")
    fun readChapterHistory(id: String): LiveData<List<ChapterHistoryItem>>

    @Query("SELECT * from readhistory where id = :id")
    suspend fun getById(id: String): ChapterHistoryItem?

    @Query("SELECT * from readhistory where manga_id = :id")
    suspend fun getChapterHistory(id: String): List<ChapterHistoryItem>

//    @Transaction
//    @Query("SELECT * FROM readhistory")
//    fun getReadHistoryKomik(): LiveData<List<pReadHistoryKomik>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ChapterHistoryItem)
//
//    @Update
//    suspend fun update(item: ReadHistoryItem)

    @Delete
    suspend fun delete(item: ChapterHistoryItem)

    @Query("DELETE FROM readhistory")
    suspend fun deleteAll()
}