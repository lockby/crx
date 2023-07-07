package com.crstlnz.komikchino.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Build.VERSION.SDK_INT
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import com.crstlnz.komikchino.config.AppSettings
import com.google.accompanist.web.AccompanistWebChromeClient
import com.google.accompanist.web.AccompanistWebViewClient
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewState

@SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
@Composable
fun WebViewComponent(
    url: String,
    onProgressChanged: (view: WebView?, newProgress: Int) -> Unit = { _, _ -> },
    onReceivedTitle: (view: WebView?, title: String?) -> Unit = { _, _ -> },
    onCreated: (webView: WebView) -> Unit = { _ -> },
    onRequestOpenBrowser: ((url: String) -> Unit)? = null,
    onPageFinished: (WebView, String?) -> Unit = { _, _ -> },
    onPageCreated: (WebView, String?, Bitmap?) -> Unit = { _, _, _ -> },
    shouldInterceptRequest: ((WebView?, WebResourceRequest?, next: (WebView?, WebResourceRequest?) -> WebResourceResponse?) -> WebResourceResponse?)? = null
) {
    val state = rememberWebViewState(url)
    val uriHandler = LocalUriHandler.current
    WebView(
        state,
        modifier = Modifier.fillMaxSize(),
        onCreated = {
            it.settings.userAgentString = AppSettings.userAgent
            it.settings.javaScriptEnabled = true
            it.settings.builtInZoomControls = true
            it.settings.displayZoomControls = false
            it.settings.domStorageEnabled = true
            if (SDK_INT >= 29) {
                it.settings.forceDark = WebSettings.FORCE_DARK_ON
            }
            it.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
            onCreated(it)
        },
        client = object : AccompanistWebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                return if (shouldInterceptRequest != null) {
                    shouldInterceptRequest(view, request) { v, r ->
                        super.shouldInterceptRequest(v, r)
                    }
                } else {
                    super.shouldInterceptRequest(view, request)
                }
            }

            override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                onPageCreated(view, url, favicon)
            }

            override fun onPageFinished(view: WebView, url: String?) {
                super.onPageFinished(view, url)
                onPageFinished(view, url)
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val u = request?.url.toString()
                val regex = Regex(".*:\\/\\/\\/.*")
                if (regex.matches(u)) {
                    uriHandler.openUri(u)
                    if (onRequestOpenBrowser != null) {
                        onRequestOpenBrowser(u)
                    } else {
                        uriHandler.openUri(u)
                    }
                    return true
                } else if (u.startsWith("http://")) {
                    val newUrl = u.replace("http://", "https://")
                    view?.loadUrl(newUrl)
                    return true
                } else if (u.startsWith("https://twitter.com")) {
                    if (onRequestOpenBrowser != null) {
                        onRequestOpenBrowser(u)
                    } else {
                        uriHandler.openUri(u)
                    }
                    return true
                } else if (u.startsWith("https://play.google.com")) {
                    if (onRequestOpenBrowser != null) {
                        onRequestOpenBrowser(u)
                    } else {
                        uriHandler.openUri(u)
                    }
                    return true
                }
                return super.shouldOverrideUrlLoading(view, request)
            }
        },
        chromeClient = object : AccompanistWebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                onProgressChanged(view, newProgress)
            }

            override fun onReceivedTitle(view: WebView, title: String?) {
                onReceivedTitle(view, title)
            }

            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                consoleMessage?.apply {
                    Log.d("WebView Console", consoleMessage.message())
                }
                return true
            }
        },
    )
}