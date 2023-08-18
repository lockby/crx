package com.crstlnz.komikchino.ui.util

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.crstlnz.komikchino.services.DownloadManagerService
import com.crstlnz.komikchino.services.INTENT_COMMAND

const val DOWNLOAD_MANAGER_START = "Start"
const val DOWNLOAD_MANAGER_STOP = "Stop"
fun Context.downloadManager(command: String) {
    val intent = Intent(this, DownloadManagerService::class.java)
    if (command == DOWNLOAD_MANAGER_START) {
        intent.putExtra(INTENT_COMMAND, command)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.startForegroundService(intent)
        } else {
            this.startService(intent)
        }
    } else if (command == DOWNLOAD_MANAGER_STOP) {
        intent.putExtra(INTENT_COMMAND, command)
        this.stopService(intent)
    }
}