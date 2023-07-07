package com.crstlnz.komikchino.ui.screens.chapter.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import com.crstlnz.komikchino.R
import com.crstlnz.komikchino.config.AppSettings
import com.crstlnz.komikchino.config.nunito
import com.crstlnz.komikchino.data.model.ImageSize
import com.crstlnz.komikchino.data.model.State
import com.crstlnz.komikchino.ui.components.ImageView
import com.crstlnz.komikchino.ui.components.customswipe.CustomSwipeRefresh
import com.crstlnz.komikchino.ui.components.customswipe.rememberCustomSwipeRefresh
import com.crstlnz.komikchino.ui.screens.chapter.ChapterViewModel
import com.crstlnz.komikchino.ui.theme.Black1
import com.crstlnz.komikchino.ui.util.ComposableLifecycle

data class ChapterImage(
    val url: String, var useHardware: Boolean
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChapterImageList(
    modifier: Modifier = Modifier,
    images: List<String>,
//    images: List<ImageBitmap?>,
    onNavChange: (isShow: Boolean?) -> Unit = {},
    viewModel: ChapterViewModel,
    onNextClick: () -> Unit = {}
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    val chapterImages = remember {
        mutableStateListOf<ChapterImage>()
    }

    LaunchedEffect(images) {
        chapterImages.clear()
        chapterImages.addAll(images.map {
            ChapterImage(
                it, true
            )
        })
    }

    val chapterScrollPostition = viewModel.getChapterScrollPosition()
    val lazyColumnState = rememberLazyListState(
        chapterScrollPostition?.initialFirstVisibleItemIndex ?: 0,
        chapterScrollPostition?.initialFirstVisibleItemScrollOffset ?: 0
    )
    val dataState by viewModel.state.collectAsState()

    val calculatedImageSize =
        if (chapterScrollPostition?.imageSize != null && chapterScrollPostition.imageSize.size == images.size) {
            chapterScrollPostition.imageSize
        } else {
            images.map {
                ImageSize(
                    false, 0f, 0f
                )
            }
        }.toMutableList()

    LaunchedEffect(dataState) {
        if (dataState.state != State.DATA) {
            lazyColumnState.scrollToItem(0, 0)
        }
    }

    LaunchedEffect(dataState) {
        if (dataState.state == State.DATA) {
            viewModel.saveHistory(
                lazyColumnState.firstVisibleItemIndex,
                lazyColumnState.firstVisibleItemScrollOffset,
                calculatedImageSize
            )
        }
    }


    LaunchedEffect(Unit) {
        lazyColumnState.scrollToItem(
            chapterScrollPostition?.initialFirstVisibleItemIndex ?: 0,
            chapterScrollPostition?.initialFirstVisibleItemScrollOffset ?: 0
        )
    }



    ComposableLifecycle(lifecycleOwner) { _, event ->
        if (event == Lifecycle.Event.ON_PAUSE) {
            viewModel.saveHistory(
                lazyColumnState.firstVisibleItemIndex,
                lazyColumnState.firstVisibleItemScrollOffset,
                calculatedImageSize
            )
        }
    }

    val refreshState = rememberCustomSwipeRefresh(isRefreshing = false)
    CompositionLocalProvider(
        LocalOverscrollConfiguration provides null
    ) {
        CustomSwipeRefresh(state = refreshState,
            refreshTriggerDistance = 110.dp,
            onRefresh = {
                onNextClick()
            },
            lazyColumnState = lazyColumnState,
            lazyColumnModifier = modifier.pointerInput(Unit) {
                detectTapGestures(onDoubleTap = {}, onTap = {
                    onNavChange(null)
                })
            }) {
            item {
                Box(
                    Modifier
                        .aspectRatio(4f / 3f)
                        .background(White),
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
            items(
                chapterImages.size
            ) { idx ->
                val imageSize = chapterScrollPostition?.imageSize?.getOrNull(idx)
                val aspectRatio =
                    if (imageSize != null && imageSize.calculated && imageSize.height > 0f && imageSize.width > 0f) {
                        imageSize.width / imageSize.height
                    } else {
                        5f / 8f
                    }

                ChapterImageView(chapterImages[idx], onDisableHardware = {
                    val data = chapterImages[idx]
                    data.useHardware = false
                    chapterImages[idx] = data
                }, onImageSizeCalculated = { h, w ->
                    calculatedImageSize[idx] = ImageSize(
                        true, h, w
                    )
                }, defaultAspectRatio = aspectRatio
                )
            }

            item {
                ImageView(
                    url = AppSettings.banner,
                    contentDescription = "Banner",
                    modifier = Modifier
                        .background(color = White)
                        .fillMaxWidth()
                        .aspectRatio(16f / 25f)
                )
            }
        }
    }

}
