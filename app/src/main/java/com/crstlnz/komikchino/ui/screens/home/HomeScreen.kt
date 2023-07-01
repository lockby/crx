package com.crstlnz.komikchino.ui.screens.home

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.crstlnz.komikchino.LocalStatusBarPadding
import com.crstlnz.komikchino.R
import com.crstlnz.komikchino.config.AppSettings
import com.crstlnz.komikchino.ui.navigations.HomeSections
import com.crstlnz.komikchino.ui.navigations.MainNavigation
import com.crstlnz.komikchino.ui.navigations.addBottomNav
import com.google.accompanist.navigation.animation.AnimatedNavHost

val LocalSnackbarHostState = compositionLocalOf {
    SnackbarHostState()
}

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class,
)
@Composable
fun HomeScreen(
    navController: NavHostController,
) {
    val bottomNav = rememberNavController()
    LaunchedEffect(Unit) {
        bottomNav.enableOnBackPressed(false)
    }
    val navBackStackEntry by bottomNav.currentBackStackEntryAsState()
    var selectedRoute by remember { mutableStateOf(HomeSections.HOME) }
    val snackBarHost = LocalSnackbarHostState.current

//    val systemUiController = rememberSystemUiController()
//    LaunchedEffect(Unit) {
//        systemUiController.isSystemBarsVisible = false
//    }

    LaunchedEffect(navBackStackEntry) {
        selectedRoute = HomeSections.values().find { section ->
            navBackStackEntry?.destination?.hierarchy?.any { it.route == section.route } == true
        } ?: HomeSections.HOME
    }
    CompositionLocalProvider(LocalSnackbarHostState provides snackBarHost) {
        Scaffold(
            contentWindowInsets = WindowInsets.ime,
            snackbarHost = { SnackbarHost(snackBarHost) },
            topBar = {
                TopAppBar(
                    modifier = Modifier.padding(top = LocalStatusBarPadding.current),
                    windowInsets = WindowInsets.ime,
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.mipmap.app_icon),
                                contentDescription = "App Icon",
                                modifier = Modifier.height(38.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                stringResource(selectedRoute.title),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
//                backgroundColor = MaterialTheme.colors.primary,
                    actions = {
                        IconButton(
                            onClick = {
                                navController.navigate(MainNavigation.SEARCH)
                            },
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_search_24),
                                contentDescription = "Search",
                            )
                        }
                        Spacer(modifier = Modifier.width(5.dp))
                    },
//                elevation = 0.dp
                )

//                 }
//            Column() {
//                Box(
//                    Modifier
//                        .background(MaterialTheme.colors.primary)
//                        .height(LocalStatusBarPadding.current)
//                        .fillMaxWidth()
//                )
//                TopAppBar(
//                    title = {
//                        Row(verticalAlignment = Alignment.CenterVertically) {
//                            Image(
//                                painter = painterResource(id = R.mipmap.app_icon),
//                                contentDescription = "App Icon",
//                                modifier = Modifier.height(36.dp)
//                            )
//                            Spacer(Modifier.width(8.dp))
//                            Text(
//                                stringResource(selectedRoute.title),
//                                maxLines = 1,
//                                overflow = TextOverflow.Ellipsis
//                            )
//                        }
//                    },
//                    backgroundColor = MaterialTheme.colors.primary,
//                    actions = {
//                        IconButton(
//                            onClick = {
//                                navController.navigate(MainNavigation.SEARCH)
//                            },
//                        ) {
//                            Icon(
//                                painter = painterResource(id = R.drawable.baseline_search_24),
//                                contentDescription = "Search",
//                            )
//                        }
//                        Spacer(modifier = Modifier.width(5.dp))
//                    },
//                    elevation = 0.dp
//                )
//            }
            },
            bottomBar = {
                BottomNavigation(
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    elevation = 14.dp
                ) {
                    for (section in HomeSections.values()) {
                        val isSelected =
                            navBackStackEntry?.destination?.hierarchy?.any { it.route == section.route } == true
                        BottomNavigationItem(
                            selected = isSelected,
                            onClick = {
                                bottomNav.navigate(section.route) {
                                    popUpTo(bottomNav.graph.findStartDestination().id) {
                                        saveState = true
                                        inclusive = false
                                    }
                                    // reselect the same item
                                    launchSingleTop = true
                                    // Restore state when reselect a previously selected item
                                    restoreState = true
                                }
                            },
                            alwaysShowLabel = false,
                            label = {
                                Text(
                                    text = if (section.route == HomeSections.HOME.route) "Home" else stringResource(
                                        section.title
                                    ),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            icon = {
                                section.Icon(isSelected)
                            }
                        )
                    }
                }
            }) { contentPadding ->
            Surface(
                Modifier
                    .padding(contentPadding)
                    .fillMaxSize()
            ) {
                @Suppress("DEPRECATION")
                AnimatedNavHost(
                    bottomNav,
                    startDestination = AppSettings.homepage?.route ?: HomeSections.HOME.route,
                    modifier = Modifier
                        .fillMaxSize(),
                    enterTransition = {
                        fadeIn(
                            tween(
                                AppSettings.animationDuration,
                                delayMillis = AppSettings.animationDuration / 2,
                                easing = EaseOutQuart
                            )
                        )
                    },
                    exitTransition = {
                        fadeOut(tween(AppSettings.animationDuration, easing = EaseOutQuart))
                    },
                    popEnterTransition = {
                        fadeIn(
                            tween(
                                AppSettings.animationDuration,
                                delayMillis = AppSettings.animationDuration / 2,
                                easing = EaseOutQuart
                            )
                        )
                    },
                    popExitTransition = {
                        fadeOut(tween(AppSettings.animationDuration, easing = EaseOutQuart))
                    },
                ) {
                    addBottomNav(navController)
                }
            }
        }
    }
}