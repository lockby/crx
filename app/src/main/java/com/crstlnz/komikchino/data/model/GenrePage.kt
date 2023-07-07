package com.crstlnz.komikchino.data.model

data class Genre(
    val id: String,
    val title: String
)

data class GenreSearch(
    val genreList: List<Genre>,
    val page: Int = 1,
    val hasNext: Boolean = false,
    val result: List<KomikSearchResult>
)

data class KomikSearchResult(
    val img: String,
    val title: String,
    val url: String,
    val slug: String,
    val score: Float? = null,
    val isColored: Boolean = false,
    val isComplete: Boolean = false,
    val isHot: Boolean = false,
    val type: String,
)