package com.crstlnz.komikchino.ui.screens.chapter

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.BottomAppBar
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.bumptech.glide.RequestManager
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.crstlnz.komikchino.R
import com.crstlnz.komikchino.data.database.KomikDatabase
import com.crstlnz.komikchino.data.database.readhistory.ReadHistoryRepository
import com.crstlnz.komikchino.data.database.readhistory.model.ReadHistoryItem
import com.crstlnz.komikchino.data.model.State
import com.crstlnz.komikchino.data.util.chapterStorageHelper
import com.crstlnz.komikchino.data.util.px
import com.crstlnz.komikchino.ui.components.ErrorView
import com.crstlnz.komikchino.ui.theme.Black1
import com.crstlnz.komikchino.ui.theme.Black2
import com.crstlnz.komikchino.ui.theme.Blue
import com.crstlnz.komikchino.ui.theme.WhiteGray
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ChapterScreen(navController: NavController, chapterTitle: String, id: Int, mangaId: Int) {
    val database = KomikDatabase.getInstance(LocalContext.current)
    val v: ChapterViewModel = viewModel(
        factory = ChapterViewModelFactory(
            id,
            chapterStorageHelper(LocalContext.current),
            ReadHistoryRepository(database.getReadHistoryDao()),
            mangaId
        )
    )

    var title by remember { mutableStateOf(chapterTitle) }
    val dataState by v.state.collectAsState()
    val data = dataState.data
    var navShow by remember { mutableStateOf(true) }
    val systemUiController = rememberSystemUiController()

    val nonActiveNested = rememberNestedScrollInteropConnection()
    val activeNested = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset, available: Offset, source: NestedScrollSource
            ): Offset {
                if (navShow) navShow = false
                return Offset.Zero
            }
        }
    }

    val scope = rememberCoroutineScope()
    var nestedScroll by remember { mutableStateOf<NestedScrollConnection>(activeNested) }

    LaunchedEffect(navShow) {
        systemUiController.isSystemBarsVisible = navShow
        nestedScroll = if (navShow) activeNested else nonActiveNested

    }

    DisposableEffect(Unit) {
        onDispose {
            systemUiController.isSystemBarsVisible = true
        }
    }

    val preloadImages by v.loadedImages.collectAsState()
    val scaffoldState = rememberScaffoldState()

    fun goToChapter(id: Int, _title: String) {
        v.loadChapter(id)
        title = _title
    }

    val chapterList by v.chapterList.collectAsState()
    val chapterPosition by v.currentPosition.collectAsState()
    val lazyListState = rememberLazyListState()
    var isFirst by remember { mutableStateOf(true) }
    LaunchedEffect(chapterList.data) {
        if (chapterList.state == State.DATA) {
            if (isFirst && chapterPosition >= 0) {
                lazyListState.scrollToItem(chapterPosition)
            }
            isFirst = false
        }
    }
    Scaffold(
        Modifier.fillMaxSize(),
        scaffoldState = scaffoldState,
        drawerGesturesEnabled = true,
        drawerContent = {
            val chapters = chapterList.data
            when (chapterList.state) {
                State.DATA -> {
                    if (chapters != null) {
                        LazyColumn(
                            Modifier
                                .fillMaxSize()
                                .statusBarsPadding(), state = lazyListState
                        ) {
                            items(chapters.size) {
                                ListItem(
                                    Modifier
                                        .clickable {
                                            scope.launch {
                                                scaffoldState.drawerState.close()
                                            }
                                            goToChapter(
                                                chapters[it].id ?: 0, chapters[it].title
                                            )
                                        }
                                        .background(
                                            color = if (chapterPosition == it) WhiteGray.copy(
                                                alpha = 0.1f
                                            ) else Color.Transparent
                                        )) {
                                    Text(
                                        chapters[it].title, modifier = Modifier.padding(
                                            horizontal = 10.dp, vertical = 20.dp
                                        )
                                    )
                                }
                            }
                        }
                    } else {
                        ErrorView(
                            resId = R.drawable.error, message = "Error misterius :("
                        ) {
                            v.loadChapterList()
                        }
                    }
                }

                State.ERROR -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        ErrorView(
                            resId = R.drawable.error,
                            message = stringResource(id = R.string.unknown_error)
                        ) {
                            v.loadChapterList()
                        }
                    }
                }

                else -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Loading chapter",
                                style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(Modifier.height(25.dp))
                            LinearProgressIndicator(Modifier.fillMaxWidth(0.5f), color = Blue)
                        }
                    }
                }
            }
        },
        drawerBackgroundColor = Black1,
        drawerScrimColor = Color.Black.copy(alpha = 0.5f)
    ) {
        Box(
            Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            when (dataState.state) {
                State.DATA -> {
                    if (data != null) {
                        if (!preloadImages.isLoading) {
                            ImageLazyList(images = preloadImages.images ?: listOf(),
                                modifier = Modifier
                                    .clickable(
                                        interactionSource = MutableInteractionSource(),
                                        indication = null,
                                    ) {
                                        navShow = !navShow
                                    }
                                    .nestedScroll(nestedScroll), v)
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        "Loading gambar",
                                        style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Spacer(Modifier.height(10.dp))
                                    Text(
                                        "${(preloadImages.progress * 100).roundToInt()}%",
                                        style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Spacer(Modifier.height(15.dp))
                                    LinearProgressIndicator(
                                        Modifier.fillMaxWidth(0.45f), color = Blue
                                    )
                                }
                            }
                        }
                    } else {
                        ErrorView(
                            resId = R.drawable.error, message = "Gambar tidak ditemukan!"
                        ) {
                            v.load()
                        }
                    }
                }

                State.ERROR -> {
                    ErrorView(resId = R.drawable.error, message = "Error Misterius!") {
                        v.load()
                    }
                }

                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                    ) {
                        LinearProgressIndicator(
                            Modifier.fillMaxWidth(0.45f), color = Blue
                        )
                    }
                }
            }

            AnimatedVisibility(
                modifier = Modifier.align(Alignment.BottomCenter),
                visible = navShow,
                enter = slideInVertically(initialOffsetY = { s ->
                    s
                }),
                exit = slideOutVertically(targetOffsetY = { s ->
                    s
                })
            ) {
                BottomAppBar(backgroundColor = Black2) {
                    IconButton(onClick = {
                        scope.launch {
                            scaffoldState.drawerState.open()
                        }
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.list),
                            contentDescription = "Menu"
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    IconButton(
                        onClick = {
                            val chapter = chapterList.data?.getOrNull(chapterPosition - 1)
                            if (chapter != null) {
                                goToChapter(
                                    chapter.id ?: 0, chapter.title
                                )
                            }
                        }, enabled = chapterPosition - 1 > 0
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_left),
                            contentDescription = "Previous"
                        )
                    }
                    Spacer(modifier = Modifier.width(15.dp))
                    IconButton(
                        onClick = {
                            val chapter = chapterList.data?.getOrNull(chapterPosition + 1)
                            if (chapter != null) {
                                goToChapter(
                                    chapter.id ?: 0, chapter.title
                                )
                            }
                        }, enabled = chapterPosition + 1 < (chapterList.data?.size ?: 0)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_right),
                            contentDescription = "Next"
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                }
            }

            AnimatedVisibility(
                visible = navShow,
                enter = slideInVertically(),
                exit = slideOutVertically(targetOffsetY = { s ->
                    -s
                })
            ) {
                Column {
                    val paddingValues = WindowInsets.systemBars.asPaddingValues()
                    Box(
                        Modifier
                            .background(
                                MaterialTheme.colors.primary
                            )
                            .height(paddingValues.calculateTopPadding())
                            .fillMaxWidth()
                    )
                    TopAppBar(title = {
                        Text(
                            title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.h6.copy(
                                fontWeight = FontWeight.Bold,
                                color = WhiteGray.copy(alpha = 0.8f)
                            )
                        )
                    }, navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Filled.ArrowBack, "backIcon")
                        }
                    }, backgroundColor = MaterialTheme.colors.primary, elevation = 0.dp
                    )
                }
            }
        }
    }
}

