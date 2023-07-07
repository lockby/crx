package com.crstlnz.komikchino.ui.screens.chapter.components

import android.os.Build.VERSION.SDK_INT
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import coil.size.Size
import com.crstlnz.komikchino.R
import com.crstlnz.komikchino.config.AppSettings

@Composable
fun ChapterImageView(
    data: ChapterImage,
    onDisableHardware: () -> Unit = {},
    onImageSizeCalculated: (height: Float, width: Float) -> Unit = { _, _ -> },
    defaultAspectRatio: Float = 5f / 8f
) {
    val painter = rememberAsyncImagePainter(
        imageLoader = AppSettings.imageLoader!!,
        model = ImageRequest.Builder(LocalContext.current).data(data.url)
            .crossfade(true)
            .setHeader("Content-Type", "image/jpeg")
            .allowHardware(data.useHardware)
            .size(Size.ORIGINAL)
            .decoderFactory(if (SDK_INT >= 28) ImageDecoderDecoder.Factory() else GifDecoder.Factory())
            .build()
    )

    if (painter.state is AsyncImagePainter.State.Success) {
        val intrinsicSize =
            (painter.state as? AsyncImagePainter.State.Success)?.painter?.intrinsicSize
        Image(
            painter = painter,
            contentDescription = "Komik Images",
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    Modifier.aspectRatio(if (intrinsicSize != null) intrinsicSize.width / intrinsicSize.height else defaultAspectRatio)
                )
        )
        if (intrinsicSize != null)
            onImageSizeCalculated(intrinsicSize.height, intrinsicSize.width)
    } else if (painter.state is AsyncImagePainter.State.Error && !data.useHardware) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(defaultAspectRatio),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painterResource(id = R.drawable.error_placeholder),
                contentDescription = "Error Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth()
            )
        }
    } else {
        if (painter.state is AsyncImagePainter.State.Error) onDisableHardware()
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(defaultAspectRatio),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painterResource(id = R.drawable.loading_placeholder),
                contentDescription = "Loading Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth(0.8f)
            )
        }
    }
}
