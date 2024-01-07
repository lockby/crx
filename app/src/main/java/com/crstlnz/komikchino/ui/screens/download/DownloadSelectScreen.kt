package com.crstlnz.komikchino.ui.screens.download

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.crstlnz.komikchino.R
import com.crstlnz.komikchino.config.AppSettings
import com.crstlnz.komikchino.data.api.Bahasa
import com.crstlnz.komikchino.data.database.model.ChapterDownloadItem
import com.crstlnz.komikchino.data.database.model.MangaChapterDownload
import com.crstlnz.komikchino.data.model.DataState.Idle.getDataOrNull
import com.crstlnz.komikchino.data.model.State
import com.crstlnz.komikchino.data.util.formatRelativeDate
import com.crstlnz.komikchino.data.util.isToday
import com.crstlnz.komikchino.ui.theme.Blue
import com.crstlnz.komikchino.ui.theme.Green
import com.crstlnz.komikchino.ui.theme.Red

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun DownloadSelectScreen(navController: NavController, title: String) {
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
                    navController.popBackStack()
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
        val pullRefreshState =
            rememberPullRefreshState(dataState.state == State.LOADING && !v.isFirstLaunch,
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

@Composable
fun DownloadList(downloadData: MangaChapterDownload?) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 15.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(downloadData?.manga?.title?.ifEmpty { "Not found!" } ?: "Not found!")
        Text((downloadData?.chapters?.size ?: 0).toString())
    }
}

@Composable
fun ChapterList(v: DownloadSelectViewModel, downloadData: MangaChapterDownload?) {
    val dataState = v.state.collectAsState()
    if (dataState.value.state === State.DATA) {
        val data by remember { mutableStateOf(dataState.value.getDataOrNull()!!) }
        val chapters by remember { mutableStateOf(data.chapters.reversed()) }
        LazyColumn(
            Modifier.fillMaxSize()
        ) {
            items(data.chapters.size, key = { iKey ->
                chapters[iKey].id
            }) { chapterIndex ->
                val isSelected =
                    downloadData?.chapters?.find { it.id == chapters[chapterIndex].id } != null
                ListItem(headlineContent = {
                    Text(text = chapters[chapterIndex].title)
                }, supportingContent = {
                    val date = chapters[chapterIndex].date
                    if (date != null) {
                        Text(
                            if (!isToday(date)) formatRelativeDate(date)
                            else if (AppSettings.komikServer!!.bahasa === Bahasa.ENGLISH) "Today"
                            else "Hari ini"
                        )
                    }
                }, trailingContent = {
                    if (isSelected) {
                        Icon(
                            Icons.Filled.Done, contentDescription = "Added", tint = Green
                        )
                    }
                }, modifier = Modifier.clickable {
                    if (!isSelected) {
                        v.select(
                            ChapterDownloadItem(
                                id = chapters[chapterIndex].id,
                                title = chapters[chapterIndex].title,
                                slug = chapters[chapterIndex].slug,
                                mangaId = chapters[chapterIndex].mangaId,
                            )
                        )
                    }
                }, colors = ListItemDefaults.colors(
                    containerColor = if (v.has(
                            chapters[chapterIndex].id
                        )
                    ) Blue.copy(
                        alpha = 0.1f
                    ) else Color.Transparent
                )
                )
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(
                        alpha = 0.5f
                    )
                )
            }
        }

    } else if (dataState.value.state === State.ERROR) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column {
                Image(painter = painterResource(id = R.drawable.empty_box), "Empty")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Chapter empty!")
            }
        }
    } else {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteDialog(viewModel: DownloadSelectViewModel, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = {
        onDismiss()
    }) {
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Download Chapter",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Are you sure you want to download ${
                        viewModel.downloadSelect.size
                    } Chapter?"
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    TextButton(
                        onClick = {
                            onDismiss()
                        }, colors = ButtonDefaults.textButtonColors(contentColor = Red)
                    ) {
                        Text("Cancel")
                    }
                    TextButton(
                        onClick = {
                            viewModel.addDownload()
                            viewModel.clear()
                            onDismiss()
                        },

                        ) {
                        Text("Download")
                    }
                }
            }
        }
    }
}