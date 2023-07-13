package com.crstlnz.komikchino.ui.screens.home.fragments.genre

import android.os.Environment
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.crstlnz.komikchino.LocalStatusBarPadding
import com.crstlnz.komikchino.R
import com.crstlnz.komikchino.config.AppSettings
import com.crstlnz.komikchino.data.model.DataState.Idle.getDataOrNull
import com.crstlnz.komikchino.data.model.KomikSearchResult
import com.crstlnz.komikchino.data.model.State
import com.crstlnz.komikchino.ui.components.ErrorView
import com.crstlnz.komikchino.ui.components.ImageView
import com.crstlnz.komikchino.ui.navigations.HomeSections
import com.crstlnz.komikchino.ui.navigations.MainNavigation
import com.crstlnz.komikchino.ui.screens.latestupdate.LoadingView
import com.crstlnz.komikchino.ui.screens.search.InfiniteState
import com.crstlnz.komikchino.ui.theme.WhiteGray
import com.crstlnz.komikchino.ui.theme.Yellow
import com.crstlnz.komikchino.ui.util.OnBottomReached
import com.crstlnz.komikchino.ui.util.getComicTypeColor
import com.crstlnz.komikchino.ui.util.noRippleClickable
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class, ExperimentalLayoutApi::class
)
@Composable
fun GenreScreen(navController: NavController) {
    val v = hiltViewModel<GenreViewModel>()
    var openBottomSheet by rememberSaveable { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )
    if (openBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { openBottomSheet = false },
            sheetState = bottomSheetState,
            windowInsets = WindowInsets.navigationBars,
        ) {
            val data by v.state.collectAsState()
            val genreList = data.getDataOrNull()?.genreList
            if (genreList != null) {
                var selectedGenre by remember { mutableStateOf(v.genreList.toList()) }
                if (AppSettings.komikServer!!.multiGenreSearch) {
                    Button(
                        onClick = {
                            openBottomSheet = false
                            v.search(selectedGenre)
                        },
                        enabled = selectedGenre != v.genreList,
                        modifier = Modifier.align(CenterHorizontally)
                    ) {
                        Text("Apply")
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(160.dp),
                    contentPadding = PaddingValues(15.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(genreList.size) {
                        val isSelected = selectedGenre.contains(genreList[it])
                        Box(modifier = Modifier
                            .clip(RoundedCornerShape(15.dp))
                            .background(color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)
                            .clickable {
                                if (AppSettings.komikServer!!.multiGenreSearch) {
                                    val genre = if (isSelected) {
                                        selectedGenre - genreList[it]
                                    } else {
                                        selectedGenre + genreList[it]
                                    }
                                    selectedGenre = genre
                                } else {
                                    openBottomSheet = false
                                    v.search(listOf(genreList[it]))
                                }
                            }) {
                            Text(
                                genreList[it].title,
                                maxLines = 1,
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 15.dp),
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }
            } else {
                ErrorView(
                    resId = R.drawable.empty_box, message = "Tidak ada genre!", showButton = false
                )
            }
        }
    }

    val scrollState = rememberLazyListState()
    scrollState.OnBottomReached(2) {
        v.next()
    }
    val dataState by v.state.collectAsState()
    LaunchedEffect(dataState.state) {
        if (dataState.state != State.DATA) {
            scrollState.scrollToItem(0, 0)
        }
    }
    val searchResult by v.searchResult.collectAsState()
    val pullRefreshState =
        rememberPullRefreshState(dataState.state == State.LOADING && !v.isFirstLaunch, { v.load() })
    val infiniteState by v.infiniteState.collectAsState()


    Column {
        TopAppBar(
            modifier = Modifier.padding(top = LocalStatusBarPadding.current),
            windowInsets = WindowInsets.ime,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
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
        Box {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    val genreList = v.genreList
                    if (genreList.isNotEmpty()) {
                        if (genreList.size == 1) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    genreList[0].title,
                                    Modifier
                                        .padding(start = 15.dp)
                                        .noRippleClickable {
                                            v.search()
                                        },
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Icon(painter = painterResource(id = R.drawable.close),
                                    modifier = Modifier
                                        .size(30.dp)
                                        .noRippleClickable {
                                            v.search()
                                        }
                                        .padding(end = 15.dp),
                                    contentDescription = null)
                            }
                        } else {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                contentPadding = PaddingValues(horizontal = 15.dp)
                            ) {
                                items(genreList.size) {
                                    val genre = genreList[it]
                                    Box(
                                        Modifier
                                            .background(
                                                color = MaterialTheme.colorScheme.secondaryContainer,
                                                shape = MaterialTheme.shapes.large
                                            )
                                            .noRippleClickable {
                                                v.search(genreList - genre)
                                            }) {
                                        Row(
                                            modifier = Modifier.padding(
                                                top = 4.5.dp,
                                                bottom = 4.5.dp,
                                                start = 13.dp,
                                                end = 10.dp
                                            ), verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                genre.title,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                                )
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Icon(
                                                painter = painterResource(id = R.drawable.close),
                                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                                modifier = Modifier.size(14.dp),
                                                contentDescription = null
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Text(
                            "All Genre",
                            modifier = Modifier.padding(horizontal = 15.dp),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .height(40.dp)
                        .background(color = MaterialTheme.colorScheme.primary)
                        .clickable {
                            openBottomSheet = true
                        }, contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Add",
                        Modifier.padding(horizontal = 24.dp),
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    )
                }
            }
        }
        Divider(Modifier.height(1.dp), thickness = 1.dp)
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
                    if (searchResult.isEmpty()) {
                        ErrorView(
                            resId = R.drawable.error, message = "Tidak ada genre!"
                        ) {
                            v.load()
                        }
                    } else {
                        LazyColumn(
                            state = scrollState, contentPadding = PaddingValues(vertical = 15.dp)
                        ) {
                            items(searchResult.size) {
                                GenreResultView(searchResult[it], onKomikClick = { komik ->
                                    MainNavigation.toKomik(navController, komik.title, komik.slug)
                                })
                            }
                            item {
                                Box(
                                    modifier = Modifier
                                        .padding(40.dp)
                                        .fillMaxWidth()
                                        .animateContentSize(), contentAlignment = Alignment.Center
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
fun GenreResultView(data: KomikSearchResult, onKomikClick: (data: KomikSearchResult) -> Unit = {}) {
    val scope = rememberCoroutineScope()
    Box(Modifier.clickable {
        scope.launch {
            onKomikClick(data)
        }
    }) {
        Row(
            Modifier.padding(10.dp)
        ) {
            ImageView(
                url = data.img,
                shape = RoundedCornerShape(5.dp),
                modifier = Modifier
                    .width(110.dp)
                    .height(160.dp),
                contentDescription = "Thumbnail"
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                if (data.type.isNotEmpty()) Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        Modifier
                            .clip(
                                RoundedCornerShape(4.dp)
                            )
                            .background(color = getComicTypeColor(data.type))
                    ) {
                        Text(
                            data.type,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                    if (data.score != null)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Star,
                                contentDescription = "Star",
                                modifier = Modifier.height(16.dp),
                                tint = Yellow
                            )
                            Spacer(Modifier.width(2.dp))
                            Text(data.score.toString(), style = MaterialTheme.typography.bodyMedium)
                        }
                }
                Spacer(Modifier.height(5.dp))
                Text(
                    data.title,
                    style = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                )
            }
        }
    }
}
