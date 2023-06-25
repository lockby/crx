package com.crstlnz.komikchino.data.model

import androidx.compose.ui.graphics.ImageBitmap
import com.crstlnz.komikchino.data.database.chapterhistory.ChapterHistoryItem
import com.fasterxml.jackson.annotation.JsonProperty

data class ChapterModel(
    val id: String,
    val mangaId: String,
    val slug: String,
    val title: String,
    @JsonProperty("name") val imgs: List<String> = listOf()
)

data class ChapterHistoryData(
    val id: String = "id",
    val readHistory: ChapterHistoryItem? = null
)

data class PreloadedImages(
    val isLoading: Boolean,
    val images: List<ImageBitmap?>,
    val progress: Float
)

//data class PreloadImage(
//    val bitmap: ImageBitmap?,
//    val error: Boolean = false
//)