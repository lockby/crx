package com.crstlnz.komikchino.ui.navigations

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.crstlnz.komikchino.config.AppSettings
import com.crstlnz.komikchino.data.database.komik.KomikHistoryItem
import com.crstlnz.komikchino.data.datastore.KomikServer
import com.crstlnz.komikchino.data.util.convertToStringURL
import com.crstlnz.komikchino.ui.screens.WebViewScreen
import com.crstlnz.komikchino.ui.screens.chapter.ChapterScreen
import com.crstlnz.komikchino.ui.screens.home.HomeScreen
import com.crstlnz.komikchino.ui.screens.home.fragments.settings.sub.ServerSelectScreen
import com.crstlnz.komikchino.ui.screens.komikdetail.KomikScreen
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
    const val KOMIKDETAIL = "komik"
    const val SEARCH = "search"
    const val CHAPTER = "chapter"
    const val WEBVIEW = "webview"
    const val SERVER_SELECTION = "server_select"

    fun toChapter(
        navController: NavController,
        chapterId: String,
        chapterTitle: String,
        komikData: KomikHistoryItem
    ) {
        navController.navigate(
            "${CHAPTER}/${convertToStringURL(chapterId)}/${chapterTitle}/${
                convertToStringURL(
                    komikData
                )
            }",
        )
    }

    fun toWebView(navController: NavController, url: String) {
        navController.navigate(
            "${WEBVIEW}/${
                URLEncoder.encode(
                    url, "utf-8"
                )
            }"
        )
    }

    fun toCommentView(
        navController: NavController, slug: String, title: String, type: ContentType
    ) {
        if (AppSettings.komikServer == KomikServer.KIRYUU) {
            toWebView(
                navController,
                "file:///android_asset/disqus.html?id=${slug}&title=${title}&type=${type}"
            )
        } else {
            val split = slug.split("/")
            val mangaSlug = split.getOrNull(0)?.split(".")?.getOrNull(0) ?: ""
            val chapterId = split.getOrNull(1)
            val id = if (chapterId != null) "$mangaSlug/$chapterId" else mangaSlug
            toWebView(
                navController,
                "file:///android_asset/mangakatanadisqus.html?url=https://mangakatana.com/manga/${slug}&id=${id}"
            )
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
        "${MainNavigation.CHAPTER}/{id}/{title}/{komikdata}",
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
//        ChapterScreen(navController, "Chapter 1", 147943, "solo-leveling")
    }


    composable(
        "${MainNavigation.WEBVIEW}/{url}"
    ) {
        val context = LocalContext.current
        WebViewScreen(
            URLDecoder.decode(it.arguments?.getString("url") ?: "", "UTF-8"), onBackPressed = {
                navController.popBackStack()
            }, title = context.getString(context.applicationInfo.labelRes)
        )
    }

    composable(MainNavigation.SERVER_SELECTION) {
        ServerSelectScreen(navController)
    }
}