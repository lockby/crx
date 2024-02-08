package com.crstlnz.komikchino.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_DEFAULT
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.crstlnz.komikchino.R
import com.crstlnz.komikchino.config.AppSettings
import com.crstlnz.komikchino.data.util.MangaDownloadManager
import dagger.hilt.android.AndroidEntryPoint
import kotlin.random.Random.Default.nextInt


const val INTENT_COMMAND = "Command"
const val INTENT_COMMAND_EXIT = "Exit"
const val INTENT_COMMAND_START_DOWNLOAD = "StartDownload"

private const val NOTIFICATION_CHANNEL_GENERAL = "Download Manager Notification"
private const val CODE_FOREGROUND_SERVICE = 1
private const val CODE_CLOSE_INTENT = 2

@AndroidEntryPoint
class DownloadManagerService : Service() {
    override fun onBind(p0: Intent?): IBinder? = null
    private lateinit var mangaDownloadManager: MangaDownloadManager

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val command = intent.getStringExtra(INTENT_COMMAND)
        if (command == INTENT_COMMAND_EXIT) {
            stopService()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        AppSettings.downloadServiceRunning = false
        mangaDownloadManager.destroy()
        Log.d("DOWNLOAD SERVICE", "ON STOP")
    }

    override fun onCreate() {
        Log.d("DOWNLOAD SERVICE", "ON CREATE")
        super.onCreate()
        AppSettings.downloadServiceRunning = true
        mangaDownloadManager = MangaDownloadManager(
            onNotification = { title, downloadName, currentIndex, totalDownload ->
                showProgressNotification(title, downloadName, currentIndex, totalDownload)
            },
            onFinish = {
                Log.d("DOWNLOAD SERVICE", "FINISH SERVICE")
                showFinishNotification()
            }
        )
    }

    private var destroyed = false
    private fun stopService() {
        if (!destroyed) {
            destroyed = true
            stopSelf()
            stopForeground(STOP_FOREGROUND_DETACH)
        }
    }

    private fun setupNotificationChannel() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                with(
                    NotificationChannel(
                        NOTIFICATION_CHANNEL_GENERAL,
                        "Download Chapter",
                        IMPORTANCE_DEFAULT
                    )
                ) {
                    enableLights(false)
                    setShowBadge(false)
                    enableVibration(false)
                    setSound(null, null)
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                    manager.createNotificationChannel(this)
                }
            } catch (e: Exception) {
                Log.d("Error", "showNotification: ${e.localizedMessage}")
            }
        }
    }

    private var isFirstNotification = true
    private fun showProgressNotification(
        title: String = "Download Chapter",
        downloadName: String = "Chapter",
        currentIndex: Int,
        totalDownload: Int
    ) {
        setupNotificationChannel()
        val closeIntent = Intent(this, DownloadManagerService::class.java).apply {
            putExtra(INTENT_COMMAND, INTENT_COMMAND_EXIT)
        }
        val closePendingIntent = PendingIntent.getService(
            this, CODE_CLOSE_INTENT, closeIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        with(
            NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_GENERAL)
        ) {
            setTicker(null)
            setContentTitle(title)
            setContentText(downloadName)
            setProgress(
                totalDownload,
                currentIndex,
                false
            )
            setAutoCancel(false)
            setOngoing(true)
            setWhen(System.currentTimeMillis())
            setSmallIcon(android.R.drawable.stat_sys_download)
            priority = if (isFirstNotification) IMPORTANCE_HIGH else IMPORTANCE_DEFAULT
//                setContentIntent(replyPendingIntent)
//                addAction(
//                    0, "REPLY", replyPendingIntent
//                )
            addAction(
                0, "STOP", closePendingIntent
            )
            startForeground(CODE_FOREGROUND_SERVICE, build())
        }
        isFirstNotification = false
    }

    private fun showFinishNotification() {
        with(
            NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_GENERAL)
        ) {
            setTicker(null)
            setContentTitle("Download Chapter")
            setContentText("Completed downloading chapter images!")
            setAutoCancel(true)
            setOngoing(false)
            setWhen(System.currentTimeMillis())
            setSmallIcon(R.drawable.ic_app_icon)
            priority = IMPORTANCE_DEFAULT
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(nextInt(100000), build())
        }
        stopService()
    }
}