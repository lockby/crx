package com.crstlnz.komikchino.ui.screens

import android.graphics.Color
import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.crstlnz.komikchino.R
import com.crstlnz.komikchino.ui.components.WebViewComponent
import com.crstlnz.komikchino.ui.theme.Black1

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentScreen(url: String, defaultTitle: String? = null, onBackPressed: () -> Unit) {
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
                        wV.setBackgroundColor(Color.parseColor("#1E2124"))
                        webView = wV
                    },
                    onRequestOpenBrowser = { url ->
                        uriHandler.openUri(url)
                    },
                    onReceivedTitle = { _, t ->
                        title = t ?: ""
                    }
                )
            }
        }
    }
}