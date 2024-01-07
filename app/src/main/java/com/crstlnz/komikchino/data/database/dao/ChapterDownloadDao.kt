package com.crstlnz.komikchino.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.crstlnz.komikchino.data.database.model.ChapterDownloadItem

@Dao
interface ChapterDownloadDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(manga: ChapterDownloadItem)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<ChapterDownloadItem>)
}