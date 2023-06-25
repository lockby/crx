package com.crstlnz.komikchino.data.database.favorite

import androidx.lifecycle.LiveData

class FavoriteKomikRepository(private val favoriteDao: FavoriteKomikDao) {
    val favorites: LiveData<List<FavoriteKomikItem>> = favoriteDao.readFavorites()
    suspend fun add(komikHistory: FavoriteKomikItem) {
        favoriteDao.insert(komikHistory)
    }

    suspend fun delete(komikHistory: FavoriteKomikItem) {
        favoriteDao.delete(komikHistory)
    }

    suspend fun deleteById(id: String) {
        favoriteDao.deleteById(id)
    }

    fun isFavorite(id: String): LiveData<Int> {
        return favoriteDao.readFavorite(id)
    }
}