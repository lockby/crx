package com.crstlnz.komikchino.ui.screens.chapter.components

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalOverscrollConfiguration
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import coil.compose.AsyncImage
import coil.decode.DecodeResult
import coil.decode.Decoder
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.crstlnz.komikchino.R
import com.crstlnz.komikchino.config.AppSettings
import com.crstlnz.komikchino.config.nunito
import com.crstlnz.komikchino.data.model.DataState.Loading.getDataOrNull
import com.crstlnz.komikchino.data.model.ImageSize
import com.crstlnz.komikchino.data.model.ImageSizeRequest
import com.crstlnz.komikchino.data.model.ScrollImagePosition
import com.crstlnz.komikchino.data.model.State
import com.crstlnz.komikchino.ui.components.customswipe.CustomSwipeRefresh
import com.crstlnz.komikchino.ui.components.customswipe.rememberCustomSwipeRefresh
import com.crstlnz.komikchino.ui.screens.chapter.ChapterViewModel
import com.crstlnz.komikchino.ui.theme.Black1
import com.crstlnz.komikchino.ui.util.ComposableLifecycle
import kotlinx.coroutines.launch
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.toggleScale
import net.engawapg.lib.zoomable.zoomable
import java.util.UUID

data class ChapterImage(
    val url: String, var useHardware: Boolean, val key: String,
)

