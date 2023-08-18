package com.crstlnz.komikchino.services

import android.app.Application
import android.os.Environment
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import com.crstlnz.komikchino.data.model.MangaDownload
import com.crstlnz.komikchino.ui.util.DOWNLOAD_MANAGER_START
import com.crstlnz.komikchino.ui.util.downloadManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

enum class DownloadState {
    STARTED, IDLE
}

class DownloadViewModel(private val application: Application) : AndroidViewModel(application) {
    private val _countState: MutableStateFlow<Int> = MutableStateFlow(0)
    val countState = _countState.asStateFlow()
    val pendingList = mutableStateListOf<List<MangaDownload>>()
    val downloadedList = mutableStateListOf<List<MangaDownload>>()

    fun increment() {
        _countState.update {
            it + 1
        }
    }


    fun startDownload() {
        if (pendingList.size > 0) {
            application.downloadManager(DOWNLOAD_MANAGER_START)
        }
    }

    private suspend fun downloadAndSaveImage(url: String, filename: String) {
        withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url(url)
                    .build()
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body
                    responseBody?.let { body ->
                        val dir = File(
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                            "/komika"
                        )
                        dir.mkdirs()
                        val file = File(
                            dir,
                            "/${filename}"
                        )
                        FileOutputStream(file).use { outputStream ->
                            outputStream.write(body.bytes())
                        }
                    }
//                    downloadedImgs.add(url)
//                    downloadViewModel.increment()
                } else {
//                    downloadViewModel.increment()
//                    failedDownloads.add(url)
                }
            } catch (e: Exception) {
//                failedDownloads.add(url)
//                downloadViewModel.increment()
                e.printStackTrace()
            }
        }
    }
}