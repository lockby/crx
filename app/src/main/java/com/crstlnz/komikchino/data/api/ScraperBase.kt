package com.crstlnz.komikchino.data.api

import com.crstlnz.komikchino.data.model.Chapter
import com.crstlnz.komikchino.data.model.ChapterApi
import com.crstlnz.komikchino.data.model.Genre
import com.crstlnz.komikchino.data.model.GenreSearch
import com.crstlnz.komikchino.data.model.HomeData
import com.crstlnz.komikchino.data.model.KomikDetail
import com.crstlnz.komikchino.data.model.LatestUpdatePage
import com.crstlnz.komikchino.data.model.SearchResult
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

interface ScraperBase {
    suspend fun getHome(): HomeData
    suspend fun search(query: String, page: Int = 1): SearchResult
    suspend fun getLatestUpdate(page: Int = 1): LatestUpdatePage
    suspend fun getDetailKomik(slug: String): KomikDetail

    //    suspend fun getDetailKomik(id: Int): KomikDetail
    suspend fun getChapterList(id: String): List<Chapter>
    suspend fun getChapter(id: String): ChapterApi
    suspend fun getChapterBySlug(slug: String): ChapterApi
    suspend fun fetch(onFetch: suspend () -> ResponseBody): Document {
        return Jsoup.parse(onFetch().string())
    }

    suspend fun searchByGenre(genreList: List<Genre>, page: Int = 1): GenreSearch
}