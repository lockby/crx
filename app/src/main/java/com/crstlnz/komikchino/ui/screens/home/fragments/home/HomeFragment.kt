package com.crstlnz.komikchino.ui.screens.home.fragments.home

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.crstlnz.komikchino.R
import com.crstlnz.komikchino.data.model.FeaturedComic
import com.crstlnz.komikchino.data.model.PopularComic
import com.crstlnz.komikchino.data.model.State
import com.crstlnz.komikchino.data.util.homeStorageHelper
import com.crstlnz.komikchino.ui.components.ErrorView
import com.crstlnz.komikchino.ui.components.ImageView
import com.crstlnz.komikchino.ui.navigations.MainNavigation
import com.crstlnz.komikchino.ui.screens.home.HomeViewModel
import com.crstlnz.komikchino.ui.screens.home.HomeViewModelFactory
import com.crstlnz.komikchino.ui.theme.Black3
import com.crstlnz.komikchino.ui.theme.Red
import com.crstlnz.komikchino.ui.theme.Yellow
import com.crstlnz.komikchino.ui.util.defaultPlaceholder
import com.crstlnz.komikchino.ui.util.getComicTypeColor
import com.crstlnz.komikchino.ui.util.lightenColor
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.delay

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun HomeFragment(navController: NavController) {
    val v = viewModel<HomeViewModel>(
        factory = HomeViewModelFactory(
            homeStorageHelper(
                LocalContext.current
            )
        )
    )
    val dataState by v.state.collectAsState()
    val data = dataState.data
    LazyColumn(Modifier.fillMaxSize()) {
        when (dataState.state) {
            State.DATA -> {
                if (data == null) {
                    item {
                        ErrorView(resId = R.drawable.empty_box,
                            message = stringResource(R.string.empty),
                            onClick = {
                                v.load()
                            })
                    }
                } else {
                    featuredView(
                        data.featured, onKomikClick = { title, slug ->
                            navController.navigate("${MainNavigation.KOMIKDETAIL}/${title}/${slug}")
                        },
                        4
                    )

                    popularView(data.popular) { title, slug ->
                        navController.navigate("${MainNavigation.KOMIKDETAIL}/${title}/${slug}")
                    }
                }
            }

            else -> {
                item {
                    Text("wew")
                }
            }
        }
    }
}

fun LazyListScope.komikLoading() {
    items(count = 5, key = {
        it
    }) {
        Card(
            modifier = Modifier
                .padding(horizontal = 15.dp, vertical = 7.5.dp)
                .defaultPlaceholder(
                    color = lightenColor(MaterialTheme.colors.surface, 20),
                    highlightColor = lightenColor(MaterialTheme.colors.surface, 60)
                )
        ) {
            val height = 90.dp
            Row(
                modifier = Modifier.padding(15.dp)
            ) {
                Box(
                    modifier = Modifier
                        .height(height)
                        .aspectRatio(8F / 10F),
                )
                Spacer(modifier = Modifier.width(15.dp))
                Column(
                    modifier = Modifier
                        .weight(1F)
                        .height(height)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1F)
                            .fillMaxWidth()
                    ) {
                        Text(
                            "Member Name",
                            modifier = Modifier.defaultPlaceholder(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                    Box(
                        modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd
                    ) {
                        Text(
                            "Graduate",
                            modifier = Modifier.defaultPlaceholder(),
                            style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.SemiBold),
                            textAlign = TextAlign.Right,
                            color = MaterialTheme.colors.primary
                        )
                    }
                }
            }
        }
    }
}


