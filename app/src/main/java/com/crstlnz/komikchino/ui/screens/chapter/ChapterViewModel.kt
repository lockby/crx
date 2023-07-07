package com.crstlnz.komikchino.ui.screens.chapter

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.crstlnz.komikchino.data.api.ScraperBase
import com.crstlnz.komikchino.data.database.model.ChapterHistoryItem
import com.crstlnz.komikchino.data.database.model.FavoriteKomikItem
import com.crstlnz.komikchino.data.database.model.KomikHistoryItem
import com.crstlnz.komikchino.data.database.repository.ChapterHistoryRepository
import com.crstlnz.komikchino.data.database.repository.FavoriteKomikRepository
import com.crstlnz.komikchino.data.database.repository.KomikHistoryRepository
import com.crstlnz.komikchino.data.model.Chapter
import com.crstlnz.komikchino.data.model.ChapterData
import com.crstlnz.komikchino.data.model.ChapterScrollPostition
import com.crstlnz.komikchino.data.model.DataState
import com.crstlnz.komikchino.data.model.DataState.Loading.getDataOrNull
import com.crstlnz.komikchino.data.model.ImageSize
import com.crstlnz.komikchino.data.model.PreloadedImages
import com.crstlnz.komikchino.data.model.State
import com.crstlnz.komikchino.data.util.StorageHelper
import com.crstlnz.komikchino.data.util.decodeStringURL
import com.crstlnz.komikchino.ui.util.ScraperViewModel
import com.fasterxml.jackson.databind.type.TypeFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class ChapterViewModel @Inject constructor(
    @Named("chapterCache") private val storage: StorageHelper<ChapterData>,
    @Named("chapterScrollPostitionCache") private val chapterScrollPostition: StorageHelper<ChapterScrollPostition>,
    private val chapterRepository: ChapterHistoryRepository,
    private val favoriteRepository: FavoriteKomikRepository,
    private val komikRepository: KomikHistoryRepository,
    savedStateHandle: SavedStateHandle,
    private val api: ScraperBase
) : ScraperViewModel<ChapterData>(
    storage, false
) {
    var id = ""
    var slug = ""
    override var cacheKey = "chapter"


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

    var komikData: KomikHistoryItem? = null
    private var isChapterListFetched = false

    init {
        val komikDataRaw = savedStateHandle.get<String>("komikdata")
        val idRaw = savedStateHandle.get<String>("id")
        val slugRaw = savedStateHandle.get<String>("slug")
        if (komikDataRaw != null)
            komikData = decodeStringURL(
                komikDataRaw,
                TypeFactory.defaultInstance().constructType(KomikHistoryItem::class.java)
            )

        if (idRaw != null)
            id = decodeStringURL(
                idRaw,
                TypeFactory.defaultInstance().constructType(String::class.java)
            ) ?: ""

        if (slugRaw != null)
            slug = decodeStringURL(
                savedStateHandle.get<String>("slug") ?: "slug",
                TypeFactory.defaultInstance().constructType(String::class.java)
            ) ?: ""

        cacheKey = "chapter-${id.ifEmpty { slug }}"
        load(force = false, isManual = false)
        viewModelScope.launch {
            _state.collect {
                if (it.state == State.DATA && !isChapterListFetched) {
                    isChapterListFetched = true
                    loadChapterList(false)
                } else {
                    _currentPosition.update {
                        calculatePosition()
                    }
                }
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
                    favoriteRepository.delete(komik.id)
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

    fun getChapterScrollPosition(): ChapterScrollPostition? {
        val mangaId = komikData?.id
        return if (mangaId != null) {
            val pos = chapterScrollPostition.get<ChapterScrollPostition>(mangaId)
            if (pos?.chapterId == id) pos else null
        } else {
            null
        }
    }

    fun saveHistory(
        firstVisibleItemIndex: Int = 0,
        firstVisibleItemScrollOffset: Int = 0,
        calculatedImageSize: List<ImageSize> = listOf()
    ) {
        val chapter = ChapterHistoryItem(
            id = id,
            mangaId = komikData?.id ?: "id",
            title = state.value.getDataOrNull()?.title ?: "",
            slug = state.value.getDataOrNull()?.slug ?: "",
        )

        Log.d("CHAPTER SAVE", chapter.toString())

        if (komikData?.id != null) {
            chapterScrollPostition.set<ChapterScrollPostition>(
                komikData!!.id,
                ChapterScrollPostition(
                    calculatedImageSize,
                    mangaId = komikData!!.id,
                    chapterId = id,
                    initialFirstVisibleItemIndex = firstVisibleItemIndex,
                    initialFirstVisibleItemScrollOffset = firstVisibleItemScrollOffset
                )
            )
        }

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
        load(force = false, isManual = false)
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

    override suspend fun fetchData(): ChapterData {
        val chapterApi = if (id.isNotEmpty()) {
            api.getChapter(id)
        } else if (slug.isNotEmpty()) {
            api.getChapterBySlug(slug)
        } else {
            throw Exception("Data is missing!")
        }

        id = chapterApi.id
        slug = chapterApi.slug
        return if (komikData != null) {
            ChapterData(
                komik = komikData!!,
                title = chapterApi.title,
                slug = chapterApi.slug,
                id = chapterApi.slug,
                imgs = chapterApi.imgs,
                disqusConfig = chapterApi.disqusConfig
            )
        } else {
            val komik = api.getDetailKomik(chapterApi.mangaSlug)
            komikData = KomikHistoryItem(
                id = komik.id,
                title = komik.title,
                slug = komik.slug,
                img = komik.img,
                type = komik.type,
                description = komik.description
            )

            ChapterData(
                komik = komikData!!,
                title = chapterApi.title,
                slug = chapterApi.slug,
                id = chapterApi.slug,
                imgs = chapterApi.imgs,
                disqusConfig = chapterApi.disqusConfig
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Stop the listening when the ViewModel is cleared
        chapterRepository.close()
        favoriteRepository.close()
        komikRepository.close()
    }
}