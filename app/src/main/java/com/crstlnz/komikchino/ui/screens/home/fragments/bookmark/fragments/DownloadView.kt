package com.crstlnz.komikchino.ui.screens.home.fragments.bookmark.fragments

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.crstlnz.komikchino.R
import com.crstlnz.komikchino.config.AppSettings
import com.crstlnz.komikchino.data.database.model.MangaChapterDownload
import com.crstlnz.komikchino.data.database.model.MangaDownloadItem
import com.crstlnz.komikchino.services.DownloadViewModel
import com.crstlnz.komikchino.ui.components.ImageView
import com.crstlnz.komikchino.ui.screens.home.fragments.bookmark.BookmarkViewModel
import com.crstlnz.komikchino.ui.util.getComicTypeColor

@Composable
fun DownloadView(
    viewModel: BookmarkViewModel,
    onKomikClick: (komik: MangaDownloadItem) -> Unit = {},
    onDownloadDetailClick: (komik: MangaDownloadItem) -> Unit = {},
    pageId: String
) {
    val v: DownloadViewModel = AppSettings.downloadViewModel
    val isEditMode = viewModel.editState.contains(pageId)
    val currentDownload by v.currentDownload.collectAsState()
    var filteredDownloadList by remember { mutableStateOf<List<MangaChapterDownload>?>(null) }
    val downloadList by v.downloadList.observeAsState()
    LaunchedEffect(downloadList) {
        filteredDownloadList = downloadList?.filter { m ->
            m.manga.id !== currentDownload?.data?.manga?.id
        }
    }

    LaunchedEffect(isEditMode) {
        if (isEditMode) {
            viewModel.setData(pageId, downloadList!!.map { it.manga.id })
        } else {
            viewModel.deselectAll(pageId)
        }
    }
    LazyColumn(
        contentPadding = PaddingValues(vertical = 10.dp),
        modifier = Modifier.absoluteOffset(0.dp, 0.dp)
    ) {
        item {
            Box(
                Modifier.animateContentSize()
            ) {
                val currentDownloadState by v.currentDownload.collectAsState()
                val currentDescription by v.currentDescription.collectAsState()
                if (currentDownloadState != null) {
                    Column(
                        Modifier.fillMaxWidth().padding(horizontal = 15.dp)
                    ) {
                        Text(
                            "Current Download",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Row(Modifier.padding(vertical = 8.dp)) {
                            ImageView(
//                                url = "https://images2.prokal.co/webkp/file/berita/2023/01/23/f37f7f4ad6acf24673357956dd65aa0f.jpg",
                                url = currentDownloadState!!.data.manga.img,
                                contentDescription = "Manga Poster",
                                modifier = Modifier.width(90.dp).aspectRatio(5f / 7f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    currentDownloadState!!.data.manga.title,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                if (currentDescription != null) {
                                    Spacer(Modifier.height(4.dp))
                                    Row {
                                        CircularProgressIndicator(
                                            Modifier.width(MaterialTheme.typography.labelMedium.lineHeight.value.dp - 2.dp)
                                                .padding(top = 1.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            currentDescription!!,
                                            style = MaterialTheme.typography.labelMedium,
                                            overflow = TextOverflow.Ellipsis,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                        HorizontalDivider(Modifier.padding(top = 12.dp, bottom = 12.dp))
                    }
                }
            }
        }

        item {
            Text(
                "Download List",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 8.dp, start = 15.dp, end = 15.dp)
            )
        }
        if (filteredDownloadList == null) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        } else if (filteredDownloadList!!.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painter = painterResource(id = R.drawable.empty_box),
                            contentDescription = "Empty",
                            modifier = Modifier.fillMaxWidth(0.7f)
                        )
                        Spacer(modifier = Modifier.height(25.dp))
                        Text(
                            "Belum ada yang di download!",
                            Modifier.fillMaxWidth(0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {


            downloadListView(
                viewModel, filteredDownloadList!!,
                onKomikClick = {
                    onKomikClick(it)
                },
                onDownloadDetailClick = {
                    onDownloadDetailClick(it)
                },
                pageId = pageId,
                isEditMode = isEditMode
            )
        }
    }
}

//@Composable
//fun DownloadListView(viewModel: DownloadViewModel) {
//    val data by viewModel.downloadList.observeAsState(listOf())
//    LazyColumn(Modifier.fillMaxSize()) {
//        items(data.size, key = { data[it].manga.id }) {
//            ListItem(
//                leadingContent = {
//                    ImageView(
//                        url = data[it].manga.img,
//                        contentDescription = "Poster",
//                        shape = RoundedCornerShape(8.dp),
//                        modifier = Modifier
//                            .width(120.dp)
//                            .aspectRatio(3f / 4f)
//                    )
//                },
//                headlineContent = {
//                    Text(data[it].manga.title)
//                }
//            )
//        }
//    }
//}

@OptIn(ExperimentalFoundationApi::class)
fun LazyListScope.downloadListView(
    viewModel: BookmarkViewModel,
    downloadList: List<MangaChapterDownload>,
    onKomikClick: (komik: MangaDownloadItem) -> Unit,
    onDownloadDetailClick: (komik: MangaDownloadItem) -> Unit,
    pageId: String,
    isEditMode: Boolean
) {

//    LazyColumn(
//        contentPadding = PaddingValues(vertical = 10.dp),
//        modifier = Modifier.absoluteOffset(0.dp, 0.dp)
//    ) {
    items(downloadList.size, key = { downloadList[it].manga.id }) {
        val komik = downloadList[it].manga
//            val chapter = downloadList[it].chapter
        Box(
            Modifier.background(
                color = if (viewModel.getSelected(pageId).contains(komik.id)) Color.White.copy(
                    alpha = 0.1f
                ) else Color.Transparent
            ).combinedClickable(onClick = {
                if (isEditMode) {
                    viewModel.select(pageId, komik.id)
                } else {
                    onDownloadDetailClick(komik)
//                                if (chapter != null) {
//                                    onChapterClick(komik, chapter)
//                                } else {
//                    onKomikClick(komik)
//                                }
                }
            }, onLongClick = {
                viewModel.edit(pageId)
                viewModel.select(pageId, komik.id)
            }).animateItemPlacement(
                animationSpec = TweenSpec(
                    250, 50, EaseOutCubic
                )
            )
        ) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 15.dp, vertical = 8.dp)
            ) {
                Box(modifier = Modifier.width(90.dp).clickable {
                    if (isEditMode) {
                        viewModel.select(pageId, komik.id)
                    } else {
                        onKomikClick(komik)
                    }
                }.aspectRatio(5f / 7f).clip(RoundedCornerShape(8.dp))) {
                    ImageView(
                        url = komik.img,
                        contentDescription = "Thumbnail",
                        modifier = Modifier.fillMaxSize()
                    )

                    Box(
                        Modifier.fillMaxSize().background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                                100F,
                                340F
                            )
                        )
                    )


                    Box(
                        Modifier.fillMaxWidth().align(Alignment.BottomCenter)
                    ) {
                        Text(
                            text = "Detail",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(5.dp).fillMaxWidth()
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        komik.title,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                    )
                    if (komik.type.isNotEmpty()) Text(
                        komik.type,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = getComicTypeColor(komik.type)
                        ),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                    )

                    Text(
                        "${downloadList[it].chapters.size} Chapter",
                        style = MaterialTheme.typography.labelMedium,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 2,
                    )

//                        if (chapter != null) {
//                            val scrollPos = viewModel.getChapterScrollPosition(komik.id, chapter.id)
//
//                            val title = if (scrollPos != null) {
//                                val imagePos = if (scrollPos.initialFirstVisibleItemIndex - 1 < 0) {
//                                    0
//                                } else {
//                                    scrollPos.initialFirstVisibleItemIndex - 1
//                                }
//
//                                "${chapter.title} - Gambar ke ${imagePos + 1} / ${scrollPos.imageSize.size}"
//                            } else {
//                                chapter.title
//                            }
//                            Spacer(modifier = Modifier.height(5.dp))
//                            Text(
//                                title,
//                                style = MaterialTheme.typography.labelMedium,
//                                overflow = TextOverflow.Ellipsis,
//                                maxLines = 2,
//                            )
//                        }

                }
//                }
            }
        }
    }
}