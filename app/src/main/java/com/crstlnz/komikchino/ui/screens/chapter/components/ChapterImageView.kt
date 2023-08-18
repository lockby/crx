package com.crstlnz.komikchino.ui.screens.chapter.components

import android.os.Build.VERSION.SDK_INT
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import coil.size.Dimension
import coil.size.Size
import com.crstlnz.komikchino.R
import com.crstlnz.komikchino.config.AppSettings
import com.crstlnz.komikchino.ui.theme.Black1
import com.crstlnz.komikchino.ui.theme.Black3

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
            .allowHardware(data.useHardware)
            .size(Size(LocalConfiguration.current.screenWidthDp, Dimension.Undefined))
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
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = R.drawable.app_icon),
                    contentDescription = "App Icon",
                    modifier = Modifier.size(110.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    "Page ${((data.key.toIntOrNull() ?: 0) + 1).toString().padStart(2, '0')}",
                    style = MaterialTheme.typography.titleMedium.copy(color = Black1.copy(alpha = 0.5f))
                )
            }
        }
    }
}
