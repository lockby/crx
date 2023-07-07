package com.crstlnz.komikchino.data.model

import java.util.Date


data class FilteredUpdate(
    val highlight: List<LatestUpdate> = emptyList(),
    val result: List<LatestUpdate> = emptyList()
)

data class LatestUpdatePage(
    var page: Int = 1,
    var result: List<LatestUpdate> = listOf(),
    var hasNext: Boolean = false
)

data class LatestUpdate(
    val title: String,
    val img: String,
    val description: String,
    val slug: String,
    val url: String,
    val chapters: List<ChapterUpdate>,
)

data class ChapterUpdate(
    val title: String,
    val slug: String,
    val url: String,
    val date: Date? = null,
)