package com.crstlnz.komikchino.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.crstlnz.komikchino.data.api.KomikClient
import com.crstlnz.komikchino.data.api.source.Kiryuu
import com.crstlnz.komikchino.data.model.SearchItem
import com.crstlnz.komikchino.data.util.StorageHelper
import com.crstlnz.komikchino.ui.util.ViewModelBase
import com.crstlnz.komikchino.ui.util.delayBlock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class InfiniteState {
    IDLE, LOADING, FINISH
}

class SearchViewModelFactory(
    private val storage: StorageHelper<List<SearchItem>>,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SearchViewModel(storage) as T
    }
}

class SearchViewModel(private val storage: StorageHelper<List<SearchItem>>) :
    ViewModelBase<List<SearchItem>>(
        storage,
        true
    ) {
    private val api: Kiryuu = Kiryuu()
    override var cacheKey = "search"
    private var query = ""
    private var page = 1

    fun search(query: String, page: Int = 1) {
        this.query = query
        this.page = page
        load()
    }

    private val _infiniteState = MutableStateFlow(InfiniteState.IDLE)
    val infiniteState: StateFlow<InfiniteState> = _infiniteState.asStateFlow()
    fun next() {
        if (_infiniteState.value == InfiniteState.LOADING || _infiniteState.value == InfiniteState.FINISH) return
        page++
        viewModelScope.launch {
            _infiniteState.update { InfiniteState.LOADING }
            val data = delayBlock(350L) {
                fetchSearch(query, page)
            }
            if (data.isNotEmpty()) {
                _state.update {
                    val oldData = it.data ?: listOf()
                    it.copy(
                        data = listOf(*oldData.toTypedArray(), *data.toTypedArray())
                    )
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