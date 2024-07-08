package com.crstlnz.komikchino.data.model

import coil3.request.ImageRequest


data class ImageSize(
    val calculated: Boolean,
    val height: Float,
    val width: Float,
)

data class ImageSizeRequest(
    var calculated: Boolean,
    var height: Float,
    var width: Float,
    var imageRequest: ImageRequest? = null
)

data class ScrollImagePosition(
    val initialFirstVisibleItemIndex: Int,
    val initialFirstVisibleItemScrollOffset: Int,
    val calculatedImageSize: List<ImageSize>
)

data class ChapterScrollPostition(
    val imageSize: List<ImageSize>,
    val mangaId: String,
    val chapterId: String,
    val initialFirstVisibleItemIndex: Int,
    val initialFirstVisibleItemScrollOffset: Int,
)

