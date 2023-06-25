package com.crstlnz.komikchino.ui.screens.chapter.components

import androidx.compose.foundation.layout.Arrangement
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
import com.crstlnz.komikchino.data.model.DataState.Idle.getDataOrNull
import com.crstlnz.komikchino.ui.screens.chapter.ChapterViewModel
import com.crstlnz.komikchino.ui.theme.Red

@Composable
fun BottomAppBar(
    viewModel: ChapterViewModel,
    onDrawerClick: () -> Unit = {},
    onChapterClick: (id: String, slug: String) -> Unit = { _, _ -> },
    onCommentClick: () -> Unit = {}
) {
    val chapterList by viewModel.chapterList.collectAsState()
    val chapterPosition by viewModel.currentPosition.collectAsState()
    val isFavorite by viewModel.isFavorite(viewModel.komikData?.id ?: "").observeAsState()
    androidx.compose.material.BottomAppBar(
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceAround) {
            IconButton(onClick = {
                onDrawerClick()
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.list), contentDescription = "Menu"
                )
            }
            IconButton(onClick = {
                onCommentClick()
            }) {

                Icon(
                    painter = painterResource(id = R.drawable.chat), contentDescription = "Comments"
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
                }, enabled = chapterPosition + 1 < (chapterList.getDataOrNull()?.size ?: 0)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_right),
                    contentDescription = "Next"
                )
            }
        }
    }
}
