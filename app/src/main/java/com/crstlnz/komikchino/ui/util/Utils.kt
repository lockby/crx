package com.crstlnz.komikchino.ui.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.Color
import androidx.core.text.HtmlCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.crstlnz.komikchino.config.AppSettings
import com.crstlnz.komikchino.data.api.KomikServer
import com.crstlnz.komikchino.data.util.StorageHelper
import com.crstlnz.komikchino.ui.theme.Blue
import com.crstlnz.komikchino.ui.theme.Green
import com.crstlnz.komikchino.ui.theme.Red
import com.crstlnz.komikchino.ui.theme.Yellow
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.security.ProviderInstaller
import kotlinx.coroutines.delay
import java.util.Locale
import javax.net.ssl.SSLContext
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
            Yellow
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

fun providerInstallerCheck(context: Activity) {
    try {
        // Checking if the ProviderInstaller is installed and updated
        ProviderInstaller.installIfNeeded(context)
        val sslContext: SSLContext = SSLContext.getInstance("TLSv1.2")
        sslContext.init(null, null, null)
        sslContext.createSSLEngine()
        Toast.makeText(
            context,
            "Provider updated",
            Toast.LENGTH_LONG
        ).show()
    } catch (e: GooglePlayServicesRepairableException) {
        /* If the ProviderInstaller is installed but not updated
        A popup asks the user to do a manual update of the Google Play Services
         */
        e.printStackTrace()
        GoogleApiAvailability.getInstance().showErrorNotification(context, e.connectionStatusCode)
        Toast.makeText(
            context,
            "Provider it outdated. Please update your Google Play Service",
            Toast.LENGTH_LONG
        ).show()
    } catch (e: GooglePlayServicesNotAvailableException) {
        e.printStackTrace()

        /* If the ProviderInstaller is not installed but not updated
        A popup redirects the user to the Google Play Services page on the Google PlayStore
        and let the user download them.
         */
        val dialog = GoogleApiAvailability.getInstance()
            .getErrorDialog(
                context, e.errorCode,
                9000
            )
        dialog!!.setCancelable(false)
        dialog.show()
    }
}

fun hideSystemUI(context: Activity) {
    WindowInsetsControllerCompat(context.window, context.window.decorView).let { controller ->
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}

fun showSystemUI(context: Activity) {
    WindowInsetsControllerCompat(
        context.window,
        context.window.decorView
    ).show(WindowInsetsCompat.Type.systemBars())
}

fun Context.getActivity(): ComponentActivity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is ComponentActivity) {
            return currentContext
        }
        currentContext = currentContext.baseContext
    }
    return null
}