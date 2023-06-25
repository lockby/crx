package com.crstlnz.komikchino.data.api.source

import com.crstlnz.komikchino.data.model.Chapter
import com.crstlnz.komikchino.data.model.ChapterModel
import com.crstlnz.komikchino.data.model.HomeData
import com.crstlnz.komikchino.data.model.KomikDetail
import com.crstlnz.komikchino.data.model.SearchQuery

interface ScraperBase {
    suspend fun getHome(): HomeData
    suspend fun search(query: String, page: Int = 1): SearchQuery
    suspend fun getDetailKomik(slug: String): KomikDetail
//    suspend fun getDetailKomik(id: Int): KomikDetail
    suspend fun getChapterList(id: String): List<Chapter>
    suspend fun getChapter(id: String): ChapterModel
}