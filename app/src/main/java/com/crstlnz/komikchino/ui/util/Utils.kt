package com.crstlnz.komikchino.ui.util

import android.os.Build
import android.text.Html
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.toLowerCase
import com.crstlnz.komikchino.ui.theme.Blue
import com.crstlnz.komikchino.ui.theme.Green
import com.crstlnz.komikchino.ui.theme.Purple
import com.crstlnz.komikchino.ui.theme.Red
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.Period
import java.util.Calendar
import java.util.Date
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

