package com.crstlnz.komikchino.ui.screens.home.fragments.home

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
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.crstlnz.komikchino.R
import com.crstlnz.komikchino.data.model.DataState
import com.crstlnz.komikchino.data.model.FeaturedComic
import com.crstlnz.komikchino.data.model.Section
import com.crstlnz.komikchino.data.model.State
import com.crstlnz.komikchino.ui.components.ErrorView
import com.crstlnz.komikchino.ui.components.ImageView
import com.crstlnz.komikchino.ui.navigations.MainNavigation
import com.crstlnz.komikchino.ui.theme.Black1
import com.crstlnz.komikchino.ui.theme.Black3
import com.crstlnz.komikchino.ui.theme.Blue
import com.crstlnz.komikchino.ui.theme.Red
import com.crstlnz.komikchino.ui.theme.Yellow
import com.crstlnz.komikchino.ui.util.defaultPlaceholder
import com.crstlnz.komikchino.ui.util.getComicTypeColor
import com.crstlnz.komikchino.ui.util.lightenColor
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeFragment(navController: NavController) {
    val v: HomeFragmenViewModel = hiltViewModel()
    val dataState by v.state.collectAsState()

    val pullRefreshState =
        rememberPullRefreshState(dataState.state == State.LOADING, { v.load() })
    Box(Modifier.pullRefresh(pullRefreshState)) {
        LazyColumn(Modifier.fillMaxSize()) {
            when (dataState.state) {
                State.DATA -> {
                    val data = (dataState as DataState.Success).data
                    featuredView(
                        data.featured, onKomikClick = { title, slug ->
                            MainNavigation.toKomik(navController, title, slug)
                        }, 4
                    )
                    sectionView(data.sections) { title, slug ->
                        MainNavigation.toKomik(navController, title, slug)
                    }
                }

                State.ERROR -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(5f / 8f),
                            contentAlignment = Alignment.Center
                        ) {
                            ErrorView(
                                resId = R.drawable.error,
                                message = "Gagal mendapatkan data!",
                                onClick = {
                                    v.load()
                                })
                        }
                    }
                }

                else -> {
                    komikLoading()
                }
            }
        }
        PullRefreshIndicator(
            dataState.state == State.LOADING && !v.isFirstLaunch,
            pullRefreshState,
            Modifier.align(Alignment.TopCenter)
        )
    }
}

@Preview
@Composable
fun LoadingPreview() {
    LazyColumn() {
        komikLoading()
    }
}

