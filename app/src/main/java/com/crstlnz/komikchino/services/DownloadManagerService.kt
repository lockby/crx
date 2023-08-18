package com.crstlnz.komikchino.services

import android.annotation.SuppressLint
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
import android.os.Environment
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.crstlnz.komikchino.R
import com.crstlnz.komikchino.config.AppSettings
import com.crstlnz.komikchino.data.api.KomikServer
import com.crstlnz.komikchino.data.model.MangaDownload
import com.crstlnz.komikchino.ui.screens.latestupdate.LatestUpdateViewModel
import com.crstlnz.komikchino.ui.util.downloadManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.internal.notify
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.net.URL
import javax.inject.Inject


const val INTENT_COMMAND = "Command"
const val INTENT_COMMAND_EXIT = "Exit"
const val INTENT_COMMAND_FINISH = "Finish"
const val INTENT_COMMAND_START_DOWNLOAD = "StartDownload"

private const val NOTIFICATION_CHANNEL_GENERAL = "Download Manager"
private const val CODE_FOREGROUND_SERVICE = 1
private const val CODE_CLOSE_INTENT = 2

@AndroidEntryPoint
class DownloadManagerService : Service() {
    override fun onBind(p0: Intent?): IBinder? = null
    lateinit var downloadViewModel: DownloadViewModel
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private val imgs = arrayListOf(
        "https://images2.prokal.co/webkp/file/berita/2023/01/23/f37f7f4ad6acf24673357956dd65aa0f.jpg",
        "https://media.hitekno.com/thumbs/2022/02/22/31129-marsha-jkt48-valkyrie48/730x480-img-31129-marsha-jkt48-valkyrie48.jpg",
        "https://scontent.fpnk3-1.fna.fbcdn.net/v/t1.6435-9/94387682_538948243477250_5948457085652959232_n.jpg?stp=dst-jpg_s600x600&_nc_cat=107&ccb=1-7&_nc_sid=8bfeb9&_nc_eui2=AeGCFlpVT5KtpsH01NLCw4lu-4BvOcxj16L7gG85zGPXouzjvJo5WA0hc3cmWgBqwJj2LupiNjRsqlcB0qK5DvmD&_nc_ohc=uWb5d1VXQqAAX-6sLll&_nc_ht=scontent.fpnk3-1.fna&oh=00_AfDj-DsMvi2SlRhZvr_P0wI1ErepQfBebdXA0UMDUNCTYA&oe=65053D43",
        "https://scontent.fpnk3-1.fna.fbcdn.net/v/t1.6435-9/94423781_538948296810578_480798164035567616_n.jpg?_nc_cat=101&ccb=1-7&_nc_sid=8bfeb9&_nc_eui2=AeE6qofZ7rU6AU-XYA7qldjMK3jBg1zoeo0reMGDXOh6jfaM-Ij_6LAOvOnwq-xxaJmt461flj2VsgH-dKIwaqcN&_nc_ohc=r9MsGpVYFngAX-lfFkV&_nc_ht=scontent.fpnk3-1.fna&oh=00_AfAfpEEiDalKxRvWjpmuQkZ-J-9XjXgaBYc_1hJoHM-Hxw&oe=65051779",
        "https://cdn.idntimes.com/content-images/community/2022/01/20220111-084721-c6dc9e4f5b9b26fb6884e8e458e27dea-4f6d6d97bd1516d69971ff1480c97ee5_600x400.jpg",
        "https://cdn.idntimes.com/content-images/community/2022/01/20220111-085503-c6dc9e4f5b9b26fb6884e8e458e27dea-321dce71e85ecbdcb68f0a6d0c35fd30.jpg",
        "https://cdn.idntimes.com/content-images/post/20201120/img-20201120-094909-5bf3d5991fcb08fe482ef141e90858d9.jpg",
        "https://static.promediateknologi.id/crop/0x856:1076x1476/750x500/webp/photo/2022/01/28/562036812.jpg",
        "https://cdn.idntimes.com/content-images/post/20201120/img-20201120-094913-c7f155914797f502f0f0e3bab433a1e7.jpg",
        "https://pbs.twimg.com/media/EyQ07tFU8AcogwZ.jpg:large",
        "https://pbs.twimg.com/media/Et7ap1ZUUAAMTaf.jpg:large",
        "https://1.bp.blogspot.com/-w25zKqQoduc/XlrEV0dpb7I/AAAAAAAAA0c/3yOqasamxmckvI5WpqzW2EwidNYhWek8QCLcBGAsYHQ/s16000/IMG_20200301_030606_7.jpg",
    )

    private var downloadedImgs = mutableListOf<String>()
    private var failedDownloads = mutableListOf<String>()

    init {
        downloadViewModel = AppSettings.downloadViewModel
    }

    private var collectJob: Job? = null

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val command = intent.getStringExtra(INTENT_COMMAND)
        if (command == INTENT_COMMAND_EXIT) {
            stopService()
            return START_NOT_STICKY
        }
        if (command == INTENT_COMMAND_FINISH) {
            return START_NOT_STICKY
        }

        collectJob?.cancel()
        collectJob = scope.launch {
            downloadViewModel.pendingList.asFlow().collect {

            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        collectJob?.cancel()
        super.onDestroy()
    }

    private fun stopService() {
        stopForeground(STOP_FOREGROUND_DETACH)
        stopSelf()
    }

    private fun runStopCommand() {
        val intent = Intent(this, DownloadManagerService::class.java).apply {
            putExtra(INTENT_COMMAND, INTENT_COMMAND_EXIT)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.startForegroundService(intent)
        } else {
            this.startService(intent)
        }
    }

    @SuppressLint("LaunchActivityFromNotification")
    private fun showNotification() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val closeIntent = Intent(this, DownloadManagerService::class.java).apply {
            putExtra(INTENT_COMMAND, INTENT_COMMAND_EXIT)
        }


        val closePendingIntent = PendingIntent.getService(
            this, CODE_CLOSE_INTENT, closeIntent, PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                with(
                    NotificationChannel(
                        NOTIFICATION_CHANNEL_GENERAL,
                        "Download Chapter",
                        NotificationManager.IMPORTANCE_HIGH
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

        if (imgs.size == 0) {
            with(
                NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_GENERAL)
            ) {
                setTicker(null)
                setContentTitle("Download Chapter")
                setContentText("Completed download ${downloadedImgs.size} of ${downloadedImgs.size + imgs.size + failedDownloads.size} chapter images!")
                setAutoCancel(true)
                setOngoing(false)
                setWhen(System.currentTimeMillis())
                setSmallIcon(R.drawable.ic_app_icon)
                priority = IMPORTANCE_DEFAULT
                startForeground(CODE_FOREGROUND_SERVICE, build())
            }
            stopService()
        } else {
            with(
                NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_GENERAL)
            ) {
                setTicker(null)
                setContentTitle("Download Chapter")
//                setContentText("Downloading chapter $filenameIndex")
//                setProgress(
//                    downloadedImgs.size + imgs.size + failedDownloads.size,
//                    downloadedImgs.size + failedDownloads.size,
//                    false
//                )
                setAutoCancel(false)
                setOngoing(true)
                setWhen(System.currentTimeMillis())
                setSmallIcon(R.drawable.ic_app_icon)
                priority = IMPORTANCE_HIGH
//                setContentIntent(replyPendingIntent)
//                addAction(
//                    0, "REPLY", replyPendingIntent
//                )
//                addAction(
//                    0, "ACHIEVE", replyPendingIntent
//                )
                startForeground(CODE_FOREGROUND_SERVICE, build())
            }
        }
    }
}