package com.crstlnz.komikchino.data.api.source

import android.util.Log
import com.crstlnz.komikchino.data.api.KomikClient
import com.crstlnz.komikchino.data.api.ScraperBase
import com.crstlnz.komikchino.data.model.Chapter
import com.crstlnz.komikchino.data.model.ChapterApi
import com.crstlnz.komikchino.data.model.ChapterUpdate
import com.crstlnz.komikchino.data.model.FeaturedComic
import com.crstlnz.komikchino.data.model.Genre
import com.crstlnz.komikchino.data.model.HomeData
import com.crstlnz.komikchino.data.model.KomikDetail
import com.crstlnz.komikchino.data.model.LatestUpdate
import com.crstlnz.komikchino.data.model.LatestUpdatePage
import com.crstlnz.komikchino.data.model.SearchResult
import com.crstlnz.komikchino.data.model.Section
import com.crstlnz.komikchino.data.model.SectionComic
import com.crstlnz.komikchino.data.model.SimilarTitle
import com.crstlnz.komikchino.data.util.getLastPathSegment
import com.crstlnz.komikchino.data.util.getLastTwoSegments
import com.crstlnz.komikchino.data.util.parseDateString
import com.crstlnz.komikchino.data.util.parseMangaKatanaChapterImages
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.util.Date
import java.util.Locale

class Mangakatana : ScraperBase {
    private val api = KomikClient.getMangaKatanaClient()
    override suspend fun getHome(): HomeData {
        val body = api.getHome()
        val document = Jsoup.parse(body.string())
        val featureds = document.select("#book_list .item")
        val featuredList = arrayListOf<FeaturedComic>()
        for (featured in featureds) {
            val url = featured.selectFirst(".media .wrap_img a")?.attr("href") ?: ""
            // mangakatana tak ade genre di home
            val genreList = arrayListOf<Genre>()
            val genres = featured.select(".genres a")
            for (genre in genres) {
                genreList.add(
                    Genre(
                        title = genre.text() ?: "",
                        url = genre.attr("href") ?: "",
                        slug = getLastPathSegment(genres.attr("href") ?: "") ?: ""
                    )
                )
            }
            featuredList.add(
                FeaturedComic(
                    title = featured.selectFirst(".text .title a")?.text()?.trim() ?: "",
                    url = url,
                    description = featured.selectFirst(".text .summary")?.text()?.trim() ?: "",
                    genre = genreList,
                    type = "",
                    img = featured.selectFirst(".media .wrap_img img")?.attr("src") ?: "",
                    slug = getLastPathSegment(url) ?: "",
                    score = null
                )
            )
        }

        val sectionList = arrayListOf<SectionComic>()
        val comics = document.select("#hot_update .slick_book li")

        for (comic in comics) {
            val url = comic.selectFirst(".wrap_img a")?.attr("href") ?: ""
            sectionList.add(
                SectionComic(
                    title = comic.selectFirst(".title")?.text()?.trim() ?: "",
                    url = url,
                    type = "", // mangakatana tak ade
                    img = comic.selectFirst(".wrap_img img")?.attr("src") ?: "",
                    slug = getLastPathSegment(url) ?: "",
                    score = null, // tak ade juge
                    chapterString = comic.selectFirst(".chapter")?.text()?.trim() ?: ""
                )
            )
        }

        val section = Section(
            title = document.selectFirst("#hot_update .heading")?.text()?.trim()
                ?.split(" ")?.joinToString(" ") { it.capitalize(Locale.ROOT) } ?: "",
            list = sectionList
        )

        return HomeData(
            featured = featuredList,
            sections = listOf(section)
        )
    }

