package com.crstlnz.komikchino.ui.screens

import android.annotation.SuppressLint
import android.webkit.CookieManager
import android.webkit.WebView
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavHostController
import com.crstlnz.komikchino.R
import com.crstlnz.komikchino.config.AppSettings
import com.crstlnz.komikchino.data.util.CustomCookieJar
import com.crstlnz.komikchino.data.util.parseCookieString
import com.crstlnz.komikchino.ui.components.WebViewComponent
import com.crstlnz.komikchino.ui.theme.Black1
import kotlinx.coroutines.flow.update
import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.apache.commons.text.StringEscapeUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import kotlin.random.Random

@SuppressLint("JavascriptInterface")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnblockCloudflare(
    navController: NavHostController,
    defaultUrl: String,
    defaultTitle: String? = null,
    onBackPressed: () -> Unit
) {
    var isDestroyed = remember { false }
    var tryRedirect = remember { 0 }
    val context = LocalContext.current
    var isBack = remember { false }

    val url = AppSettings.cloudflareState.value.url ?: defaultUrl

    fun back() {
        if (!isBack) {
            isBack = true
            navController.popBackStack()
        }
    }

    fun isBlocked(document: Document): Boolean {
        val check1 = document.selectFirst("#challenge-form") != null
        val check2 = document.selectFirst("#challenge-error-title") != null
        val check3 = document.selectFirst("#challenge-stage") != null
        val check4 = document.selectFirst("input[name=cf_challenge_response]") != null
        return check1 || check2 || check3 || check4
    }

    fun getCookieString(): String? {
        return CookieManager.getInstance().getCookie(url) ?: return null
    }

    fun getCookie(): List<Cookie>? {
        val cookieString = getCookieString() ?: return null
        return parseCookieString(cookieString, url)
    }

    fun hasCloudflareKey(cookies: List<Cookie>): Boolean {
        return cookies.find { cookie ->
            return cookie.name == "cf_clearance"
        } != null
    }

    var webView: WebView? by remember { mutableStateOf(null) }

    DisposableEffect(Unit) {
        AppSettings.cloudflareState.update {
            it.copy(
                isUnblockInProgress = true
            )
        }

        onDispose {
            webView?.clearHistory()
            webView?.onPause()
            webView?.removeAllViews()
            webView?.pauseTimers()
            webView?.destroy()
            webView = null
            isDestroyed = true
            AppSettings.cloudflareState.update {
                it.copy(
                    isUnblockInProgress = false
                )
            }
        }
    }
    var title by remember {
        mutableStateOf(
            defaultTitle ?: context.getString(context.applicationInfo.labelRes)
        )
    }
    Scaffold(contentWindowInsets = WindowInsets.ime, topBar = {
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
    }) {
        Surface(
            modifier = Modifier
                .padding(it)
                .background(color = Black1)
        ) {
            Column {
                WebViewComponent(url + Random.nextInt(1, 1000000).toString(), onCreated = { wV ->
                    if (AppSettings.cloudflareState.value.mustClearCache) {
                        wV.clearCache(true)
                        AppSettings.cloudflareState.update { state ->
                            state.copy(
                                isUnblockInProgress = true, mustClearCache = false
                            )
                        }
                    }
                    val cookieManager = CookieManager.getInstance()
                    wV.settings.domStorageEnabled = true
                    wV.settings.databaseEnabled = true
                    cookieManager.setAcceptThirdPartyCookies(wV, true)
                    cookieManager.setAcceptCookie(true)
                    cookieManager.removeAllCookies(null)
                    cookieManager.removeSessionCookies(null)
                    cookieManager.acceptCookie()
                    cookieManager.flush()
                    webView = wV
                }, onReceivedTitle = { _, t ->
                    title = t ?: ""
                }, shouldInterceptRequest = { webView, request, next ->
                    if (isDestroyed) {
                        next(webView, request)
                    } else {
                        val cookies = getCookie()
                        if (cookies != null && hasCloudflareKey(cookies)) {
                            webView?.post {
                                val httpURL = url.toHttpUrlOrNull()
                                if (httpURL != null) {
                                    (AppSettings.cookieJar as CustomCookieJar).updateCookies(
                                        httpURL, cookies
                                    )
                                }
                                if (AppSettings.cloudflareState.value.isBlocked) {
                                    AppSettings.cloudflareState.update { state ->
                                        state.copy(
                                            isBlocked = false, key = 0, autoReloadConsumed = false
                                        )
                                    }
                                    back()
                                }
                            }

                        }
                        next(webView, request)
                    }

                }, onPageFinished = { wV, _url ->
                    if (!isDestroyed) {
                        if (AppSettings.cloudflareState.value.isBlocked && wV.progress == 100)
                            wV.evaluateJavascript(
                                "(function(){return window.document.body.outerHTML})();"
                            ) { str ->
                                val document = Jsoup.parse(StringEscapeUtils.unescapeJava(str))
                                val isBlocked = isBlocked(document)
                                val updatedCookies = getCookie()
                                if (!isBlocked && updatedCookies != null) {
                                    tryRedirect += 1
                                    Thread.sleep(1000L)
                                    wV.loadUrl(url + "?_=${Random.nextInt(1, 1000000)}")
                                } else if (!isBlocked) {
                                    Thread.sleep(1000L)
                                    if (tryRedirect >= 3) {
                                        if (AppSettings.cloudflareState.value.isBlocked) {
                                            AppSettings.cloudflareState.update { state ->
                                                state.copy(
                                                    mustManualTrigger = true,
                                                    isBlocked = true,
                                                    isUnblockInProgress = false,
                                                    mustClearCache = true,
                                                    key = 0
                                                )
                                            }
                                            Toast.makeText(
                                                context,
                                                "Failed to sign cloudflare!",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            back()
                                        }
                                    } else {
                                        tryRedirect += 1
                                        wV.loadUrl(url + "?_=${Random.nextInt(1, 1000000)}")
                                    }
                                }
                            }
                    }
                })
            }
        }
    }
}