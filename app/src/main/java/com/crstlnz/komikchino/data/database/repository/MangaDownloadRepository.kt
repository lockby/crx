package com.crstlnz.komikchino.data.database.repository

import androidx.lifecycle.LiveData
import com.crstlnz.komikchino.data.database.dao.ChapterDownloadDao
import com.crstlnz.komikchino.data.database.dao.MangaDownloadDao
import com.crstlnz.komikchino.data.database.model.ChapterDownloadItem
import com.crstlnz.komikchino.data.database.model.ChapterImages
import com.crstlnz.komikchino.data.database.model.ChapterImagesDownload
import com.crstlnz.komikchino.data.database.model.MangaChapterDownload
import com.crstlnz.komikchino.data.database.model.MangaDownloadItem

//import com.crstlnz.komikchino.data.database.model.MangaChapterDownload

class MangaDownloadRepository(
    private val mangaDownloadDao: MangaDownloadDao,
    private val chapterDownloadDao: ChapterDownloadDao
) {
    val downloads: LiveData<List<MangaChapterDownload>> = mangaDownloadDao.readDownloads()
//    suspend fun add(komikHistory: KomikHistoryItem) {
//        komikDao.insert(komikHistory)
//    }

    suspend fun getDownload(id: String): MangaChapterDownload? {
        return mangaDownloadDao.getDownload(id)
    }

    suspend fun getDownloads(): List<MangaChapterDownload> {
        return mangaDownloadDao.getDownloads()
    }

    fun readDownloadData(id: String): LiveData<MangaChapterDownload> {
        return mangaDownloadDao.readDownload(id)
    }

    fun readDownloadDataBySlug(slug: String): LiveData<MangaChapterDownload> {
        return mangaDownloadDao.readDownloadBySlug(slug)
    }

    suspend fun getChapterImages(id: String): ChapterImagesDownload {
        return mangaDownloadDao.getChapterImages(id)
    }

    suspend fun addChapterImages(chapterImages: ChapterImages) {
        return mangaDownloadDao.insertChapterImages(chapterImages)
    }

    suspend fun addDownload(
        manga: MangaDownloadItem,
        chapters: List<ChapterDownloadItem>
    ): Boolean {
        return try {
            mangaDownloadDao.insert(manga)
            chapterDownloadDao.insertAll(chapters)
            true
        } catch (e: Exception) {
            false
        }
    }
}