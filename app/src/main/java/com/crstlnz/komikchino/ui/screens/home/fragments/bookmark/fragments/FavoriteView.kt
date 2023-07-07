package com.crstlnz.komikchino.ui.screens.home.fragments.bookmark.fragments

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.crstlnz.komikchino.R
import com.crstlnz.komikchino.data.database.model.FavoriteKomikItem
import com.crstlnz.komikchino.data.database.model.KomikHistoryItem
import com.crstlnz.komikchino.ui.components.ErrorView
import com.crstlnz.komikchino.ui.components.ImageView
import com.crstlnz.komikchino.ui.screens.home.fragments.bookmark.BookmarkViewModel
import com.crstlnz.komikchino.ui.theme.Blue

@Composable
fun FavoriteView(
    viewModel: BookmarkViewModel,
    onKomikClick: (komik: KomikHistoryItem) -> Unit = {},
    pageId: String
) {
    val data by viewModel.favorites.observeAsState()
    if (data == null) {
        LoadingView()
    } else if (data!!.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize()) {
            EmptyFavoriteView()
        }
    } else {
        FavoriteGridView(viewModel, data!!, onKomikClick = {
            onKomikClick(it)
        }, pageId = pageId)
    }
}

@Composable
fun EmptyFavoriteView() {
    ErrorView(
        resId = R.drawable.empty_box,
        message = "Belum ada komik yang difavoritkan!",
        showButton = false
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FavoriteGridView(
    viewModel: BookmarkViewModel,
    favorites: List<FavoriteKomikItem>,
    onKomikClick: (komik: KomikHistoryItem) -> Unit = {},
    pageId: String
) {
    val isEditMode = viewModel.editState.contains(pageId)

    LaunchedEffect(isEditMode) {
        if (isEditMode) {
            viewModel.setData(pageId, favorites.map { it.id })
        } else {
            viewModel.deselectAll(pageId)
        }
    }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(140.dp),
        contentPadding = PaddingValues(horizontal = 15.dp, vertical = 15.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(favorites.size) {
            val favorite = favorites[it]
            Card(
                Modifier
                    .fillMaxWidth()
                    .combinedClickable(onClick = {
                        if (isEditMode) {
                            viewModel.select(pageId, favorite.id)
                        } else {
                            onKomikClick(
                                KomikHistoryItem(
                                    title = favorite.title,
                                    img = favorite.img,
                                    description = favorite.description,
                                    slug = favorite.slug,
                                    id = favorite.id,
                                    type = favorite.type
                                )
                            )
                        }
                    },
                        onLongClick = {
                            viewModel.edit(pageId)
                            viewModel.select(pageId, favorite.id)
                        })
                    .aspectRatio(5f / 7f), shape = RoundedCornerShape(8.dp)
            ) {
                Box {
                    ImageView(
                        url = favorite.img,
                        contentDescription = "Thumbnail",
                        modifier = Modifier.fillMaxSize()
                    )
                    Column(
                        Modifier
                            .fillMaxSize()

                    ) {
                        Spacer(Modifier.weight(1f))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(color = Color.Black.copy(alpha = 0.7f))
                        ) {
                            Column(Modifier.padding(10.dp)) {
                                Text(
                                    favorite.title,
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

                    if (viewModel.getSelected(pageId).contains(favorite.id)) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color = Blue.copy(alpha = 0.3f))
                        )
                    }
                }
            }
        }
    }
}