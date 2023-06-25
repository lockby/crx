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

@Composable
fun ChapterImageView(data: ChapterImage, onDisableHardware: () -> Unit = {}) {
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current).data(data.url)
            .crossfade(true)
            .setHeader("Content-Type", "image/jpeg")
            .allowHardware(data.useHardware)
            .size(Size.ORIGINAL)
            .decoderFactory(if (SDK_INT >= 28) ImageDecoderDecoder.Factory() else GifDecoder.Factory())
            .build()
    )

    if (painter.state is AsyncImagePainter.State.Error) {
        if (data.useHardware) {
            onDisableHardware()
        }
//        val error = painter.state as AsyncImagePainter.State.Error
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(5f / 7f),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painterResource(id = R.drawable.error_placeholder),
                contentDescription = "Error Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth()
            )
        }
    } else if (painter.state is AsyncImagePainter.State.Loading) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(5f / 7f),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painterResource(id = R.drawable.loading_placeholder),
                contentDescription = "Loading Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth(0.8f)
            )
        }
    } else {
        Image(painter = painter,
            contentDescription = "Komik Images",
            modifier = Modifier
                .fillMaxWidth()
                .then((painter.state as? AsyncImagePainter.State.Success)?.painter?.intrinsicSize?.let { intrinsicSize ->
                    Modifier.aspectRatio(intrinsicSize.width / intrinsicSize.height)
                } ?: Modifier.aspectRatio(5f / 7f)))
    }

}
