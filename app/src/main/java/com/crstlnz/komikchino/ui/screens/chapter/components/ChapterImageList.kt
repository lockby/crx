package com.crstlnz.komikchino.ui.screens.chapter.components

import android.util.Log
import android.view.MotionEvent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.gestures.zoomBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import com.crstlnz.komikchino.R
import com.crstlnz.komikchino.config.AppSettings
import com.crstlnz.komikchino.config.nunito
import com.crstlnz.komikchino.data.model.DataState.Loading.getDataOrNull
import com.crstlnz.komikchino.data.model.ImageSize
import com.crstlnz.komikchino.data.model.ScrollImagePosition
import com.crstlnz.komikchino.data.model.State
import com.crstlnz.komikchino.ui.components.ImageView
import com.crstlnz.komikchino.ui.components.customswipe.CustomSwipeRefresh
import com.crstlnz.komikchino.ui.components.customswipe.rememberCustomSwipeRefresh
import com.crstlnz.komikchino.ui.screens.chapter.ChapterViewModel
import com.crstlnz.komikchino.ui.theme.Black1
import com.crstlnz.komikchino.ui.util.ComposableLifecycle
import com.smarttoolfactory.zoom.rememberZoomState
import com.smarttoolfactory.zoom.zoom
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive

data class ChapterImage(
    val url: String, var useHardware: Boolean, val key: String,
)

