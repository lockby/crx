package com.crstlnz.komikchino.data.database.komik

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface KomikHistoryDao {
    @Transaction
    @Query("SELECT * from komik")
    fun readHistory(): LiveData<List<KomikReadHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(komik: KomikHistoryItem)
}