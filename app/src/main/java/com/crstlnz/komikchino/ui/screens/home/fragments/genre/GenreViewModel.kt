package com.crstlnz.komikchino.ui.screens.home.fragments.genre

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.crstlnz.komikchino.config.AppSettings
import com.crstlnz.komikchino.data.api.KomikServer
import com.crstlnz.komikchino.data.api.ScraperBase
import com.crstlnz.komikchino.data.model.DataState
import com.crstlnz.komikchino.data.model.Genre
import com.crstlnz.komikchino.data.model.GenreSearch
import com.crstlnz.komikchino.data.model.KomikSearchResult
import com.crstlnz.komikchino.data.util.StorageHelper
import com.crstlnz.komikchino.ui.screens.search.InfiniteState
import com.crstlnz.komikchino.ui.util.ScraperViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class GenreViewModel @Inject constructor(
    @Named("genreSearchCache") private val genreSearchCache: StorageHelper<GenreSearch>,
    private val api: ScraperBase,
) : ScraperViewModel<GenreSearch>(genreSearchCache) {
    override var cacheKey = "genreSearch"
    private var page = 1
    val genreList = mutableListOf<Genre>()

    init {
        load(false)
    }

    private var lastFetch = 0L
    private val mustDelay = 3000L

    private val _searchResult = MutableStateFlow<MutableList<KomikSearchResult>>(arrayListOf())
    val searchResult = _searchResult.asStateFlow()
    private val _infiniteState = MutableStateFlow(InfiniteState.IDLE)
    val infiniteState: StateFlow<InfiniteState> = _infiniteState.asStateFlow()

    fun next() {
        if (_infiniteState.value == InfiniteState.LOADING || _infiniteState.value == InfiniteState.FINISH) return
        page++
        viewModelScope.launch {
            _infiniteState.update { InfiniteState.LOADING }
            if (AppSettings.komikServer == KomikServer.MANGAKATANA) {
                val timeElapsed = System.currentTimeMillis() - lastFetch
                if (timeElapsed < mustDelay) {
                    withContext(Dispatchers.IO) {
                        Thread.sleep(mustDelay - timeElapsed)
                    }
                }
            }

            try {
                val data = fetchSearch(listOf(), page)
                if (data.result.isNotEmpty()) {
                    _searchResult.update {
                        (it + data.result).toMutableList()
                    }
                    _infiniteState.update { InfiniteState.IDLE }
                } else {
                    _infiniteState.update { InfiniteState.FINISH }
                }
            } catch (e: Exception) {
                Log.d("ERROR BAI", e.stackTraceToString())
                _infiniteState.update { InfiniteState.FINISH }
            }

        }
    }

    private suspend fun fetchSearch(
        genreList: List<Genre> = listOf(), page: Int = 1
    ): GenreSearch {
        this.page = page
        val result = api.searchByGenre(genreList, page)
        if (!result.hasNext) {
            _infiniteState.update { InfiniteState.FINISH }
        }
        lastFetch = System.currentTimeMillis()
        return result
    }

    fun search(
        genreList: List<Genre> = listOf(), page: Int = 1
    ) {
        _searchResult.update {
            arrayListOf()
        }

        this.genreList.clear()
        this.genreList.addAll(genreList)
        this.page = page
        load(true)
    }

    override suspend fun fetchData(): GenreSearch {
        return fetchSearch(genreList, 1)
    }

    init {
        viewModelScope.launch {
            _state.collectLatest { state ->
                if (state is DataState.Success) {
                    val data = state.data
                    _searchResult.update {
                        data.result.toMutableList()
                    }
                }
            }
        }
    }
}