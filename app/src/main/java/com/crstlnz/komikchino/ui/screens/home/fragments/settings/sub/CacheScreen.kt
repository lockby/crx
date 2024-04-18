package com.crstlnz.komikchino.ui.screens.home.fragments.settings.sub

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import com.crstlnz.komikchino.R
import com.crstlnz.komikchino.config.IMAGE_CACHE_PATH
import com.crstlnz.komikchino.data.api.KomikServer
import com.crstlnz.komikchino.data.util.clearCache
import com.crstlnz.komikchino.data.util.formatSize
import com.crstlnz.komikchino.data.util.getCacheFolderSize
import com.crstlnz.komikchino.hilt.Cache
import com.crstlnz.komikchino.ui.navigations.HomeSections
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CacheScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var imageCacheSize by remember {
        mutableStateOf(
            formatSize(
                getCacheFolderSize(
                    context, IMAGE_CACHE_PATH
                )
            )
        )
    }

    fun refreshImageCache() {
        imageCacheSize = formatSize(
            getCacheFolderSize(
                context, IMAGE_CACHE_PATH
            )
        )
    }

    Scaffold(topBar = {
        TopAppBar(navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
        }, title = {
            Text("Cache")
        }, actions = {})
    }) {
        Surface(Modifier.padding(it)) {
            val homepageList = HomeSections.values()
            LazyColumn(Modifier.fillMaxSize()) {
                item {
                    ListItem(modifier = Modifier.clickable {
                        Toast.makeText(
                            context, "Menghapus image cache...", Toast.LENGTH_LONG
                        ).show()
                        clearCache(context, IMAGE_CACHE_PATH)
                        refreshImageCache()
                    }, leadingContent = {
                        Icon(
                            painter = painterResource(id = R.drawable.image_outline),
                            contentDescription = null
                        )
                    }, headlineContent = {
                        Text("Image Cache")
                    }, supportingContent = {
                        Text("Click to clear cache")
                    }, trailingContent = {
                        Text(imageCacheSize)
                    })
                }

                item {
                    val scope = rememberCoroutineScope()
                    ListItem(
                        modifier = Modifier.clickable {
                            scope.launch {
                                for (server in KomikServer.values()) {
                                    try {
                                        for (cache in Cache.values()) {
                                            context.getSharedPreferences(
                                                "$server-$cache", Context.MODE_PRIVATE
                                            ).edit().clear().apply()
                                        }
                                    } catch (e: Exception) {
                                        e.stackTraceToString()
                                    }
                                }
                                Toast.makeText(
                                    context, "Menghapus data cache...", Toast.LENGTH_LONG
                                ).show()
                            }
                        },
                        leadingContent = {
                            Icon(
                                painter = painterResource(id = R.drawable.database),
                                contentDescription = null
                            )
                        },
                        headlineContent = {
                            Text("Data Cache")
                        },
                        supportingContent = {
                            Text("Click to clear cache")
                        },
                    )
                }
            }
        }
    }
}