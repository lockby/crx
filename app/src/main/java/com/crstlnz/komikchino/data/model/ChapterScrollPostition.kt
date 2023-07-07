package com.crstlnz.komikchino.data.model


data class ImageSize(
    val calculated: Boolean,
    val height: Float,
    val width: Float,
)

data class ChapterScrollPostition(
    val imageSize: List<ImageSize>,
    val mangaId: String,
    val chapterId: String,
    val initialFirstVisibleItemIndex: Int,
    val initialFirstVisibleItemScrollOffset: Int,
)

