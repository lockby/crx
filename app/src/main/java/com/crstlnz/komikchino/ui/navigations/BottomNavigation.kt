package com.crstlnz.komikchino.ui.navigations

import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideOut
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.crstlnz.komikchino.R
import com.crstlnz.komikchino.ui.screens.home.fragments.home.HomeFragment
import com.crstlnz.komikchino.ui.screens.home.fragments.settings.SettingScreen
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.navigation
import java.net.URLEncoder

enum class HomeSections(
    @StringRes val title: Int,
    private val icon: Any,
    private val selectedIcon: Any? = null,
    val route: String
) {
    HOME(R.string.home, Icons.Outlined.Home, Icons.Filled.Home, "stats"),
    LIVE(
        R.string.home_live,
        Icons.Filled.Create,
        route = "live"
    ),
    MEMBERS(R.string.home_member, Icons.Outlined.Person, Icons.Filled.Person, "members"),
    RECENT(R.string.home_recent, Icons.Outlined.List, Icons.Filled.List, "recent"),
    SETTINGS(R.string.settings, Icons.Outlined.Settings, Icons.Filled.Settings, "settings");

    @Composable
    fun Icon(isSelected: Boolean = false) {
        val iconData = if (isSelected) selectedIcon ?: icon else icon;
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

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.addBottomNav(
    navController: NavHostController,
    bottomNavController: NavHostController,
) {
//    navigation(startDestination = HomeSections.HOME.route, route = "home") {
    composable(HomeSections.HOME.route) {
        HomeFragment(navController)
    }


    composable(HomeSections.MEMBERS.route) {
        HomeFragment(navController)
    }

    composable(HomeSections.LIVE.route) {
        HomeFragment(navController)
    }

    composable(HomeSections.SETTINGS.route) {
        SettingScreen(navController)
    }

    composable(HomeSections.RECENT.route) {
        HomeFragment(navController)
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