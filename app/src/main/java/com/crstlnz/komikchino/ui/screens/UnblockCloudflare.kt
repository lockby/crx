package com.crstlnz.komikchino.ui.screens

import android.annotation.SuppressLint
import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import org.apache.commons.text.StringEscapeUtils
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavHostController
import com.crstlnz.komikchino.R
import com.crstlnz.komikchino.config.AppSettings
import com.crstlnz.komikchino.config.CloudflareState
import com.crstlnz.komikchino.data.util.CustomCookieJar
import com.crstlnz.komikchino.data.util.extractDomain
import com.crstlnz.komikchino.ui.components.WebViewComponent
import com.crstlnz.komikchino.ui.theme.Black1
import kotlinx.coroutines.flow.update
import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.jsoup.Jsoup
import kotlin.random.Random

@SuppressLint("JavascriptInterface")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnblockCloudflare(
    navController: NavHostController,
    url: String,
    defaultTitle: String? = null,
    onBackPressed: () -> Unit
) {
    DisposableEffect(Unit) {
        AppSettings.cloudflareState.update {
            it.copy(
                isUnblockInProgress = true
            )
        }

        onDispose {
            AppSettings.cloudflareState.update {
                it.copy(
                    isUnblockInProgress = false
                )
            }
        }
    }
    val context = LocalContext.current
    var title by remember {
        mutableStateOf(
            defaultTitle ?: context.getString(context.applicationInfo.labelRes)
        )
    }
    var webView: WebView? by remember { mutableStateOf(null) }
    Scaffold(
        contentWindowInsets = WindowInsets.ime,
        topBar = {
            Box(contentAlignment = Alignment.BottomCenter) {
                TopAppBar(
                    title = {
                        Text(
                            title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },

                    navigationIcon = {
                        IconButton(onClick = { onBackPressed() }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.go_back)
                            )
                        }
                    },
                )
            }
        }
    ) {
        Surface(
            modifier = Modifier
                .padding(it)
                .background(color = Black1)
        ) {
            Column {
                val uriHandler = LocalUriHandler.current
                WebViewComponent(
                    url,
                    onCreated = { wV ->
                        // Get the CookieManager instance
                        val cookieManager = CookieManager.getInstance()
                        // Clear all cookies
                        cookieManager.removeAllCookies(null)
                        // Clear session cookies
                        cookieManager.removeSessionCookies(null)
                        // Make sure the changes are saved
                        cookieManager.flush()
                        webView = wV
                    },
                    onRequestOpenBrowser = { url ->
                        uriHandler.openUri(url)
                    },
                    onReceivedTitle = { _, t ->
                        title = t ?: ""
                    }
                ) { wV, _ ->
                    wV.evaluateJavascript(
                        "(function(){return window.document.body.outerHTML})();"
                    ) { str ->
                        val document = Jsoup.parse(StringEscapeUtils.unescapeJava(str))
                        val isBlocked =
                            document.selectFirst("#challenge-form") != null || document.selectFirst(
                                "#challenge-error-title"
                            ) != null
                        val cookieString = CookieManager.getInstance().getCookie(url)
                        if (!isBlocked && cookieString != null && AppSettings.cookieJar is CustomCookieJar) {
                            navController.popBackStack()
                            val updatedCookies = mutableListOf<Cookie>()
                            val cookieArray = cookieString.split(";")
                            for (cookieItem in cookieArray) {
                                val cookiePair = cookieItem.trim().split("=")
                                if (cookiePair.size == 2) {
                                    val cookieName = cookiePair[0]
                                    val cookieValue = cookiePair[1]
                                    val cookie = Cookie.Builder()
                                        .domain(
                                            extractDomain(url) ?: ""
                                        ) // Set the appropriate domain for the cookies
                                        .path("/")
                                        .name(cookieName)
                                        .value(cookieValue)
                                        .build()
                                    updatedCookies.add(cookie)
                                }
                            }

                            Log.d("COOKIES", updatedCookies.toString())
                            Log.d("COOKIES STRING", cookieString)
                            val httpURL = url.toHttpUrlOrNull()
                            Log.d("COOKIE JAR WEBVIEW ", httpURL?.host.toString())
                            if (httpURL != null) {
                                (AppSettings.cookieJar as CustomCookieJar).updateCookies(
                                    httpURL,
                                    updatedCookies
                                )
                            }

                            val hasCloudflareKey =
                                updatedCookies.find { cookie -> cookie.name == "cf_clearance" } != null
                            Log.d("COOKIE HAS ADA", "HASIL $hasCloudflareKey")

                            if (hasCloudflareKey) {
                                AppSettings.cloudflareState.update { state ->
                                    state.copy(
                                        isBlocked = false,
                                        key = 0
                                    )
                                }
                            } else {
                                Log.d("COOKIE PEGI", "ROUTE 1")
                                wV.loadUrl(url + Random.nextInt(1, 1000000).toString())
                            }
                        } else if (!isBlocked) {
                            Log.d("COOKIE PEGI", "ROUTE 2")
                            wV.loadUrl(url + Random.nextInt(1, 1000000).toString())
                        }
                    };
                }
            }
        }
    }
}