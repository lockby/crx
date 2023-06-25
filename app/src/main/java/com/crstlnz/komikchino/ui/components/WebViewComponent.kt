package com.crstlnz.komikchino.ui.components

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
import android.webkit.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.google.accompanist.web.*

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewComponent(
    url: String,
    onProgressChanged: (view: WebView?, newProgress: Int) -> Unit = { _, _ -> },
    onReceivedTitle: (view: WebView?, title: String?) -> Unit = { _, _ -> },
    onCreated: (webView: WebView) -> Unit = { _ -> },
    onRequestOpenBrowser: (url: String) -> Unit = {}
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
            it.setBackgroundColor(Color.parseColor("#1E2124"))
            it.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
            onCreated(it)
        },
        client = object : AccompanistWebViewClient() {
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