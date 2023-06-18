package com.crstlnz.komikchino.data.model

import android.graphics.Bitmap
import android.media.Image
import androidx.compose.ui.graphics.ImageBitmap
import com.crstlnz.komikchino.data.database.readhistory.model.ReadHistoryItem
import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.coroutines.flow.MutableStateFlow

data class ChapterModel(
    val id: Int = 0,
    val komikId: Int = 0,
    val title: String = "",
    @JsonProperty("name") val imgs: List<String> = listOf()
)

data class ChapterHistoryData(
    val id: Int = 0,
    val readHistory: ReadHistoryItem? = null
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