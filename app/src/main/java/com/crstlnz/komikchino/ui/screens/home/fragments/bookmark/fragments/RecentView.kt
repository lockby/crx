package com.crstlnz.komikchino.ui.screens.home.fragments.bookmark.fragments

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.crstlnz.komikchino.R
import com.crstlnz.komikchino.data.database.model.ChapterEmbed
import com.crstlnz.komikchino.data.database.model.ChapterHistoryItem
import com.crstlnz.komikchino.data.database.model.KomikHistoryItem
import com.crstlnz.komikchino.data.database.model.KomikReadHistory
import com.crstlnz.komikchino.ui.components.ErrorView
import com.crstlnz.komikchino.ui.components.ImageView
import com.crstlnz.komikchino.ui.screens.home.fragments.bookmark.BookmarkViewModel
import com.crstlnz.komikchino.ui.util.getComicTypeColor


@Composable
fun RecentView(
    viewModel: BookmarkViewModel,
    onKomikClick: (komik: KomikHistoryItem) -> Unit = {},
    onChapterClick: (komik: KomikHistoryItem, chapter: ChapterEmbed) -> Unit = { _, _ -> },
    pageId: String
) {
    val data by viewModel.histories.observeAsState()

    if (data == null) {
        LoadingView()
    } else if (data!!.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize()) {
            EmptyView()
        }
    } else {
        val sortedData = data!!.sortedByDescending { it.updatedAt }
        RecentListView(
            viewModel,
            sortedData,
            onKomikClick = {
                onKomikClick(it)
            },
            pageId = pageId,
            onChapterClick = { komik, chapter ->
                onChapterClick(komik, chapter)
            })
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RecentListView(
    viewModel: BookmarkViewModel,
    histories: List<KomikHistoryItem>,
    onKomikClick: (komik: KomikHistoryItem) -> Unit,
    onChapterClick: (komik: KomikHistoryItem, chapter: ChapterEmbed) -> Unit,
    pageId: String
) {
    val isEditMode = viewModel.editState.contains(pageId)
    LaunchedEffect(isEditMode) {
        if (isEditMode) {
            viewModel.setData(pageId, histories.map { it.id })
        } else {
            viewModel.deselectAll(pageId)
        }
    }


    LazyColumn(
        contentPadding = PaddingValues(vertical = 10.dp),
        modifier = Modifier.absoluteOffset(0.dp, 0.dp)
    ) {
        items(histories.size) {
            val komik = histories[it]
            val chapter = histories[it].chapter
            Box(
                Modifier
                    .background(
                        color = if (viewModel
                                .getSelected(pageId)
                                .contains(komik.id)
                        ) Color.White.copy(
                            alpha = 0.1f
                        ) else Color.Transparent
                    )
                    .combinedClickable(
                        onClick = {
                            if (isEditMode) {
                                viewModel.select(pageId, komik.id)
                            } else {
                                if (chapter != null) {
                                    onChapterClick(komik, chapter)
                                } else {
                                    onKomikClick(komik)
                                }
                            }
                        },
                        onLongClick = {
                            viewModel.edit(pageId)
                            viewModel.select(pageId, komik.id)
                        }
                    )) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp, vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(90.dp)
                            .clickable {
                                if (isEditMode) {
                                    viewModel.select(pageId, komik.id)
                                } else {
                                    onKomikClick(komik)
                                }
                            }
                            .aspectRatio(5f / 7f)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        ImageView(
                            url = komik.img,
                            contentDescription = "Thumbnail",
                            modifier = Modifier
                                .fillMaxSize()
                        )

                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                                        100F,
                                        340F
                                    )
                                )
                        )


                        Box(
                            Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                        ) {
                            Text(
                                text = "Detail",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .padding(5.dp)
                                    .fillMaxWidth()
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
                        if (komik.type.isNotEmpty())
                            Text(
                                komik.type,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = getComicTypeColor(komik.type)
                                ),
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 2,
                            )

                        if (chapter != null) {
                            val scrollPos = viewModel.getChapterScrollPosition(komik.id, chapter.id)

                            val title = if (scrollPos != null) {
                                val imagePos = if (scrollPos.initialFirstVisibleItemIndex - 1 < 0) {
                                    0
                                } else {
                                    scrollPos.initialFirstVisibleItemIndex - 1
                                }

                                "${chapter.title} - Gambar ke ${imagePos + 1} / ${scrollPos.imageSize.size}"
                            } else {
                                chapter.title
                            }
                            Spacer(modifier = Modifier.height(5.dp))
                            Text(
                                title,
                                style = MaterialTheme.typography.labelMedium,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 2,
                            )
                        }

                    }
                }
            }
        }
    }
}

@Composable
fun RecentGridView(
    histories: List<KomikReadHistory>,
    onKomikClick: (komik: KomikHistoryItem) -> Unit = {},
    onChapterClick: (komik: KomikHistoryItem, chapter: ChapterHistoryItem) -> Unit = { _, _ -> }
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(140.dp),
        contentPadding = PaddingValues(horizontal = 15.dp, vertical = 15.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(histories.size) {
            Card(
                Modifier
                    .fillMaxWidth()
                    .pointerInput(histories) {
                        detectTapGestures(
                            onTap = { _ ->
                                val chapter = histories[it].chapter
                                if (chapter != null) {
                                    onChapterClick(histories[it].komik, chapter)
                                } else {
                                    onKomikClick(histories[it].komik)
                                }
                            }, onLongPress = { _ ->
                                onKomikClick(histories[it].komik)
                            })
                    }
                    .aspectRatio(5f / 7f), shape = RoundedCornerShape(8.dp)) {
                Box {
                    ImageView(
                        url = histories[it].komik.img,
                        contentDescription = "Thumbnail",
                        modifier = Modifier.fillMaxSize()
                    )
                    Column(
                        Modifier
                            .fillMaxSize()

                    ) {
                        Spacer(Modifier.weight(1f))
                        val chapter = histories[it].chapter
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(color = Color.Black.copy(alpha = 0.7f))
                        ) {
                            Column(Modifier.padding(6.dp)) {
                                if (chapter != null) {
                                    Text(
                                        chapter.title,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        color = getComicTypeColor(histories[it].komik.type),
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                        ),
                                        textAlign = TextAlign.Center
                                    )

                                }
                                Text(
                                    histories[it].komik.title,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        shadow = Shadow(color = Color.Black, blurRadius = 8f)
                                    ),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun EmptyView() {
    ErrorView(
        resId = R.drawable.empty_box, message = "Belum ada komik yang dibaca!", showButton = false
    )
}

@Preview
@Composable
fun LoadingView() {
    Box(
        Modifier
            .fillMaxWidth()
            .aspectRatio(1f), contentAlignment = Alignment.Center
    ) {
        Image(
            painterResource(id = R.drawable.sleeping_girl),
            contentDescription = "Loading",
            modifier = Modifier.fillMaxWidth(0.8f),
            contentScale = ContentScale.Crop
        )
    }
}

