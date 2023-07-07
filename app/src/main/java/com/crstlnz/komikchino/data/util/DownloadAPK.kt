package com.crstlnz.komikchino.data.util

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import android.widget.Toast
import androidx.core.net.toUri
import java.io.File


@SuppressLint("QueryPermissionsNeeded")
fun Context.installAPK(apkUri: Uri) {
    val installIntent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(apkUri, "application/vnd.android.package-archive")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    if (installIntent.resolveActivity(packageManager) != null) {
        startActivity(installIntent)
    } else {
        Toast.makeText(this, "Unable to find an app to install the APK", Toast.LENGTH_SHORT)
            .show()
    }
}


fun Context.downloadApk(
    url: String,
    fileName: String,
    onDownloadFinish: () -> Unit = {},
    onReceiverCreated: (BroadcastReceiver) -> Unit = {}
) {
    val directory = Environment.DIRECTORY_DOWNLOADS
    val file = File(directory, fileName)
    if (file.exists()) {
        onDownloadFinish()
        Toast.makeText(this, "Installing", Toast.LENGTH_SHORT).show()
        val apkUri = file.toUri()
        installAPK(apkUri)
    } else {
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle(fileName)
            .setMimeType("application/vnd.android.package-archive")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            .setDestinationInExternalPublicDir(directory, fileName)
        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)
        Toast.makeText(this, "Downloading $fileName", Toast.LENGTH_SHORT).show()
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val action = intent?.action
                if (action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
                    val downloadIdComplete =
                        intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    if (downloadId == downloadIdComplete) {
                        onDownloadFinish()
                        Toast.makeText(context, "Installing", Toast.LENGTH_SHORT).show()
                        val apkUri = downloadManager.getUriForDownloadedFile(downloadIdComplete)
                        installAPK(apkUri)
                        unregisterReceiver(this)
                    }
                }
            }
        }

        onReceiverCreated(receiver)
        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        registerReceiver(receiver, filter)
    }
}

fun getInternalStorageDirectoryPath(context: Context): String? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val storageManager: StorageManager =
            context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
        storageManager.primaryStorageVolume.directory?.absolutePath
    } else {
        Environment.getExternalStorageDirectory().absolutePath
    }
}