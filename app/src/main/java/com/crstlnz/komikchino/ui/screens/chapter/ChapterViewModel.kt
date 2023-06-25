package com.crstlnz.komikchino.ui.screens.chapter

import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.crstlnz.komikchino.data.api.source.ScraperBase
import com.crstlnz.komikchino.data.database.chapterhistory.ChapterHistoryItem
import com.crstlnz.komikchino.data.database.chapterhistory.ChapterHistoryRepository
import com.crstlnz.komikchino.data.database.favorite.FavoriteKomikItem
import com.crstlnz.komikchino.data.database.favorite.FavoriteKomikRepository
import com.crstlnz.komikchino.data.database.komik.KomikHistoryItem
import com.crstlnz.komikchino.data.database.komik.KomikHistoryRepository
import com.crstlnz.komikchino.data.model.Chapter
import com.crstlnz.komikchino.data.model.ChapterHistoryData
import com.crstlnz.komikchino.data.model.ChapterModel
import com.crstlnz.komikchino.data.model.DataState
import com.crstlnz.komikchino.data.model.DataState.Loading.getDataOrNull
import com.crstlnz.komikchino.data.model.PreloadedImages
import com.crstlnz.komikchino.data.model.State
import com.crstlnz.komikchino.data.util.StorageHelper
import com.crstlnz.komikchino.data.util.decodeStringURL
import com.crstlnz.komikchino.ui.util.ViewModelBase
import com.fasterxml.jackson.databind.type.TypeFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class ChapterViewModel @Inject constructor(
    @Named("chapterCache") private val storage: StorageHelper<ChapterModel>,
    private val chapterRepository: ChapterHistoryRepository,
    private val favoriteRepository: FavoriteKomikRepository,
    private val komikRepository: KomikHistoryRepository,
    savedStateHandle: SavedStateHandle,
    private val api: ScraperBase
) : ViewModelBase<ChapterModel>(
    storage, true
) {
    var id = "0"
    override var cacheKey = "chapter-${id}"


    private val _chapterList = MutableStateFlow<DataState<List<Chapter>>>(DataState.Idle)
    val chapterList: StateFlow<DataState<List<Chapter>>> = _chapterList.asStateFlow()


    private val _currentPosition = MutableStateFlow<Int>(0) // chapter position in chapter list
    val currentPosition: StateFlow<Int> = _currentPosition.asStateFlow()

    private val _loadedImages = MutableStateFlow<PreloadedImages>(
        PreloadedImages(
            isLoading = false,
            images = listOf(),
            progress = 0f,
        )
    )

    private var preloadJob: Job? = Job()
    private var asyncResults: List<Deferred<ImageBitmap?>> = listOf()
    var chapterHistoryData = ChapterHistoryData()
    var komikData: KomikHistoryItem? = null

    init {
        komikData = decodeStringURL(
            savedStateHandle.get<String>("komikdata").orEmpty(),
            TypeFactory.defaultInstance().constructType(KomikHistoryItem::class.java)
        )

        id = decodeStringURL(
            savedStateHandle.get<String>("id") ?: "id",
            TypeFactory.defaultInstance().constructType(String::class.java)
        ) ?: ""
        cacheKey = "chapter-${id}"
        load(false)
        loadChapterList(false)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                chapterHistoryData = ChapterHistoryData(
                    id,
                    chapterRepository.get(id)
                )
            }

        }
        viewModelScope.launch {
            _state.collect {
                _currentPosition.update {
                    calculatePosition()
                }

//                if (it.state == State.DATA) {
//                    val data = (it as DataState.Success).data
//                    preloadImages(storage.getContext(), data.imgs ?: listOf())
//                } else {
//                    _loadedImages.update { preload ->
//                        preload.copy(isLoading = true)
//                    }
//                }
            }
        }
        viewModelScope.launch {
            _chapterList.collect {
                _currentPosition.update {
                    calculatePosition()
                }
            }
        }
    }

    fun isFavorite(id: String): LiveData<Int> {
        return favoriteRepository.isFavorite(id)
    }

    fun setFavorite(isFavorite: Boolean) {
        val komik = komikData
        if (komik != null) {
            viewModelScope.launch {
                if (!isFavorite) {
                    favoriteRepository.deleteById(komik.id)
                } else {
                    favoriteRepository.add(
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

    fun saveHistory(firstVisibleItemIndex: Int = 0, firstVisibleItemScrollOffset: Int = 0) {
        val chapter = ChapterHistoryItem(
            id = id,
            mangaId = komikData?.id ?: "id",
            title = state.value.getDataOrNull()?.title ?: "",
            slug = state.value.getDataOrNull()?.slug ?: "",
        )

        viewModelScope.launch {
            komikData?.let {
                komikRepository.add(it)
            }
        }

        viewModelScope.launch {
            chapterRepository.add(chapter)
        }
    }

    private fun calculatePosition(): Int {
        if (_chapterList.value.state == State.DATA) {
            val chapterList = (_chapterList.value as DataState.Success).data
            return chapterList.indexOfFirst { it.id == id }
        }
        return -1
    }

    fun loadChapter(id: String) {
        this.id = id
        cacheKey = "chapter-${id}"
        load(false)
    }

    fun loadChapterList(force: Boolean = true) {
        viewModelScope.launch {
            loadWithCache<List<Chapter>>(
                key = "chapterlist-${komikData?.id}",
                fetch = {
                    api.getChapterList(komikData?.id ?: "id").reversed()
                }, _chapterList, force,
                TypeFactory.defaultInstance()
                    .constructParametricType(List::class.java, Chapter::class.java)
            )
        }

    }

    override suspend fun fetchData(): ChapterModel {
        return api.getChapter(id)
    }
}