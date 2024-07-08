package com.crstlnz.komikchino.ui.components

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.rememberAsyncImagePainter
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Size
import com.crstlnz.komikchino.config.AppSettings

@OptIn(ExperimentalCoilApi::class)
@Composable
fun ImageView(
    url: String,
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    applyImageRequest: (ImageRequest.Builder) -> ImageRequest.Builder = { it },
    contentDescription: String?,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val imageRequest = ImageRequest.Builder(LocalContext.current)
        .data(url)
        .crossfade(true)
//        .httpHeaders(NetworkHeaders.Builder().add("Referer", AppSettings.komikServer!!.url).build())
//        .decoderFactory(if (Build.VERSION.SDK_INT >= 28) ImageDecoderDecoder.Factory() else GifDecoder.Factory())
        .size(Size.ORIGINAL) // Set the target size to load the image at.

    val painter = rememberAsyncImagePainter(
        model = applyImageRequest(imageRequest)
            .build()
    )

    Image(
        painter = painter,
        contentDescription = contentDescription,
        modifier = Modifier
            .clip(shape)
            .background(color = MaterialTheme.colorScheme.surfaceVariant)
            .then(modifier),
        contentScale = contentScale
    )
}