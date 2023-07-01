package com.crstlnz.komikchino.ui.screens.latestupdate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crstlnz.komikchino.config.AppSettings
import com.crstlnz.komikchino.data.api.KomikServer
import com.crstlnz.komikchino.data.api.ScraperBase
import com.crstlnz.komikchino.data.model.DataState
import com.crstlnz.komikchino.data.model.DataState.Idle.getDataOrNull
import com.crstlnz.komikchino.data.model.LatestUpdate
import com.crstlnz.komikchino.data.model.SearchResult
import com.crstlnz.komikchino.data.util.StorageHelper
import com.crstlnz.komikchino.ui.screens.search.InfiniteState
import com.crstlnz.komikchino.ui.util.ScraperViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import javax.inject.Inject
import javax.inject.Named


@HiltViewModel
class LatestUpdateViewModel @Inject constructor(
    @Named("latestUpdateCache") private val storage: StorageHelper<List<LatestUpdate>>,
    private val api: ScraperBase
) : ScraperViewModel<List<LatestUpdate>>(storage, true) {
    override var cacheKey = "latestUpdate"
    private var page = 1

    init {
        load(false)
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
                val data = fetchSearch(page)
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

    private suspend fun fetchSearch(page: Int = 1): List<LatestUpdate> {
        val result = api.getLatestUpdate(page)
        if (!result.hasNext) {
            _infiniteState.update { InfiniteState.FINISH }
        }
        lastFetch = System.currentTimeMillis()
        return result.result
    }

    override suspend fun fetchData(): List<LatestUpdate> {
        return fetchSearch(1)
    }
}