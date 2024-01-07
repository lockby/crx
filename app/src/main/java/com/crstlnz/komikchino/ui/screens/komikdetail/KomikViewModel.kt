package com.crstlnz.komikchino.ui.screens.komikdetail

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.crstlnz.komikchino.data.api.ScraperBase
import com.crstlnz.komikchino.data.firebase.model.ChapterHistoryItem
import com.crstlnz.komikchino.data.firebase.model.FavoriteKomikItem
import com.crstlnz.komikchino.data.firebase.repository.ChapterHistoryRepository
import com.crstlnz.komikchino.data.firebase.repository.FavoriteKomikRepository
import com.crstlnz.komikchino.data.datastore.Settings
import com.crstlnz.komikchino.data.model.DataState.Idle.getDataOrNull
import com.crstlnz.komikchino.data.model.KomikDetail
import com.crstlnz.komikchino.data.util.StorageHelper
import com.crstlnz.komikchino.ui.util.ScraperViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.URLDecoder
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class KomikViewModel @Inject constructor(
    @Named("komikCache") storage: StorageHelper<KomikDetail>,
    savedStateHandle: SavedStateHandle,
    private val chapterRepository: ChapterHistoryRepository,
    private val favoriteKomikRepository: FavoriteKomikRepository,
    val settings: Settings,
    private val api: ScraperBase,
) : ScraperViewModel<KomikDetail>(
    storage, false
) {
    private var slug = "0"
    override var cacheKey = "komik-${slug}"

    init {
        slug = URLDecoder.decode(savedStateHandle.get<String>("slug").orEmpty(), "UTF-8")
            .ifEmpty { "0" }
        cacheKey = "komik-$slug"
        load(force = false, isManual = false)
    }

    fun isFavorite(id: String): LiveData<Int> {
        return favoriteKomikRepository.isFavorite(id)
    }

    fun readChapterHistory(id: String): LiveData<List<ChapterHistoryItem>> {
        return chapterRepository.readChapterHistory(id)
    }

    fun setFavorite(isFavorite: Boolean) {
        val komik = _state.value.getDataOrNull()
        if (komik != null) {
            viewModelScope.launch {
                if (!isFavorite) {
                    favoriteKomikRepository.delete(komik.id)
                } else {
                    favoriteKomikRepository.add(
                        FavoriteKomikItem(
                            id = komik.id,
                            title = komik.title,
                            description = komik.description,
                            slug = komik.slug,
                            img = komik.img,
                            type = komik.type
                        )
                    )
                }
            }
        }
    }

    override suspend fun fetchData(): KomikDetail {
        return api.getDetailKomik(slug)
    }

    override fun onCleared() {
        super.onCleared()
        chapterRepository.close()
        favoriteKomikRepository.close()
    }

}