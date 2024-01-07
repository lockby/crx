package com.crstlnz.komikchino.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.crstlnz.komikchino.data.database.model.ChapterImages
import com.crstlnz.komikchino.data.database.model.ChapterImagesDownload
import com.crstlnz.komikchino.data.database.model.MangaChapterDownload
import com.crstlnz.komikchino.data.database.model.MangaDownloadItem

@Dao
interface MangaDownloadDao {
    @Transaction
    @Query("SELECT * from mangaDownload ORDER BY createdAt DESC")
    fun readDownloads(): LiveData<List<MangaChapterDownload>>

    @Transaction
    @Query("SELECT * from mangaDownload WHERE id = :id")
    fun readDownload(id: String): LiveData<MangaChapterDownload>

    @Query("SELECT * from mangaDownload WHERE id = :id")
    suspend fun getDownload(id: String): MangaChapterDownload?

    @Query("SELECT * from mangaDownload")
    suspend fun getDownloads(): List<MangaChapterDownload>

    @Query("SELECT * from chapterDownload WHERE id = :id")
    suspend fun getChapterImages(id: String): ChapterImagesDownload

    @Transaction
    @Query("SELECT * from mangaDownload WHERE slug = :slug")
    fun readDownloadBySlug(slug: String): LiveData<MangaChapterDownload>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(manga: MangaDownloadItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapterImages(chapterImages: ChapterImages)
}