@OptIn(
    ExperimentalMaterialApi::class, ExperimentalFoundationApi::class,
    ExperimentalGlideComposeApi::class
)
@Composable
fun ImageLazyList(
//    images: List<String>,
    images: List<ImageBitmap?>,
    modifier: Modifier = Modifier,
    v: ChapterViewModel
) {
    val errorHeight = 600
    val chapterHistory = v.chapterHistoryData
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val scrollState = rememberLazyListState(
        initialFirstVisibleItemIndex = if (chapterHistory.id == v.id) chapterHistory.readHistory?.firstVisibleItemIndex
            ?: 0 else 0,
        initialFirstVisibleItemScrollOffset = if (chapterHistory.id == v.id) chapterHistory.readHistory?.firstVisibleItemScrollOffset
            ?: 0 else 0,
    )
    val scope = rememberCoroutineScope()
    DisposableEffect(LocalLifecycleOwner.current) {
        scope.launch {
            v.saveHistory(
                ReadHistoryItem(
                    chapterId = v.id,
                    mangaId = v.mangaId,
                    firstVisibleItemIndex = scrollState.firstVisibleItemIndex,
                    firstVisibleItemScrollOffset = scrollState.firstVisibleItemScrollOffset
                )
            )
        }

        onDispose {
            v.saveHistory(
                ReadHistoryItem(
                    chapterId = v.id,
                    mangaId = v.mangaId,
                    firstVisibleItemIndex = scrollState.firstVisibleItemIndex,
                    firstVisibleItemScrollOffset = scrollState.firstVisibleItemScrollOffset
                )
            )
        }
    }
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier,
            state = scrollState
        ) {
            items(images.size) { idx ->
                val preload = images[idx]
                if (preload == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(errorHeight.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Image not loaded!",
                            style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                } else {
                    Image(
                        bitmap = preload,
                        contentDescription = "Komik Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(screenWidth.dp * preload.height / preload.width),
                        contentScale = ContentScale.FillHeight
                    )
                }
            }
        }
    }
}
