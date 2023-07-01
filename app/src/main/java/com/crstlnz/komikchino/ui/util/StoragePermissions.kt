package com.crstlnz.komikchino.ui.util

import android.Manifest
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import androidx.compose.runtime.Composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun checkWriteExternalPermission(): Boolean {
    if (SDK_INT >= Build.VERSION_CODES.R) return true
    val storagePermission = rememberPermissionState(
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    return storagePermission.status.isGranted
}