package com.crstlnz.komikchino.ui.screens.komikdetail

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.crstlnz.komikchino.data.api.KomikAPI
import com.crstlnz.komikchino.data.api.KomikScrapeAPI
import com.crstlnz.komikchino.data.api.KomikClient
import com.crstlnz.komikchino.data.api.source.Kiryuu
import com.crstlnz.komikchino.data.database.readhistory.model.ReadHistoryDao
import com.crstlnz.komikchino.data.database.readhistory.model.ReadHistoryItem
import com.crstlnz.komikchino.data.model.Chapter
import com.crstlnz.komikchino.data.model.DataState
import com.crstlnz.komikchino.data.model.Genre
import com.crstlnz.komikchino.data.model.KomikDetail
import com.crstlnz.komikchino.data.model.SearchItem
import com.crstlnz.komikchino.data.model.SimilarTitle
import com.crstlnz.komikchino.data.model.State
import com.crstlnz.komikchino.data.util.StorageHelper
import com.crstlnz.komikchino.data.util.getIdFromUrl
import com.crstlnz.komikchino.data.util.getLastPathSegment
import com.crstlnz.komikchino.data.util.parseRelativeDate
import com.crstlnz.komikchino.ui.util.ViewModelBase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class KomikViewModelFactory(
    private val storage: StorageHelper<KomikDetail>,
    private val slug: String,
    private val database: ReadHistoryDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return KomikViewModel(storage, slug, database) as T
    }
}

class KomikViewModel(
    storage: StorageHelper<KomikDetail>,
    private val slug: String,
    private val database: ReadHistoryDao
) : ViewModelBase<KomikDetail>(
    storage, true
) {
    override var cacheKey = "komik-${slug}"
    private val api: Kiryuu = Kiryuu()
    private val _isReversed = MutableStateFlow<Boolean>(false)

    var chapterReadHistory: LiveData<List<ReadHistoryItem>> =
        MutableLiveData<List<ReadHistoryItem>>().apply { value = listOf() }

    val isReversed: StateFlow<Boolean> = _isReversed.asStateFlow()
    fun toggleReversed() {
        _isReversed.update {
            !_isReversed.value
        }
    }

    init {
        load(false)
        viewModelScope.launch {
            _state.collect {
                if (State.DATA == it.state) {
                    chapterReadHistory = database.getKomikHistory(it.data?.id?.toLong() ?: 0L)
                }
            }
        }
    }

    override suspend fun fetchData(): KomikDetail {
        return api.getDetailKomik(slug)
//        val body = scraper.getKomikPage(slug)
//        Log.d("FETCHING", slug)
//        val document: Document = Jsoup.parse(body.string())
//        val link =
//            document.selectFirst("link[rel=\"alternate\"][type=\"application/json\"]")?.attr("href")
//                ?: ""
//        val id = if (link.isNotEmpty()) (getLastPathSegment(link)?.toIntOrNull()
//            ?: 0) else (document.selectFirst(".bokfav")?.attr("data-post-id")?.toIntOrNull() ?: 0)
//
//        val type = document.selectFirst(".infox .spe a")?.text() ?: "Error"
//
//        val komiDetail = api.getKomikDetail(id)
//        val img = document.selectFirst(".infoanime .thumb img")?.attr("src") ?: ""
//        val score =
//            document.selectFirst(".ratingmanga .archiveanime-rating i")?.text()?.toFloatOrNull()
//                ?: 0f
//        val genres = document.select(".infox .genre-info a")
//        val genreList = arrayListOf<Genre>()
//
//
//        for (genre in genres) {
//            genreList.add(
//                Genre(
//                    title = genre.text(), slug = getLastPathSegment(genre.attr("href")) ?: ""
//                )
//            )
//        }
//
//        val similarList = arrayListOf<SimilarTitle>()
//        val similars = document.select("#mirip ul li")
//
//        for (similar in similars) {
//            val typegenre =
//                (similar.selectFirst(".imgseries .extras")?.text() ?: "").trim().split(" ")
//            val slug = similar.selectFirst(".imgseries a")?.attr("href") ?: ""
//            similarList.add(
//                SimilarTitle(
//                    title = similar.selectFirst(".leftseries .series")?.text() ?: "",
//                    img = (similar.selectFirst(".imgseries .series img")?.attr("src")
//                        ?: "").split("?").getOrNull(0) ?: "",
//                    type = typegenre.getOrNull(0)?.trim() ?: "",
//                    genre = typegenre.getOrNull(1)?.trim() ?: "",
//                    isColored = similar.selectFirst(".imgseries .warnalabel") != null,
//                    slug = getLastPathSegment(slug) ?: ""
//                )
//            )
//        }
//
//        val chapterList = arrayListOf<Chapter>()
//        val chapters = document.select("#chapter_list ul li")
//
//        for (chapter in chapters) {
//            chapterList.add(
//                Chapter(
//                    title = chapter.selectFirst(".lchx")?.text()?.trim() ?: "",
//                    date = parseRelativeDate(chapter.selectFirst(".dt")?.text()?.trim() ?: ""),
//                    id = getIdFromUrl(
//                        chapter.selectFirst(".dl a")?.attr("href")?.trim() ?: ""
//                    )?.toIntOrNull(),
//                    slug = getLastPathSegment(chapter.selectFirst(".lchx a")?.attr("href") ?: "")
//                        ?: ""
//                )
//            )
//        }
//
//
//        return KomikDetail(
//            title = komiDetail.title?.rendered ?: "",
//            description = komiDetail.content?.rendered ?: "",
//            id = id,
//            img = img,
//            score = score,
//            genre = genreList,
//            type = type,
//            similar = similarList,
//            chapters = chapterList
//        )


    }

}