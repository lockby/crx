package com.crstlnz.komikchino.ui.screens.latestupdate

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.crstlnz.komikchino.config.AppSettings
import com.crstlnz.komikchino.data.api.KomikServer
import com.crstlnz.komikchino.data.api.ScraperBase
import com.crstlnz.komikchino.data.firebase.model.KomikHistoryItem
import com.crstlnz.komikchino.data.firebase.repository.KomikHistoryRepository
import com.crstlnz.komikchino.data.model.DataState.Idle.getDataOrNull
import com.crstlnz.komikchino.data.model.FilteredUpdate
import com.crstlnz.komikchino.data.model.LatestUpdate
import com.crstlnz.komikchino.data.model.State
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
class LatestUpdateViewModel @Inject constructor(
    private val historyRepository: KomikHistoryRepository,
    @Named("latestUpdateCache") private val storage: StorageHelper<List<LatestUpdate>>,
    private val api: ScraperBase
) : ScraperViewModel<List<LatestUpdate>>(storage) {
    override var cacheKey = "latestUpdate"
    private var page = 1


    private var lastFetch = 0L
    private val mustDelay = 2800L

    private val _infiniteState = MutableStateFlow(InfiniteState.IDLE)
    val infiniteState: StateFlow<InfiniteState> = _infiniteState.asStateFlow()

    fun next() {
        Log.d("LATEST NEXT", page.toString())
        if (_infiniteState.value == InfiniteState.LOADING || _infiniteState.value == InfiniteState.FINISH) return
        viewModelScope.launch {
            _infiniteState.update { InfiniteState.LOADING }
            page++
            Log.d("LATEST NEXT AFTER", page.toString())
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
                    _filteredUpdate.update {
                        it.copy(
                            result = it.result + data
                        )
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

    private suspend fun fetchSearch(page: Int = 1): List<LatestUpdate> {
        this.page = page
        val result = api.getLatestUpdate(1)
        if (!result.hasNext) {
            _infiniteState.update { InfiniteState.FINISH }
        }
        lastFetch = System.currentTimeMillis()
        return result.result
    }

    override suspend fun fetchData(): List<LatestUpdate> {

        return fetchSearch(1)
    }

    private val _filteredUpdate = MutableStateFlow(FilteredUpdate())
    val filteredUpdate = _filteredUpdate.asStateFlow()
    private var komikHistory = mutableListOf<KomikHistoryItem>()

    private suspend fun updateKomikHistories() {
        komikHistory = historyRepository.getKomikHistories().toMutableList()
    }

    init {
        viewModelScope.launch {
            _state.collectLatest { state ->
                updateKomikHistories()
                if (state.state == State.DATA) {
                    _filteredUpdate.update { _ ->
                        val data = state.getDataOrNull()!!
                        val highlight = data.filter { item ->
                            komikHistory.find {
                                it.slug == item.slug
                            } != null
                        }

                        FilteredUpdate(
                            highlight = highlight,
                            result = data - highlight.toSet()
                        )

                    }
                }
            }
        }
        load(force = false, isManual = false)
    }
}