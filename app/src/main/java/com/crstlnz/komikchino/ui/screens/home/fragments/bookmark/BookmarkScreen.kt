package com.crstlnz.komikchino.ui.screens.home.fragments.bookmark

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.crstlnz.komikchino.data.model.TabRowItem
import com.crstlnz.komikchino.ui.navigations.MainNavigation
import com.crstlnz.komikchino.ui.screens.home.fragments.bookmark.fragments.FavoriteView
import com.crstlnz.komikchino.ui.screens.home.fragments.bookmark.fragments.RecentView
import com.crstlnz.komikchino.ui.theme.WhiteGray
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch

@Composable
fun BookmarkScreen(navController: NavController) {
    val viewModel = hiltViewModel<BookmarkViewModel>()
    val tabItems: List<TabRowItem> = arrayListOf(TabRowItem(title = "Recent", screen = {
        RecentView(
            viewModel,
            onKomikClick = {
                MainNavigation.toKomik(navController, it.title, it.slug)
            }, onChapterClick = { komik, chapter ->
                MainNavigation.toChapter(navController, chapter.id, chapter.title, komik)
            }
        )
    }), TabRowItem(title = "Favorites", screen = {
        FavoriteView(
            viewModel,
            onKomikClick = {
                MainNavigation.toKomik(navController, it.title, it.slug)
            }
        )
    }))

    val pagerState = rememberPagerState()
    val scope = rememberCoroutineScope()


    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = pagerState.currentPage) {
            tabItems.forEachIndexed { index, item ->
                Tab(
                    selected = index == pagerState.currentPage,
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
        HorizontalPager(
            count = tabItems.size,
            modifier = Modifier.weight(1f),
            state = pagerState,
            verticalAlignment = Alignment.Top,
        ) {
            tabItems[it].screen()
        }
    }
}

