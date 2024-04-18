package com.crstlnz.komikchino.ui.screens.download

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.crstlnz.komikchino.R
import com.crstlnz.komikchino.data.model.DataState.Idle.getDataOrNull
import com.crstlnz.komikchino.data.model.State

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun DownloadDetailScreen(onBack: () -> Unit, title: String) {
    val v: DownloadSelectViewModel = hiltViewModel()
    val selected = v.downloadSelect.size
    val downloadData by v.downloadData.observeAsState()
    BackHandler(
        enabled = selected > 0
    ) {
        v.clear()
    }

    var openConfirmDialog by remember { mutableStateOf(false) }
    if (openConfirmDialog) {
        DeleteDialog(v, onDismiss = { openConfirmDialog = false })
    }

    Scaffold(topBar = {
        TopAppBar(navigationIcon = {
            IconButton(onClick = {
                if (selected > 0) {
                    v.clear()
                } else {
                    onBack()
                }
            }) {
                Icon(
                    if (selected > 0) Icons.Filled.Close else Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        }, title = {
            Text(
                if (selected <= 0) v.state.value.getDataOrNull()?.title
                    ?: title else "$selected Selected",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }, actions = {
            val dataState = v.state.collectAsState()
            val enabled = if (downloadData == null) {
                true
            } else if (dataState.value.getDataOrNull() == null) {
                false
            } else {
                (downloadData?.chapters?.filter { c -> v.downloadSelect.find { it.id == c.id } == null }
                    ?: listOf()).isEmpty()
            }

            Checkbox(enabled = enabled,
                checked = if (enabled) v.isAllSelected() else false,
                onCheckedChange = {
                    if (v.isAllSelected()) {
                        v.deselectAll()
                    } else {
                        v.selectAll()
                    }
                })
            IconButton(
                onClick = {
                    openConfirmDialog = true
//                    if (v.getSelected(pageId).size > 0)
//                        openDeleteDialog = true
                }, enabled = selected > 0
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.download),
                    contentDescription = "Delete",
                )
            }
//            TextButton(
//                enabled = selected > 0,
//                onClick = {
//                }) {
//                Text("DOWNLOAD")
//            }
        })
    }) {
        val dataState by v.state.collectAsState()
        val pullRefreshState = rememberPullRefreshState(
            dataState.state == State.LOADING && !v.isFirstLaunch,
            { v.load() })
        Surface(Modifier.padding(it)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pullRefresh(pullRefreshState)
            ) {
                ChapterList(v, downloadData)
                PullRefreshIndicator(
                    dataState.state == State.LOADING,
                    pullRefreshState,
                    Modifier.align(Alignment.TopCenter)
                )
            }

//            Column {
//                DownloadList(downloadData)
//            }
        }
    }
}