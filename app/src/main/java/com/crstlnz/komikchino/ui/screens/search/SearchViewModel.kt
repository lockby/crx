package com.crstlnz.komikchino.ui.screens.search

import androidx.lifecycle.viewModelScope
import com.crstlnz.komikchino.config.AppSettings
import com.crstlnz.komikchino.data.api.KomikServer
import com.crstlnz.komikchino.data.api.ScraperBase
import com.crstlnz.komikchino.data.model.DataState
import com.crstlnz.komikchino.data.model.DataState.Idle.getDataOrNull
import com.crstlnz.komikchino.data.model.SearchHistoryModel
import com.crstlnz.komikchino.data.model.SearchResult
import com.crstlnz.komikchino.data.util.StorageHelper
import com.crstlnz.komikchino.ui.util.ScraperViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named

enum class InfiniteState {
    IDLE, LOADING, FINISH
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    @Named("searchCache") private val storage: StorageHelper<List<SearchResult.ExactMatch>>,
    @Named("searchHistoryCache") private val searchHistoryCache: StorageHelper<SearchHistoryModel>,
    private val api: ScraperBase
) : ScraperViewModel<List<SearchResult.ExactMatch>>(
    storage, true
) {
    override var cacheKey = "search"
    private var query = ""
    private var page = 1

    fun getCurrentQuery(): String {
        return query
    }

    fun search(query: String, page: Int = 1) {
        this.query = query
        this.page = page
        load()
    }


    private val _searchHistory = MutableStateFlow<List<SearchHistoryModel>>(listOf())
    val searchHistory: StateFlow<List<SearchHistoryModel>> = _searchHistory.asStateFlow()

    init {
        _searchHistory.update {
            searchHistoryCache.getAll().sortedByDescending { it.timestamp }
        }
    }

    fun deleteSearchHistory(q: String) {
        searchHistoryCache.delete(q)
        _searchHistory.update {
            searchHistoryCache.getAll().sortedByDescending { it.timestamp }
        }
    }

    private fun updateSearchHistory(q: String) {
        searchHistoryCache.set<SearchHistoryModel>(
            q,
            SearchHistoryModel(
                q,
                System.currentTimeMillis()
            )
        )
        _searchHistory.update {
            searchHistoryCache.getAll().sortedByDescending { it.timestamp }
        }
    }

    private var lastFetch = 0L
    private val mustDelay = 2800L

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
                val data = fetchSearch(query, page)
                if (data.isNotEmpty()) {
                    _state.update {
                        val oldData = it.getDataOrNull() ?: arrayListOf()
                        DataState.Success(oldData.plus(data))
                    }
                    _infiniteState.update { InfiniteState.IDLE }
                } else {
                    _infiniteState.update { InfiniteState.FINISH }
                }
            } catch (e: Exception) {
                _infiniteState.update { InfiniteState.FINISH }
            }
        }
    }

    private val _exactMatch = MutableStateFlow<SearchResult.ExactMatch?>(null)
    val exactMatch = _exactMatch.asStateFlow()

    fun consumeExactMatch() {
        _exactMatch.update {
            null
        }
    }

    private suspend fun fetchSearch(query: String, page: Int = 1): List<SearchResult.ExactMatch> {
        this.page = page
        val result = api.search(query, page)
        return if (result is SearchResult.SearchList) {
            if (!result.hasNext) {
                _infiniteState.update { InfiniteState.FINISH }
            }
            lastFetch = System.currentTimeMillis()
            result.result
        } else {
            _infiniteState.update { InfiniteState.FINISH }

            _exactMatch.update {
                result as SearchResult.ExactMatch
            }

            listOf(result as SearchResult.ExactMatch)
        }

    }

    override suspend fun fetchData(): List<SearchResult.ExactMatch> {
        cacheKey = "${query}-search"
        updateSearchHistory(query)
        return fetchSearch(query, 1)
    }
}