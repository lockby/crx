package com.crstlnz.komikchino.ui.util

import android.content.Context
import android.os.Build
import android.text.Html
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.crstlnz.komikchino.config.AppSettings
import com.crstlnz.komikchino.data.datastore.KomikServer
import com.crstlnz.komikchino.data.util.StorageHelper
import com.crstlnz.komikchino.ui.theme.Blue
import com.crstlnz.komikchino.ui.theme.Green
import com.crstlnz.komikchino.ui.theme.Purple
import com.crstlnz.komikchino.ui.theme.Red
import com.fasterxml.jackson.databind.JavaType
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.system.measureTimeMillis

suspend fun <T> delayBlock(time: Long = 300L, block: suspend () -> T): T {
    val usedTime = measureTimeMillis {
        block()
    }
    val delayTime = time - usedTime
    if (delayTime > 0) {
        delay(delayTime)
    }
    return block()
}

@Composable
fun LazyListState.OnBottomReached(
    itemOffset: Int = 1,
    loadMore: () -> Unit
) {
    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index
                ?: 0
            lastVisibleItem >= layoutInfo.totalItemsCount - 1 - itemOffset
        }
    }

    LaunchedEffect(shouldLoadMore) {
        snapshotFlow { shouldLoadMore.value }
            .collect {
                if (it) loadMore()
            }
    }
}

fun convertHTML(str: String): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(str, Html.FROM_HTML_MODE_LEGACY).toString()
    } else {
        Html.fromHtml(str).toString()
    }
}

fun getComicTypeColor(type: String): Color {
    if(AppSettings.komikServer == KomikServer.MANGAKATANA) return Blue
    return when (type.lowercase(Locale.ROOT).trim()) {
        "manhwa" -> {
            Green
        }

        "manga" -> {
            Blue
        }

        "manhua" -> {
            Red
        }

        else -> {
            Purple
        }
    }
}

suspend fun <T> loadWithCacheMain(
    key: String,
    fetch: suspend () -> T,
    storage: StorageHelper<T>,
    force: Boolean
): T? {
    val cache = storage.getRaw<T>(key)
    return if (cache?.isValid == true && !force) {
        cache.data
    } else {
        val data = fetch()
        storage.set<T>(key, data)
        data
    }
}

suspend fun <T> loadWithCache(
    context: Context,
    key: String,
    fetch: suspend () -> T,
    type: JavaType,
    force: Boolean = true,
): T? {
    val storage = StorageHelper<T>(context, "CACHE", type)
    return loadWithCacheMain(key, fetch, storage, force)
}

suspend fun <T> loadWithCache(
    key: String,
    fetch: suspend () -> T,
    storage: StorageHelper<T>,
    force: Boolean = true,
): T? {
    return loadWithCacheMain(key, fetch, storage, force)
}


@Composable
fun ComposableLifecycle(
    lifeCycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onEvent: (LifecycleOwner, Lifecycle.Event) -> Unit
) {
    DisposableEffect(lifeCycleOwner) {
        val observer = LifecycleEventObserver { source, event ->
            onEvent(source, event)
        }
        lifeCycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifeCycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

