package com.crstlnz.komikchino

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.crstlnz.komikchino.config.AppSettings
import com.crstlnz.komikchino.ui.navigations.MainNavigation
import com.crstlnz.komikchino.ui.navigations.addMainNavigation
import com.crstlnz.komikchino.ui.theme.KomikChinoTheme
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.rememberAnimatedNavController

var LocalNavPadding = compositionLocalOf {
    0.dp
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actionBar?.show()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        } else {
            window.insetsController?.apply {
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
        setContent {
            KomikChinoTheme {
                MainApp()
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainApp() {
    LocalNavPadding
    val paddingValues = androidx.compose.foundation.layout.WindowInsets.systemBars.asPaddingValues()
    LocalNavPadding = compositionLocalOf {
        paddingValues.calculateTopPadding()
    }
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                modifier= Modifier.statusBarsPadding(),
//                title = {
//                    Text(text = "Top App Bar")
//                },
//                navigationIcon = {
//                    IconButton(onClick = {}) {
//                        Icon(Icons.Filled.ArrowBack, "backIcon")
//                    }
//                },
//                backgroundColor = MaterialTheme.colors.primary,
//                contentColor = Color.White,
//                elevation = 10.dp
//            )
//        }, content = {
//            Column(
//                modifier = Modifier
//                    .padding(it)
//                    .fillMaxSize()
//                    .background(Color(0xff8d6e63)),
//                verticalArrangement = Arrangement.Center,
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                Text(
//                    text = "Content of the page",
//                    fontSize = 30.sp,
//                    color = Color.White
//                )
//            }
//
//        })
    val navController = rememberAnimatedNavController()

    AnimatedNavHost(
        navController,
        modifier = Modifier
            .padding(0.dp)
            .navigationBarsPadding()
            .fillMaxSize(),
//        startDestination = "${MainNavigation.KOMIKDETAIL}/Solo Leveling/916855-solo-leveling",
//        startDestination = MainNavigation.SEARCH,
        startDestination = MainNavigation.HOME,
//        startDestination = "${MainNavigation.CHAPTER}/Chapter 1/389424/232",
        enterTransition = {
            scaleIn(
                animationSpec = tween(AppSettings.animationDuration, easing = EaseOutCubic),
                initialScale = 0.95F
            ) + fadeIn(tween(AppSettings.animationDuration))
        },
        exitTransition = {
            scaleOut(
                animationSpec = tween(
                    (AppSettings.animationDuration * 1.5f).toInt(), easing = EaseOutCubic
                ), targetScale = 1.05F
            ) + fadeOut(tween(AppSettings.animationDuration), 0.4f)
        },
        popEnterTransition = {
            scaleIn(
                animationSpec = tween(
                    (AppSettings.animationDuration * 1.5f).toInt(), easing = EaseOutCubic
                ), initialScale = 1.1F
            )
        },
        popExitTransition = {
            scaleOut(
                animationSpec = tween(AppSettings.animationDuration, easing = EaseOutCubic),
                targetScale = 0.95F
            ) + fadeOut(tween(AppSettings.animationDuration))
        },
    ) {
        addMainNavigation(navController)
    }

}