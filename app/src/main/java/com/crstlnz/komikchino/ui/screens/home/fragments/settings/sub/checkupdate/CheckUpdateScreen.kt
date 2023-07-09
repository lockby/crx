package com.crstlnz.komikchino.ui.screens.home.fragments.settings.sub.checkupdate

import android.content.BroadcastReceiver
import android.widget.Space
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import com.crstlnz.komikchino.R
import com.crstlnz.komikchino.data.model.DataState
import com.crstlnz.komikchino.data.model.DataState.Idle.getDataOrNull
import com.crstlnz.komikchino.data.model.State
import com.crstlnz.komikchino.data.util.downloadApk
import com.crstlnz.komikchino.data.util.getAppVersion
import com.crstlnz.komikchino.data.util.versionCheck
import com.crstlnz.komikchino.ui.navigations.MainNavigation
import com.crstlnz.komikchino.ui.util.ComposableLifecycle
import com.crstlnz.komikchino.ui.util.checkWriteExternalPermission
import com.crstlnz.komikchino.ui.util.convertHTML
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun CheckUpdateScreen(navController: NavController) {
    val context = LocalContext.current
    var receiver: BroadcastReceiver? = remember { null }
    ComposableLifecycle { _, event ->
        if (event == Lifecycle.Event.ON_DESTROY) {
            if (receiver != null) {
                context.unregisterReceiver(receiver)
            }
        }
    }
    var isUpdating by remember { mutableStateOf(false) }

    val v = hiltViewModel<CheckUpdateViewModel>()
    val dataState by v.state.collectAsState()
    val appVersion = getAppVersion(context)
    val isUpdateAvailable = versionCheck(
        dataState.getDataOrNull()?.tagName ?: "",
        appVersion
    )
    val updatesVersion = dataState.getDataOrNull()?.tagName ?: ""
    val changeLog = dataState.getDataOrNull()?.body ?: ""
    val downloadUrl = dataState.getDataOrNull()?.assets?.getOrNull(0)?.browserDownloadUrl ?: ""
    val name = dataState.getDataOrNull()?.name ?: ""
    val htmlUrl = dataState.getDataOrNull()?.htmlUrl ?: ""
    val title = if (dataState.state == State.ERROR) {
        "Check error"
    } else {
        if (isUpdateAvailable) "Updates available" else "App is up to date"
    }

    val errorString = if (dataState.state == State.ERROR) {
        (dataState as DataState.Error).error
    } else {
        null
    }


    val isGranted = checkWriteExternalPermission()

    fun downloadUpdates() {
        if (isGranted) {
            isUpdating = true
            context.downloadApk(downloadUrl, "$name.apk", onDownloadFinish = {
                isUpdating = false
            },
                onReceiverCreated = {
                    receiver = it
                })
        } else {
            navController.navigate(MainNavigation.STORAGE_REQUEST)
        }
    }

    Scaffold(contentWindowInsets = WindowInsets.ime) {
        Surface(
            Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            Column(
                Modifier
                    .padding(25.dp)
                    .fillMaxSize()
                    .scrollable(rememberScrollState(), Orientation.Vertical)
            ) {
                Column(
                    Modifier
                        .weight(1f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(Modifier.weight(1.5f), contentAlignment = BottomCenter) {
                        Image(
                            painter = painterResource(id = R.drawable.files),
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .padding(top = 20.dp),
                            contentDescription = "Update illustration"
                        )
                    }
                    Spacer(Modifier.height(30.dp))
                    Text(
                        title,
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(15.dp))
                    if (isUpdateAvailable) {
                        val uriHandler = LocalUriHandler.current
                        LazyColumn(
                            Modifier
                                .weight(1f)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(8.dp)
                                )
                        ) {
                            item {
                                MarkdownText(
                                    markdown =
                                    "#### `${appVersion}` &rarr;  `${updatesVersion}`\n\n" +
                                            changeLog + "\n\n**[Open Github](${htmlUrl})**",
                                    modifier = Modifier.padding(15.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.labelMedium,
                                    onLinkClicked = { url ->
                                        uriHandler.openUri(url)
                                    }
                                )
                            }
                        }
                    } else {
                        LazyColumn(Modifier.weight(1f)) {
                            if (errorString != null) {
                                item {
                                    Text(
                                        "Version ${getAppVersion(context)}",
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = MaterialTheme.colorScheme.onSurface.copy(
                                                alpha = 0.6f
                                            )
                                        ),
                                        textAlign = TextAlign.Center
                                    )
                                }

                            } else {
                                item {
                                    Text(
                                        "Version ${getAppVersion(context)}",
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = MaterialTheme.colorScheme.onSurface.copy(
                                                alpha = 0.6f
                                            )
                                        ),
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(Modifier.height(5.dp))
                                }
                                item {
                                    Text(
                                        "Current version is currenlty up to dates, wait for the next updates \uD83D\uDE0E",
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = MaterialTheme.colorScheme.onSurface.copy(
                                                alpha = 0.6f
                                            )
                                        ),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                }

                Spacer(Modifier.height(30.dp))


                Button(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = dataState.state != State.LOADING || (downloadUrl.isNotEmpty() && !isUpdating),
                    onClick = {
                        if (isUpdateAvailable) {
                            downloadUpdates()
                        } else {
                            v.load()
                        }
                    }) {
                    when (dataState.state) {
                        State.LOADING -> {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier
                                    .width(22.dp)
                                    .height(22.dp)
                                    .align(CenterVertically)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "Please wait",
                                modifier = Modifier.padding(vertical = 8.dp),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }

                        else -> {
                            if (isUpdateAvailable) {
                                if (downloadUrl.isEmpty()) {
                                    Text(
                                        "Download url missing!",
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                } else {
                                    if (isUpdating) {
                                        CircularProgressIndicator(
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier
                                                .width(22.dp)
                                                .height(22.dp)
                                                .align(CenterVertically)
                                        )
                                        Spacer(Modifier.width(10.dp))
                                        Text(
                                            "Updating",
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        )
                                    } else {
                                        Text(
                                            "Update",
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    "Check",
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }


            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 15.dp)
                    .statusBarsPadding(), contentAlignment = Alignment.TopEnd
            ) {
                FilledTonalButton(
                    onClick = { navController.popBackStack() },
                    contentPadding = PaddingValues(18.dp, 0.dp)
                ) {
                    Text(
                        "Back",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }
    }
}