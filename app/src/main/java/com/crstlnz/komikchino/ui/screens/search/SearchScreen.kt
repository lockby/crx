package com.crstlnz.komikchino.ui.screens.search

import androidx.activity.ComponentActivity
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.crstlnz.komikchino.R
import com.crstlnz.komikchino.data.model.SearchItem
import com.crstlnz.komikchino.data.model.State
import com.crstlnz.komikchino.data.util.getLastPathSegment
import com.crstlnz.komikchino.data.util.searchStorageHelper
import com.crstlnz.komikchino.ui.components.ErrorView
import com.crstlnz.komikchino.ui.components.ImageView
import com.crstlnz.komikchino.ui.navigations.MainNavigation
import com.crstlnz.komikchino.ui.theme.Blue
import com.crstlnz.komikchino.ui.theme.Green
import com.crstlnz.komikchino.ui.theme.Red
import com.crstlnz.komikchino.ui.theme.WhiteGray
import com.crstlnz.komikchino.ui.theme.Yellow
import com.crstlnz.komikchino.ui.util.OnBottomReached
import kotlinx.coroutines.launch

@Composable
fun SearchScreen(navController: NavController) {
    var text by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val v: SearchViewModel = viewModel(
        LocalContext.current as ComponentActivity, factory = SearchViewModelFactory(
            searchStorageHelper(LocalContext.current)
        )
    )
    val dataState by v.state.collectAsState()
    LaunchedEffect(Unit) {
        if (dataState.state != State.DATA) {
            focusRequester.requestFocus()
        }
    }

//    SideEffect {
//        focusRequester.requestFocus()
//    }


    Scaffold(
        topBar = {
            TopAppBar(
                Modifier
                    .statusBarsPadding()
                    .fillMaxWidth()
                    .height(60.dp),
                backgroundColor = Color.Transparent,
                elevation = 0.dp
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Filled.ArrowBack, "backIcon")
                }
                Spacer(Modifier.width(5.dp))
                val customTextSelectionColors = TextSelectionColors(
                    handleColor = Blue,
                    backgroundColor = Blue.copy(alpha = 0.4f)
                )
                CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
                    TextField(
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(onSearch = {
                            if (text.isNotEmpty()) {
                                focusManager.clearFocus()
                                v.search(text, 1)
                            }
                        }),
                        textStyle = TextStyle(fontSize = 20.sp),
                        colors = TextFieldDefaults.textFieldColors(
                            backgroundColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            cursorColor = Color.Gray
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester),
                        value = text,
                        placeholder = {
                            Text(text = "Search...", style = TextStyle(fontSize = 20.sp))
                        },
                        onValueChange = { newText ->
                            text = newText
                        }
                    )
                }
                Spacer(Modifier.width(5.dp))
                if (text.isNotEmpty()) {
                    IconButton(onClick = { text = "" }) {
                        Icon(Icons.Filled.Close, "clear")
                    }
                }
            }
//            TopAppBar(
//                modifier = Modifier.statusBarsPadding(),
//                backgroundColor = Color.Transparent,
//                elevation = 0.dp,
//                title = {
//                    TextField(
//                        singleLine = true,
//                        keyboardOptions = KeyboardOptions(
//                            keyboardType = KeyboardType.Text,
//                            imeAction = ImeAction.Search
//                        ),
//                        keyboardActions = KeyboardActions(onSearch = {
//                            if (text.isNotEmpty()) {
//                                v.search(text, 1)
//                            }
//                        }),
//                        textStyle = TextStyle(fontSize = 24.sp),
//                        colors = TextFieldDefaults.textFieldColors(
//                            backgroundColor = Color.Transparent,
//                            focusedIndicatorColor = Color.Transparent,
//                            unfocusedIndicatorColor = Color.Transparent,
//                            disabledIndicatorColor = Color.Transparent,
//                            cursorColor = Color.Gray
//                        ),
//                        modifier = Modifier
//                            .statusBarsPadding()
//                            .fillMaxWidth()
//                            .focusRequester(focusRequester),
//                        value = text,
//                        placeholder = {
//                            Text(text = "Search...", style = TextStyle(fontSize = 24.sp))
//                        },
//                        onValueChange = { newText ->
//                            text = newText
//                        }
//                    )
//                },
//                navigationIcon = {
//                    IconButton(onClick = { navController.popBackStack() }) {
//                        Icon(Icons.Filled.ArrowBack, "backIcon")
//                    }
//                }
//            )
        }
    ) {
        Surface(Modifier.padding(it), color = MaterialTheme.colors.background) {
            val data = dataState.data
            when (dataState.state) {
                State.DATA -> {
                    if (data == null) {
                        ErrorView(resId = R.drawable.empty_box,
                            message = stringResource(R.string.empty),
                            onClick = {
                                v.load()
                            })
                    } else {
                        SearchView(navController, data)
                    }
                }

                State.IDLE -> {
                }

                State.LOADING -> {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp)
                    ) {
                        CircularProgressIndicator(color = WhiteGray.copy(alpha = 0.6f))
                    }
                }

                State.ERROR -> {
                    if (data !== null) {
                        SearchView(navController, data)
                    } else {
                        ErrorView(resId = R.drawable.error,
                            message = stringResource(R.string.unknown_error),
                            buttonName = stringResource(R.string.retry),
                            onClick = {
                                v.load()
                            })
                    }
                }
            }
        }
    }
}


@Composable
fun SearchView(navController: NavController, searchData: List<SearchItem>) {
    val scrollState = rememberLazyListState()
    val v: SearchViewModel = viewModel(LocalContext.current as ComponentActivity)
    scrollState.OnBottomReached(2) {
        v.next()
    }
    val infiniteState by v.infiniteState.collectAsState()
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        state = scrollState,
        userScrollEnabled = true
    ) {
        items(
            count = searchData.size,
            key = {
                searchData[it].url.ifEmpty { it }
            }
        ) {
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
                    .animateContentSize(), contentAlignment = Alignment.Center
            ) {
                if (infiniteState == InfiniteState.LOADING) {
                    CircularProgressIndicator(
                        Modifier.padding(20.dp),
                        color = WhiteGray.copy(alpha = 0.6f)
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
fun SearchItemView(navController: NavController, data: SearchItem) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    Box(Modifier.clickable {
        scope.launch {
            navController.navigate(
                "${MainNavigation.KOMIKDETAIL}/${data.title}/${
                    getLastPathSegment(
                        data.url
                    )
                }",
            )
        }
    }) {
        Row(
            Modifier
                .padding(10.dp)
        ) {
            ImageView(
                url = data.img, modifier = Modifier
                    .width(110.dp)
                    .height(160.dp)
                    .clip(RoundedCornerShape(5.dp))
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column() {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        Modifier
                            .clip(
                                RoundedCornerShape(4.dp)
                            )
                            .background(color = if (data.type == "Manga") Blue else (if (data.type == "Manhwa") Green else Red))
                    ) {
                        Text(
                            data.type,
                            style = MaterialTheme.typography.caption.copy(color = Color.White),
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = "Star",
                            modifier = Modifier.height(16.dp),
                            tint = Yellow
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(data.score.toString(), style = MaterialTheme.typography.body2)
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