    override suspend fun getLatestUpdate(page: Int): LatestUpdatePage {
        val body = api.getLatestUpdate(page)
        val document = Jsoup.parse(body.string())
        val latestUpdateElemets = document.select("#book_list .item")
        val latestUpdate = arrayListOf<LatestUpdate>()

        for (latest in latestUpdateElemets) {
            val url = latest.selectFirst(".title a")?.attr("href") ?: ""
            val chapterElements = latest.select(".chapters .chapter")
            val chapters = arrayListOf<ChapterUpdate>()

            val dates = latest.select(".chapters .update_time")
            val updateTime = arrayListOf<Date?>()

            for (date in dates) {
                updateTime.add(
                    parseDateString(
                        date.text()?.trim() ?: "",
                        "MMM-dd-yyyy",
                        Locale.ENGLISH
                    )
                )
            }

            for ((index, chapter) in chapterElements.withIndex()) {
                val cUrl = chapter.selectFirst("a")?.attr("href") ?: ""
                chapters.add(
                    ChapterUpdate(
                        title = chapter.selectFirst("a")?.text()?.trim() ?: "",
                        slug = getLastPathSegment(cUrl) ?: "",
                        url = cUrl,
                        date = updateTime.getOrNull(index)
                    )
                )
            }
            latestUpdate.add(
                LatestUpdate(
                    title = latest.selectFirst(".title a")?.text() ?: "",
                    img = latest.selectFirst(".wrap_img img")?.attr("src") ?: "",
                    description = latest.selectFirst(".summary")?.text() ?: "",
                    slug = getLastPathSegment(url) ?: "",
                    url = url,
                    chapters = chapters
                )
            )
        }

        val hasNext = document.selectFirst(".uk-pagination .next") != null
        return LatestUpdatePage(
            page = page,
            result = latestUpdate,
            hasNext = hasNext
        )
    }

    override suspend fun search(query: String, page: Int): SearchResult {
        val body = api.search(page, query)
        val document = Jsoup.parse(body.string())
        val book = document.selectFirst("#single_book")
        if (book != null) {
            val url = document.selectFirst("link[rel='canonical']")?.attr("href") ?: ""
            return SearchResult.ExactMatch(
                title = book.selectFirst(".info .heading")?.text()?.trim() ?: "",
                img = book.selectFirst(".cover img")?.attr("src") ?: "",
                score = null,
                type = "",
                isColored = false,
                isComplete = book.selectFirst(".status.Completed") != null,
                isHot = false,
                url = url,
                slug = getLastPathSegment(url) ?: ""
            )
        } else {
            val searchItems = arrayListOf<SearchResult.ExactMatch>()
            val searchList = document.select("#book_list .item")
            for (search in searchList) {
                val url = search.selectFirst(".text .title a")?.attr("href") ?: ""
                searchItems.add(
                    SearchResult.ExactMatch(
                        title = search.selectFirst(".text .title a")?.text()?.trim() ?: "",
                        img = search.selectFirst(".media .wrap_img img")?.attr("src") ?: "",
                        score = null,
                        type = "",
                        isColored = false,
                        isComplete = search.selectFirst(".status.Completed") != null,
                        isHot = false,
                        url = url,
                        slug = getLastPathSegment(url) ?: ""
                    )
                )
            }

            return SearchResult.SearchList(
                page = page,
                result = searchItems,
                hasNext = document.selectFirst(".uk-pagination .next") != null
            )
        }


    }

