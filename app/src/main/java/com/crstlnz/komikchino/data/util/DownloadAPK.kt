package com.crstlnz.komikchino.data.util

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.widget.Toast
import androidx.core.net.toUri
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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


@OptIn(DelicateCoroutinesApi::class)
fun Context.downloadApk(
    url: String,
    fileName: String,
    onDownloadFinish: () -> Unit = {},
    onReceiverCreated: (BroadcastReceiver) -> Unit = {},
    onProgressChange: (Float) -> Unit = {}
) {
//    val directory = Environment.DIRECTORY_DOWNLOADS
    val directory = if (SDK_INT > Build.VERSION_CODES.O) {
        kotlin.io.path.createTempFile(fileName, null).parent.toString()
    } else {
        createTempFile(fileName, null).name
    }

    var isFinish = false;
    var downloadProgress: Float? = null
    fun finish() {
        isFinish = true
        downloadProgress = null
        onDownloadFinish()
    }

    val file = File(directory, fileName)
    if (file.exists()) {
        finish()
        val apkUri = file.toUri()
        installAPK(apkUri)
    } else {
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle(fileName)
            .setMimeType("application/vnd.android.package-archive")
            .setDescription("Downloading updates...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            .setAllowedOverMetered(true)
            .setRequiresCharging(false)
            .setAllowedOverRoaming(true)
            .setDestinationInExternalFilesDir(this, directory, fileName)
        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val action = intent?.action
                if (action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
                    val downloadIdComplete =
                        intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    if (downloadId == downloadIdComplete) {
                        finish()
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

        val updateInterval = 1000L // Interval in milliseconds
        val context = this

        GlobalScope.launch(Dispatchers.Main) {
            while (!isFinish) {
                val progress = getDownloadProgress(context, downloadId)
                if (progress != downloadProgress) {
                    downloadProgress = progress
                    onProgressChange(progress)
                }
                println("Download Progress: $progress%")
                delay(updateInterval)
            }
        }
    }
}

@SuppressLint("Range")
fun getDownloadProgress(context: Context, downloadId: Long): Float {
    try {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query().apply {
            setFilterById(downloadId)
        }

        val cursor = downloadManager.query(query)
        cursor?.use {
            if (it.moveToFirst()) {
                val downloadedBytes =
                    it.getLong(it.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                val totalBytes =
                    it.getLong(it.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                if (totalBytes != -1L) {
                    return (downloadedBytes * 100F / totalBytes)
                }
            }
        }
        return 0f
    } catch (_: Exception) {
        return 0f
    }
}