package com.crstlnz.komikchino.ui.screens

import android.webkit.WebView
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.crstlnz.komikchino.R
import com.crstlnz.komikchino.ui.components.WebViewComponent
import com.crstlnz.komikchino.ui.theme.Black1

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebViewScreen(url: String, title: String = "Comments", onBackPressed: () -> Unit) {
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
        Surface(modifier = Modifier
            .padding(it)
            .background(color = Black1)) {
            Column {
                val uriHandler = LocalUriHandler.current
                WebViewComponent(
                    url,
                    onCreated = { wV ->
                        webView = wV
                    },
                    onRequestOpenBrowser = { url ->
//                        uriHandler.openUri(url)
                        uriHandler.openUri("https://void-scans.com/")
                    }
                )
            }
        }
    }
}