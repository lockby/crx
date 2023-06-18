package com.crstlnz.komikchino.ui.util

import com.fasterxml.jackson.databind.JavaType
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crstlnz.komikchino.data.model.DataState
import com.crstlnz.komikchino.data.model.State
import com.crstlnz.komikchino.data.util.StorageHelper
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

open class ViewModelBase<T>(
    private val storage: StorageHelper<T>,
    private val alwaysRefresh: Boolean = false,
) : ViewModel() {
    protected val _state = MutableStateFlow(DataState<T>())
    private var firstLaunch = true;
    protected open var cacheKey: String = "";

    //    var onError: ((message: String, dataState: DataState<T>) -> Unit)? = null
    private val _onError = MutableSharedFlow<String>()
    val onError = _onError.asSharedFlow()

    //    private val storage by lazy {
//        StorageHelper<T>(
//            application,
//            sharedPreferencesName = "CACHE",
//            objectType
//        )
//    }
    val state: StateFlow<DataState<T>> = _state.asStateFlow()

    open suspend fun fetchData(): T {
        throw Error("No FetchData Method provided!")
    }

    protected fun <T> loadWithCache(
        key: String,
        fetch: suspend () -> T,
        stateData: MutableStateFlow<DataState<T>>,
        force: Boolean = true,
        type: JavaType? = null
    ) {
        if (stateData.value.state == State.LOADING && !firstLaunch) return
        firstLaunch = false
        val cache = storage.getRaw<T>(key, type)
        viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
            throwable.printStackTrace()
            viewModelScope.launch {
                val errorString = "Fetch Fail";
                _onError.emit(errorString)
                stateData.update {
                    DataState(
                        data = cache?.data, state = State.ERROR, error = errorString
                    )
                }
            }
        }) {
            if (cache?.isValid == true && !alwaysRefresh && !force) {
                stateData.update {
                    DataState(
                        data = cache.data, state = State.DATA, error = null
                    )
                }
            } else {
                stateData.update {
                    DataState(state = State.LOADING)
                }
                val data = fetch()
                storage.set<T>(key, data)
                stateData.update {
                    DataState(
                        data = data, state = State.DATA, error = null
                    )
                }
            }
        }
    }

    fun load(force: Boolean = true) {
        loadWithCache<T>(
            cacheKey,
            fetch = {
                fetchData()
            }, _state, force
        )
    }
}