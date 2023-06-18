package com.crstlnz.komikchino.ui.screens.komikdetail

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.crstlnz.komikchino.LocalNavPadding
import com.crstlnz.komikchino.R
import com.crstlnz.komikchino.data.database.KomikDatabase
import com.crstlnz.komikchino.data.model.State
import com.crstlnz.komikchino.data.model.TabRowItem
import com.crstlnz.komikchino.data.util.BlurTransformation
import com.crstlnz.komikchino.data.util.komikStorageHelper
import com.crstlnz.komikchino.ui.components.ErrorView
import com.crstlnz.komikchino.ui.components.ImageView
import com.crstlnz.komikchino.ui.navigations.MainNavigation
import com.crstlnz.komikchino.ui.screens.komikdetail.tab.ChapterList
import com.crstlnz.komikchino.ui.screens.komikdetail.tab.DetailScreen
import com.crstlnz.komikchino.ui.theme.Black1
import com.crstlnz.komikchino.ui.theme.WhiteGray
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun KomikScreen(
    navController: NavController, title: String = "Solo Leveling", slug: String
) {
    val database = KomikDatabase.getInstance(LocalContext.current)
    val v = viewModel<KomikViewModel>(
        factory = KomikViewModelFactory(
            komikStorageHelper(LocalContext.current),
            slug,
            database.getReadHistoryDao()
        )
    )

    val scope = rememberCoroutineScope()
    val dataState by v.state.collectAsState()
    Scaffold() {
        Surface(
            Modifier
                .padding(it)
                .padding(top = LocalNavPadding.current), color = MaterialTheme.colors.background
        ) {
            val pagerState = com.google.accompanist.pager.rememberPagerState(initialPage = 0)
            val tabItems: List<TabRowItem> = arrayListOf(
                TabRowItem(title = "Informasi", screen = {
                    DetailScreen(
                        komikDetail = dataState.data!!,
                        onKomikClick = { title, slug ->
                            navController.navigate("${MainNavigation.KOMIKDETAIL}/${title}/${slug}")
                        }
                    )

                }),
                TabRowItem(title = "Chapters", screen = {
                    ChapterList(
                        viewModel = v,
                        chapterList = dataState.data?.chapters ?: listOf(),
                        onReload = {
                            v.load()
                        }) { title, id ->
                        navController.navigate(
                            "${MainNavigation.CHAPTER}/${dataState.data?.id}/${title}/${id}",
                        )
                    }
                })
            )

            val scrollState = rememberScrollState()
            val screenHeight = LocalConfiguration.current.screenHeightDp

            val overscroll = LocalOverscrollConfiguration.current
            CompositionLocalProvider(
                LocalOverscrollConfiguration provides null
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(state = scrollState)
                ) {
                    TopAppBar(
                        title = {
                            Text(stringResource(R.string.detail))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        backgroundColor = Color.Transparent,
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.Filled.ArrowBack, "backIcon")
                            }
                        },
                        actions = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.Filled.Favorite, "backIcon")
                            }
                        },
                        elevation = 0.dp
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 7f)
                    ) {
                        ImageView(
                            url = dataState.data?.banner ?: dataState.data?.img ?: "",
                            modifier = Modifier.fillMaxSize(),
                            requestOptions = { requestOptions ->
                                if (dataState.data?.banner?.isEmpty() == true || dataState.data?.banner == dataState.data?.img) {
                                    requestOptions.transform(BlurTransformation(20))
                                } else {
                                    requestOptions
                                }
                            }
                        )

                        if (dataState.state == State.DATA) {
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.9f)
                                            ),
                                            120F,
                                            420F
                                        )
                                    )
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 15.dp, vertical = 8.dp)
                        ) {
                            Text(
                                dataState.data?.title ?: title,
                                style = MaterialTheme.typography.h5.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    shadow = Shadow(color = Black, blurRadius = 8f)
                                ),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }


                    Column(Modifier.height(screenHeight.dp)) {
                        TabRow(
                            selectedTabIndex = pagerState.currentPage,
                            backgroundColor = Black1
                        ) {
                            tabItems.forEachIndexed { index, item ->
                                Tab(selected = index == pagerState.currentPage, onClick = {
                                    scope.launch {
                                        pagerState.animateScrollToPage(
                                            index
                                        )
                                    }
                                }, text = { Text(text = item.title) })
                            }
                        }



                        CompositionLocalProvider(
                            LocalOverscrollConfiguration provides overscroll,
                        ) {
                            com.google.accompanist.pager.HorizontalPager(
                                count = tabItems.size,
                                verticalAlignment = Alignment.Top,
                                state = pagerState,
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .nestedScroll(remember {
                                        object : NestedScrollConnection {
                                            override fun onPreScroll(
                                                available: Offset,
                                                source: NestedScrollSource
                                            ): Offset {
                                                return if (available.y > 0) Offset.Zero else Offset(
                                                    x = 0f,
                                                    y = -scrollState.dispatchRawDelta(-available.y)
                                                )
                                            }
                                        }
                                    })
                            ) { page ->
                                when (dataState.state) {
                                    State.DATA -> {
                                        if (dataState.data != null) {
                                            tabItems[page].screen()
                                        } else {
                                            Column(modifier = Modifier.aspectRatio(4f / 5f)) {
                                                ErrorView(
                                                    resId = R.drawable.error,
                                                    message = stringResource(id = R.string.unknown_error)
                                                ) {
                                                    v.load()
                                                }
                                            }
                                        }
                                    }

                                    State.ERROR -> {
                                        Box(
                                            modifier = Modifier.aspectRatio(4f / 5f)
                                        ) {
                                            ErrorView(
                                                resId = R.drawable.error,
                                                message = stringResource(id = R.string.unknown_error)
                                            ) {
                                                v.load()
                                            }
                                        }
                                    }

                                    else -> {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(5f / 3f),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(color = WhiteGray.copy(alpha = 0.6f))
                                        }
                                    }
                                }
                            }
                        }

                    }
                }
            }

        }
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
fun LazyListScope.komikDetailView(
    navController: NavController, tabItems: List<TabRowItem>, pagerState: PagerState
) {
//    LazyColumn(Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {

    item {
        HorizontalPager(
            state = pagerState,
        ) {
            tabItems[it].screen()
        }
    }
}