package com.crstlnz.komikchino.data.util

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import coil.ImageLoader
import coil.disk.DiskCache
import coil.request.ErrorResult
import com.crstlnz.komikchino.MAX_BITMAP_SIZE
import com.crstlnz.komikchino.UrlLoggingInterceptor
import com.crstlnz.komikchino.config.AppSettings
import com.crstlnz.komikchino.config.IMAGE_CACHE_PATH
import com.crstlnz.komikchino.data.api.KomikServer
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient

fun getCustomHttpClient(): OkHttpClient {
//        AppSettings.downloadDir =
//            File(
//                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
//                "/komikchino"
//            ).path
//        val dlViewModel: DownloadViewModel by viewModels()
//        AppSettings.downloadViewModel = dlViewModel
    return OkHttpClient.Builder()
        .connectionSpecs(
            listOf(
                ConnectionSpec.COMPATIBLE_TLS,
                ConnectionSpec.CLEARTEXT,
                ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                    .allEnabledTlsVersions()
                    .allEnabledCipherSuites()
                    .build()
            )
        )
        .followRedirects(true) // Enable automatic following of redirects
        .followSslRedirects(true)
        .cookieJar(AppSettings.cookieJar)
        .addInterceptor(HttpErrorInterceptor())
        .addInterceptor(RequestHeaderInterceptor())
        .addInterceptor(UrlLoggingInterceptor())
        .build()
}

fun getImageLoader(context: Context): ImageLoader {
    return ImageLoader
        .Builder(context)
        .okHttpClient(getImageClient(AppSettings.komikServer!!))
        .components {
            add { chain ->
                val request = chain.request
                val result = chain.proceed(request)
                val bitmap = (result.drawable as? BitmapDrawable)?.bitmap
                if (bitmap != null && bitmap.byteCount >= MAX_BITMAP_SIZE) {
                    ErrorResult(
                        request.error,
                        request,
                        RuntimeException("Bitmap is too large (${bitmap.byteCount} bytes)")
                    )
                } else {
                    result
                }

            }
        }
        .diskCache {
            DiskCache.Builder()
                .directory(context.cacheDir.resolve(IMAGE_CACHE_PATH))
                .maxSizePercent(0.05)
                .build()
        }
        .build()
}

fun getImageClient(komikServer: KomikServer): OkHttpClient {
    if (komikServer === KomikServer.COSMICSCANSINDO || komikServer === KomikServer.COSMICSCANS) {
        return AppSettings.customHttpClient.newBuilder().addInterceptor {
            val newRequest = it.request().newBuilder().addHeader(
                "Accept", "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8"
            ).build()
            it.proceed(newRequest)
        }.build()

    }
    return AppSettings.customHttpClient
}