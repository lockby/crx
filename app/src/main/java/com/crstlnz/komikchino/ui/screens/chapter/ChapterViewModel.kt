package com.crstlnz.komikchino.ui.screens.chapter

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.crstlnz.komikchino.data.api.ScraperBase
import com.crstlnz.komikchino.data.firebase.model.ChapterEmbed
import com.crstlnz.komikchino.data.firebase.model.ChapterHistoryItem
import com.crstlnz.komikchino.data.firebase.model.FavoriteKomikItem
import com.crstlnz.komikchino.data.firebase.model.KomikHistoryItem
import com.crstlnz.komikchino.data.firebase.repository.ChapterHistoryRepository
import com.crstlnz.komikchino.data.firebase.repository.FavoriteKomikRepository
import com.crstlnz.komikchino.data.firebase.repository.KomikHistoryRepository
import com.crstlnz.komikchino.data.model.Chapter
import com.crstlnz.komikchino.data.model.ChapterData
import com.crstlnz.komikchino.data.model.ChapterScrollPostition
import com.crstlnz.komikchino.data.model.DataState
import com.crstlnz.komikchino.data.model.DataState.Loading.getDataOrNull
import com.crstlnz.komikchino.data.model.PreloadedImages
import com.crstlnz.komikchino.data.model.ScrollImagePosition
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
    @Named("chapterListCache") private val chapterListStorage: StorageHelper<List<Chapter>>,
    @Named("chapterScrollPositionCache") private val chapterScrollPositionCache: StorageHelper<ChapterScrollPostition>,
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
    private val _chapterKey = MutableStateFlow(0)
    val chapterKey = _chapterKey.asStateFlow()

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
        if (komikDataRaw != null) komikData = decodeStringURL(
            komikDataRaw, TypeFactory.defaultInstance().constructType(KomikHistoryItem::class.java)
        )

        if (idRaw != null) id = decodeStringURL(
            idRaw, TypeFactory.defaultInstance().constructType(String::class.java)
        ) ?: ""

        if (slugRaw != null) slug = decodeStringURL(
            savedStateHandle.get<String>("slug") ?: "slug",
            TypeFactory.defaultInstance().constructType(String::class.java)
        ) ?: ""

        cacheKey = "chapter-${id.ifEmpty { slug }}"
        load(force = false, isManual = false)
        viewModelScope.launch {
            _state.collect {
                if (it.state == State.DATA && !isChapterListFetched) {
                    isChapterListFetched = true
                    loadChapterList(true)
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
        val komikId = state.value.getDataOrNull()?.komik?.id ?: return null
        val pos = chapterScrollPositionCache.get<ChapterScrollPostition>(komikId)
        return if (pos?.chapterId == id) pos else null
    }

    fun saveHistory(scrollImagePosition: ScrollImagePosition? = null) {
        val chapterId = state.value.getDataOrNull()?.id
        val komikId = state.value.getDataOrNull()?.komik?.id
        val chapter = ChapterHistoryItem(
            id = chapterId ?: id,
            mangaId = komikId ?: "id",
            title = state.value.getDataOrNull()?.title ?: "",
            slug = state.value.getDataOrNull()?.slug ?: "",
        )

        if (komikId != null && scrollImagePosition != null) {
            chapterScrollPositionCache.set<ChapterScrollPostition>(
                komikId, ChapterScrollPostition(
                    scrollImagePosition.calculatedImageSize,
                    mangaId = komikId,
                    chapterId = chapterId ?: id,
                    initialFirstVisibleItemIndex = scrollImagePosition.initialFirstVisibleItemIndex,
                    initialFirstVisibleItemScrollOffset = scrollImagePosition.initialFirstVisibleItemScrollOffset
                )
            )
        }

        val komik = state.value.getDataOrNull()?.komik ?: komikData
        viewModelScope.launch {
            komik?.let {
                komikRepository.add(
                    it.copy(
                        chapter = ChapterEmbed(
                            id = chapter.id, title = chapter.title, slug = chapter.slug
                        )
                    )
                )
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
        _chapterKey.update {
            it + 1
        }
        load(force = false, isManual = false)
    }

    fun loadChapterList(force: Boolean = true) {
        val key = "chapterList-${komikData?.id}"
        val komikId = state.value.getDataOrNull()?.komik?.id
        viewModelScope.launch {
            _chapterList.update {
                DataState.Loading
            }
            val cache = chapterListStorage.getRaw<List<Chapter>>(key)
            if (cache?.isValid == true && !force) {
                _chapterList.update {
                    DataState.Success(cache.data)
                }
            } else {
                try {
                    val data = api.getChapterList(komikData?.id ?: "id").reversed()
                    chapterListStorage.set<List<Chapter>>(key, data)
                    _chapterList.update {
                        DataState.Success(data)
                    }
                } catch (e: Exception) {
                    if (cache != null) {
                        _chapterList.update {
                            DataState.Success(cache.data)
                        }
                    } else {
                        _chapterList.update {
                            DataState.Error()
                        }
                    }

                }
            }
            loadWithCache<List<Chapter>>(
                key = "chapterlist-${komikId}",
                fetch = {
                    api.getChapterList(komikId ?: "id").reversed()
                },
                _chapterList,
                force,
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
        if (komikData == null) {
            val komik = api.getDetailKomik(chapterApi.mangaSlug)
            komikData = KomikHistoryItem(
                id = komik.id,
                title = komik.title,
                slug = komik.slug,
                img = komik.img,
                type = komik.type,
                description = komik.description
            )
        }

        return ChapterData(
            komik = komikData!!,
            title = chapterApi.title,
            slug = chapterApi.slug,
            id = chapterApi.id,
            imgs = chapterApi.imgs,
            disqusConfig = chapterApi.disqusConfig
        )
    }

    override fun onCleared() {
        super.onCleared()
        // Stop the listening when the ViewModel is cleared
        chapterRepository.close()
        favoriteRepository.close()
        komikRepository.close()
    }
}