const val PRELOAD_COUNT = 2

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChapterImageList(
    modifier: Modifier = Modifier,
    images: List<String>,
    onNavChange: (isShow: Boolean?) -> Unit = {},
    viewModel: ChapterViewModel,
    onNextClick: () -> Unit = {}
) {
    var banner by remember { mutableStateOf("") }
    val lifecycleOwner = LocalLifecycleOwner.current
    val defaultAspectRatio = remember { 5f / 8f }
    val screenWidthPixel =
        with(LocalDensity.current) { LocalConfiguration.current.screenWidthDp.dp.toPx() }.toInt()
    val chapterImages = remember { mutableStateListOf<ChapterImage>() }

    var chapterScrollPosition = remember { viewModel.getChapterScrollPosition() }

    fun getCalculatedImageSize(): List<ImageSizeRequest> {
        return if ((chapterScrollPosition?.imageSize != null) && (chapterScrollPosition!!.imageSize.size == images.size)) {
            chapterScrollPosition!!.imageSize.map {
                ImageSizeRequest(it.calculated, it.height, it.width)
            }
        } else {
            images.map {
                ImageSizeRequest(
                    false, 0f, 0f
                )
            }
        }
    }

    val calculatedImageSize = remember { mutableListOf<ImageSizeRequest>() }
    val dataState by viewModel.state.collectAsState()
    val chapterKey by viewModel.chapterKey.collectAsState()
    val lazyColumnState = rememberLazyListState(
        chapterScrollPosition?.initialFirstVisibleItemIndex ?: 0,
        chapterScrollPosition?.initialFirstVisibleItemScrollOffset ?: 0
    )

    LaunchedEffect(chapterKey) {
        if (chapterKey > 0) {
            lazyColumnState.scrollToItem(0, 0)
            chapterScrollPosition = viewModel.getChapterScrollPosition()
        }
    }

    LaunchedEffect(images) {
        calculatedImageSize.clear()
        calculatedImageSize.addAll(getCalculatedImageSize())
        chapterImages.clear()
        chapterImages.addAll(images.mapIndexed { _, url ->
            ChapterImage(
                url, true, UUID.randomUUID().toString()
            )
        })
    }

    LaunchedEffect(dataState) {
        if (dataState.state == State.DATA) {
            banner = AppSettings.banner()
            viewModel.saveHistory()
        }
    }

    LaunchedEffect(Unit) {
        if (chapterKey == 0) {
            val firstVisible = chapterScrollPosition?.initialFirstVisibleItemIndex ?: 0
            val firstVisibleOffset = chapterScrollPosition?.initialFirstVisibleItemScrollOffset ?: 0
            if (lazyColumnState.firstVisibleItemIndex != firstVisible || lazyColumnState.firstVisibleItemScrollOffset != firstVisibleOffset) {
                lazyColumnState.scrollToItem(
                    chapterScrollPosition?.initialFirstVisibleItemIndex ?: 0,
                    chapterScrollPosition?.initialFirstVisibleItemScrollOffset ?: 0
                )
            }
        }
    }

    ComposableLifecycle(lifecycleOwner) { _, event ->
        if (event == Lifecycle.Event.ON_PAUSE) {
            viewModel.saveHistory(
                ScrollImagePosition(
                    lazyColumnState.firstVisibleItemIndex,
                    lazyColumnState.firstVisibleItemScrollOffset,
                    calculatedImageSize.map {
                        ImageSize(
                            it.calculated, it.height, it.width
                        )
                    }
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
            refreshTriggerDistance = 120.dp,
            indicatorBackground = White,
            onRefresh = {
                onNextClick()
            }
        ) {
            val density = LocalDensity.current
            val localConfig = LocalConfiguration.current
            val height = remember { with(density) { localConfig.screenHeightDp.dp.toPx() } }
            val width = remember { with(density) { localConfig.screenWidthDp.dp.toPx() } }
            val zoomState = rememberZoomState(
                contentSize = androidx.compose.ui.geometry.Size(
                    width,
                    height
                )
            )
            val scope = rememberCoroutineScope()
            LazyColumn(
                modifier
                    .weight(1f)
                    .zoomable(zoomState)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                onNavChange(null)
                            },
                            onDoubleTap = { tapOffset ->
                                scope.launch {
                                    zoomState.toggleScale(2f, tapOffset)
                                }
                            }
                        )
                    },
                state = lazyColumnState,
            ) {
                item(key = "first") {
                    Box(
                        Modifier
                            .aspectRatio(4f / 3f)
                            .fillMaxWidth()
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
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Black
                                    )
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
                    for (i in 0..PRELOAD_COUNT) {
                        val index = idx + i
                        val image = chapterImages.getOrNull(index)
                        if (image != null) {
                            val imgSizeReq = calculatedImageSize.getOrNull(index)
                            if (imgSizeReq?.imageRequest != null) continue
                            val imgReq = ImageRequest.Builder(LocalContext.current)
                                .data(image.url)
                                // Disable reading from/writing to the memory cache.
                                .memoryCachePolicy(CachePolicy.DISABLED)
                                // Set a custom `Decoder.Factory` that skips the decoding step.
                                .decoderFactory { _, _, _ ->
                                    Decoder { DecodeResult(ColorDrawable(Color.BLACK), false) }
                                }
                                .build()

                            imgSizeReq?.let {
                                it.imageRequest = imgReq
                            }

                            imgReq.lifecycle

                            AppSettings.imageLoader?.enqueue(
                                imgReq
                            )
                        }
                    }

                    val imageSizeRequest = calculatedImageSize.getOrNull(idx)
                    if (imageSizeRequest != null) {
                        var aspectRatio =
                            if (imageSizeRequest.calculated && imageSizeRequest.height > 0f && imageSizeRequest.width > 0f) {
                                imageSizeRequest.width / imageSizeRequest.height
                            } else {
                                defaultAspectRatio
                            }

                        if (aspectRatio <= 0f || aspectRatio.isNaN()) aspectRatio =
                            defaultAspectRatio
                        ChapterImageView(
                            chapterImages[idx],
                            onDisableHardware = {
                                val data = chapterImages[idx]
                                data.useHardware = false
                                chapterImages[idx] = data
                            },
                            pageNumber = idx + 1,
                            onImageSizeCalculated = { h, w ->
                                calculatedImageSize.getOrNull(idx)?.let {
                                    it.height = h
                                    it.width = w
                                }
                            },
                            defaultAspectRatio = defaultAspectRatio,
                            aspectRatio = aspectRatio,
                            screenWidthPixel = screenWidthPixel,
                        )
                    }
                }

                item(key = "banner") {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(banner)
                            .crossfade(true)
                            .networkCachePolicy(CachePolicy.ENABLED)
                            .diskCachePolicy(CachePolicy.DISABLED)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .build(),
                        contentDescription = "Banner",
                        contentScale = ContentScale.Crop,
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
