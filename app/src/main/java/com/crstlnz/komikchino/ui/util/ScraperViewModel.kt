package com.crstlnz.komikchino.ui.util

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crstlnz.komikchino.config.AppSettings
import com.crstlnz.komikchino.data.model.DataState
import com.crstlnz.komikchino.data.model.State
import com.crstlnz.komikchino.data.util.StorageHelper
import com.crstlnz.komikchino.hilt.Cache
import com.fasterxml.jackson.databind.JavaType
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

open class ScraperViewModel<T>(
    private val storage: StorageHelper<T>,
    private val alwaysRefresh: Boolean = false,
) : ViewModel() {
    protected val _state = MutableStateFlow<DataState<T>>(DataState.Idle)
    protected open var cacheKey: String = ""

    private val _onError = MutableSharedFlow<String>()
    val onError = _onError.asSharedFlow()

    val state: StateFlow<DataState<T>> = _state.asStateFlow()

    open suspend fun fetchData(): T {
        throw Error("No FetchData Method provided!")
    }

    private suspend fun <T> loadWithCacheMain(
        key: String,
        fetch: suspend () -> T,
        stateData: MutableStateFlow<DataState<T>>,
        storage: StorageHelper<T>,
        force: Boolean = true,
    ) {
        if (stateData.value.state == State.LOADING) return
        stateData.update {
            DataState.Loading
        }

        try {
            val data = loadWithCacheUtil(key, fetch, storage, force)
            if (data != null) {
                stateData.update {
                    DataState.Success(data)
                }
            } else {
                stateData.update {
                    DataState.Error("Data tidak ditemukan!")
                }
            }
        } catch (e: Exception) {
            Log.e("ERROR", e.stackTraceToString())
            val errorString = e.message ?: "Fetch fail!"
            _onError.emit(errorString)
            stateData.update {
                DataState.Error(errorString)
            }
        }

    }

    protected suspend fun <T> loadWithCache(
        key: String,
        fetch: suspend () -> T,
        stateData: MutableStateFlow<DataState<T>>,
        force: Boolean = true,
        type: JavaType
    ) {
        this.loadWithCacheMain(
            key,
            fetch,
            stateData,
            StorageHelper<T>(
                this.storage.getContext(),
                "${AppSettings.komikServer}-${Cache.DEFAULT_CACHE}",
                type
            ),
            force
        )
    }

    private suspend fun <T> loadWithCache(
        key: String,
        fetch: suspend () -> T,
        stateData: MutableStateFlow<DataState<T>>,
        storage: StorageHelper<T>,
        force: Boolean = true,
    ) {
        this.loadWithCacheMain(
            key, fetch, stateData, storage, force
        )
    }

    var isFirstLaunch = true

    init {
        viewModelScope.launch {
            AppSettings.cloudflareState.collect {
                if (!it.isBlocked && !isFirstLaunch) {
                    withContext(Dispatchers.IO) {
                        Thread.sleep(500)
                        if (!it.autoReloadConsumed) {
                            AppSettings.cloudflareState.update { state ->
                                state.copy(autoReloadConsumed = true)
                            }
                            load(force = true, isManual = false)
                        }
                    }
                }
            }
        }
    }

    fun load(force: Boolean = true, isManual: Boolean = true) {
        if (isManual && AppSettings.cloudflareState.value.mustManualTrigger) {
            AppSettings.cloudflareState.update {
                it.copy(mustManualTrigger = false)
            }
        }

        viewModelScope.launch(CoroutineExceptionHandler { _, exception ->
            Log.d("ERROR", exception.stackTraceToString())
            _state.update { DataState.Error() }
        }) {
            loadWithCache(
                cacheKey, fetch = {
                    fetchData()
                }, _state, storage, alwaysRefresh || force
            )
            if (isFirstLaunch) isFirstLaunch = false
        }
    }
}