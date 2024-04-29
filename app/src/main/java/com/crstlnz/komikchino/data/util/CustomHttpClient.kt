package com.crstlnz.komikchino.data.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.util.LruCache
import coil.ImageLoader
import coil.decode.DataSource
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.ErrorResult
import coil.request.ImageResult
import coil.request.SuccessResult
import com.crstlnz.komikchino.config.AppSettings
import com.crstlnz.komikchino.config.IMAGE_CACHE_PATH
import com.crstlnz.komikchino.data.api.KomikServer
import okhttp3.ConnectionSpec
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

const val MAX_BITMAP_SIZE = 100 * 1024 * 1024 // 100 MB

class UrlLoggingInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        val url = request.url.toString()
        println("Request URL: $url")
        return chain.proceed(request)
    }
}

//const val requests = Map<>

class SameUrlBlocker : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        val url = request.url.toString()
        println("Request URL: $url")
        return chain.proceed(request)
    }
}

fun getCustomHttpClient(): OkHttpClient {
    return try {
        // Create a trust manager that does not validate certificate chains
        val trustAllCerts =
            arrayOf<TrustManager>(
                @SuppressLint("CustomX509TrustManager")
                object : X509TrustManager {
                    @SuppressLint("TrustAllX509TrustManager")
                    @Throws(CertificateException::class)
                    override fun checkClientTrusted(
                        chain: Array<X509Certificate>,
                        authType: String
                    ) {
                    }

                    @SuppressLint("TrustAllX509TrustManager")
                    @Throws(CertificateException::class)
                    override fun checkServerTrusted(
                        chain: Array<X509Certificate>,
                        authType: String
                    ) {
                    }

                    override fun getAcceptedIssuers(): Array<X509Certificate> {
                        return arrayOf()
                    }
                }
            )

        // Install the all-trusting trust manager
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())
        // Create an ssl socket factory with our all-trusting manager
        val sslSocketFactory: SSLSocketFactory = sslContext.socketFactory
        val builder = OkHttpClient.Builder()
        builder.sslSocketFactory(
            sslSocketFactory,
            trustAllCerts[0] as X509TrustManager
        )
        builder.hostnameVerifier(HostnameVerifier { hostname, session -> true })
        builder.followRedirects(true)
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
            .followSslRedirects(true)
            .followRedirects(true) // Enable automatic following of redirects
            .cookieJar(AppSettings.cookieJar)
            .addInterceptor(UrlLoggingInterceptor())
            .addInterceptor(RequestHeaderInterceptor())
            .addInterceptor(HttpErrorInterceptor())
        builder.build()
    } catch (e: Exception) {
        throw RuntimeException(e)
    }
//        AppSettings.downloadDir =
//            File(
//                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
//                "/komikchino"
//            ).path
//        val dlViewModel: DownloadViewModel by viewModels()
//        AppSettings.downloadViewModel = dlViewModel
//    return OkHttpClient.Builder()
//        .connectionSpecs(
//            listOf(
//                ConnectionSpec.COMPATIBLE_TLS,
//                ConnectionSpec.CLEARTEXT,
//                ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
//                    .allEnabledTlsVersions()
//                    .allEnabledCipherSuites()
//                    .build()
//            )
//        )
//        .followRedirects(true) // Enable automatic following of redirects
//        .followSslRedirects(true)
//        .cookieJar(AppSettings.cookieJar)
//        .addInterceptor(UrlLoggingInterceptor())
//        .addInterceptor(RequestHeaderInterceptor())
//        .addInterceptor(HttpErrorInterceptor())
//        .build()
}

class CustomCacheCoil(
    private val cache: LruCache<String, Drawable>
) : coil.intercept.Interceptor {

    override suspend fun intercept(chain: coil.intercept.Interceptor.Chain): ImageResult {
        val value = cache.get(chain.request.data.toString())
        if (value != null) {
            return SuccessResult(
                drawable = value,
                request = chain.request,
                dataSource = DataSource.MEMORY_CACHE
            )
        }
        return chain.proceed(chain.request)
    }
}

val cache = LruCache<String, Drawable>(100)
fun getImageLoader(context: Context): ImageLoader {
    return ImageLoader
        .Builder(context)
        .components {
            add(CustomCacheCoil(cache))
        }
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
        .memoryCache {
            MemoryCache.Builder(context)
                .maxSizePercent(0.4)
                .build()
        }
        .diskCache {
            DiskCache.Builder()
                .directory(context.cacheDir.resolve(IMAGE_CACHE_PATH))
                .maxSizePercent(0.1)
                .build()
        }
        .respectCacheHeaders(false)
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