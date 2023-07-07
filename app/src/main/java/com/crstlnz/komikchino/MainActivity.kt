package com.crstlnz.komikchino

//import com.facebook.drawee.backends.pipeline.Fresco
//import com.facebook.imagepipeline.backends.okhttp3.OkHttpImagePipelineConfigFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.ImageLoader
import coil.disk.DiskCache
import com.crstlnz.komikchino.config.AppSettings
import com.crstlnz.komikchino.config.IMAGE_CACHE_PATH
import com.crstlnz.komikchino.config.USER_DATA
import com.crstlnz.komikchino.data.api.ApiClient
import com.crstlnz.komikchino.data.api.KomikServer
import com.crstlnz.komikchino.data.database.model.User
import com.crstlnz.komikchino.data.datastore.Settings
import com.crstlnz.komikchino.data.model.UpdateState
import com.crstlnz.komikchino.data.util.CustomCookieJar
import com.crstlnz.komikchino.data.util.FirebaseInitializer
import com.crstlnz.komikchino.data.util.HttpErrorInterceptor
import com.crstlnz.komikchino.data.util.RequestHeaderInterceptor
import com.crstlnz.komikchino.data.util.getAppVersion
import com.crstlnz.komikchino.data.util.versionCheck
import com.crstlnz.komikchino.ui.components.UpdateDialog
import com.crstlnz.komikchino.ui.navigations.HomeSections
import com.crstlnz.komikchino.ui.navigations.MainNavigation
import com.crstlnz.komikchino.ui.navigations.addMainNavigation
import com.crstlnz.komikchino.ui.theme.KomikChinoTheme
import com.crstlnz.komikchino.ui.util.NotificationPermission
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject

var LocalStatusBarPadding = compositionLocalOf {
    0.dp
}

var LocalSystemBarPadding = compositionLocalOf {
    0.dp
}

private lateinit var firebaseAnalytics: FirebaseAnalytics

class UrlLoggingInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        val url = request.url.toString()

        // Log or store the URL as per your requirement
        println("Request URL: $url")

        return chain.proceed(request)
    }
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var databaseKey: KomikServer

    @Inject
    lateinit var homepage: HomeSections

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseInitializer.initialize(this)
        firebaseAnalytics = Firebase.analytics
        actionBar?.show()
        AppSettings.komikServer = databaseKey
        AppSettings.homepage = homepage

        AppSettings.cookieJar = CustomCookieJar(this)
        AppSettings.customHttpClient = OkHttpClient.Builder()
            .followRedirects(true) // Enable automatic following of redirects
            .followSslRedirects(true)
            .cookieJar(AppSettings.cookieJar)
            .addInterceptor(HttpErrorInterceptor())
            .addInterceptor(RequestHeaderInterceptor())
            .addInterceptor(UrlLoggingInterceptor())
            .build()

        AppSettings.imageLoader =
            ImageLoader
                .Builder(this)
                .okHttpClient(AppSettings.customHttpClient)
                .diskCache {
                    DiskCache.Builder()
                        .directory(this.cacheDir.resolve(IMAGE_CACHE_PATH))
                        .maxSizePercent(0.05)
                        .build()
                }
                .build()

        WindowCompat.setDecorFitsSystemWindows(window, false)
        // Calculating Status Bar and SystemBar or Navigation bar on bottom
        val statusBarHeightId = resources.getIdentifier("status_bar_height", "dimen", "android")
        val systemBarHeighId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        val density = resources.displayMetrics.density
        val statusBarHeight = resources.getDimensionPixelSize(statusBarHeightId) / density
        val systemBarHeight = resources.getDimensionPixelSize(systemBarHeighId) / density

//        val pipelineConfig =
//            OkHttpImagePipelineConfigFactory
//                .newBuilder(this, OkHttpClient.Builder().build())
//                .setDiskCacheEnabled(true)
//                .setDownsampleEnabled(true)
//                .setResizeAndRotateEnabledForNetwork(true)
//                .build()
//
//        Fresco.initialize(this, pipelineConfig)

        LocalStatusBarPadding = compositionLocalOf {
            statusBarHeight.dp
        }
//
        LocalSystemBarPadding = compositionLocalOf {
            systemBarHeight.dp
        }

        // make a immersive fullscreen
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            window.decorView.systemUiVisibility =
                (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp() {
    val systemUiController = rememberSystemUiController()
    LaunchedEffect(Unit) {
        systemUiController.isSystemBarsVisible = true
    }

    NotificationPermission()

    val navController = rememberNavController()

    LaunchedEffect(Unit) {
        AppSettings.cloudflareState.collect {
            if (it.isBlocked && !it.isUnblockInProgress && !it.mustManualTrigger) {
                AppSettings.cloudflareState.update { state ->
                    state.copy(
                        isUnblockInProgress = true,
                        mustManualTrigger = true,
                    )
                }
                MainNavigation.unblockCloudflare(
                    navController = navController, AppSettings.komikServer!!.url
                )
            }
        }
    }


    val currentUser = FirebaseAuth.getInstance().currentUser
    NavHost(
        navController,
        modifier = Modifier
            .padding(0.dp)
            .navigationBarsPadding()
            .fillMaxSize(),
        startDestination = if (currentUser != null) MainNavigation.HOME else MainNavigation.LOGIN,
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


    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    var isLogin by remember { mutableStateOf(false) }
    val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        isLogin = user != null
    }

    FirebaseAuth.getInstance().addAuthStateListener(authStateListener)

    LaunchedEffect(isLogin) {
        if (currentRoute == null) return@LaunchedEffect
        if (!isLogin) {
            if (currentRoute != MainNavigation.LOGIN) {
                MainNavigation.toLogin(navController)
            }
        } else {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                FirebaseFirestore.getInstance()
                    .collection(USER_DATA)
                    .document(user.uid)
                    .set(
                        User(
                            id = user.uid,
                            name = user.displayName ?: "",
                            email = user.email ?: "",
                            img = user.photoUrl.toString(),
                        )
                    ).addOnSuccessListener {
                        Log.d("FIRESTORE SUCCESS", "INSERT USER DATA")
                    }.addOnFailureListener {
                        Log.d("FIRESTORE FAILED", it.stackTraceToString())
                    }
            }
            if (currentRoute != MainNavigation.HOME) {
                MainNavigation.toHome(navController)
            }
        }
    }

    var openDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val settings = Settings(context)
    LaunchedEffect(Unit) {
        val updateState = settings.getUpdate()
        if (updateState != null && updateState.reminder && versionCheck(
                updateState.version,
                getAppVersion(context)
            )
        ) {
            openDialog = true
        }

        try {
            val releases = ApiClient.getGithubClient().getReleases()
            if (updateState?.version != releases.tagName) {
                if (versionCheck(releases.tagName ?: "", getAppVersion(context))) {
                    settings.setUpdate(
                        UpdateState(
                            version = releases.tagName ?: "",
                            reminder = true
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.d("UPDATE CHECK FAILED", e.stackTraceToString())
        }
    }


    val scope = rememberCoroutineScope()
    if (openDialog && currentUser != null) {
        UpdateDialog(onDismiss = {
            openDialog = false
        }, onUpdate = {
            openDialog = false
            navController.navigate(MainNavigation.CHECK_UPDATE)
        }, onIgnore = {
            openDialog = false
            scope.launch {
                val updateState = settings.getUpdate()
                if (updateState != null) {
                    settings.setUpdate(updateState.copy(reminder = false))
                }
            }
        })
    }
}