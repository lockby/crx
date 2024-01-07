package com.crstlnz.komikchino.data.database.model

import androidx.room.Embedded
import androidx.room.Relation

data class MangaChapterDownload(
    @Embedded
    val manga: MangaDownloadItem,
    @Relation(
        parentColumn = "id",
        entityColumn = "mangaId"
    )
    val chapters: List<ChapterDownloadItem>
)

data class ChapterImagesDownload(
    @Embedded
    val chapter: ChapterDownloadItem,
    @Relation(
        parentColumn = "id",
        entityColumn = "id"
    )
    val images: List<ChapterImages>
)

data class MangaChapterImages(
    @Embedded
    val manga: MangaDownloadItem,
    @Relation(
        parentColumn = "id",
        entityColumn = "mangaId"
    )
    val chapters: List<ChapterImagesDownload>
)