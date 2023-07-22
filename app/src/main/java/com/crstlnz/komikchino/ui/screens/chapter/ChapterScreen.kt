package com.crstlnz.komikchino.ui.screens.chapter

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.crstlnz.komikchino.R
import com.crstlnz.komikchino.data.model.Chapter
import com.crstlnz.komikchino.data.model.ChapterData
import com.crstlnz.komikchino.data.model.DataState
import com.crstlnz.komikchino.data.model.DataState.Idle.getDataOrNull
import com.crstlnz.komikchino.data.model.State
import com.crstlnz.komikchino.ui.components.ErrorView
import com.crstlnz.komikchino.ui.components.customswipe.CustomSwipeRefresh
import com.crstlnz.komikchino.ui.components.customswipe.rememberCustomSwipeRefresh
import com.crstlnz.komikchino.ui.navigations.ContentType
import com.crstlnz.komikchino.ui.navigations.MainNavigation
import com.crstlnz.komikchino.ui.screens.chapter.components.BottomAppBar
import com.crstlnz.komikchino.ui.screens.chapter.components.ChapterImageList
import com.crstlnz.komikchino.ui.theme.Blue
import com.crstlnz.komikchino.ui.theme.WhiteGray
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun ChapterScreen(navController: NavController, chapterTitle: String) {
    val v: ChapterViewModel = hiltViewModel()
    val systemUiController = rememberSystemUiController()
    val scope = rememberCoroutineScope()
    DisposableEffect(Unit) {
        onDispose {
            systemUiController.isSystemBarsVisible = true
        }
    }

    Scaffold(
        Modifier.fillMaxSize(), contentWindowInsets = WindowInsets.ime
    ) {
        val chapterKey by v.chapterKey.collectAsState()
        var title by remember { mutableStateOf(chapterTitle) }
        var navShow by remember { mutableStateOf(true) }
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
        var nestedScroll by remember { mutableStateOf<NestedScrollConnection>(activeNested) }
        val nonActiveNested = rememberNestedScrollInteropConnection()
        LaunchedEffect(navShow) {
            if (systemUiController.isSystemBarsVisible != navShow)
                systemUiController.isSystemBarsVisible = navShow
            nestedScroll = if (navShow) activeNested else nonActiveNested
        }

        fun goToChapter(id: String, _title: String) {
            v.loadChapter(id)
            title = _title
        }

        val chapterPosition by v.currentPosition.collectAsState()
        val lazyListState = rememberLazyListState()
        var isFirst by remember { mutableStateOf(true) }



        LaunchedEffect(chapterKey) {
            if (chapterKey > 0) {
                lazyListState.scrollToItem(chapterPosition)
            }
        }

        val drawerState = rememberDrawerState(DrawerValue.Closed)
        ModalNavigationDrawer(modifier = Modifier.fillMaxSize(),
            gesturesEnabled = drawerState.isOpen,
            drawerState = drawerState,
            drawerContent = {
                val chapterList by v.chapterList.collectAsState()
                LaunchedEffect(chapterList) {
                    if (chapterList.state == State.DATA) {
                        if (isFirst && chapterPosition >= 0) {
                            lazyListState.scrollToItem(chapterPosition)
                        }
                        isFirst = false
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.65f)
                        .fillMaxSize()
                ) {
                    when (chapterList) {
                        is DataState.Success -> {
                            val chapters =
                                (chapterList as DataState.Success<List<Chapter>>).data
                            CustomSwipeRefresh(
                                state = rememberCustomSwipeRefresh(isRefreshing = false),
                                refreshTriggerDistance = 100.dp,
                                onRefresh = {
                                    v.loadChapterList(true)

                                }) {
                                LazyColumn(
                                    Modifier
                                        .fillMaxSize()
                                        .weight(1f)
                                        .statusBarsPadding(), state = lazyListState
                                ) {
                                    items(chapters.size) {
                                        ListItem(
                                            modifier = Modifier
                                                .clickable {
                                                    scope.launch {
                                                        drawerState.close()
                                                        goToChapter(
                                                            chapters[it].id ?: "0",
                                                            chapters[it].title
                                                        )
                                                    }

                                                },
                                            colors = ListItemDefaults.colors(
                                                containerColor = if (chapterPosition == it) WhiteGray.copy(
                                                    alpha = 0.1f
                                                ) else Color.Transparent
                                            ), headlineContent = {
                                                Text(
                                                    chapters[it].title, modifier = Modifier.padding(
                                                        horizontal = 10.dp, vertical = 20.dp
                                                    )
                                                )
                                            })
                                    }
                                }
                            }
                        }

                        is DataState.Error -> {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                ErrorView(
                                    resId = R.drawable.error,
                                    message = stringResource(id = R.string.unknown_error)
                                ) {
                                    v.loadChapterList(true)
                                }
                            }
                        }

                        else -> {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        "Loading chapter",
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                    Spacer(Modifier.height(25.dp))
                                    LinearProgressIndicator(
                                        Modifier.fillMaxWidth(0.5f), color = Blue
                                    )
                                }
                            }
                        }
                    }
                }
            }) {
            Box(
                Modifier
                    .padding(it)
                    .fillMaxSize()
            ) {
                val dataState by v.state.collectAsState()
                LaunchedEffect(dataState) {
                    if (dataState is DataState.Success) {
                        title = dataState.getDataOrNull()?.title ?: ""
                    }
                }
                when (dataState) {
                    is DataState.Success -> {
                        if ((dataState as DataState.Success<ChapterData>).data.imgs.isNotEmpty()) {
                            val context = LocalContext.current
                            ChapterImageList(
                                Modifier.nestedScroll(nestedScroll),
                                images = (dataState as DataState.Success<ChapterData>).data.imgs,
                                onNavChange = { it ->
                                    if (it == null) {
                                        navShow = !navShow
                                    } else {
                                        navShow = it
                                    }
                                },
                                onNextClick = {
                                    if (!navShow) navShow = true
                                    if (v.chapterList.value.state === State.LOADING) {
                                        Toast.makeText(
                                            context,
                                            "Chapter list sedang loading, coba lagi!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        val chapter =
                                            v.chapterList.value.getDataOrNull()
                                                ?.getOrNull(chapterPosition + 1)
                                        if (chapter != null) {
                                            goToChapter(
                                                chapter.id ?: "", chapter.title
                                            )
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Tidak ada chapter selanjutnya",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                },
                                viewModel = v
                            )
                        } else {
                            ErrorView(
                                resId = R.drawable.error, message = "Gambar tidak ditemukan!"
                            ) {
                                v.load()
                            }
                        }
                    }

                    is DataState.Error -> {
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
                    BottomAppBar(viewModel = v, onDrawerClick = {
                        scope.launch { drawerState.open() }
                    }, onChapterClick = { id, slug ->
                        goToChapter(id, slug)
                    }, onCommentClick = {
                        if (dataState is DataState.Success) {
                            val data = (dataState as DataState.Success<ChapterData>).data
                            MainNavigation.toCommentView(
                                navController,
                                slug = data.disqusConfig?.identifier ?: data.slug,
                                title = data.title,
                                url = data.disqusConfig?.url ?: data.slug,
                                ContentType.CHAPTER
                            )
                        }
                    })
                }

                AnimatedVisibility(
                    visible = navShow,
                    enter = slideInVertically(initialOffsetY = { s ->
                        -s
                    }),
                    exit = slideOutVertically(targetOffsetY = { s ->
                        -s
                    })
                ) {
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        title = {
                            Text(
                                title,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }, navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.Filled.ArrowBack, "backIcon")
                            }
                        },
                        actions = {
                            IconButton(
                                modifier = Modifier.padding(end = 5.dp),
                                onClick = {
                                    val data = dataState.getDataOrNull()
                                    if (data != null) {
                                        MainNavigation.toKomik(
                                            navController = navController,
                                            data.komik.title,
                                            data.komik.slug
                                        )

                                    }
                                }) {
                                Icon(Icons.Outlined.Info, "backIcon")
                            }
                        }
                    )
                }
            }
        }
    }
}