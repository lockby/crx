package com.crstlnz.komikchino.ui.screens.chapter

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.FutureTarget
import com.crstlnz.komikchino.data.api.source.Kiryuu
import com.crstlnz.komikchino.data.database.readhistory.ReadHistoryRepository
import com.crstlnz.komikchino.data.database.readhistory.model.ReadHistoryDao
import com.crstlnz.komikchino.data.database.readhistory.model.ReadHistoryItem
import com.crstlnz.komikchino.data.model.Chapter
import com.crstlnz.komikchino.data.model.ChapterHistoryData
import com.crstlnz.komikchino.data.model.ChapterModel
import com.crstlnz.komikchino.data.model.DataState
import com.crstlnz.komikchino.data.model.PreloadedImages
import com.crstlnz.komikchino.data.model.State
import com.crstlnz.komikchino.data.util.StorageHelper
import com.crstlnz.komikchino.ui.util.ViewModelBase
import com.fasterxml.jackson.databind.type.TypeFactory
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.async

class ChapterViewModelFactory(
    private val id: Int,
    private val storage: StorageHelper<ChapterModel>,
    private val repository: ReadHistoryRepository,
    private val mangaId: Int,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChapterViewModel(storage, repository, id, mangaId) as T
    }
}

class ChapterViewModel(
    private val storage: StorageHelper<ChapterModel>,
    private val repository: ReadHistoryRepository,
    var id: Int,
    val mangaId: Int
) : ViewModelBase<ChapterModel>(
    storage, false
) {
    private val api: Kiryuu = Kiryuu()
    override var cacheKey = "chapter-${id}"


    private val _chapterList = MutableStateFlow<DataState<List<Chapter>>>(DataState())
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
    val loadedImages = _loadedImages.asStateFlow()
    private var preloadJob: Job? = Job()
    private var asyncResults: List<Deferred<ImageBitmap?>> = listOf()
    var chapterHistoryData = ChapterHistoryData()

    init {
        load(false)
        loadChapterList(false)
        viewModelScope.launch {
            chapterHistoryData = ChapterHistoryData(
                id,
                withContext(Dispatchers.IO) {
                    repository.get(id)
                }
            )
        }
        viewModelScope.launch {
            _state.collect {
                _currentPosition.update {
                    calculatePosition()
                }

                if (it.state == State.DATA) {
                    val data = it.data
                    if (data != null) {
                        preloadImages(storage.getContext(), it.data?.imgs ?: listOf())
                    }
                } else {
                    _loadedImages.update { preload ->
                        preload.copy(isLoading = true)
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

    private fun calculatePosition(): Int {
        if (_chapterList.value.state == State.DATA) {
            val chapterList = _chapterList.value.data
            if (chapterList != null) {
                return chapterList.indexOfFirst { it.id == id }
            }
        }
        return -1
    }

    fun saveHistory(history: ReadHistoryItem) {
        viewModelScope.launch {
            try {
                repository.add(history)
            } catch (e: Exception) {
                Log.d("DATABASE ERROR", e.stackTraceToString())
            }
        }
    }


    private suspend fun loadImage(context: Context, img: String): ImageBitmap? {
        return withContext(Dispatchers.IO) {
            try {
                Glide.with(context).asBitmap().load(img).submit().get().asImageBitmap()
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun preloadImages(context: Context, imageUrls: List<String>) {
        preloadJob?.cancel()
        asyncResults.forEach {
            it.cancel()
        }
        preloadJob = viewModelScope.launch {
            _loadedImages.update {
                it.copy(progress = 0f, isLoading = true, images = listOf())
            }
            val imgCount = imageUrls.size;
            var finished = 0
            fun finish() {
                finished += 1
                _loadedImages.update {
                    it.copy(progress = finished.toFloat() / imgCount.toFloat())
                }
            }

            asyncResults = imageUrls.map { img ->
                async {
                    val data = loadImage(context, img)
                    finish()
                    data
                }
            }

            val loaded = asyncResults.awaitAll()
            _loadedImages.update {
                it.copy(
                    images = loaded ?: listOf(),
                    isLoading = false
                )
            }
        }
    }

    fun loadChapter(id: Int) {
        this.id = id
        cacheKey = "chapter-${id}"
        load(false)
    }

    fun loadChapterList(force: Boolean = true) {
        viewModelScope.launch {
            loadWithCache<List<Chapter>>(
                key = "chapterlist-${mangaId}",
                fetch = {
                    api.getChapterList(mangaId).reversed()
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