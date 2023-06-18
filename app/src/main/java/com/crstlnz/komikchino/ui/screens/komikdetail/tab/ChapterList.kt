package com.crstlnz.komikchino.ui.screens.komikdetail.tab

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.crstlnz.komikchino.R
import com.crstlnz.komikchino.data.model.Chapter
import com.crstlnz.komikchino.data.util.formatRelativeDate
import com.crstlnz.komikchino.ui.components.ErrorView
import com.crstlnz.komikchino.ui.screens.komikdetail.KomikViewModel
import com.crstlnz.komikchino.ui.theme.Black2
import com.crstlnz.komikchino.ui.theme.WhiteGray

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ChapterList(
    modifier: Modifier = Modifier,
    viewModel: KomikViewModel,
    state: LazyListState = rememberLazyListState(),
    chapterList: List<Chapter> = listOf(),
    onReload: () -> Unit = {},
    onChapterClick: (title: String, id: Int) -> Unit = { _, _ -> },
) {
    val isReversed by viewModel.isReversed.collectAsState()
    val chapters = if (isReversed) chapterList.reversed() else chapterList
    val interactionSource = remember { MutableInteractionSource() }
    val chapterHistory = viewModel.chapterReadHistory.observeAsState(listOf())
    viewModel.chapterReadHistory.observeForever {
        Log.d("INIDIA", it.toString())
    }
    LazyColumn(modifier, state = state) {
        if (chapters.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 5f)
                        .padding(horizontal = 30.dp), contentAlignment = Alignment.Center
                ) {
                    ErrorView(resId = R.drawable.empty_box, message = "Tidak ada chapter") {
                        onReload()
                    }
                }
            }
        } else {
            item() {
                Column() {
                    Row(
                        Modifier
                            .padding(start = 15.dp, end = 15.dp, top = 20.dp, bottom = 10.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${chapters.size} ${if (chapters.size > 1) "chapters" else "chapter"}",
                            style = MaterialTheme.typography.caption
                        )
                        Text(
                            if (!isReversed) "Terbaru" else "Terlama",
                            modifier = Modifier.clickable(
                                interactionSource = interactionSource, indication = null
                            ) {
                                viewModel.toggleReversed()
                            },
                            style = MaterialTheme.typography.caption.copy(
                                color = WhiteGray.copy(
                                    alpha = 0.7f
                                )
                            )
                        )
                    }
                }
            }

            items(chapters.size, key = { chapters[it].id ?: chapters[it].slug }) { index ->
                ListItem(modifier = Modifier
                    .background(color = if (chapterHistory.value.any { c ->
                            chapters[index].id == c.chapterId
                        }) WhiteGray.copy(alpha = 0.1f) else Color.Transparent)
                    .clickable {
                        onChapterClick(chapters[index].title, chapters[index].id ?: 0)
                    }, text = {
                    Column(Modifier.padding(vertical = 10.dp)) {
                        Text(
                            chapters[index].title,
                            style = TextStyle(fontWeight = FontWeight.SemiBold)
                        )
                        Text(chapters[index].date?.let { formatRelativeDate(it) } ?: "",
                            style = MaterialTheme.typography.caption)
                    }
                })
                Divider(color = Black2.copy(alpha = 0.7f))
            }
        }
    }
}
