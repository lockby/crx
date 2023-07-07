package com.crstlnz.komikchino.ui.screens.home.fragments.bookmark

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.crstlnz.komikchino.LocalStatusBarPadding
import com.crstlnz.komikchino.R
import com.crstlnz.komikchino.data.model.TabRowItem
import com.crstlnz.komikchino.ui.navigations.HomeSections
import com.crstlnz.komikchino.ui.navigations.MainNavigation
import com.crstlnz.komikchino.ui.screens.home.fragments.bookmark.fragments.FavoriteView
import com.crstlnz.komikchino.ui.screens.home.fragments.bookmark.fragments.RecentView
import com.crstlnz.komikchino.ui.theme.Red
import com.crstlnz.komikchino.ui.theme.WhiteGray
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarkScreen(navController: NavController) {
    val viewModel = hiltViewModel<BookmarkViewModel>()

    LaunchedEffect(Unit) {
        viewModel.updateHistories()
    }

    val tabItems: List<TabRowItem> = arrayListOf(TabRowItem(title = "Recent", screen = { id ->
        RecentView(
            viewModel,
            onKomikClick = {
                MainNavigation.toKomik(navController, it.title, it.slug)
            }, onChapterClick = { komik, chapter ->
                MainNavigation.toChapter(
                    navController,
                    chapterId = chapter.id,
                    chapter.title,
                    komik,
                )
            },
            pageId = id
        )
    }), TabRowItem(title = "Favorites", screen = { id ->
        FavoriteView(
            viewModel,
            onKomikClick = {
                MainNavigation.toKomik(navController, it.title, it.slug)
            },
            pageId = id
        )
    }))

    val pagerState = rememberPagerState()
    val pageId = pagerState.currentPage.toString()
    LaunchedEffect(pagerState.currentPage) {
        viewModel.deselectAll(pageId)
    }

    var openDeleteDialog by remember { mutableStateOf(false) }
    if (openDeleteDialog) {
        DeleteDialog(viewModel, pageId = pageId, onDismiss = { openDeleteDialog = false })
    }

    val scope = rememberCoroutineScope()
    val isEditMode = viewModel.editState.contains(pageId)

    LaunchedEffect(pagerState.currentPage) {
        viewModel.editState.clear()
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.editState.clear()
        }
    }

    BackHandler(isEditMode) {
        viewModel.cancelEdit(pageId)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            modifier = Modifier.padding(top = LocalStatusBarPadding.current),
            windowInsets = WindowInsets.ime,
            title = {
                if (isEditMode) {
                    Row(
                        verticalAlignment = CenterVertically,
                    ) {
                        IconButton(
                            onClick = { viewModel.cancelEdit(pageId) },
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.close),
                                contentDescription = "Cancel",
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                        Text(
                            "${viewModel.getSelected(pageId).size} Selected",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                } else {
                    Row(verticalAlignment = CenterVertically) {
                        Image(
                            painter = painterResource(id = R.mipmap.app_icon),
                            contentDescription = "App Icon",
                            modifier = Modifier.height(38.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            stringResource(HomeSections.BOOKMARK.title),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            },
            actions = {
                if (isEditMode) {
                    Checkbox(checked = viewModel.isAllSelected(pageId), onCheckedChange = {
                        if (viewModel.isAllSelected(pageId)) {
                            viewModel.deselectAll(pageId)
                        } else {
                            viewModel.selectAll(pageId)
                        }
                    })
                    IconButton(
                        onClick = {
                            if (viewModel.getSelected(pageId).size > 0)
                                openDeleteDialog = true
                        },
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.delete),
                            contentDescription = "Delete",
                        )
                    }
                } else {
                    IconButton(
                        onClick = {
                            viewModel.edit(pageId)
                        },
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.edit),
                            contentDescription = "Edit",
                        )
                    }
                }
                Spacer(modifier = Modifier.width(5.dp))
            },
        )
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
        ) { page ->
            tabItems[page].screen(page.toString())
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteDialog(viewModel: BookmarkViewModel, pageId: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = {
            // Dismiss the dialog when the user clicks outside the dialog or on the back
            // button. If you want to disable that functionality, simply use an empty
            // onDismissRequest.
            onDismiss()
        }
    ) {
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Delete",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Are you sure you want to delete ${
                        viewModel.getSelected(pageId).size
                    } Item?"
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    TextButton(
                        onClick = {
                            onDismiss()
                        },
                    ) {
                        Text("Cancel")
                    }
                    TextButton(
                        onClick = {
                            viewModel.deleteItem(pageId)
                            onDismiss()
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Red)
                    ) {
                        Text("Delete")
                    }
                }
            }
        }
    }
}

