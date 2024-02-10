package com.crstlnz.komikchino.ui.screens.komikdetail

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.crstlnz.komikchino.LocalStatusBarPadding
import com.crstlnz.komikchino.R
import com.crstlnz.komikchino.config.AppSettings
import com.crstlnz.komikchino.data.firebase.model.KomikHistoryItem
import com.crstlnz.komikchino.data.model.DataState
import com.crstlnz.komikchino.data.model.DataState.Idle.getDataOrNull
import com.crstlnz.komikchino.data.model.State
import com.crstlnz.komikchino.data.model.TabRowItem
import com.crstlnz.komikchino.data.util.px
import com.crstlnz.komikchino.ui.components.ErrorView
import com.crstlnz.komikchino.ui.components.ImageView
import com.crstlnz.komikchino.ui.navigations.ContentType
import com.crstlnz.komikchino.ui.navigations.MainNavigation
import com.crstlnz.komikchino.ui.screens.komikdetail.tab.ChapterList
import com.crstlnz.komikchino.ui.screens.komikdetail.tab.DetailScreen
import com.crstlnz.komikchino.ui.theme.Black2
import com.crstlnz.komikchino.ui.theme.Red
import com.crstlnz.komikchino.ui.theme.WhiteGray
import com.crstlnz.komikchino.ui.util.BlurTransformation
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class,
    ExperimentalMaterialApi::class
)
@Composable
fun KomikScreen(
    navController: NavController, title: String = "Solo Leveling"
) {
    val v: KomikViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()
    val imageAspectRatio = 16f / 7f
    val screenHeight = LocalConfiguration.current.screenHeightDp
    val screenWidth = LocalConfiguration.current.screenWidthDp.px
    val imageHeight = screenWidth * 7f / 16f + 64.px
    val overscroll = LocalOverscrollConfiguration.current
    val context = LocalContext.current
    var progress: Float by remember { mutableFloatStateOf(0f) }
    val scrollState = rememberScrollState()
    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.value }.collect {
            progress = (it / imageHeight).takeIf { it <= 1 } ?: 1f
        }
    }
    val dataState by v.state.collectAsState()
    val pullRefreshState =
        rememberPullRefreshState(dataState.state == State.LOADING && !v.isFirstLaunch, { v.load() })
    val isFavorite by v.isFavorite(dataState.getDataOrNull()?.id ?: "0").observeAsState()
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets.ime,
    ) {
        Surface(
            Modifier.padding(it)
        ) {

            val tabItems: List<TabRowItem> = arrayListOf(TabRowItem(title = "Informasi", screen = {
                DetailScreen(komikDetail = (dataState as DataState.Success).data,
                    onKomikClick = { title, slug ->
                        MainNavigation.toKomik(navController, title, slug)
                    },
                    onChapterClick = { title, id ->
                        val data = (dataState as DataState.Success).data
                        MainNavigation.toChapter(
                            navController, chapterId = id, title, KomikHistoryItem(
                                title = data.title,
                                id = data.id,
                                slug = data.slug,
                                description = data.description,
                                type = data.type,
                                img = data.img,
                            )
                        )
                    })

            }), TabRowItem(title = "Chapters", screen = {
                ChapterList(viewModel = v,
                    chapterList = dataState.getDataOrNull()?.chapters ?: listOf(),
                    onReload = {
                        v.load()
                    }) { title, id ->
                    val data = (dataState as DataState.Success).data
                    MainNavigation.toChapter(
                        navController, chapterId = id, title, KomikHistoryItem(
                            title = data.title,
                            id = data.id,
                            slug = data.slug,
                            description = data.description,
                            type = data.type,
                            img = data.img,
                        )
                    )
                }
            })
            )

            val pagerState =
                rememberPagerState(initialPage = 0, pageCount = { tabItems.size })

            CompositionLocalProvider(
                LocalOverscrollConfiguration provides null
            ) {
                Box(Modifier.pullRefresh(pullRefreshState)) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(state = scrollState)
                    ) {
                        TopAppBar(
                            windowInsets = WindowInsets.ime,
                            title = {
                                Text(stringResource(R.string.detail))
                            },
                            modifier = Modifier
                                .padding(top = LocalStatusBarPadding.current)
                                .fillMaxWidth(),
                            navigationIcon = {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "backIcon")
                                }
                            },
                            actions = {
                                IconButton(
                                    enabled = AppSettings.komikServer!!.haveComment,
                                    onClick = {
                                        val komik = dataState.getDataOrNull()
                                        if (komik != null) {
                                            MainNavigation.toCommentView(
                                                navController,
                                                slug = komik.disqusConfig?.identifier ?: komik.slug,
                                                title = komik.title,
                                                url = komik.disqusConfig?.url ?: komik.url,
                                                type = ContentType.MANGA
                                            )
                                        }
                                    }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.chat),
                                        contentDescription = "Comments",
                                    )
                                }
                                IconButton(onClick = { v.setFavorite(isFavorite != 1) }) {
                                    if (isFavorite == 1) {
                                        Icon(
                                            Icons.Filled.Favorite,
                                            contentDescription = "Favorite",
                                            tint = Red
                                        )
                                    } else {
                                        Icon(
                                            Icons.Filled.FavoriteBorder,
                                            contentDescription = "Favorite"
                                        )
                                    }
                                }

                                val uriHandler = LocalUriHandler.current
                                IconButton(modifier = Modifier.padding(end = 5.dp), onClick = {
                                    v.getKomikUrl()?.let { url ->
                                        uriHandler.openUri(url)
                                    }
                                }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.open_in_browser),
                                        "Open in browser"
                                    )
                                }
                            },
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(color = Black2)
                                .aspectRatio(imageAspectRatio)
                        ) {
                            ImageView(
                                url = dataState.getDataOrNull()?.banner
                                    ?: dataState.getDataOrNull()?.img ?: "",
                                modifier = Modifier.fillMaxSize(),
                                applyImageRequest = { imageRequest ->
                                    val banner = dataState.getDataOrNull()?.banner
                                    if (banner?.isEmpty() == true || banner == dataState.getDataOrNull()?.img) {
                                        imageRequest.transformations(
                                            BlurTransformation(context)
                                        )
                                    } else {
                                        imageRequest
                                    }
                                },
                                contentDescription = "Thumbnail"
                            )

                            if (dataState.state == State.DATA) {
                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(
                                                    Color.Transparent,
                                                    Black.copy(alpha = 0.7f)
                                                ), 120F, 420F
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
                                    dataState.getDataOrNull()?.title ?: title,
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        shadow = Shadow(color = Black, blurRadius = 8f)
                                    ),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .background(color = MaterialTheme.colorScheme.surface.copy(alpha = progress))
                            )
                        }
                        Spacer(modifier = Modifier.height(1.dp))

                        Column(Modifier.height(screenHeight.dp)) {
                            TabRow(
                                selectedTabIndex = pagerState.currentPage
                            ) {
                                tabItems.forEachIndexed { index, item ->
                                    Tab(selected = index == pagerState.currentPage,
                                        unselectedContentColor = WhiteGray,
                                        onClick = {
                                            scope.launch {
                                                pagerState.animateScrollToPage(
                                                    index
                                                )
                                            }
                                        },
                                        text = { Text(text = item.title) })
                                }
                            }

                            CompositionLocalProvider(
                                LocalOverscrollConfiguration provides overscroll,
                            ) {
                                HorizontalPager(
                                    verticalAlignment = Alignment.Top,
                                    state = pagerState,
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .nestedScroll(remember {
                                            object : NestedScrollConnection {
                                                override fun onPreScroll(
                                                    available: Offset, source: NestedScrollSource
                                                ): Offset {
                                                    return if (available.y > 0) Offset.Zero else Offset(
                                                        x = 0f,
                                                        y = -scrollState.dispatchRawDelta(-available.y)
                                                    )
                                                }
                                            }
                                        }
                                        ),
                                ) { page ->
                                    when (dataState.state) {
                                        State.DATA -> {
                                            if (dataState.getDataOrNull() != null) {
                                                tabItems[page].screen(page.toString())
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
                                                CircularProgressIndicator()
                                            }
                                        }
                                    }
                                }


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
        }
    }
}