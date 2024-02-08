package com.crstlnz.komikchino.ui.screens.home.fragments.settings.sub

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.crstlnz.komikchino.data.api.KomikServer
import com.crstlnz.komikchino.ui.screens.home.fragments.settings.SettingViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ServerSelectScreen(navController: NavController) {
    val v = hiltViewModel<SettingViewModel>()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val serverState = v.settings.komikServer
    val server by serverState.collectAsState(initial = KomikServer.KIRYUU)
    var selected by remember { mutableStateOf(server) }
    LaunchedEffect(Unit) {
        serverState.collectLatest {
            selected = it
        }
    }
    Scaffold(topBar = {
        TopAppBar(
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            title = {
                Text("Server")
            },
            actions = {
                TextButton(onClick = {
                    scope.launch {
                        v.settings.setServer(selected)
                        Toast.makeText(
                            context,
                            "Merestart aplikasi",
                            Toast.LENGTH_LONG
                        ).show()
                        val packageManager: PackageManager = context.packageManager
                        val intent: Intent =
                            packageManager.getLaunchIntentForPackage(context.packageName)!!
                        val componentName: ComponentName = intent.component!!
                        val restartIntent: Intent = Intent.makeRestartActivityTask(componentName)
                        context.startActivity(restartIntent)
                        Runtime.getRuntime().exit(0)
                    }

                }, enabled = server != selected) {
                    Text("SAVE")
                }
            }
        )
    }) {
        val uriHandler = LocalUriHandler.current
        Surface(Modifier.padding(it)) {
            val serverList =
                remember { KomikServer.values().sortedBy { server -> server.title } }
            LazyColumn(Modifier.fillMaxSize()) {
                items(serverList.size, key = { idx -> serverList[idx].value }) {
                    ListItem(
                        modifier = Modifier
                            .combinedClickable(onLongClick = {
                                uriHandler.openUri(serverList[it].url)
                            }) { selected = serverList[it] },
                        leadingContent = {
                            Image(
                                painter = painterResource(id = serverList[it].bahasa.icon),
                                contentDescription = serverList[it].bahasa.title,
                                modifier = Modifier.clip(RoundedCornerShape(5.dp))
                            )
                        },
                        supportingContent = {
                            Text(serverList[it].url)
                        },
                        headlineContent = {
                            Text(serverList[it].title)
                        },
                        trailingContent = {
                            if (selected == serverList[it]) {
                                Icon(Icons.Filled.Check, contentDescription = "Selected")
                            }
                        }
                    )
                }
            }
        }
    }
}