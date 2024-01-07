package com.crstlnz.komikchino.data.util

import android.util.Log
import androidx.lifecycle.Observer
import com.crstlnz.komikchino.config.AppSettings
import com.crstlnz.komikchino.data.database.model.ChapterDownloadItem
import com.crstlnz.komikchino.data.database.model.ChapterImages
import com.crstlnz.komikchino.data.database.model.DownloadState
import com.crstlnz.komikchino.data.database.model.MangaChapterDownload
import com.crstlnz.komikchino.data.model.ChapterApi
import com.crstlnz.komikchino.services.DownloadViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DownloadNotification(
    val title: String,
    val description: String,
    val progress: Int,
    val progressTotal: Int,
)

class MangaDownloadManager(
    val onNotification: (title: String, downloadName: String, currentIndex: Int, totalDownload: Int) -> Unit,
    val onFinish: () -> Unit
) {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private val downloadViewModel = AppSettings.downloadViewModel
    private var downloadList: List<MangaChapterDownload> = listOf()
    private var collectJob: Job? = null
    private var observeJob: Job? = null

    private val downloadObserver = Observer<List<MangaChapterDownload>> { downloadList ->
        this.downloadList = downloadList
        val currentDownload = downloadViewModel.currentDownload.value
        if (currentDownload != null) {
            val mangaChapter = downloadList.find { it.manga.id == currentDownload.data.manga.id }
            if (mangaChapter == null || mangaChapter.chapters.isEmpty()) {
                // the manga is deleted from download list
                currentDownload.stop()
            } else {
                onNotification("Testing title", "Chapter 100", 1, downloadList.size)
            }
        }
    }

    init {
        Log.d("DOWNLOAD SERVICE", "INIT DOWNLOAD MANAGER")
        collectJob?.cancel()
        observeJob?.cancel()
        observeJob = scope.launch(Dispatchers.Main) {
            downloadViewModel.downloadList.observeForever(downloadObserver)
        }
        collectJob = scope.launch(Dispatchers.IO) {
            downloadList = downloadViewModel.getDownloads()
            Log.d("DOWNLOAD SERVICE", "DOWNLOAD LIST DATA ${downloadList.size}")
            if (downloadList.isEmpty()) {
                Log.d("DOWNLOAD SERVICE", "EMPTY DOWNLOAD DATA")
                onFinish()
            } else {
                Log.d("DOWNLOAD SERVICE", "DOWNLOAD ADA")
                download()
            }
        }
    }

    var index = 0
    suspend fun download(str: String = "kucing") {
        Log.d("DOWNLOAD SERVICE", "DOWNLOAD CALLED $str")
        val downloadData = downloadList.getOrNull(index)
        if (downloadData != null) {
            Log.d("DOWNLOAD SERVICE", downloadData.manga.title)
            downloadViewModel.setCurrentDownload(downloadData)
            downloadViewModel.currentDownload.value?.download(onChapterChange = {
                onNotification(
                    downloadData.manga.title, it.title, 1, 4
                )
                downloadViewModel.setDescription("Downloading ${it.title}")
            })
            Log.d("DOWNLOAD SERVICE", "DOWNLOAD FUN SELESAI")
            index += 1
            download("waewewe download boisssss")
        } else {
            Log.d("DOWNLOAD SERVICE", "EMPTY DOWNLOAD DATA in download")
            onFinish()
        }
    }

//    fun onDownload

    fun destroy() {
        collectJob?.cancel()
        observeJob?.cancel()
        downloadViewModel.downloadList.removeObserver(downloadObserver)
        downloadViewModel.clearCurrentDownload()
        downloadViewModel.clearDescription()
    }
}

class CurrentDownloadManager(
    val data: MangaChapterDownload,
    val viewModel: DownloadViewModel,
) {
    private var downloadIndex = 0
    fun stop() {

    }

    suspend fun download(onChapterChange: (chapter: ChapterDownloadItem) -> Unit) {
        val chapter = data.chapters.getOrNull(downloadIndex) ?: return
        onChapterChange(chapter)
        val chapterImages = viewModel.mangaDownloadRepository.getChapterImages(chapter.id)
        Log.d("DOWNLOAD SERVICE", "Download Chapter : ${chapter.title}")
        Log.d("DOWNLOAD SERVICE", "Chapter Images Count: ${chapterImages.images.size}")
        Log.d("DOWNLOAD SERVICE", "Chapter Images: ${chapterImages.images.getOrNull(0).toString()}")
        if (chapterImages.images.isEmpty()) {
            try {
                Log.d("DOWNLOAD SERVICE", "Fetching Chapter : ${chapter.title}")
                val chapterData = getChapterData(chapter)
                Log.d("DOWNLOAD SERVICE", "Fetched Images : ${chapterData.imgs.size}")
                for ((index, url) in chapterData.imgs.withIndex()) {
                    viewModel.mangaDownloadRepository.addChapterImages(
                        ChapterImages(
                            id = chapterData.id,
                            index = index,
                            url = url,
                            state = DownloadState.PENDING
                        )
                    )
                }
            } catch (e: Exception) {
                Log.d("DOWNLOAD SERVICE", "Error : ${e.stackTraceToString()}")
                // TODO handle error
            }
        } else {
            Log.d("DOWNLOAD SERVICE", "Chapter Already Fetched : ${chapter.title}")
        }
        withContext(Dispatchers.IO) {
            Thread.sleep(100)
        }
        downloadIndex += 1
        download(onChapterChange)
    }

    private suspend fun getChapterData(
        chapter: ChapterDownloadItem,
        tryCount: Int = 0
    ): ChapterApi {
        return try {
            viewModel.api.getChapter(chapter.id)
        } catch (e: Exception) {
            if (tryCount < 3) {
                getChapterData(chapter, (tryCount + 1))
            } else {
                throw e
            }
        }
    }
}