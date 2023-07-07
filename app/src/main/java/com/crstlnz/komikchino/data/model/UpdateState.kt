package com.crstlnz.komikchino.data.model

data class UpdateState(
    val version: String,
    val reminder: Boolean = true // if false dont show dialog
)