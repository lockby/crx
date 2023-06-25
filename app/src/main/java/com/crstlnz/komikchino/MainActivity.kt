package com.crstlnz.komikchino

import android.annotation.SuppressLint
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.crstlnz.komikchino.config.AppSettings
import com.crstlnz.komikchino.data.datastore.KomikServer
import com.crstlnz.komikchino.ui.navigations.MainNavigation
import com.crstlnz.komikchino.ui.navigations.addMainNavigation
import com.crstlnz.komikchino.ui.theme.KomikChinoTheme
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.backends.okhttp3.OkHttpImagePipelineConfigFactory
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.OkHttpClient
import javax.inject.Inject

var LocalStatusBarPadding = compositionLocalOf {
    0.dp
}

var LocalSystemBarPadding = compositionLocalOf {
    0.dp
}


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var databaseKey: KomikServer

    @SuppressLint("InternalInsetResource", "DiscouragedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actionBar?.show()
        AppSettings.komikServer = databaseKey
        WindowCompat.setDecorFitsSystemWindows(window, false)
        // Calculating Status Bar and SystemBar or Navigation bar on bottom
        val statusBarHeightId = resources.getIdentifier("status_bar_height", "dimen", "android")
        val systemBarHeighId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        val density = resources.displayMetrics.density
        val statusBarHeight = resources.getDimensionPixelSize(statusBarHeightId) / density
        val systemBarHeight = resources.getDimensionPixelSize(systemBarHeighId) / density

        val pipelineConfig =
            OkHttpImagePipelineConfigFactory
                .newBuilder(this, OkHttpClient.Builder().build())
                .setDiskCacheEnabled(true)
                .setDownsampleEnabled(true)
                .setResizeAndRotateEnabledForNetwork(true)
                .build()

        Fresco.initialize(this, pipelineConfig)

        LocalStatusBarPadding = compositionLocalOf {
            statusBarHeight.dp
        }
//
        LocalSystemBarPadding = compositionLocalOf {
            systemBarHeight.dp
        }

        // make a immersive fullscreen
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
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
    val systemUiController = rememberSystemUiController()
    LaunchedEffect(Unit) {
        systemUiController.isSystemBarsVisible = true
    }

    val navController = rememberNavController()


    NavHost(
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
            ) + fadeIn(tween((AppSettings.animationDuration).toInt()))
        },
        exitTransition = {
            scaleOut(
                animationSpec = tween(
                    (AppSettings.animationDuration * 1.5f).toInt(), easing = EaseOutCubic
                ), targetScale = 1.05F
            ) + fadeOut(tween((AppSettings.animationDuration / 1.5f).toInt()), 0.4f)
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
            ) + fadeOut(tween((AppSettings.animationDuration / 1.5f).toInt()))
        },
    ) {
        addMainNavigation(navController)
    }

}