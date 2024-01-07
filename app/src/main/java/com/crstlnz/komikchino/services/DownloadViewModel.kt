package com.crstlnz.komikchino.services

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.crstlnz.komikchino.config.AppSettings
import com.crstlnz.komikchino.data.api.ScraperBase
import com.crstlnz.komikchino.data.database.model.MangaChapterDownload
import com.crstlnz.komikchino.data.database.repository.MangaDownloadRepository
import com.crstlnz.komikchino.data.util.CurrentDownloadManager
import com.crstlnz.komikchino.ui.util.DOWNLOAD_MANAGER_START
import com.crstlnz.komikchino.ui.util.downloadManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

enum class DownloadState {
    STARTED, IDLE
}

@HiltViewModel
class DownloadViewModel @Inject constructor(
    private val application: Application,
    val mangaDownloadRepository: MangaDownloadRepository,
    val api: ScraperBase,
) : AndroidViewModel(application) {
    private val _countState: MutableStateFlow<Int> = MutableStateFlow(0)
    val countState = _countState.asStateFlow()
    val downloadList = mangaDownloadRepository.downloads
    private val _currentDownload: MutableStateFlow<CurrentDownloadManager?> = MutableStateFlow(null)
    var currentDownload = _currentDownload.asStateFlow()
    private val _currentDescription: MutableStateFlow<String?> = MutableStateFlow(null)
    var currentDescription = _currentDescription.asStateFlow()
    fun setDescription(description: String) {
        _currentDescription.update {
            description
        }
    }

    fun clearDescription() {
        _currentDescription.update {
            null
        }
    }

    fun setCurrentDownload(chapterDownload: MangaChapterDownload) {
        _currentDownload.update {
            CurrentDownloadManager(
                chapterDownload, this
            )
        }
    }

    fun clearCurrentDownload() {
        _currentDownload.update {
            null
        }
    }

    suspend fun getDownloads(): List<MangaChapterDownload> {
        return mangaDownloadRepository.getDownloads()
    }

    //    val pendingList = mutableStateListOf<List<MangaDownload>>()
//    val downloadedList = mutableStateListOf<List<MangaDownload>>()
//    val downloadSelect = mutableStateListOf<List<ChapterDataDownload>>()
    private val downloadObserver = Observer<List<MangaChapterDownload>> { _ ->
        viewModelScope.launch {
            val downloads = getDownloads()
            // TODO CHECK IF HAS PENDING DOWNLOAD, IF NO PENDING DON'T RUN SERVICE
            if (downloads.isNotEmpty()) {
                if (!AppSettings.downloadServiceRunning) {
                    application.downloadManager(DOWNLOAD_MANAGER_START)
                }
            }
        }
    }

    init {
//        AppSettings.downloadViewModel = this
        downloadList.observeForever(downloadObserver)
    }

    override fun onCleared() {
        super.onCleared()
        downloadList.removeObserver(downloadObserver)
    }

    fun increment() {
        _countState.update {
            it + 1
        }
    }

    fun startDownload() {
//        if (pendingList.size > 0) {
//            application.downloadManager(DOWNLOAD_MANAGER_START)
//        }
    }

    private suspend fun downloadAndSaveImage(url: String, filename: String): Boolean {
        try {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body
                responseBody?.let { body ->
                    File(AppSettings.downloadDir).mkdirs()
                    FileOutputStream(
                        File(
                            AppSettings.downloadDir, "/${filename}"
                        )
                    ).use { outputStream ->
                        outputStream.write(body.bytes())
                    }
                }
                return true
            } else {
                return false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}