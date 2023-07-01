package com.crstlnz.komikchino.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build.VERSION.SDK_INT
import android.text.Html
import android.util.Log
import android.webkit.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.text.toHtml
import com.crstlnz.komikchino.config.AppSettings
import com.google.accompanist.web.*
import org.jsoup.Jsoup

@SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
@Composable
fun WebViewComponent(
    url: String,
    onProgressChanged: (view: WebView?, newProgress: Int) -> Unit = { _, _ -> },
    onReceivedTitle: (view: WebView?, title: String?) -> Unit = { _, _ -> },
    onCreated: (webView: WebView) -> Unit = { _ -> },
    onRequestOpenBrowser: (url: String) -> Unit = {},
    onPageFinished: (WebView, String?) -> Unit = { _, _ -> }
) {
    val state = rememberWebViewState(url)
    WebView(
        state,
        modifier = Modifier.fillMaxSize(),
        onCreated = {
            it.settings.javaScriptEnabled = true
            it.settings.builtInZoomControls = true
            it.settings.displayZoomControls = false
            it.settings.domStorageEnabled = true
            if (SDK_INT >= 29) {
                it.settings.forceDark = WebSettings.FORCE_DARK_ON
            }
            it.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
            it.settings.userAgentString = AppSettings.userAgent
            it.addJavascriptInterface(object {
            }, "helper")
            onCreated(it)
        },
        client = object : AccompanistWebViewClient() {
            override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                onPageFinished(view, url)
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView, url: String?) {
//                onPageFinished(view, url)
                super.onPageFinished(view, url)
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val u = request?.url.toString()
                val regex = Regex(".*:\\/\\/\\/.*")
                if (regex.matches(u)) {
                    onRequestOpenBrowser(u)
                    return true;
                } else if (u.startsWith("http://")) {
                    val newUrl = u.replace("http://", "https://")
                    view?.loadUrl(newUrl)
                    return true
                } else if (u.startsWith("https://play.google.com")) {
                    onRequestOpenBrowser(u)
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