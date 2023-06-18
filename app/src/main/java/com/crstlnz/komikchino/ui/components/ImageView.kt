package com.crstlnz.komikchino.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.RequestOptions.bitmapTransform
import com.crstlnz.komikchino.data.util.BlurTransformation
import com.crstlnz.komikchino.ui.theme.Black1
import com.crstlnz.komikchino.ui.theme.Black2
import com.crstlnz.komikchino.ui.util.defaultPlaceholder
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.animation.crossfade.CrossfadePlugin
import com.skydoves.landscapist.components.rememberImageComponent
import com.skydoves.landscapist.glide.GlideImage
import com.skydoves.landscapist.placeholder.shimmer.ShimmerPlugin

@Composable
fun ImageView(
    url: String,
    modifier: Modifier = Modifier,
    imageOptions: ImageOptions = ImageOptions(),
    requestOptions: (requestOptions: RequestOptions) -> RequestOptions = { it }
) {
    GlideImage(
        imageModel = { url },
        component = rememberImageComponent {
            +CrossfadePlugin(
                duration = 800
            )
            +ShimmerPlugin(
                baseColor = MaterialTheme.colors.primary,
                highlightColor = MaterialTheme.colors.secondary
            )
        },
        imageOptions = imageOptions.copy(
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center
        ),
        requestOptions = {
            requestOptions(
                RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
            )
        },
        failure = {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(color = Black2)
            )
        },
//        loading = {
//            Surface(
//                Modifier
//                    .defaultPlaceholder(color = MaterialTheme.colors.primary)
//                    .fillMaxSize()
//            ) {
//            }
//        },
        modifier = modifier,
    )
}