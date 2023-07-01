package com.crstlnz.komikchino.ui.screens.home.fragments.bookmark


import android.util.Log
import androidx.lifecycle.ViewModel
import com.crstlnz.komikchino.data.database.favorite.FavoriteKomikRepository
import com.crstlnz.komikchino.data.database.komik.KomikHistoryRepository
import com.crstlnz.komikchino.data.model.ChapterScrollPostition
import com.crstlnz.komikchino.data.model.SearchHistoryModel
import com.crstlnz.komikchino.data.util.StorageHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class BookmarkViewModel @Inject constructor(
    historyRepository: KomikHistoryRepository,
    favoriteRepository: FavoriteKomikRepository,
    @Named("chapterScrollPostitionCache") private val chapterScrollPostition: StorageHelper<ChapterScrollPostition>,
) : ViewModel() {
    val histories = historyRepository.histories
    val favorites = favoriteRepository.favorites

    fun getChapterScrollPosition(mangaId: String, chapterId: String): ChapterScrollPostition? {
        val pos = chapterScrollPostition.get<ChapterScrollPostition>(mangaId)
        return if (pos != null && pos.chapterId == chapterId) pos else null
    }
}