package com.crstlnz.komikchino.ui.screens.home.fragments.settings.sub

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.crstlnz.komikchino.ui.navigations.HomeSections
import com.crstlnz.komikchino.ui.screens.home.fragments.settings.SettingViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeSelection(navController: NavController) {
    val v = hiltViewModel<SettingViewModel>()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val homepageState = v.settings.homepage
    val homepage by homepageState.collectAsState(initial = HomeSections.HOME)
    var selected by remember { mutableStateOf(homepage) }
    LaunchedEffect(Unit) {
        homepageState.collectLatest {
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
                Text("Homepage")
            },
            actions = {
                TextButton(onClick = {
                    scope.launch {
                        v.settings.setHomepage(selected)
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

                }, enabled = homepage != selected) {
                    Text("SAVE")
                }
            }
        )
    }) {
        Surface(Modifier.padding(it)) {
            val homepageList = HomeSections.values()
            LazyColumn(Modifier.fillMaxSize()) {
                items(homepageList.size) {
                    ListItem(
                        modifier = Modifier.clickable { selected = homepageList[it] },
                        headlineContent = {
                            Text(stringResource(id = homepageList[it].title))
                        },
                        trailingContent = {
                            if (selected.route == homepageList[it].route) {
                                Icon(Icons.Filled.Check, contentDescription = "Selected")
                            }
                        }
                    )
                }
            }
        }
    }
}