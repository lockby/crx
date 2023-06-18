package com.crstlnz.komikchino.data.database.readhistory.model

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ReadHistoryDao {
    @Query("SELECT * from readhistory")
    fun getAll(): LiveData<List<ReadHistoryItem>>

    @Query("SELECT * from readhistory where chapter_id = :id")
    fun getById(id: Long): ReadHistoryItem?

    @Query("SELECT * from readhistory where manga_id = :id")
    fun getKomikHistory(id: Long): LiveData<List<ReadHistoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ReadHistoryItem)
//
//    @Update
//    suspend fun update(item: ReadHistoryItem)

    @Delete
    suspend fun delete(item: ReadHistoryItem)

    @Query("DELETE FROM readhistory")
    suspend fun deleteAll()
}