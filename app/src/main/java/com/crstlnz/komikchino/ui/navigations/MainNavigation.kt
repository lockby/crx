package com.crstlnz.komikchino.ui.navigations

import android.util.Log
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.crstlnz.komikchino.config.AppSettings
import com.crstlnz.komikchino.data.api.KomikServer
import com.crstlnz.komikchino.data.firebase.model.KomikHistoryItem
import com.crstlnz.komikchino.data.util.convertToStringURL
import com.crstlnz.komikchino.data.util.decodeBase64
import com.crstlnz.komikchino.ui.screens.CommentScreen
import com.crstlnz.komikchino.ui.screens.LoginScreen
import com.crstlnz.komikchino.ui.screens.UnblockCloudflare
import com.crstlnz.komikchino.ui.screens.chapter.ChapterScreen
import com.crstlnz.komikchino.ui.screens.download.DownloadDetailScreen
import com.crstlnz.komikchino.ui.screens.download.DownloadScreen
import com.crstlnz.komikchino.ui.screens.download.DownloadSelectScreen
import com.crstlnz.komikchino.ui.screens.home.HomeScreen
import com.crstlnz.komikchino.ui.screens.home.fragments.settings.sub.AppInfoScreen
import com.crstlnz.komikchino.ui.screens.home.fragments.settings.sub.CacheScreen
import com.crstlnz.komikchino.ui.screens.home.fragments.settings.sub.HomeSelection
import com.crstlnz.komikchino.ui.screens.home.fragments.settings.sub.ServerSelectScreen
import com.crstlnz.komikchino.ui.screens.home.fragments.settings.sub.checkupdate.CheckUpdateScreen
import com.crstlnz.komikchino.ui.screens.komikdetail.KomikScreen
import com.crstlnz.komikchino.ui.screens.permissions.WriteExternalStorageScreen
import com.crstlnz.komikchino.ui.screens.search.SearchScreen
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.ArrayList

enum class ContentType(private val value: String) {
    CHAPTER("chapter"), MANGA("manga");

    override fun toString(): String {
        return value
    }
}

object MainNavigation {
    const val HOME = "home"
    const val LOGIN = "login"
    const val KOMIKDETAIL = "komik"
    const val SEARCH = "search"
    const val CHAPTER = "chapter"
    const val WEBVIEW = "webview"
    const val CLOUDFLARE_UNBLOCK = "cloudflare_unblock"
    const val DOWNLOAD_SCREEN = "download_screen"
    const val DOWNLOAD_SELECT_SCREEN = "download_screen"
    const val DOWNLOAD_DETAIL = "download_detail"

    const val CHECK_UPDATE = "update"

    const val SERVER_SELECTION = "server_select"
    const val HOME_SELECTION = "home_select"
    const val CACHE_SCREEN = "cache_screen"
    const val APP_INFO = "app_info"

    const val STORAGE_REQUEST = "storage_request"
    fun toChapter(
        chapterId: String,
        chapterTitle: String,
        komikData: KomikHistoryItem,
    ): String {
        return "${CHAPTER}/id/${convertToStringURL(chapterId).ifEmpty { "0" }}/${chapterTitle.ifEmpty { "0" }}/${
            convertToStringURL(
                komikData
            ).ifEmpty { "0" }
        }"
    }

    fun toLogin(navigateTo: (to: String, builder: NavOptionsBuilder.() -> Unit) -> Unit) {
        navigateTo(LOGIN) {
            popUpTo(LOGIN) {
                inclusive = true
            }
        }
    }

    fun toChapter(
        chapterSlug: String,
        chapterTitle: String,
    ): String {
        return "${CHAPTER}/slug/${convertToStringURL(chapterSlug).ifEmpty { "0" }}/${chapterTitle.ifEmpty { "0" }}"
    }

    fun unblockCloudflare(url: String): String {
        return "${CLOUDFLARE_UNBLOCK}/${URLEncoder.encode(url, "utf-8")}"
    }

    fun toWebView(url: String, title: String? = null): String {
        return if (title.isNullOrEmpty()) {
            "${WEBVIEW}/${URLEncoder.encode(url, "UTF-8")}"
        } else {
            "${WEBVIEW}/${title}/${URLEncoder.encode(url, "UTF-8")}"
        }
    }

    fun toCommentView(
        slug: String, title: String, url: String = "", type: ContentType = ContentType.MANGA
    ): String {
        when (AppSettings.komikServer) {
            KomikServer.KIRYUU -> {
                return toWebView(
                    "file:///android_asset/kiryuudisqus.html?id=${slug}&title=${title}&type=${type}",
                    title
                )
            }

            KomikServer.MANGAKATANA -> {
                val split = slug.split("/")
                val mangaSlug = split.getOrNull(0)?.split(".")?.getOrNull(0) ?: ""
                val chapterId = split.getOrNull(1)
                val id = if (chapterId != null) "$mangaSlug/$chapterId" else mangaSlug
                return toWebView(
                    "file:///android_asset/mangakatanadisqus.html?url=https://mangakatana.com/manga/${slug}&id=${id}&title=${title}",
                    title
                )
            }

            KomikServer.VOIDSCANS -> {
                return toWebView(
                    "file:///android_asset/voidscansdisqus.html?id=${
                        decodeBase64(slug)
                    }&title=${title.ifEmpty { "Empty Title" }}&url=${
                        decodeBase64(url)
                    }", title
                )
            }

            KomikServer.MANHWALIST -> {
                return toWebView(
                    "file:///android_asset/manhwalistdisqus.html?id=${
                        decodeBase64(slug)
                    }&title=${title.ifEmpty { "Empty Title" }}&url=${
                        decodeBase64(url)
                    }", title
                )
            }

