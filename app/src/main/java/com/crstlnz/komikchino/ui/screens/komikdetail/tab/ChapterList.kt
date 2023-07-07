package com.crstlnz.komikchino.ui.screens.komikdetail.tab

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
import androidx.compose.material3.Divider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.crstlnz.komikchino.R
import com.crstlnz.komikchino.data.model.Chapter
import com.crstlnz.komikchino.data.model.DataState.Loading.getDataOrNull
import com.crstlnz.komikchino.data.util.formatRelativeDate
import com.crstlnz.komikchino.data.util.isToday
import com.crstlnz.komikchino.ui.components.ErrorView
import com.crstlnz.komikchino.ui.screens.komikdetail.KomikViewModel
import com.crstlnz.komikchino.ui.theme.WhiteGray
import kotlinx.coroutines.launch

@Composable
fun ChapterList(
    modifier: Modifier = Modifier,
    viewModel: KomikViewModel,
    state: LazyListState = rememberLazyListState(),
    chapterList: List<Chapter> = listOf(),
    onReload: () -> Unit = {},
    onChapterClick: (title: String, id: String) -> Unit = { _, _ -> },
) {
    val scope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }
    val dataState by viewModel.state.collectAsState()
    val chapterHistory by viewModel.readChapterHistory(dataState.getDataOrNull()?.id ?: "")
        .observeAsState()
    val chapterMap = chapterHistory?.associateBy { it.id } ?: mapOf()
    val isAscending by viewModel.settings.isChapterSortAscending.collectAsState(initial = false)
    val chapters = if (isAscending) chapterList.reversed() else chapterList

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
            item {
                Column {
                    Row(
                        Modifier
                            .padding(start = 15.dp, end = 15.dp, top = 20.dp, bottom = 10.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${chapters.size} ${if (chapters.size > 1) "chapters" else "chapter"}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            if (!isAscending) "Terbaru" else "Terlama",
                            modifier = Modifier.clickable(
                                interactionSource = interactionSource, indication = null
                            ) {
                                scope.launch {
                                    viewModel.settings.setChapterSort(!isAscending)
                                }
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            items(chapters.size, key = { chapters[it].id ?: chapters[it].slug }) { index ->
                ListItem(modifier = Modifier
                    .clickable {
                        onChapterClick(chapters[index].title, chapters[index].id ?: "")
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = if (chapterMap.containsKey(chapters[index].id)) WhiteGray.copy(
                            alpha = 0.1f
                        ) else Color.Transparent
                    ),
                    headlineContent = {
                        Text(
                            chapters[index].title,
                        )
                    },
                    supportingContent = {
                        val date = chapters[index].date
                        if (date != null) {
                            Text(if (!isToday(date)) formatRelativeDate(date) else "Hari ini")
                        }
                    }
                )
                Divider()
            }
        }
    }
}
