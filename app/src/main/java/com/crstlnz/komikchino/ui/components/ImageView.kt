package com.crstlnz.komikchino.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.crstlnz.komikchino.config.AppSettings

@Composable
fun ImageView(
    url: String,
    modifier: Modifier = Modifier,
    applyImageRequest: (ImageRequest.Builder) -> ImageRequest.Builder = { it },
    contentDescription: String?,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val imageRequest = ImageRequest.Builder(LocalContext.current)
        .data(url)
        .crossfade(true)
        .size(Size.ORIGINAL) // Set the target size to load the image at.

    val painter = rememberAsyncImagePainter(
        imageLoader = AppSettings.imageLoader!!,
        model = applyImageRequest(imageRequest)
            .build()
    )

    Image(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier.background(color = MaterialTheme.colorScheme.surfaceVariant),
        contentScale = contentScale
    )
}