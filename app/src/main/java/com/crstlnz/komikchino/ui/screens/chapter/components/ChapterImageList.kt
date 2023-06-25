package com.crstlnz.komikchino.ui.screens.chapter.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import com.crstlnz.komikchino.R
import com.crstlnz.komikchino.config.nunito
import com.crstlnz.komikchino.data.model.DataState.Idle.getDataOrNull
import com.crstlnz.komikchino.data.model.State
import com.crstlnz.komikchino.ui.screens.chapter.ChapterViewModel
import com.crstlnz.komikchino.ui.theme.Black1
import com.crstlnz.komikchino.ui.util.ComposableLifecycle
import com.crstlnz.komikchino.ui.util.noRippleClickable

data class ChapterImage(
    val url: String,
    var useHardware: Boolean
)

@Composable
fun ChapterImageList(
    modifier: Modifier = Modifier, images: List<String>,
//    images: List<ImageBitmap?>,
    onNavChange: (isShow: Boolean?) -> Unit = {},
    viewModel: ChapterViewModel,
    onNextClick: () -> Unit = {}
) {
//    val chapterHistory = viewModel.chapterHistoryData
    val scrollState = rememberLazyListState()
//    val scrollState = rememberLazyListState(
//        initialFirstVisibleItemIndex = if (chapterHistory.id == viewModel.id) chapterHistory.readHistory?.firstVisibleItemIndex
//            ?: 0 else 0,
//        initialFirstVisibleItemScrollOffset = if (chapterHistory.id == viewModel.id) chapterHistory.readHistory?.firstVisibleItemScrollOffset
//            ?: 0 else 0,
//    )
    val chapterList by viewModel.chapterList.collectAsState()
    val chapterPosition by viewModel.currentPosition.collectAsState()
    val chapter =
        chapterList.getDataOrNull()?.getOrNull(chapterPosition + 1)
    val lifecycleOwner = LocalLifecycleOwner.current
    val dataState by viewModel.state.collectAsState()
//    val komikHeight =
//        screenHeight.dp + LocalStatusBarPadding.current + LocalSystemBarPadding.current
    val chapterImages = remember {
        mutableStateListOf<ChapterImage>()
    }

    chapterImages.addAll(images.map {
        ChapterImage(
            it,
            true
        )
    })

//    val imageList = remember { Mutal}
    LaunchedEffect(dataState) {
        if (dataState.state == State.DATA) viewModel.saveHistory(
            scrollState.firstVisibleItemIndex, scrollState.firstVisibleItemScrollOffset
        )
    }

    ComposableLifecycle(lifecycleOwner) { _, event ->
        if (event == Lifecycle.Event.ON_PAUSE) {
            viewModel.saveHistory(
                scrollState.firstVisibleItemIndex, scrollState.firstVisibleItemScrollOffset
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White)
    ) {
        LazyColumn(
            modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onDoubleTap = {}, onTap = {
                        onNavChange(null)
                    })
                }, state = scrollState
        ) {
            item {
                Box(
                    Modifier
                        .aspectRatio(4f / 3f)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.mipmap.app_icon),
                            contentDescription = "App Icon"
                        )
                        Spacer(modifier = Modifier.width(15.dp))
                        Text(
                            "Komik Chino",
                            color = Black1,
                            fontFamily = nunito,
                            style = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.Black)
                        )
                        Spacer(Modifier.width(5.dp))
                    }
                }
            }
            items(images.size) { idx ->
                ChapterImageView(chapterImages[idx], onDisableHardware = {
                    val data = chapterImages[idx]
                    data.useHardware = false
                    chapterImages[idx] = data
                })
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(5f / 4f), contentAlignment = Alignment.Center
                ) {
                    if (chapter != null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Lanjut",
                                style = MaterialTheme.typography.h6.copy(
                                    fontFamily = nunito,
                                    color = Black1,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                            Spacer(Modifier.height(13.dp))
                            Image(
                                modifier = Modifier
                                    .width(50.dp)
                                    .noRippleClickable {
                                        onNextClick()
                                    },
                                painter = painterResource(id = R.drawable.next_button),
                                contentDescription = "Next Button"
                            )
                        }
                    } else {
                        Text(
                            "Tak ade next chapter boi :(",
                            style = MaterialTheme.typography.h6.copy(
                                fontFamily = nunito,
                                color = Black1,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                    }

                }
            }
        }
    }
}