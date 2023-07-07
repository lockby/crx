package com.crstlnz.komikchino.data.model

sealed class DataState<out T>(
    val state: State
) {
    data class Success<T>(val data: T) : DataState<T>(State.DATA)
    data class Error(val error: String? = null) : DataState<Nothing>(State.ERROR)
    object Loading : DataState<Nothing>(State.LOADING)
    object Idle : DataState<Nothing>(State.IDLE)

    inline fun <reified T> DataState<T>.getDataOrNull(): T? {
        return if (this is Success<*> && data is T) {
            data
        } else {
            null
        }
    }
}

enum class State {
    LOADING, ERROR, DATA, IDLE
}