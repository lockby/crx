package com.crstlnz.komikchino.ui.navigations

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.crstlnz.komikchino.ui.screens.chapter.ChapterScreen
import com.crstlnz.komikchino.ui.screens.home.HomeScreen
import com.crstlnz.komikchino.ui.screens.home.WebViewScreen
import com.crstlnz.komikchino.ui.screens.komikdetail.KomikScreen
import com.crstlnz.komikchino.ui.screens.search.SearchScreen
import com.google.accompanist.navigation.animation.composable
import java.net.URLDecoder


object MainNavigation {
    const val HOME = "home"
    const val KOMIKDETAIL = "komik"
    const val SEARCH = "search"
    const val CHAPTER = "chapter"
    const val WEBVIEW = "webview"
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
        val slug = it.arguments?.getString("slug")
        KomikScreen(navController, title ?: "", slug ?: "")
    }

    composable(
        "${MainNavigation.CHAPTER}/{mangaid}/{title}/{id}",
        arguments = listOf(
            navArgument("title") {
                NavType.StringType
            },
            navArgument("id") {
                NavType.StringType
            }
        )
    ) {
        val title = it.arguments?.getString("title")
        val mangaId = it.arguments?.getString("mangaid")?.toIntOrNull() ?: 0;
        val id = it.arguments?.getString("id")
        ChapterScreen(navController, title ?: "", id?.toIntOrNull() ?: 0, mangaId)
//        ChapterScreen(navController, "Chapter 1", 147943, "solo-leveling")
    }

//    composable(
//        MainNavigation.SETTINGS,
//    ) {
//        SettingsScreen(onBack = {
//            navController.popBackStack()
//        })
//    }
//
//    composable(
//        MainNavigation.RECENTDETAIL,
//    ) {
//        RecentDetailScreen()
//    }

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

//    composable(
//        MainNavigation.NOTIFICATION_REQUEST,
//    ) {
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
//            navController.popBackStack()
//        } else {
//            NotificationsRequestScreen(onDismiss = {
//                navController.popBackStack()
//            })
//        }
//    }

//    composable(
//        MainNavigation.NOTIFICATION_LISTENER_REQUEST,
//    ) {
//        NotificationListenerRequestScreen(onDismiss = {
//            navController.popBackStack()
//        })
//    }
}