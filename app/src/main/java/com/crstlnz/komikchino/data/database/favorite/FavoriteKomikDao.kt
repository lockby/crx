package com.crstlnz.komikchino.data.database.favorite

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface FavoriteKomikDao {
    @Transaction
    @Query("SELECT * from favorite_komik ORDER BY _id DESC")
    fun readFavorites(): LiveData<List<FavoriteKomikItem>>

    @Transaction
    @Query("SELECT EXISTS(SELECT :id from favorite_komik where id = :id)")
    fun readFavorite(id: String): LiveData<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(komik: FavoriteKomikItem)

    @Delete
    suspend fun delete(komik: FavoriteKomikItem)

    @Query("DELETE FROM favorite_komik WHERE id = :id")
    suspend fun deleteById(id: String)
}