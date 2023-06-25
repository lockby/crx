package com.crstlnz.komikchino.ui.screens.search

import androidx.lifecycle.viewModelScope
import com.crstlnz.komikchino.config.AppSettings
import com.crstlnz.komikchino.data.api.source.ScraperBase
import com.crstlnz.komikchino.data.datastore.KomikServer
import com.crstlnz.komikchino.data.model.DataState
import com.crstlnz.komikchino.data.model.DataState.Idle.getDataOrNull
import com.crstlnz.komikchino.data.model.SearchItem
import com.crstlnz.komikchino.data.util.StorageHelper
import com.crstlnz.komikchino.ui.util.ViewModelBase
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
    @Named("searchCache") private val storage: StorageHelper<List<SearchItem>>,
    private val api: ScraperBase
) :
    ViewModelBase<List<SearchItem>>(
        storage,
        true
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
            val data = fetchSearch(query, page)
            if (data.isNotEmpty()) {
                _state.update {
                    val oldData = it.getDataOrNull() ?: arrayListOf()
                    DataState.Success(oldData.plus(data))
//                    DataState.Success(listOf(*oldData.toTypedArray(), *data.toTypedArray()))
                }
                _infiniteState.update { InfiniteState.IDLE }
            } else {
                _infiniteState.update { InfiniteState.FINISH }
            }
        }
    }

    private suspend fun fetchSearch(query: String, page: Int = 1): List<SearchItem> {
        val result = api.search(query, page)
        if (!result.hasNext) {
            _infiniteState.update { InfiniteState.FINISH }
        }
        lastFetch = System.currentTimeMillis()
        return result.result
//        val body = api.search(page, query)
//        val document: Document = Jsoup.parse(body.string())
//        val hasNext = document.selectFirst(".pagination .page-numbers.next") != null
//        val comics = document.select("#content .animepost")
//        val result = arrayListOf<SearchItem>()
//        for (comic in comics) {
//            val title = comic.selectFirst(".bigors a")?.text() ?: ""
//            val img = comic.selectFirst("a img")?.attr("src") ?: ""
//            val score = comic.selectFirst(".bigors .rating")?.text()?.toFloatOrNull() ?: 0f
//            val type =
//                comic.selectFirst(".limit span.typeflag")?.classNames()?.toList()?.getOrNull(1)
//                    ?: ""
//            val isColored = comic.selectFirst(".warnalabel") != null
//            val url = comic.selectFirst("a")?.attr("href") ?: ""
//
//            result.add(
//                SearchItem(
//                    title,
//                    img,
//                    score,
//                    type,
//                    isColored,
//                    url = url,
//                )
//            )
//        }
//
//        if (!hasNext) {
//            _infiniteState.update { InfiniteState.FINISH }
//        }
//
//        return result
    }

    override suspend fun fetchData(): List<SearchItem> {
        return fetchSearch(query, 1)
    }
}