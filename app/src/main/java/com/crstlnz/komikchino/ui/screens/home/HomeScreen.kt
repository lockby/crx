package com.crstlnz.komikchino.ui.screens.home

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.crstlnz.komikchino.R
import com.crstlnz.komikchino.config.AppSettings
import com.crstlnz.komikchino.ui.navigations.HomeSections
import com.crstlnz.komikchino.ui.navigations.MainNavigation
import com.crstlnz.komikchino.ui.navigations.addBottomNav
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.rememberAnimatedNavController

val LocalSnackbarHostState = compositionLocalOf {
    SnackbarHostState()
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    bottomNav: NavHostController = rememberAnimatedNavController(),
    route: String = AppSettings.homeDefaultRoute
) {
    bottomNav.enableOnBackPressed(false)

    val navBackStackEntry by bottomNav.currentBackStackEntryAsState()
    val paddingValues = WindowInsets.systemBars.asPaddingValues()
    var selectedRoute by remember { mutableStateOf(HomeSections.HOME) }
    val snackBarHost = LocalSnackbarHostState.current

    LaunchedEffect(navBackStackEntry) {
        selectedRoute = HomeSections.values().find { section ->
            navBackStackEntry?.destination?.hierarchy?.any { it.route == section.route } == true
        } ?: HomeSections.HOME
    }
    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHost) },
        topBar = {
            Column() {
                Box(
                    Modifier
                        .background(MaterialTheme.colors.primary)
                        .height(paddingValues.calculateTopPadding())
                        .fillMaxWidth()
                )
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.mipmap.app_icon),
                                contentDescription = "App Icon",
                                modifier = Modifier.height(36.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                stringResource(selectedRoute.title),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    backgroundColor = MaterialTheme.colors.primary,
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
                    elevation = 0.dp
                )
            }
        },
        bottomBar = {
            BottomNavigation(
                Modifier.navigationBarsPadding(),
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = MaterialTheme.colors.onPrimary,
                elevation = 0.dp
            ) {
                for (section in HomeSections.values()) {
                    val isSelected =
                        navBackStackEntry?.destination?.hierarchy?.any { it.route == section.route } == true
                    BottomNavigationItem(selected = isSelected,
                        onClick = {
                            bottomNav.navigate(section.route) {
                                popUpTo(bottomNav.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        },
                        alwaysShowLabel = false,
                        label = {
                            Text(
                                text = if (section.route == HomeSections.HOME.route) "Home" else stringResource(
                                    selectedRoute.title
                                ),
                                fontWeight = FontWeight.SemiBold,
                            )
                        },
                        icon = {
                            section.Icon(isSelected)
                        })
                }
            }
        }) { contentPadding ->
        CompositionLocalProvider(LocalSnackbarHostState provides snackBarHost) {
            AnimatedNavHost(
                bottomNav,
            startDestination = route,
//                startDestination = HomeSections.SETTINGS.route,
                modifier = Modifier
                    .padding(contentPadding)
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
                addBottomNav(navController, bottomNav)
            }
        }

    }
}