fun LazyListScope.featuredView(
    featureds: List<FeaturedComic>,
    onKomikClick: (title: String, slug: String) -> Unit = { _, _ -> },
    autoScrollSecond: Long = 0
) {
    item {
        val pagerState = rememberPagerState(initialPage = Int.MAX_VALUE / 2)
        var elapsedTime by remember { mutableIntStateOf(0) }
        LaunchedEffect(true) {
            if (autoScrollSecond > 0) {
                while (true) {
                    delay(1000)
                    elapsedTime += 1000
                    if (elapsedTime / 1000 > autoScrollSecond) {
                        elapsedTime = 0
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                }
            }
        }
        HorizontalPager(
            count = Int.MAX_VALUE, state = pagerState,
            modifier = Modifier.nestedScroll(remember {
                object : NestedScrollConnection {
                    override fun onPostScroll(
                        consumed: Offset,
                        available: Offset,
                        source: NestedScrollSource
                    ): Offset {
                        elapsedTime = 0
                        return Offset.Zero
                    }
                }
            })
        ) { index ->
            val it = index % featureds.size
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 10f)
                    .background(color = Black3)
                    .clickable {
                        onKomikClick(featureds[it].title, featureds[it].slug)
                    }
            ) {
                ImageView(url = featureds[it].img)
                Box(
                    Modifier
                        .background(color = Color.Black.copy(alpha = 0.3f))
                        .fillMaxSize()
                )
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                                100F,
                                600F
                            )
                        )
                )
                Column(
                    Modifier
                        .padding(15.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        featureds[it].title, style = MaterialTheme.typography.h6.copy(
                            fontWeight = FontWeight.SemiBold, shadow = Shadow(
                                Color.Black, blurRadius = 8f, offset = Offset(2f, 0f)
                            )
                        ),
                        modifier = Modifier.fillMaxWidth(0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        featureds[it].description, style = MaterialTheme.typography.body2.copy(
                            fontWeight = FontWeight.SemiBold, shadow = Shadow(
                                Color.Black, blurRadius = 8f, offset = Offset(2f, 0f)
                            )
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            featureds[it].type,
                            style = MaterialTheme.typography.body2.copy(
                                color = getComicTypeColor(featureds[it].type),
                                fontWeight = FontWeight.SemiBold,
                                shadow = Shadow(
                                    Color.Black, blurRadius = 8f, offset = Offset(2f, 0f)
                                )
                            )
                        )
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = "Star",
                            modifier = Modifier.height(16.dp),
                            tint = Yellow
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            featureds[it].score.toString(),
                            style = MaterialTheme.typography.body2.copy(
                                fontWeight = FontWeight.SemiBold,
                                shadow = Shadow(
                                    Color.Black, blurRadius = 8f, offset = Offset(2f, 0f)
                                )
                            )
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            featureds[it].genre.joinToString(", ") { it.title },
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.body2.copy(
                                fontWeight = FontWeight.SemiBold,
                                shadow = Shadow(
                                    Color.Black, blurRadius = 8f, offset = Offset(2f, 0f)
                                )
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
fun LazyListScope.popularView(
    popular: List<PopularComic> = listOf(),
    onKomikClick: (title: String, slug: String) -> Unit = { _, _ -> }
) {
    item {
        Row(
            Modifier.padding(start = 15.dp, end = 15.dp, top = 20.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(backgroundColor = Red, shape = CircleShape) {
                androidx.compose.material.Icon(
                    painter = painterResource(id = R.drawable.fire),
                    contentDescription = "Popular Icon",
                    modifier = Modifier
                        .padding(
                            start = 3.5.dp,
                            end = 2.5.dp,
                            top = 3.dp,
                            bottom = 3.dp
                        )
                        .width(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                "Popular Today",
                style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.SemiBold)
            )
        }
    }
    items(count = popular.size, key = {
        popular[it].slug.ifEmpty {
            it
        }
    }) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onKomikClick(
                    popular[it].title,
                    popular[it].slug
                )
            }) {
            Row(Modifier.padding(15.dp)) {
                ImageView(
                    url = popular[it].img,
                    modifier = Modifier
                        .width(120.dp)
                        .aspectRatio(11f / 16f)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(Modifier.width(10.dp))
                Column() {
                    Text(
                        popular[it].title, style = MaterialTheme.typography.body1.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    FlowRow(verticalArrangement = Arrangement.Center) {
                        Text(
                            popular[it].type,
                            style = MaterialTheme.typography.caption.copy(
                                color = getComicTypeColor(popular[it].type),
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Spacer(Modifier.width(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Star,
                                contentDescription = "Star",
                                modifier = Modifier.height(14.dp),
                                tint = Yellow
                            )
                            Spacer(Modifier.width(2.dp))
                            Text(
                                popular[it].score.toString(),
                                style = MaterialTheme.typography.caption.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    shadow = Shadow(
                                        Color.Black, blurRadius = 8f, offset = Offset(2f, 0f)
                                    )
                                )
                            )
                        }
                        Spacer(Modifier.width(6.dp))
                        Text(popular[it].chapterString, style = MaterialTheme.typography.caption)
                    }
                }
            }
        }
    }
}