@OptIn(
    ExperimentalFoundationApi::class, ExperimentalLayoutApi::class,
    ExperimentalComposeUiApi::class
)
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
    val screenWidthPixel =
        with(LocalDensity.current) { LocalConfiguration.current.screenWidthDp.dp.toPx() }.toInt()
    val chapterImages = remember {
        mutableStateListOf<ChapterImage>()
    }

    LaunchedEffect(images) {
        chapterImages.clear()
        chapterImages.addAll(images.mapIndexed { index, url ->
            ChapterImage(
                url, true, index.toString()
            )
        })
    }

    val chapterScrollPostition = remember { viewModel.getChapterScrollPosition() }

    val lazyColumnState = rememberLazyListState(
        chapterScrollPostition?.initialFirstVisibleItemIndex ?: 0,
        chapterScrollPostition?.initialFirstVisibleItemScrollOffset ?: 0
    )

    val dataState by viewModel.state.collectAsState()
    val chapterKey by viewModel.chapterKey.collectAsState()

    val calculatedImageSize =
        remember {
            if (chapterScrollPostition?.imageSize != null && chapterScrollPostition.imageSize.size == images.size) {
                chapterScrollPostition.imageSize
            } else {
                images.map {
                    ImageSize(
                        false, 0f, 0f
                    )
                }
            }.toMutableList()
        }

    LaunchedEffect(chapterKey) {
        if (chapterKey > 0) {
            lazyColumnState.scrollToItem(0, 0)
        }
    }

    LaunchedEffect(dataState) {
        if (dataState.state == State.DATA) {
            viewModel.saveHistory()
        }
    }

    LaunchedEffect(Unit) {
//        Log.d("CHAPTER FIRST ITEM", chapterScrollPostition?.initialFirstVisibleItemIndex.toString())
//        Log.d(
//            "CHAPTER SCROLL OFFSET",
//            chapterScrollPostition?.initialFirstVisibleItemScrollOffset.toString()
//        )
//        Log.d("CHAPTER STATE ITEM", lazyColumnState.firstVisibleItemIndex.toString())
//        Log.d("CHAPTER STATE OFFSET", lazyColumnState.firstVisibleItemScrollOffset.toString())
        lazyColumnState.scrollToItem(
            chapterScrollPostition?.initialFirstVisibleItemIndex ?: 0,
            chapterScrollPostition?.initialFirstVisibleItemScrollOffset ?: 0
        )
    }
    ComposableLifecycle(lifecycleOwner) { _, event ->
        if (event == Lifecycle.Event.ON_PAUSE) {
            viewModel.saveHistory(
                ScrollImagePosition(
                    lazyColumnState.firstVisibleItemIndex,
                    lazyColumnState.firstVisibleItemScrollOffset,
                    calculatedImageSize
                )
            )
        }
    }

    val refreshState = rememberCustomSwipeRefresh(isRefreshing = false)
    CompositionLocalProvider(
        LocalOverscrollConfiguration provides null
    ) {
        CustomSwipeRefresh(
            state = refreshState,
            modifier = Modifier.background(White),
            refreshTriggerDistance = 100.dp,
            indicatorBackground = White,
            onRefresh = {
                onNextClick()
            }) {
            val interactionSource = remember { MutableInteractionSource() }
//            var isMultipleTouch by remember { mutableStateOf(false) }
//            var scale by remember { mutableStateOf(1f) }
//            var rotation by remember { mutableStateOf(0f) }
//            var offset by remember { mutableStateOf(Offset.Zero) }
//            val state = rememberTransformableState { zoomChange, offsetChange, rotationChange ->
//                scale *= zoomChange
//                rotation += rotationChange
//                offset += offsetChange
//            }

            LazyColumn(
                modifier
                    .fillMaxWidth()
//                    .zoom(
//                        key = Unit,
//                        consume = isMultipleTouch,
//                        clip = true,
//                        zoomState = rememberZoomState(limitPan = true),
//                        onGestureStart = null,
//                        onGesture = null,
//                        onGestureEnd = null
//                    )
//                    .graphicsLayer(
//                        scaleX = scale,
//                        scaleY = scale,
//                        rotationZ = rotation,
//                        translationX = offset.x,
//                        translationY = offset.y
//                    )
//                    .transformable(state, lockRotationOnZoomPan = true,isMultipleTouch)
//                    .pointerInput(Unit) {
//                        detectTransformGestures { centroid, pan, zoom, rotation ->
//                            scale *= zoom
//                        }
//                        val currentContext = currentCoroutineContext()
//                        awaitEachGesture {
//                            do {
//                                val event = awaitPointerEvent()
//                                isMultipleTouch = event.changes.size > 1
//                            } while (event.changes.any { it.pressed } && currentContext.isActive)
//                        }
//                    }
//                    .zoomable(rememberZoomableState(), enabled = isMultipleTouch)
                    .combinedClickable(interactionSource = interactionSource, indication = null) {
                        onNavChange(null)
                    }
                    .weight(1f),
                state = lazyColumnState,
//                userScrollEnabled = !isMultipleTouch
            ) {
                item(key = "first") {
                    Box(
                        Modifier
                            .aspectRatio(4f / 3f)
                            .background(White),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(id = R.drawable.app_icon),
                                    contentDescription = "App Icon",
                                    modifier = Modifier.size(38.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    "Komik Chino",
                                    color = Black1,
                                    fontFamily = nunito,
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black)
                                )
                                Spacer(Modifier.width(5.dp))
                            }
                            Spacer(Modifier.height(25.dp))
                            Text(
                                "${viewModel.komikData?.title ?: dataState.getDataOrNull()?.komik?.title}",
                                fontFamily = nunito,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = Black1.copy(alpha = 0.7f),
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .height(2.2.dp)
                                    .width(6.dp)
                                    .background(
                                        color = Black1.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                            )
                            Spacer(Modifier.height(6.5.dp))
                            Text(
                                dataState.getDataOrNull()?.title ?: "",
                                fontFamily = nunito,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Black1.copy(alpha = 0.55f),
                                )
                            )
                        }
                    }
                }

                items(
                    chapterImages.size,
                    key = { chapterImages[it].key },
                    contentType = { ChapterImage::class.java }
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
                    }, defaultAspectRatio = aspectRatio,
                        screenWidthPixel = screenWidthPixel
                    )
                }

                item(key = "last") {
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
}
