package com.crstlnz.komikchino.ui.screens.home.fragments.bookmark.fragments

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.crstlnz.komikchino.data.database.favorite.FavoriteKomikItem
import com.crstlnz.komikchino.data.database.komik.KomikHistoryItem
import com.crstlnz.komikchino.ui.components.ImageView
import com.crstlnz.komikchino.ui.screens.home.fragments.bookmark.BookmarkViewModel

@Composable
fun FavoriteView(
    viewModel: BookmarkViewModel,
    onKomikClick: (komik: KomikHistoryItem) -> Unit = {},
) {
    val data by viewModel.favorites.observeAsState()
    if (data == null) {
        LoadingView()
    } else if (data!!.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize()) {
            EmptyView()
        }
    } else {
        FavoriteGridView(data!!, onKomikClick = {
            onKomikClick(it)
        })
    }
}

@Composable
fun FavoriteGridView(
    favorites: List<FavoriteKomikItem>,
    onKomikClick: (komik: KomikHistoryItem) -> Unit = {},
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(140.dp),
        contentPadding = PaddingValues(horizontal = 15.dp, vertical = 15.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(favorites.size) {
            Card(
                Modifier
                    .fillMaxWidth()
                    .clickable {
                        val favorite = favorites[it]
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
                    .aspectRatio(5f / 7f), shape = RoundedCornerShape(8.dp)) {
                Box() {
                    ImageView(
                        url = favorites[it].img,
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
                                    favorites[it].title,
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