package com.crstlnz.komikchino.ui.screens.latestupdate

import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.crstlnz.komikchino.LocalStatusBarPadding
import com.crstlnz.komikchino.R
import com.crstlnz.komikchino.data.model.DataState.Loading.getDataOrNull
import com.crstlnz.komikchino.data.model.LatestUpdate
import com.crstlnz.komikchino.data.model.State
import com.crstlnz.komikchino.data.util.formatRelativeDate
import com.crstlnz.komikchino.ui.components.ErrorView
import com.crstlnz.komikchino.ui.components.ImageView
import com.crstlnz.komikchino.ui.navigations.HomeSections
import com.crstlnz.komikchino.ui.navigations.MainNavigation
import com.crstlnz.komikchino.ui.screens.search.InfiniteState
import com.crstlnz.komikchino.ui.theme.Blue
import com.crstlnz.komikchino.ui.theme.WhiteGray
import com.crstlnz.komikchino.ui.util.OnBottomReached
import com.crstlnz.komikchino.ui.util.noRippleClickable

@OptIn(
    ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class
)
@Composable
fun LatestUpdateScreen(navController: NavController) {
    val v = hiltViewModel<LatestUpdateViewModel>()
    val scrollState = rememberLazyListState()
    scrollState.OnBottomReached(2) {
        Log.d("LATEST NEXT", "AWWW")
        v.next()
    }
    val dataState by v.state.collectAsState()
    val pullRefreshState =
        rememberPullRefreshState(dataState.state == State.LOADING && !v.isFirstLaunch, { v.load() })
    val infiniteState by v.infiniteState.collectAsState()

    Column {
        TopAppBar(
            modifier = Modifier.padding(top = LocalStatusBarPadding.current),
            windowInsets = WindowInsets.ime,
            title = {
                Row(verticalAlignment = CenterVertically) {
                    Image(
                        painter = painterResource(id = R.mipmap.app_icon),
                        contentDescription = "App Icon",
                        modifier = Modifier.height(38.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        stringResource(HomeSections.LATEST_UPDATE.title),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = {
                        navController.navigate(MainNavigation.SEARCH)
                    },
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_search_24),
                        contentDescription = "Search",
                    )
                }
                Spacer(modifier = Modifier.width(5.dp))
            },
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .pullRefresh(pullRefreshState)
        ) {
            when (dataState.state) {
                State.ERROR -> {
                    ErrorView(
                        resId = R.drawable.error,
                        message = stringResource(id = R.string.unknown_error)
                    ) {
                        v.load()
                    }
                }

                State.DATA -> {
                    val filteredUpdate by v.filteredUpdate.collectAsState()
                    val highlight = filteredUpdate.highlight
                    val latestUpdates = filteredUpdate.result
                    if (latestUpdates.isEmpty() && dataState.getDataOrNull()?.isEmpty() == true) {
                        ErrorView(
                            resId = R.drawable.error,
                            message = "Tidak ada latest update!"
                        ) {
                            v.load()
                        }
                    } else if (latestUpdates.isEmpty()) {
                        LazyColumn(contentPadding = PaddingValues(vertical = 15.dp)) {
                            items(5) {
                                LoadingView()
                            }
                        }
                    } else {
                        LazyColumn(
                            state = scrollState,
                            contentPadding = PaddingValues(vertical = if (highlight.isNotEmpty()) 0.dp else 15.dp)
                        ) {
                            if (highlight.isNotEmpty()) {
                                stickyHeader {
                                    Box(
                                        Modifier
                                            .fillMaxWidth()
                                            .background(color = MaterialTheme.colorScheme.surface)
                                    ) {
                                        Row(
                                            verticalAlignment = CenterVertically,
                                            modifier = Modifier.padding(
                                                horizontal = 15.dp,
                                                vertical = 10.dp
                                            )
                                        ) {
                                            Image(
                                                painter = painterResource(id = R.drawable.books),
                                                null
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                "Komik kamu",
                                                style = MaterialTheme.typography.labelLarge.copy(
                                                    fontWeight = FontWeight.Bold
                                                ),
                                            )
                                        }
                                    }
                                }
                                items(highlight.size) {
                                    LatestUpdateView(highlight[it], navController)
                                }
                            }

                            if (highlight.isNotEmpty()) {
                                stickyHeader {
                                    Box(
                                        Modifier
                                            .fillMaxWidth()
                                            .background(color = MaterialTheme.colorScheme.surface)
                                    ) {
                                        Row(
                                            verticalAlignment = CenterVertically,
                                            modifier = Modifier.padding(
                                                horizontal = 15.dp,
                                                vertical = 10.dp
                                            )
                                        ) {
                                            Image(
                                                painter = painterResource(id = R.drawable.new_button),
                                                null
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                "Latest Updates",
                                                style = MaterialTheme.typography.labelLarge.copy(
                                                    fontWeight = FontWeight.Bold
                                                ),
                                            )
                                        }
                                    }
                                }
                            }

                            items(latestUpdates.size) {
                                LatestUpdateView(latestUpdates[it], navController)
                            }

                            item {
                                Box(
                                    modifier = Modifier
                                        .padding(40.dp)
                                        .fillMaxWidth()
                                        .animateContentSize(),
                                    contentAlignment = Center
                                ) {
                                    if (infiniteState == InfiniteState.LOADING) {
                                        CircularProgressIndicator(
                                            Modifier.padding(20.dp),
                                            color = WhiteGray.copy(alpha = 0.6f)
                                        )
                                    } else if (infiniteState == InfiniteState.FINISH) {
                                        Text("No more data :(")
                                    }
                                }
                            }
                        }
                    }

                }

                else -> {
                    LazyColumn(contentPadding = PaddingValues(vertical = 15.dp)) {
                        items(5) {
                            LoadingView()
                        }
                    }

                }
            }
            PullRefreshIndicator(
                dataState.state == State.LOADING,
                pullRefreshState,
                Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
fun LatestUpdateView(data: LatestUpdate, navController: NavController) {
    Box {
        Row(Modifier.padding(horizontal = 15.dp, vertical = 8.dp)) {
            ImageView(
                url = data.img,
                contentDescription = data.title,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .width(100.dp)
                    .noRippleClickable {
                        MainNavigation.toKomik(navController, data.title, data.slug)
                    }
                    .aspectRatio(5f / 7f)
            )
            Spacer(Modifier.width(13.dp))
            Column {
                Text(
                    text = data.title,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.noRippleClickable {
                        MainNavigation.toKomik(navController, data.title, data.slug)
                    }
                )
                data.chapters.forEach { chapter ->
                    Spacer(Modifier.height(15.dp))
                    Row(
                        verticalAlignment = CenterVertically,
                        modifier = Modifier.noRippleClickable {
                            MainNavigation.toChapter(navController, chapter.slug, chapter.title)
                        }) {
                        Box(
                            Modifier
                                .width(8.dp)
                                .height(8.dp)
                                .align(CenterVertically)
                                .background(shape = CircleShape, color = Blue)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            chapter.title,
                            modifier = Modifier.weight(1f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                            )
                        )
                        chapter.date?.let {
                            Spacer(
                                modifier = Modifier
                                    .width(10.dp)
                            )
                            Text(
                                formatRelativeDate(it),
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Right,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color =
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun LoadingView() {
    Box {
        Row(Modifier.padding(horizontal = 15.dp, vertical = 8.dp)) {
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .aspectRatio(5f / 7f)
            )
            Spacer(Modifier.width(13.dp))
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(8.dp)
                        )
                )

                for (idx in 1..3) {
                    Spacer(Modifier.height(15.dp))
                    Row {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(15.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(8.dp)
                                )
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .height(15.dp)
                                .weight(1f)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(8.dp)
                                )
                        )
                    }
                }
            }
        }
    }
}