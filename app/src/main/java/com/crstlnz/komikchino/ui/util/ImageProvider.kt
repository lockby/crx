package com.crstlnz.komikchino.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import coil.ImageLoader

//fun createImageLoader(context: Context): ImageLoader {
//    return ImageLoader.Builder(context)
//        .okHttpClient(customHTTPClient)
//        .build()
//}

val LocalImageLoader = staticCompositionLocalOf<ImageLoader> {
    error("No ImageLoader provided")
}

@Composable
fun ProvideImageLoader(imageLoader: ImageLoader, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalImageLoader provides imageLoader) {
        content()
    }
}