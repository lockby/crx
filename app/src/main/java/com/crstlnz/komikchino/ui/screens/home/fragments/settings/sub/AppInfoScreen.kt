package com.crstlnz.komikchino.ui.screens.home.fragments.settings.sub

import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.crstlnz.komikchino.R

@Composable
fun AppInfoScreen() {
    val context = LocalContext.current
    val packageName = context.packageName
    val packageManager = context.packageManager
    val appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
    val pkgInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_META_DATA)
    val appName = packageManager.getApplicationLabel(appInfo).toString()
    val versionName = pkgInfo.versionName
    Scaffold {
        Surface(Modifier.padding(it)) {
            LazyColumn(Modifier.fillMaxSize()) {
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            painterResource(id = R.mipmap.ic_launcher),
                            null,
                            modifier = Modifier.fillMaxSize(0.5f)
                        )
                        Text(appName, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(10.dp))
                        Text(versionName, style = MaterialTheme.typography.titleSmall)
                    }
                }
            }
        }
    }
}