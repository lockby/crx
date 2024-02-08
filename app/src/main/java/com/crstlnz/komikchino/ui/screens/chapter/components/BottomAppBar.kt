package com.crstlnz.komikchino.ui.screens.chapter.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.crstlnz.komikchino.R
import com.crstlnz.komikchino.config.AppSettings
import com.crstlnz.komikchino.data.model.DataState.Idle.getDataOrNull
import com.crstlnz.komikchino.ui.screens.chapter.ChapterViewModel
import com.crstlnz.komikchino.ui.theme.Red

@Composable
fun BottomAppBar(
    viewModel: ChapterViewModel,
    onDrawerClick: () -> Unit = {},
    onChapterClick: (id: String, slug: String) -> Unit = { _, _ -> },
    onCommentClick: () -> Unit = {},
    onMangaClick: () -> Unit = {},
    onDownloadClick: () -> Unit = {},
) {
    val chapterList by viewModel.chapterList.collectAsState()
    val chapterPosition by viewModel.currentPosition.collectAsState()
    val isFavorite by viewModel.isFavorite(viewModel.komikData?.id ?: "").observeAsState()
    val outline = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    Column {
//        Surface(
//            color = MaterialTheme.colorScheme.surfaceVariant,
//            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
//        ) {
//            Row(
//                Modifier
//                    .fillMaxWidth()
//                    .drawBehind {
//                        val strokeWidth = 1 * density
//                        val y = size.height - strokeWidth / 2
//                        drawLine(
//                            outline,
//                            Offset(0f, y),
//                            Offset(size.width, y),
//                            strokeWidth
//                        )
//                    }
//                    .padding(horizontal = 18.dp, vertical = 2.dp),
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//                Box(
//                    Modifier
//                        .clip(RoundedCornerShape(8.dp))
//                        .background(Color.Black.copy(alpha = 0.3f))
//                ) {
//                    Text(
//                        "Online Mode",
//                        Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
//                        style = MaterialTheme.typography.titleSmall
//                    )
//                }
//                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
//                    IconButton(
//                        onClick = {
//                            onMangaClick()
//                        },
//                    ) {
//                        Icon(
//                            painter = painterResource(id = R.drawable.info),
//                            contentDescription = "Manga info"
//                        )
//                    }
//                    IconButton(
//                        onClick = {
//                            onDownloadClick()
//                        },
//                    ) {
//                        Icon(
//                            painter = painterResource(id = R.drawable.download),
//                            contentDescription = "Download"
//                        )
//                    }
//                }
//            }
//        }
        androidx.compose.material.BottomAppBar(
            backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ) {
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceAround) {
                IconButton(onClick = {
                    onDrawerClick()
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.list), contentDescription = "Menu"
                    )
                }
                IconButton(
                    enabled = AppSettings.komikServer!!.haveComment,
                    onClick = {
                    onCommentClick()
                }) {

                    Icon(
                        painter = painterResource(id = R.drawable.chat),
                        contentDescription = "Comments"
                    )
                }
                IconButton(onClick = {
                    viewModel.setFavorite(isFavorite != 1)
                }) {
                    if (isFavorite == 1) {
                        Icon(
                            Icons.Filled.Favorite, contentDescription = "Favorite", tint = Red
                        )
                    } else {
                        Icon(
                            Icons.Filled.FavoriteBorder, contentDescription = "Favorite"
                        )
                    }
                }
                IconButton(
                    onClick = {
                        val chapter = chapterList.getDataOrNull()?.getOrNull(chapterPosition - 1)
                        if (chapter != null) {
                            onChapterClick(
                                chapter.id ?: "", chapter.title
                            )
                        }
                    }, enabled = chapterPosition > 0
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.arrow_left),
                        contentDescription = "Previous"
                    )
                }
                IconButton(
                    onClick = {
                        val chapter = chapterList.getDataOrNull()?.getOrNull(chapterPosition + 1)
                        if (chapter != null) {
                            onChapterClick(
                                chapter.id ?: "", chapter.title
                            )
                        }
                    }, enabled = (chapterPosition + 1) < (chapterList.getDataOrNull()?.size ?: 0)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.arrow_right),
                        contentDescription = "Next"
                    )
                }
            }
        }
    }
}
