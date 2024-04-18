package com.crstlnz.komikchino.ui.screens.home

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.crstlnz.komikchino.R
import com.crstlnz.komikchino.config.AppSettings
import com.crstlnz.komikchino.data.datastore.Settings
import com.crstlnz.komikchino.ui.navigations.HomeSections
import com.crstlnz.komikchino.ui.navigations.MainNavigation
import com.crstlnz.komikchino.ui.navigations.addBottomNav
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Singleton

val LocalSnackbarHostState = compositionLocalOf {
    SnackbarHostState()
}

@Singleton
object HomeState {
    var isEditMode = MutableStateFlow(false)
}

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class,
)
@Composable
fun HomeScreen(
    navigateTo: (to: String) -> Unit
) {
    val bottomNav = rememberNavController()
    LaunchedEffect(Unit) {
        bottomNav.enableOnBackPressed(false)
    }
    val navBackStackEntry by bottomNav.currentBackStackEntryAsState()
    var selectedRoute by remember { mutableStateOf(HomeSections.HOME) }
    val snackBarHost = LocalSnackbarHostState.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    LaunchedEffect(navBackStackEntry) {
        selectedRoute = HomeSections.entries.find { section ->
            navBackStackEntry?.destination?.hierarchy?.any { it.route == section.route } == true
        } ?: HomeSections.HOME
    }
    CompositionLocalProvider(LocalSnackbarHostState provides snackBarHost) {
        Scaffold(floatingActionButton = {
            if (selectedRoute !== HomeSections.SETTINGS) {
                ExtendedFloatingActionButton(backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.secondary,
                    onClick = {
                        scope.launch {
                            Settings(context).setTempHomepage(selectedRoute)
                            navigateTo(MainNavigation.SERVER_SELECTION)
                        }
                    },
                    text = {
                        Text(AppSettings.komikServer?.title ?: "Server")
                    },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.web),
                            contentDescription = "Server Icon"
                        )
                    })
            }
        },
            contentWindowInsets = WindowInsets.ime,
            snackbarHost = { SnackbarHost(snackBarHost) },
            bottomBar = {
                BottomNavigation(
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    elevation = 14.dp
                ) {
                    for (section in HomeSections.entries) {
                        val isSelected = selectedRoute.route == section.route
                        BottomNavigationItem(selected = isSelected, onClick = {
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
                        }, alwaysShowLabel = false, label = {
                            Text(
                                text = if (section.route == HomeSections.HOME.route) "Home" else stringResource(
                                    section.title
                                ), style = MaterialTheme.typography.labelSmall
                            )
                        }, icon = {
                            section.Icon(isSelected)
                        })
                    }
                }
            }) { contentPadding ->
            Surface(
                Modifier
                    .padding(contentPadding)
                    .fillMaxSize()
            ) {
                NavHost(
                    bottomNav,
                    startDestination = AppSettings.homepage?.route ?: HomeSections.HOME.route,
                    modifier = Modifier.fillMaxSize(),
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
                    addBottomNav {
                        navigateTo(it)
                    }
                }
            }
        }
    }
}