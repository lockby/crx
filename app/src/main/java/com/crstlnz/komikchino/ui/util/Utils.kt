package com.crstlnz.komikchino.ui.util

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.Color
import androidx.core.text.HtmlCompat
import com.crstlnz.komikchino.config.AppSettings
import com.crstlnz.komikchino.data.api.KomikServer
import com.crstlnz.komikchino.data.util.StorageHelper
import com.crstlnz.komikchino.ui.theme.Blue
import com.crstlnz.komikchino.ui.theme.Green
import com.crstlnz.komikchino.ui.theme.Purple
import com.crstlnz.komikchino.ui.theme.Red
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
    return HtmlCompat.fromHtml(str, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
}

fun getComicTypeColor(type: String): Color {
    if (AppSettings.komikServer == KomikServer.MANGAKATANA) {
        return if (type.lowercase(Locale.ROOT) == "completed") {
            Blue
        } else if (type.lowercase(Locale.ROOT) == "ongoing") {
            Green
        } else {
            Red
        }
    }
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

suspend fun <T> loadWithCacheMainUtil(
    key: String,
    fetch: suspend () -> T,
    storage: StorageHelper<T>,
    force: Boolean
): T? {
    val cache = storage.getRaw<T>(key)
    return if (cache?.isValid == true && !force) {
        cache.data
    } else {
        try {
            val data = fetch()
            storage.set<T>(key, data)
            data
        } catch (e: Exception) {
            if (cache?.data !== null) {
                cache.data
            } else {
                throw e
            }
        }
    }
}

suspend fun <T> loadWithCacheUtil(
    key: String,
    fetch: suspend () -> T,
    storage: StorageHelper<T>,
    force: Boolean = true,
): T? {
    return loadWithCacheMainUtil(key, fetch, storage, force)
}