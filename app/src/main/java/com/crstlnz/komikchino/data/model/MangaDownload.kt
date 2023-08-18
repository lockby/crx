package com.crstlnz.komikchino.data.model

import com.crstlnz.komikchino.data.database.model.KomikHistoryItem

data class MangaDownload(
    val id: String,
    val title: String,
    val description: String,
    val img: String,
    val banner: String = "",
    val type: String = "",
    val genreLinks: List<GenreLink> = listOf(),
    val downloads: List<ChapterDataDownload>
)

data class ChapterDataDownload(
    val id: String,
    val title: String,
    val slug: String,
    val pendingImgs: List<ChapterImageDownload> = listOf(),
    val downloadedImgs: List<ChapterImageDownload> = listOf(),
    val komik: KomikHistoryItem,
    val disqusConfig: DisqusConfig? = null
)

data class ChapterImageDownload(
    val index: Int,
    val url: String,
)