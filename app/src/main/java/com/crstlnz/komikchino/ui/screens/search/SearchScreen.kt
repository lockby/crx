package com.crstlnz.komikchino.ui.screens.search

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.crstlnz.komikchino.LocalStatusBarPadding
import com.crstlnz.komikchino.R
import com.crstlnz.komikchino.config.AppSettings
import com.crstlnz.komikchino.data.api.KomikServer
import com.crstlnz.komikchino.data.model.DataState
import com.crstlnz.komikchino.data.model.SearchResult
import com.crstlnz.komikchino.data.model.State
import com.crstlnz.komikchino.data.util.getLastPathSegment
import com.crstlnz.komikchino.ui.components.ErrorView
import com.crstlnz.komikchino.ui.components.ImageView
import com.crstlnz.komikchino.ui.navigations.MainNavigation
import com.crstlnz.komikchino.ui.theme.WhiteGray
import com.crstlnz.komikchino.ui.theme.Yellow
import com.crstlnz.komikchino.ui.util.OnBottomReached
import com.crstlnz.komikchino.ui.util.getComicTypeColor
import com.crstlnz.komikchino.ui.util.noRippleClickable
import kotlinx.coroutines.launch

@Composable
fun SearchScreen(navController: NavController) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    var isFocused by remember { mutableStateOf(false) }

    val v: SearchViewModel = hiltViewModel()
    var text by remember { mutableStateOf(v.getCurrentQuery()) }
    val dataState by v.state.collectAsState()

    LaunchedEffect(Unit) {
        v.exactMatch.collect {
            if (it != null) {
                v.consumeExactMatch()
                MainNavigation.toKomik(
                    navController, it.title, it.slug
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        if (dataState.state != State.DATA) {
            focusRequester.requestFocus()
        }
    }

    Scaffold(contentWindowInsets = WindowInsets.ime, modifier = Modifier.fillMaxSize()) {
        Surface(
            Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .height(LocalStatusBarPadding.current)
                        .fillMaxWidth()
                        .background(color = MaterialTheme.colorScheme.surface)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.height(64.dp)
                ) {
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = { navController.popBackStack() },
                    ) {
                        Icon(Icons.Filled.ArrowBack, "backIcon")
                    }


                    TextField(singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password, imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(onSearch = {
                            if (text.isNotEmpty()) {
                                focusManager.clearFocus()
                                v.search(text, 1)
                            }
                        }),
                        textStyle = TextStyle(fontSize = 20.sp),
                        colors = run {
                            TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
                            )
                        },
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f)
                            .padding(end = 8.dp)
                            .onFocusChanged { state ->
                                isFocused = state.isFocused
                            }
                            .focusRequester(focusRequester),
                        value = text,
                        placeholder = {
                            Text(text = "Search...", style = TextStyle(fontSize = 20.sp))
                        },
                        onValueChange = { newText ->
                            text = newText
                        })

                    if (text.isNotEmpty()) {
                        IconButton(
                            onClick = { text = "" },
                        ) {
                            Icon(Icons.Filled.Close, "clear")
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (dataState.state) {
                        State.DATA -> {
                            SearchView(navController, (dataState as DataState.Success).data, v)
                        }

                        State.ERROR -> {
                            if (AppSettings.komikServer == KomikServer.MANGAKATANA && text.length < 3) {
                                ErrorView(
                                    resId = R.drawable.space,
                                    message = "Please enter at least 3 characters...",
                                    showButton = false
                                )
                            } else {
                                ErrorView(resId = R.drawable.error,
                                    message = stringResource(R.string.unknown_error),
                                    buttonName = stringResource(R.string.retry),
                                    onClick = {
                                        v.load()
                                    })
                            }

                        }

                        State.IDLE -> {}

                        else -> {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 40.dp, vertical = 60.dp)
                            ) {
                                CircularProgressIndicator(color = WhiteGray.copy(alpha = 0.6f))
                            }
                        }
                    }

                    if (isFocused) HistoryView(v, onQueryClick = { query ->
                        text = query
                        focusManager.clearFocus()
                        v.search(query, 1)
                    })
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryView(viewModel: SearchViewModel, onQueryClick: (String) -> Unit = {}) {
    val searchHistory by viewModel.searchHistory.collectAsState()
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            Modifier.fillMaxSize()
        ) {
            items(items = searchHistory, key = {
                it.query
            }) { item ->
                ListItem(
                    modifier = Modifier
                        .animateItemPlacement(
                            animationSpec = TweenSpec(
                                250,
                                50,
                                EaseOutCubic
                            )
                        )
                        .noRippleClickable {
                            onQueryClick(item.query)
                        },
                    leadingContent = {
                        Icon(painter = painterResource(id = R.drawable.history), null)
                    },
                    headlineContent = {
                        Text(text = item.query)
                    },
                    trailingContent = {
                        Icon(
                            modifier = Modifier
                                .noRippleClickable {
                                    viewModel.deleteSearchHistory(item.query)
                                }
                                .width(20.dp)
                                .height(20.dp),
                            painter = painterResource(id = R.drawable.close),
                            contentDescription = null
                        )
                    }
                )
            }
        }
    }
}


@Composable
fun SearchView(
    navController: NavController,
    searchData: List<SearchResult.ExactMatch>,
    viewModel: SearchViewModel
) {
    val scrollState = rememberLazyListState()
    scrollState.OnBottomReached(2) {
        viewModel.next()
    }
    val infiniteState by viewModel.infiniteState.collectAsState()
    LazyColumn(
        modifier = Modifier.fillMaxSize(), state = scrollState, userScrollEnabled = true
    ) {
        items(count = searchData.size, key = {
            searchData[it].url.ifEmpty { it }
        }) {
            SearchItemView(navController, searchData[it])
            Divider(
                modifier = Modifier.padding(horizontal = 10.dp), thickness = Dp.Hairline
            )
        }

        item {
            Box(
                modifier = Modifier
                    .padding(40.dp)
                    .fillMaxWidth()
                    .animateContentSize(),
                contentAlignment = Alignment.Center
            ) {
                if (infiniteState == InfiniteState.LOADING) {
                    CircularProgressIndicator(
                        Modifier.padding(20.dp), color = WhiteGray.copy(alpha = 0.6f)
                    )
                } else if (infiniteState == InfiniteState.FINISH) {
                    if (searchData.isEmpty()) {
                        Text("Data not found :(", modifier = Modifier.padding(top = 20.dp))
                    } else {
                        Text("No more data :(")
                    }
                }
            }
        }
    }
}

@Composable
fun SearchItemView(navController: NavController, data: SearchResult.ExactMatch) {
    val scope = rememberCoroutineScope()
    Box(Modifier.clickable {
        scope.launch {
            MainNavigation.toKomik(
                navController, data.title, data.slug
            )
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if(data.score != null) {
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