@OptIn(ExperimentalLayoutApi::class)
fun LazyListScope.komikLoading() {
    item {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 10f)
                .defaultPlaceholder(
                    color = lightenColor(Black1, 25),
                    highlightColor = lightenColor(Black1, 60),
                    shape = RectangleShape
                )
        )
    }

    item {
        Row(
            Modifier.padding(start = 15.dp, end = 15.dp, top = 20.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.background(color = Red, shape = CircleShape)) {
                Icon(
                    tint = Color.White,
                    painter = painterResource(id = R.drawable.fire),
                    contentDescription = "Popular Icon",
                    modifier = Modifier
                        .padding(
                            start = 3.5.dp, end = 2.5.dp, top = 3.dp, bottom = 3.dp
                        )
                        .width(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                "Popular Today",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
        }
    }

    items(count = 10, key = { it }) {
        Row(Modifier.padding(15.dp)) {
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .aspectRatio(11f / 16f)
                    .clip(RoundedCornerShape(8.dp))
                    .defaultPlaceholder(
                        color = lightenColor(Black1, 25), highlightColor = lightenColor(Black1, 60)
                    )
            )
            Spacer(Modifier.width(10.dp))
            Column() {
                Box(
                    modifier = Modifier
                        .height(16.dp)
                        .fillMaxWidth(0.8f)
                        .defaultPlaceholder(
                            color = lightenColor(Black1, 25),
                            highlightColor = lightenColor(Black1, 60)
                        )
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(verticalArrangement = Arrangement.Center) {
                    Box(
                        modifier = Modifier
                            .height(12.dp)
                            .fillMaxWidth(0.18f)
                            .defaultPlaceholder(
                                color = lightenColor(Black1, 25),
                                highlightColor = lightenColor(Black1, 60)
                            )
                    )
                    Spacer(Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .height(12.dp)
                            .fillMaxWidth(0.1f)
                            .defaultPlaceholder(
                                color = lightenColor(Black1, 25),
                                highlightColor = lightenColor(Black1, 60)
                            )
                    )
                    Spacer(Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .height(12.dp)
                            .fillMaxWidth(0.28f)
                            .defaultPlaceholder(
                                color = lightenColor(Black1, 25),
                                highlightColor = lightenColor(Black1, 60)
                            )
                    )
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
        if (featureds.isNotEmpty())
            HorizontalPager(
                count = Int.MAX_VALUE,
                state = pagerState,
                modifier = Modifier.nestedScroll(remember {
                    object : NestedScrollConnection {
                        override fun onPostScroll(
                            consumed: Offset, available: Offset, source: NestedScrollSource
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
                        }) {
                    ImageView(
                        url = featureds[it].img,
                        modifier = Modifier.fillMaxSize(),
                        contentDescription = "Thumbnail"
                    )
                    Box(
                        Modifier
                            .background(color = Color.Black.copy(alpha = 0.15f))
                            .fillMaxSize()
                    )
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                                    100F,
                                    600F
                                )
                            )
                    )
                    Column(
                        Modifier
                            .padding(15.dp)
                            .fillMaxSize(), verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            featureds[it].title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold, shadow = Shadow(
                                    Color.Black, blurRadius = 8f, offset = Offset(2f, 0f)
                                )
                            ),
                            color = Color.White,
                            modifier = Modifier.fillMaxWidth(0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            featureds[it].description,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                shadow = Shadow(
                                    Color.Black, blurRadius = 8f, offset = Offset(2f, 0f)
                                )
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (featureds[it].type.isNotEmpty()) {
                                Text(
                                    featureds[it].type,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = getComicTypeColor(featureds[it].type),
                                        fontWeight = FontWeight.SemiBold,
                                        shadow = Shadow(
                                            Color.Black, blurRadius = 8f, offset = Offset(2f, 0f)
                                        )
                                    )
                                )
                                Spacer(Modifier.width(6.dp))
                            }

                            if (featureds[it].score != null) {
                                Icon(
                                    Icons.Filled.Star,
                                    contentDescription = "Star",
                                    modifier = Modifier.height(16.dp),
                                    tint = Yellow
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    featureds[it].score.toString(),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = Color.White,
                                        shadow = Shadow(
                                            Color.Black, blurRadius = 8f, offset = Offset(2f, 0f)
                                        )
                                    )
                                )
                                Spacer(Modifier.width(10.dp))
                            }

                            if (featureds[it].type.isEmpty() && featureds[it].score == null) {
                                Icon(
                                    painter = painterResource(id = R.drawable.web),
                                    contentDescription = "Genre",
                                    modifier = Modifier.height(16.dp),
                                    tint = Yellow
                                )
                                Spacer(Modifier.width(6.dp))
                            }
                            Text(
                                featureds[it].genre.joinToString(", ") { it.title },
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = if (featureds[it].type.isEmpty() || featureds[it].score == null) Blue else Color.White,
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
fun LazyListScope.sectionView(
    sections: List<Section> = listOf(),
    onKomikClick: (title: String, slug: String) -> Unit = { _, _ -> }
) {
    val section = sections.getOrNull(0)
    if (section == null) {
        item { }
    } else {
        val komikList = section.list
        item {
            Row(
                Modifier.padding(start = 15.dp, end = 15.dp, top = 20.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.background(color = Red, shape = CircleShape)) {
                    Icon(
                        tint = Color.White,
                        painter = painterResource(id = R.drawable.fire),
                        contentDescription = "Popular Icon",
                        modifier = Modifier
                            .padding(
                                start = 3.5.dp, end = 2.5.dp, top = 3.dp, bottom = 3.dp
                            )
                            .width(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    if (section.title.isEmpty()) "Data not found!" else section.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
        items(count = komikList.size, key = {
            komikList[it].slug.ifEmpty {
                it
            }
        }) {
            Box(modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onKomikClick(
                        komikList[it].title, komikList[it].slug
                    )
                }) {
                Row(Modifier.padding(15.dp)) {
                    ImageView(
                        url = komikList[it].img,
                        modifier = Modifier
                            .width(120.dp)
                            .aspectRatio(11f / 16f)
                            .clip(RoundedCornerShape(8.dp)),
                        contentDescription = "Thumbnail"
                    )
                    Spacer(Modifier.width(10.dp))
                    Column() {
                        Text(
                            komikList[it].title, style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ), maxLines = 2, overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        FlowRow(verticalArrangement = Arrangement.Center) {
                            if (komikList[it].type.isNotEmpty()) {
                                Text(
                                    komikList[it].type,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        color = getComicTypeColor(komikList[it].type),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                Spacer(Modifier.width(6.dp))
                            }

                            if (komikList[it].score != null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Filled.Star,
                                        contentDescription = "Star",
                                        modifier = Modifier.height(14.dp),
                                        tint = Yellow
                                    )
                                    Spacer(Modifier.width(2.dp))
                                    Text(
                                        komikList[it].score.toString(),
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                                Spacer(Modifier.width(6.dp))
                            }

                            Text(
                                komikList[it].chapterString,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }
    }

}
