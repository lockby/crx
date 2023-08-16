package com.crstlnz.komikchino.data.model

data class HomeData(
    var featured: List<FeaturedComic> = listOf(),
//    val mangaUpdates: List<LatestUpdate>,
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
    val genreLink: List<GenreLink> = listOf(),
    val type: String = "",
    val img: String = "",
    val slug: String = "",
    val score: Float? = null,
)

enum class OpenType {
    KOMIK,
    CHAPTER
}

data class SectionComic(
    var title: String = "",
    var url: String = "",
    val type: String = "",
    val img: String = "",
    val slug: String = "",
    val score: Float? = null,
    val chapterString: String = "",
    val openType : OpenType = OpenType.KOMIK
)