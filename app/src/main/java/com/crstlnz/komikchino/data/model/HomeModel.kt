package com.crstlnz.komikchino.data.model

data class HomeData(
    var featured: List<FeaturedComic> = listOf(),
    var sections: List<Section> = listOf()
)

data class Section(
    val title: String,
    val list: List<SectionComic> = listOf()
)

data class FeaturedComic(
    var title: String = "",
    var url: String = "",
    val description: String = "",
    val genre: List<Genre> = listOf(),
    val type: String = "",
    val img: String = "",
    val slug: String = "",
    val score: Float? = null,
)

data class SectionComic(
    var title: String = "",
    var url: String = "",
    val type: String = "",
    val img: String = "",
    val slug: String = "",
    val score: Float? = null,
    val chapterString: String = ""
)