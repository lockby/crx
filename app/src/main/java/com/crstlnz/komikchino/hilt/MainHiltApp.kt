package com.crstlnz.komikchino.hilt

import android.app.Application
import android.os.Build.VERSION.SDK_INT
import android.util.Log
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import coil3.memory.MemoryCache
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.CachePolicy
import com.crstlnz.komikchino.config.AppSettings
import com.crstlnz.komikchino.config.IMAGE_CACHE_PATH
import com.crstlnz.komikchino.data.util.getImageClient
import com.crstlnz.komikchino.data.util.getImageClientCoil3
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainHiltApp : Application(), SingletonImageLoader.Factory {
    override fun newImageLoader(context: PlatformContext): ImageLoader {
        Log.d("IMAGELOADER", AppSettings.komikServer?.title.toString())
        return ImageLoader
            .Builder(this)
            .components {
                add(factory = OkHttpNetworkFetcherFactory(getImageClientCoil3(AppSettings.komikServer!!)))
                if (SDK_INT >= 28) {
                    add(AnimatedImageDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(this, 0.4)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve(IMAGE_CACHE_PATH))
                    .maxSizePercent(0.1)
                    .build()
            }
            .build()
    }

}