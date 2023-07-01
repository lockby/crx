package com.crstlnz.komikchino.ui.screens.permissions

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.crstlnz.komikchino.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun WriteExternalStorageScreen(onDismiss: () -> Unit = {}) {
    val context = LocalContext.current
    val storagePermission = rememberPermissionState(
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    val isGranted = storagePermission.status.isGranted
    LaunchedEffect(isGranted) {
        if (storagePermission.status.isGranted) {
            onDismiss()
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
                    Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logistics),
                        modifier = Modifier.fillMaxWidth(0.85f),
                        contentDescription = "Storage Illustration"
                    )

                    Spacer(Modifier.height(50.dp))
                    Text(
                        "Storage Permission",
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(30.dp))
                    Text(
                        "Storage permission is required to download and install the latest features and improvements.",
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(30.dp))


                Button(modifier = Modifier.fillMaxWidth(), onClick = {
                    if (storagePermission.status.shouldShowRationale) {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", context.packageName, null)
                        intent.data = uri;
                        context.startActivity(intent)
                        Toast.makeText(
                            context,
                            "Please enable storage permission!",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        storagePermission.launchPermissionRequest()
                    }
                }) {
                    Text(
                        "Allow",
                        modifier = Modifier.padding(vertical = 8.dp),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
                Spacer(Modifier.height(20.dp))
            }

            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 15.dp)
                    .statusBarsPadding(),
                contentAlignment = Alignment.TopEnd
            ) {
                FilledTonalButton(
                    onClick = { onDismiss() }, contentPadding = PaddingValues(18.dp, 0.dp)
                ) {
                    Text(
                        "Skip",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }
    }
}