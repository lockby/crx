package com.crstlnz.komikchino.ui.screens.home

import android.annotation.SuppressLint
import android.util.Log
import android.webkit.WebView
import android.widget.Space
import androidx.activity.OnBackPressedCallback
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.crstlnz.komikchino.R
import com.crstlnz.komikchino.ui.components.WebViewComponent
import kotlinx.coroutines.launch

@Composable
fun WebViewScreen(url: String, title: String? = null, onBackPressed: () -> Unit) {
    var webTitle: String? by remember { mutableStateOf(null) }
    var openBottomSheet by rememberSaveable { mutableStateOf(false) }
//    val bottomSheetState = rememberModalBottomSheetState(
//        skipPartiallyExpanded = false
//    )
    val scope = rememberCoroutineScope()
    var webView: WebView? by remember { mutableStateOf(null) }

//    fun closeModal() {
//        scope.launch { bottomSheetState.hide() }.invokeOnCompletion {
//            if (!bottomSheetState.isVisible) {
//                openBottomSheet = false
//            }
//        }
//    }
//
//    if (openBottomSheet) {
//        ModalBottomSheet(
//            modifier = Modifier
//                .fillMaxWidth()
//                .windowInsetsPadding(WindowInsets.ime),
//            onDismissRequest = { openBottomSheet = false },
//            sheetState = bottomSheetState,
//        ) {
//            Column(
//                modifier = Modifier
//                    .padding(horizontal = 35.dp)
//                    .padding(bottom = 25.dp)
//            ) {
//                Row(modifier = Modifier
//                    .fillMaxWidth()
//                    .noRippleClickable {
//                        webView?.reload()
//                        closeModal()
//                    }
//                    .padding(vertical = 12.dp)
//                ) {
//                    Icon(
//                        Icons.Rounded.Refresh,
//                        contentDescription = stringResource(id = R.string.refresh),
//                        tint = MaterialTheme.colorScheme.onBackground
//                    )
//                    Spacer(modifier = Modifier.width(10.dp))
//                    Text(stringResource(id = R.string.refresh))
//                }
//
//                val uriHandler = LocalUriHandler.current
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .noRippleClickable(onClick = {
//                            uriHandler.openUri(webView?.url ?: url)
//                            closeModal()
//                        })
//                        .padding(vertical = 12.dp)
//                ) {
//                    Icon(
//                        painter = painterResource(R.drawable.round_open_in_browser_24),
//                        contentDescription = stringResource(R.string.open_in_browser),
//                        tint = MaterialTheme.colorScheme.onBackground
//                    )
//                    Spacer(modifier = Modifier.width(10.dp))
//                    Text(stringResource(R.string.open_in_browser))
//                }
//            }
//        }
//    }

    var progress by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            Box(contentAlignment = Alignment.BottomCenter) {
                TopAppBar(
                    title = {
                        Text(
                            title ?: webTitle ?: stringResource(R.string.web_browser),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold)
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
//                Divider(thickness = 0.5.dp)
//                ProgressBar(progress)
            }
        }
    ) {
        Surface(modifier = Modifier.padding(it)) {
            Column {
                val uriHandler = LocalUriHandler.current
                WebViewComponent(
                    url,
                    onProgressChanged = { _, num ->
                        progress = num
                    },
                    onReceivedTitle = { _, title ->
                        webTitle = title
                    },
                    onCreated = { wV ->
                        webView = wV
                    },
                    onRequestOpenBrowser = { url ->
                        uriHandler.openUri(url)
                    }
                )
            }
        }
    }
}