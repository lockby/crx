package com.crstlnz.komikchino.ui.navigations

import android.util.Log
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.crstlnz.komikchino.config.AppSettings
import com.crstlnz.komikchino.data.api.KomikServer
import com.crstlnz.komikchino.data.database.model.KomikHistoryItem
import com.crstlnz.komikchino.data.util.convertToStringURL
import com.crstlnz.komikchino.data.util.decodeBase64
import com.crstlnz.komikchino.ui.screens.CommentScreen
import com.crstlnz.komikchino.ui.screens.LoginScreen
import com.crstlnz.komikchino.ui.screens.UnblockCloudflare
import com.crstlnz.komikchino.ui.screens.chapter.ChapterScreen
import com.crstlnz.komikchino.ui.screens.home.HomeScreen
import com.crstlnz.komikchino.ui.screens.home.fragments.settings.sub.AppInfoScreen
import com.crstlnz.komikchino.ui.screens.home.fragments.settings.sub.CacheScreen
import com.crstlnz.komikchino.ui.screens.home.fragments.settings.sub.HomeSelection
import com.crstlnz.komikchino.ui.screens.home.fragments.settings.sub.ServerSelectScreen
import com.crstlnz.komikchino.ui.screens.home.fragments.settings.sub.checkupdate.CheckUpdateScreen
import com.crstlnz.komikchino.ui.screens.komikdetail.KomikScreen
import com.crstlnz.komikchino.ui.screens.permissions.WriteExternalStorageScreen
import com.crstlnz.komikchino.ui.screens.search.SearchScreen
import com.google.accompanist.navigation.animation.composable
import java.net.URLDecoder
import java.net.URLEncoder

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

    const val CHECK_UPDATE = "update"

    const val SERVER_SELECTION = "server_select"
    const val HOME_SELECTION = "home_select"
    const val CACHE_SCREEN = "cache_screen"
    const val APP_INFO = "app_info"

    const val STORAGE_REQUEST = "storage_request"
    fun toChapter(
        navController: NavController,
        chapterId: String,
        chapterTitle: String,
        komikData: KomikHistoryItem,
    ) {
        navController.navigate(
            "${CHAPTER}/id/${convertToStringURL(chapterId).ifEmpty { "0" }}/${chapterTitle.ifEmpty { "0" }}/${
                convertToStringURL(
                    komikData
                ).ifEmpty { "0" }
            }",
        )
    }

    fun toHome(navController: NavController) {
        navController.navigate(HOME) {
            popUpTo(HOME) {
                inclusive = true
            }
        }
    }

    fun toLogin(navController: NavController) {
        navController.navigate(LOGIN) {
            popUpTo(LOGIN) {
                inclusive = true
            }
        }
    }

    fun toChapter(
        navController: NavController,
        chapterSlug: String,
        chapterTitle: String,
    ) {
        navController.navigate(
            "${CHAPTER}/slug/${convertToStringURL(chapterSlug).ifEmpty { "0" }}/${chapterTitle.ifEmpty { "0" }}",
        )
    }

    fun unblockCloudflare(navController: NavController, url: String) {
        navController.navigate("${CLOUDFLARE_UNBLOCK}/${URLEncoder.encode(url, "utf-8")}")
    }

    fun toWebView(navController: NavController, url: String, title: String? = null) {
        val route = if (title.isNullOrEmpty()) {
            "${WEBVIEW}/${URLEncoder.encode(url, "UTF-8")}"
        } else {
            "${WEBVIEW}/${title}/${URLEncoder.encode(url, "UTF-8")}"
        }
        navController.navigate(route)
    }

    fun toCommentView(
        navController: NavController,
        slug: String,
        title: String,
        url: String = "",
        type: ContentType = ContentType.MANGA
    ) {
        when (AppSettings.komikServer) {
            KomikServer.KIRYUU -> {
                toWebView(
                    navController,
                    "file:///android_asset/kiryuudisqus.html?id=${slug}&title=${title}&type=${type}",
                    title
                )
            }

            KomikServer.MANGAKATANA -> {
                val split = slug.split("/")
                val mangaSlug = split.getOrNull(0)?.split(".")?.getOrNull(0) ?: ""
                val chapterId = split.getOrNull(1)
                val id = if (chapterId != null) "$mangaSlug/$chapterId" else mangaSlug
                Log.d("SLUG DISQUS", slug)
                Log.d("ID DISQUS", id)
                toWebView(
                    navController,
                    "file:///android_asset/mangakatanadisqus.html?url=https://mangakatana.com/manga/${slug}&id=${id}&title=${title}",
                    title
                )
            }

            KomikServer.VOIDSCANS -> {
                toWebView(
                    navController,
                    "file:///android_asset/voidscansdisqus.html?id=${
                        decodeBase64(slug)
                    }&title=${title.ifEmpty { "Empty Title" }}&url=${
                        decodeBase64(url)
                    }",
                    title
                )
            }

            KomikServer.MANHWALIST -> {
                toWebView(
                    navController,
                    "file:///android_asset/manhwalistdisqus.html?id=${
                        decodeBase64(slug)
                    }&title=${title.ifEmpty { "Empty Title" }}&url=${
                        decodeBase64(url)
                    }",
                    title
                )
            }

            KomikServer.COSMICSCANS -> {
                toWebView(
                    navController,
                    "file:///android_asset/cosmicscansdisqus.html?id=${
                        decodeBase64(slug)
                    }&title=${title.ifEmpty { "Empty Title" }}&url=${
                        decodeBase64(url)
                    }",
                    title
                )
            }

            else -> {}
        }
    }

    fun toKomik(
        navController: NavController,
        title: String,
        slug: String,
    ) {
        navController.navigate(
            "${KOMIKDETAIL}/${title}/${slug}",
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.addMainNavigation(navController: NavHostController) {
    composable(
        MainNavigation.HOME,
    ) {
        HomeScreen(navController)
    }

    composable(
        MainNavigation.LOGIN,
    ) {
        LoginScreen(navController)
    }

    composable(
        MainNavigation.SEARCH,
    ) {
        SearchScreen(navController)
    }

    composable(
        "${MainNavigation.KOMIKDETAIL}/{title}/{slug}",
    ) {
        val title = it.arguments?.getString("title")
        KomikScreen(navController, title ?: "")
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
        ChapterScreen(navController, title ?: "")
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
        ChapterScreen(navController, title ?: "")
    }


    composable(
        "${MainNavigation.WEBVIEW}/{title}/{url}"
    ) {
        val context = LocalContext.current
        CommentScreen(
            URLDecoder.decode(it.arguments?.getString("url") ?: "", "UTF-8"),
            it.arguments?.getString("title") ?: "",
            onBackPressed = {
                navController.popBackStack()
            }
        )
    }

    composable(
        "${MainNavigation.WEBVIEW}/{url}"
    ) {
        CommentScreen(
            URLDecoder.decode(it.arguments?.getString("url") ?: "", "UTF-8"), onBackPressed = {
                navController.popBackStack()
            }
        )
    }

    composable(
        "${MainNavigation.CLOUDFLARE_UNBLOCK}/{url}"
    ) {
        UnblockCloudflare(
            navController,
            URLDecoder.decode(it.arguments?.getString("url") ?: "", "UTF-8"), onBackPressed = {
                navController.popBackStack()
            }
        )
    }

    composable(MainNavigation.CHECK_UPDATE) {
        CheckUpdateScreen(navController)
    }

    composable(MainNavigation.STORAGE_REQUEST) {
        WriteExternalStorageScreen(onDismiss = {
            navController.popBackStack()
        })
    }

    composable(MainNavigation.SERVER_SELECTION) {
        ServerSelectScreen(navController)
    }

    composable(MainNavigation.HOME_SELECTION) {
        HomeSelection(navController)
    }

    composable(MainNavigation.CACHE_SCREEN) {
        CacheScreen(navController)
    }

    composable(MainNavigation.APP_INFO) {
        AppInfoScreen(navController)
    }
}