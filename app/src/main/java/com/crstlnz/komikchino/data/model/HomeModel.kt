package com.crstlnz.komikchino.data.model

data class HomeData(
    var featured: List<FeaturedComic> = listOf(),
    var popular: List<PopularComic> = listOf()
)

data class FeaturedComic(
    var title: String = "",
    var url: String = "",
    val description: String = "",
    val genre: List<Genre> = listOf(),
    val type: String = "",
    val img: String = "",
    val slug: String = "",
    val score: Float = 0f,
)

data class PopularComic(
    var title: String = "",
    var url: String = "",
    val type: String = "",
    val img: String = "",
    val slug: String = "",
    val score: Float = 0f,
    val chapterString: String = ""
)