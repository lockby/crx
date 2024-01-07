package com.crstlnz.komikchino.data.model

import androidx.compose.ui.graphics.ImageBitmap
import com.crstlnz.komikchino.data.firebase.model.ChapterHistoryItem
import com.crstlnz.komikchino.data.firebase.model.KomikHistoryItem


data class ChapterData(
    val id: String,
    val title: String,
    val slug: String,
    val imgs: List<String> = listOf(),
    val komik: KomikHistoryItem,
    val disqusConfig: DisqusConfig? = null
)

data class ChapterApi(
    val id: String,
    val title: String,
    val slug: String,
    val mangaId: String,
    val mangaSlug: String,
    val imgs: List<String> = listOf(),
    val disqusConfig: DisqusConfig? = null
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