package com.crstlnz.komikchino.data.model

data class DataState<T>(
    val state: State = State.IDLE,
    var data: T? = null,
    var error: String? = null
)

enum class State {
    LOADING, ERROR, DATA, IDLE
}