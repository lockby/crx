package com.crstlnz.komikchino.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.imageLoader
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

//    AsyncImage(
//        model = applyImageRequest(imageRequest).build(),
//        contentDescription = contentDescription,
//        imageLoader = AppSettings.imageLoader!!,
//        modifier.background(color = MaterialTheme.colorScheme.surfaceVariant),
//        contentScale = contentScale
//    )
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
//    AsyncImage(
//        model = url,
//        modifier = modifier.background(color= Blue),
//        contentDescription = contentDescription,
//        contentScale = ContentScale.Crop,
//        alignment = Alignment.Center
//        component = rememberImageComponent {
//            +CrossfadePlugin(
//                duration = 800
//            )
//            +ShimmerPlugin(
//                baseColor = MaterialTheme.colorScheme.surfaceVariant,
//                highlightColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
//            )
//        },
//        imageOptions = imageOptions.copy(
//            contentScale = ContentScale.Crop,
//            alignment = Alignment.Center
//        ),
//        requestOptions = {
//            requestOptions(
//                RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
//            )
//        },
//        failure = {
//            Box(
//                modifier
//                    .fillMaxSize()
//                    .background(color = Black2)
//            )
//        },
//        loading = {
//            Surface(
//                Modifier
//                    .defaultPlaceholder(color = MaterialTheme.colors.primary)
//                    .fillMaxSize()
//            ) {
//            }
//        },
//    )
}