//            KomikServer.COSMICSCANS -> {
//                return toWebView(
//                    "file:///android_asset/cosmicscansdisqus.html?id=${
//                        decodeBase64(slug)
//                    }&title=${title.ifEmpty { "Empty Title" }}&url=${
//                        decodeBase64(url)
//                    }", title
//                )
//            }

            KomikServer.COSMICSCANSINDO -> {
                return toWebView(
                    "file:///android_asset/cosmicscansindonesiadisqus.html?id=${
                        decodeBase64(slug)
                    }&title=${title.ifEmpty { "Empty Title" }}&url=${
                        decodeBase64(url)
                    }", title
                )
            }


            KomikServer.MIRRORKOMIK -> {
                return toWebView(
                    "file:///android_asset/mirrorkomik.html?id=${
                        decodeBase64(slug)
                    }&title=${title.ifEmpty { "Empty Title" }}&url=${
                        decodeBase64(url)
                    }", title
                )
            }

            else -> {
                return HOME
            }
        }
    }

    fun toKomik(
        title: String,
        slug: String,
    ): String {
        return "${KOMIKDETAIL}/${title}/${URLEncoder.encode(slug, "UTF-8")}"
    }

    fun toDownloadSelect(
        title: String,
        slug: String,
    ): String {
        return "${DOWNLOAD_SELECT_SCREEN}/${title}/${URLEncoder.encode(slug, "UTF-8")}"
    }

    fun toDownloadDetail(
        title: String,
        slug: String,
    ): String {
        return "${DOWNLOAD_DETAIL}/${title}/${URLEncoder.encode(slug, "UTF-8")}"
    }
}

fun NavGraphBuilder.addMainNavigation(navigateTo: (to: String) -> Unit, onBack: () -> Unit) {
    composable(
        MainNavigation.HOME,
    ) {
        HomeScreen(navigateTo)
    }

    composable(
        MainNavigation.LOGIN,
    ) {
        LoginScreen(navigateTo)
    }

    composable(
        MainNavigation.SEARCH,
    ) {
        SearchScreen(navigateTo, onBack)
    }

    composable(
        "${MainNavigation.KOMIKDETAIL}/{title}/{slug}",
    ) {
        val title = it.arguments?.getString("title")
        KomikScreen(navigateTo, onBack, title ?: "")
    }

    composable(
        "${MainNavigation.CHAPTER}/slug/{slug}/{title}",
        arguments = listOf(navArgument("title") {
            type = NavType.StringType
        }, navArgument("slug") {
            type = NavType.StringType
        }),
    ) {
        val title = it.arguments?.getString("title")
        ChapterScreen(navigateTo, onBack, title ?: "")
    }

    composable(
        "${MainNavigation.CHAPTER}/id/{id}/{title}/{komikdata}",
        arguments = listOf(navArgument("title") {
            type = NavType.StringType
        }, navArgument("id") {
            type = NavType.StringType
        }, navArgument("komikdata") {
            type = NavType.StringType
        }),
    ) {
        val title = it.arguments?.getString("title")
        ChapterScreen(navigateTo, onBack, title ?: "")
    }


    composable(
        "${MainNavigation.WEBVIEW}/{title}/{url}"
    ) {
        CommentScreen(URLDecoder.decode(it.arguments?.getString("url") ?: "", "UTF-8"),
            it.arguments?.getString("title") ?: "",
            onBackPressed = {
                onBack()
            })
    }

    composable(
        "${MainNavigation.WEBVIEW}/{url}"
    ) {
        CommentScreen(
            URLDecoder.decode(it.arguments?.getString("url") ?: "", "UTF-8"), onBackPressed = onBack
        )
    }

    composable(
        "${MainNavigation.CLOUDFLARE_UNBLOCK}/{url}"
    ) {
        UnblockCloudflare(
            onBack,
            URLDecoder.decode(it.arguments?.getString("url") ?: "", "UTF-8"),
            onBackPressed = onBack
        )
    }

    composable(MainNavigation.CHECK_UPDATE) {
        CheckUpdateScreen(onBack)
    }

    composable(MainNavigation.STORAGE_REQUEST) {
        WriteExternalStorageScreen(onDismiss = onBack)
    }

    composable(MainNavigation.SERVER_SELECTION) {
        ServerSelectScreen(onBack)
    }

    composable(MainNavigation.HOME_SELECTION) {
        HomeSelection(onBack)
    }

    composable(MainNavigation.CACHE_SCREEN) {
        CacheScreen(onBack)
    }

    composable(MainNavigation.APP_INFO) {
        AppInfoScreen()
    }

    composable(MainNavigation.DOWNLOAD_SCREEN) {
        DownloadScreen()
    }

    composable(
        "${MainNavigation.DOWNLOAD_SELECT_SCREEN}/{title}/{slug}",
    ) {
        val title = it.arguments?.getString("title")
        DownloadSelectScreen(onBack, title ?: "")
    }

    composable(
        "${MainNavigation.DOWNLOAD_DETAIL}/{title}/{id}",
    ) {
        val title = it.arguments?.getString("title")
        DownloadDetailScreen(onBack, title ?: "")
    }
}