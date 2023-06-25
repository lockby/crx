package com.crstlnz.komikchino.ui.screens.home.fragments.bookmark


import androidx.lifecycle.ViewModel
import com.crstlnz.komikchino.data.database.favorite.FavoriteKomikRepository
import com.crstlnz.komikchino.data.database.komik.KomikHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BookmarkViewModel @Inject constructor(
    historyRepository: KomikHistoryRepository,
    favoriteRepository: FavoriteKomikRepository
) : ViewModel() {
    val histories = historyRepository.histories
    val favorites = favoriteRepository.favorites
}