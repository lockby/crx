package com.crstlnz.komikchino.ui.screens.home.fragments.bookmark.fragments

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.crstlnz.komikchino.data.database.chapterhistory.ChapterHistoryItem
import com.crstlnz.komikchino.data.database.komik.KomikHistoryItem
import com.crstlnz.komikchino.data.database.komik.KomikReadHistory
import com.crstlnz.komikchino.ui.components.ErrorView
import com.crstlnz.komikchino.ui.components.ImageView
import com.crstlnz.komikchino.ui.screens.home.fragments.bookmark.BookmarkViewModel
import com.crstlnz.komikchino.ui.util.getComicTypeColor


@Composable
fun RecentView(
    viewModel: BookmarkViewModel,
    onKomikClick: (komik: KomikHistoryItem) -> Unit = {},
    onChapterClick: (komik: KomikHistoryItem, chapter: ChapterHistoryItem) -> Unit = { _, _ -> }
) {
    val data by viewModel.histories.observeAsState()
    if (data == null) {
        LoadingView()
    } else if (data!!.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize()) {
            EmptyView()
        }
    } else {
        val sortedData = data!!.sortedByDescending { it.chapter?.data_id }
        RecentGridView(sortedData, onKomikClick = {
            onKomikClick(it)
        }, onChapterClick = { komik, chapter ->
            onChapterClick(komik, chapter)
        })
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
                Box() {
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
                                        style = MaterialTheme.typography.caption.copy(
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
                                    style = MaterialTheme.typography.caption.copy(
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

