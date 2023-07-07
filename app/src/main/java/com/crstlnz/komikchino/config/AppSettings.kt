package com.crstlnz.komikchino.config

import coil.ImageLoader
import com.crstlnz.komikchino.data.api.KomikServer
import com.crstlnz.komikchino.data.model.CloudflareState
import com.crstlnz.komikchino.data.util.EmptyCookieJar
import com.crstlnz.komikchino.ui.navigations.HomeSections
import kotlinx.coroutines.flow.MutableStateFlow
import okhttp3.CookieJar
import okhttp3.OkHttpClient
import javax.inject.Singleton
import kotlin.random.Random

const val USER_DATA = "users"
const val SERVER = "server"

const val FAVORITES = "favorites"
const val KOMIK = "komik"
const val CHAPTER = "chapter"

const val IMAGE_CACHE_PATH = "image_cache"

@Singleton
object AppSettings {
    val homeDefaultRoute = HomeSections.HOME.route
    const val animationDuration = 180
    var komikServer: KomikServer? = null
    var homepage: HomeSections? = null
    private fun bannerURL(id: String): String {
        return "https://lockby.github.io/assets/img/$id.jpg"
    }

    val banner = bannerURL(Random.nextInt(1, 6).toString())

    var imageLoader: ImageLoader? = null
    var customHttpClient: OkHttpClient = OkHttpClient.Builder().build()

    var cookieJar: CookieJar = EmptyCookieJar()
    const val userAgent =
        "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.5735.130 Mobile Safari/537.36"

    //    val cloudflareBlock: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)
    val cloudflareState: MutableStateFlow<CloudflareState> =
        MutableStateFlow(CloudflareState(false, 0, false))
}