    override suspend fun getDetailKomik(slug: String): KomikDetail {
        val body: ResponseBody = api.getKomikPage(slug)
        val document = Jsoup.parse(body.string())
//        val table = document.select(".infotable tbody tr")
        val mangaId = document.selectFirst(".bookmark")?.attr("data-id") ?: "0"
        val title = document.selectFirst("#single_book .heading")?.text()
        val genreList = arrayListOf<Genre>()
        val genres = document.select("#single_book .info .genres a")

        for (genre in genres) {
            val url = genre.attr("url") ?: ""
            genreList.add(
                Genre(
                    title = genre.text(),
                    slug = getLastPathSegment(url) ?: "",
                    url = url,
                )
            )
        }

        val similarList = arrayListOf<SimilarTitle>()
        val similars = document.select("#hot_book .widget-body .item")
        for (similar in similars) {
            val url = similar.selectFirst(".wrap_img a")?.attr("href") ?: ""
            similarList.add(
                SimilarTitle(
                    title = similar.selectFirst(".text .title a")?.text()?.trim() ?: "",
                    img = similar.selectFirst(".media .wrap_img img")?.attr("data-src") ?: "",
                    genre = null,
                    type = similar.selectFirst(".text .status")?.text()?.trim() ?: "",
                    isColored = false,
                    slug = getLastPathSegment(url) ?: "",
                    url = url
                )
            )
        }

        val chapterList = arrayListOf<Chapter>()
        val chapters = document.select(".chapters tbody tr")

        for (chapter in chapters) {
            val url = chapter.selectFirst(".chapter a")?.attr("href") ?: ""
            chapterList.add(
                Chapter(
                    title = chapter.selectFirst(".chapter a")?.text()
                        ?.replace(title ?: "", "")?.trim()
                        ?: "",
                    date = parseDateString(
                        chapter.selectFirst(".update_time")?.text()?.trim() ?: "",
                        "MMM-dd-yyyy",
                        Locale.ENGLISH
                    ),
                    slug = getLastTwoSegments(url),
                    id = getLastTwoSegments(url),
                    mangaId = mangaId,
                    url = url,
                )
            )
        }


        val img = document.selectFirst("#single_book .cover img")?.attr("src") ?: ""
        return KomikDetail(
            id = slug,
            slug = getLastPathSegment(
                document.selectFirst("link[rel='canonical']")?.attr("href") ?: ""
            ) ?: "",
            title = title ?: "",
            img = img,
            banner = img,
            type = document.selectFirst(".info .meta .status")?.text()?.trim() ?: "",
            description = document.selectFirst(".summary p")?.text()?.trim() ?: "",
            score = null,
            genre = genreList,
            similar = similarList,
            chapters = chapterList
        )
    }

    override suspend fun getChapterList(id: String): List<Chapter> {
        val body = api.getKomikPage(id)
        val document = Jsoup.parse(body.string())
        val title = document.selectFirst("#single_book .heading")?.text()
        val mangaId = document.selectFirst(".bookmark")?.attr("data-id") ?: "0"
        val chapterList = arrayListOf<Chapter>()
        val chapters = document.select(".chapters tbody tr")

        for (chapter in chapters) {
            val url = chapter.selectFirst(".chapter a")?.attr("href") ?: ""
            chapterList.add(
                Chapter(
                    title = chapter.selectFirst(".chapter a")?.text()
                        ?.replace(title ?: "", "")?.trim()
                        ?: "",
                    date = parseDateString(
                        chapter.selectFirst(".update_time")?.text()?.trim() ?: "",
                        "MMM-dd-yyyy",
                        Locale.ENGLISH
                    ),
                    slug = getLastTwoSegments(url),
                    id = getLastTwoSegments(url),
                    mangaId = mangaId,
                    url = url,
                )
            )
        }

        return chapterList
    }

    private fun parseChapter(document: Document): ChapterApi {
        var imgList = listOf<String>()
        try {
            imgList = parseMangaKatanaChapterImages(document.html())
        } catch (e: Exception) {
            Log.d("ERROR PARSE CHAPTER IMGS", e.stackTraceToString())
        }

        val breadCrumbs = document.select("#breadcrumb_wrap ol li")
        val mangaTitle = breadCrumbs.getOrNull(1)?.selectFirst("span")?.text()?.trim()
        val chapterTitle =
            breadCrumbs.getOrNull(2)?.selectFirst("span")?.text()?.replace(mangaTitle ?: "", "")
                ?.trim() ?: ""

        val slug =
            getLastTwoSegments(document.selectFirst("link[rel='canonical']")?.attr("href") ?: "")

        val mangaId: String = slug.split(".").getOrNull(1) ?: ""
        val mangaSlug = slug.split("/").getOrNull(0) ?: ""
        return ChapterApi(
            id = slug,
            imgs = imgList,
            title = chapterTitle,
            slug = slug,
            mangaId = mangaId,
            mangaSlug = mangaSlug
        )
    }

    override suspend fun getChapter(id: String): ChapterApi {
        val split = id.split("/")
        val body = api.getChapter(split.getOrNull(0) ?: "", split.getOrNull(1) ?: "")
        val document = Jsoup.parse(body.string())
        return parseChapter(document)
    }

    override suspend fun getChapterBySlug(slug: String): ChapterApi {
        val split = slug.split("/")
        val body = api.getChapter(split.getOrNull(0) ?: "", split.getOrNull(1) ?: "")
        val document = Jsoup.parse(body.string())
        return parseChapter(document)
    }
}