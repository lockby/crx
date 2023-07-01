package com.crstlnz.komikchino.ui.screens.komikdetail.tab

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.crstlnz.komikchino.R
import com.crstlnz.komikchino.data.model.KomikDetail
import com.crstlnz.komikchino.ui.components.ImageView
import com.crstlnz.komikchino.ui.theme.Black1
import com.crstlnz.komikchino.ui.theme.Blue
import com.crstlnz.komikchino.ui.theme.LightYellow
import com.crstlnz.komikchino.ui.theme.WhiteGray
import com.crstlnz.komikchino.ui.theme.Yellow
import com.crstlnz.komikchino.ui.util.convertHTML
import com.crstlnz.komikchino.ui.util.getComicTypeColor

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun DetailScreen(
    komikDetail: KomikDetail,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    onKomikClick: (title: String, slug: String) -> Unit = { _, _ -> }
) {
    LazyColumn(
        contentPadding = PaddingValues(15.dp),
        state = state,
        verticalArrangement = Arrangement.Bottom
    ) {
        item {
            Column() {
                Row(verticalAlignment = Alignment.Top) {
                    ImageView(
                        url = komikDetail.img, contentDescription = "Thumbnail",
                        modifier = Modifier
                            .width(100.dp)
                            .aspectRatio(12f / 16f)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            komikDetail.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                komikDetail.type,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = getComicTypeColor(komikDetail.type),
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            if (komikDetail.score != null) {
                                Spacer(Modifier.width(6.dp))
                                Icon(
                                    Icons.Filled.Star,
                                    contentDescription = "Star",
                                    modifier = Modifier.height(16.dp),
                                    tint = Yellow
                                )

                                Spacer(Modifier.width(4.dp))
                                Text(
                                    komikDetail.score.toString(),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(15.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.mipmap.synopsis),
                        modifier = Modifier.height(22.dp),
                        contentDescription = "Synopsis Icon"
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        stringResource(R.string.synopsis),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Spacer(modifier = Modifier.height(7.dp))
                Text(
                    convertHTML(komikDetail.description).trim(),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(20.dp))
                FlowRow(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.wrapContentWidth()
                ) {
                    komikDetail.genre.forEach {
                        Box(
                            Modifier.border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(15.dp)
                            )
                        ) {
                            Text(
                                it.title,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium.copy(color = Blue)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                if (komikDetail.similar.isNotEmpty()) {
                    val pagerState = rememberPagerState() {
                        komikDetail.similar.size
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.mipmap.komik),
                            modifier = Modifier.height(28.dp),
                            contentDescription = "Komik Icon"
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Similar Title", style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(modifier = Modifier.height(15.dp))
                    androidx.compose.foundation.pager.HorizontalPager(
                        state = pagerState,
                        pageSpacing = 12.dp,
                        pageSize = PageSize.Fixed(155.dp),
                        modifier = Modifier.nestedScroll(remember {
                            object : NestedScrollConnection {
                                override fun onPostScroll(
                                    consumed: Offset,
                                    available: Offset,
                                    source: NestedScrollSource
                                ): Offset {
                                    return available
                                }
                            }
                        })
                    ) {
                        val similar = komikDetail.similar[it]
                        Box(
                            modifier = modifier
                                .fillMaxWidth()
                                .aspectRatio(12f / 16f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable() {
                                    Log.d("SLUG", similar.slug)
                                    onKomikClick(
                                        similar.title, similar.slug
                                    )
                                }) {
                            ImageView(
                                url = similar.img,
                                modifier.fillMaxSize(),
                                contentDescription = "Thumbnail"
                            )
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color.Transparent, Color.Black), 240F, 635F
                                        )
                                    )
                            )
                            Column(
                                Modifier
                                    .fillMaxHeight()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = if (similar.isColored) LightYellow else Color.Transparent,
                                            shape = RoundedCornerShape(15.dp)
                                        )
                                ) {
                                    if (similar.isColored) {
                                        Row(
                                            Modifier.padding(
                                                horizontal = 8.dp,
                                                vertical = 6.dp
                                            ),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.baseline_palette_24),
                                                contentDescription = "Warna",
                                                modifier = Modifier.width(14.dp),
                                                tint = Black1
                                            )
                                            Spacer(Modifier.width(3.dp))
                                            Text(
                                                "WARNA",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = Black1,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            )
                                        }
                                    }
                                }

                                Column() {
                                    Row() {
                                        Text(
                                            similar.type,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                color = getComicTypeColor(similar.type),
                                                fontWeight = FontWeight.SemiBold,
                                                shadow = Shadow(
                                                    color = Color.Black, blurRadius = 10f
                                                )
                                            )
                                        )
                                        Spacer(Modifier.width(5.dp))
                                        similar.genre?.let { it1 ->
                                            Text(
                                                it1,
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    color = WhiteGray, shadow = Shadow(
                                                        color = Color.Black, blurRadius = 10f
                                                    )
                                                )
                                            )
                                        }
                                    }
                                    Text(
                                        similar.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = WhiteGray,
                                            fontWeight = FontWeight.SemiBold,
                                            shadow = Shadow(
                                                color = Color.Black,
                                                blurRadius = 10f
                                            )
                                        ),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

        }
    }
}
