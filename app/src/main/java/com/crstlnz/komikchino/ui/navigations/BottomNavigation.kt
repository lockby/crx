package com.crstlnz.komikchino.ui.navigations

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.crstlnz.komikchino.R
import com.crstlnz.komikchino.ui.screens.home.fragments.bookmark.BookmarkScreen
import com.crstlnz.komikchino.ui.screens.home.fragments.genre.GenreScreen
import com.crstlnz.komikchino.ui.screens.home.fragments.home.HomeFragment
import com.crstlnz.komikchino.ui.screens.home.fragments.settings.SettingScreen

enum class HomeSections(
    @StringRes val title: Int,
    private val icon: Any,
    private val selectedIcon: Any? = null,
    val route: String
) {
    HOME(R.string.home, Icons.Outlined.Home, Icons.Filled.Home, "stats"),
    LIST(
        R.string.home_adadeh,
        Icons.Filled.Create,
        route = "live"
    ),
    GENRE(R.string.home_genre, R.drawable.book_open, R.drawable.book_open_filled, "members"),
    BOOKMARK(R.string.home_bookmark, R.drawable.bookmark, R.drawable.bookmark_filled, "bookmark"),
    SETTINGS(R.string.settings, Icons.Outlined.Settings, Icons.Filled.Settings, "settings");

    @Composable
    fun Icon(isSelected: Boolean = false) {
        val iconData = if (isSelected) selectedIcon ?: icon else icon
        IconView(iconData, title)
    }
}

@Composable
private fun IconView(iconData: Any, name: Int) {
    if (iconData is ImageVector) {
        androidx.compose.material.Icon(
            iconData,
            contentDescription = stringResource(name),
        )
    } else if (iconData is Int) {
        androidx.compose.material.Icon(
            painterResource(id = iconData),
            contentDescription = stringResource(name),
        )
    }
}

fun NavGraphBuilder.addBottomNav(
    navController: NavHostController,
) {
//    navigation(startDestination = HomeSections.HOME.route, route = "home") {
    composable(HomeSections.HOME.route) {
        HomeFragment(navController)
    }


    composable(HomeSections.GENRE.route) {
        GenreScreen()
    }

    composable(HomeSections.LIST.route) {
        GenreScreen()
    }

    composable(HomeSections.SETTINGS.route) {
        SettingScreen(navController)
    }

    composable(HomeSections.BOOKMARK.route) {
        BookmarkScreen(navController)
    }
//    }
//    composable(HomeSections.MEMBERS.route) {
//        MemberScreen(onOpenMemberProfile = { url ->
//            navController.navigate("webview/${URLEncoder.encode(url, "UTF-8")}")
//        }, snackBar)
//    }
//    composable(HomeSections.RECENT.route) {
//        RecentScreen(navController, snackBar)
//    }
//
//    composable(HomeSections.LIVE.route) {
//        val uriHandler = LocalUriHandler.current
//        NowLiveScreen(onLiveClick = { roomId ->
//            uriHandler.openUri("showroom:///room?room_id=${roomId}&launch_type=from_sp_browser")
//        }, snackBar)
//    }
//
//    composable(HomeSections.SETTINGS.route) {
//        StatsScreen(navController, snackBar)
//    }
//    }
//    navigation(startDestination = "stats", route = "home") {
//        composable("stats") {
//            StatsScreen()
//        }
//        composable("recents") {
//            MemberScreen()
//        }
//        composable("members") {
//            MemberScreen()
//        }
//    }
}