package com.crstlnz.komikchino.data.api.source

import com.crstlnz.komikchino.data.api.KomikClient
import com.crstlnz.komikchino.data.api.KomikServer
import com.crstlnz.komikchino.data.api.ScraperBase
import com.crstlnz.komikchino.data.model.Chapter
import com.crstlnz.komikchino.data.model.ChapterApi
import com.crstlnz.komikchino.data.model.ChapterUpdate
import com.crstlnz.komikchino.data.model.FeaturedComic
import com.crstlnz.komikchino.data.model.Genre
import com.crstlnz.komikchino.data.model.GenreLink
import com.crstlnz.komikchino.data.model.GenreSearch
import com.crstlnz.komikchino.data.model.HomeData
import com.crstlnz.komikchino.data.model.KomikDetail
import com.crstlnz.komikchino.data.model.KomikSearchResult
import com.crstlnz.komikchino.data.model.LatestUpdate
import com.crstlnz.komikchino.data.model.LatestUpdatePage
import com.crstlnz.komikchino.data.model.SearchResult
import com.crstlnz.komikchino.data.model.Section
import com.crstlnz.komikchino.data.model.SectionComic
import com.crstlnz.komikchino.data.model.SimilarTitle
import com.crstlnz.komikchino.data.util.getLastPathSegment
import com.crstlnz.komikchino.data.util.getVoidScansDisqus
import com.crstlnz.komikchino.data.util.parseDateString
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.util.Locale

class KomikuId : ScraperBase {
    private val api = KomikClient.getKomikuIdClient()

    override fun getChapterUrl(slug: String): String {
        return "${KomikServer.KOMIKUID.url}$slug"
    }

    override fun getChapterUrlById(id: String): String {
        return getChapterUrl(id)
    }

    override fun getDetailKomikUrl(slug: String): String {
        return "${KomikServer.KOMIKUID.url}manga/$slug"
    }

    override suspend fun getHome(): HomeData {
        val body = api.getHome()
        val document = Jsoup.parse(body.string())
        val featureds = document.select("#Trending_Komik article")
        val featuredList = arrayListOf<FeaturedComic>()
        for (featured in featureds) {
            val url = featured.selectFirst("a")?.attr("href") ?: ""
            val genreLinkList = arrayListOf<GenreLink>()
            featuredList.add(
                FeaturedComic(
                    title = featured.selectFirst("h4 a")?.text()?.trim() ?: "",
                    url = url,
                    description = "No description.",
                    genreLink = genreLinkList,
                    type = featured.selectFirst(".ls2j .ls2t")?.text()?.trim() ?: "",
                    img = featured.selectFirst("img")?.attr("data-src") ?: "",
                    slug = getLastPathSegment(url) ?: "",
                    score = null
                )
            )
        }

        val sectionList = arrayListOf<SectionComic>()
        val comics = document.select("#Komik_Hot article")

        for (comic in comics) {
            val url = comic.selectFirst("a")?.attr("href") ?: ""
            sectionList.add(
                SectionComic(
                    title = comic.selectFirst("h4 a")?.text()?.trim() ?: "",
                    url = url,
                    type = "",
                    img = comic.selectFirst("img")?.attr("data-src") ?: "",
                    slug = getLastPathSegment(url) ?: "",
                    score = null,
                    chapterString = comic.selectFirst(".ls2l")?.text()?.trim() ?: ""
                )
            )
        }

        val section = Section(
            title = document.selectFirst("#Komik_Hot h3")?.text()?.trim()
                ?.split(" ")?.joinToString(" ") {
                    it.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(
                            Locale.ROOT
                        ) else it.toString()
                    }
                } ?: "",
            list = sectionList
        )

        return HomeData(
            featured = featuredList, sections = listOf(section)
        )
    }

    private fun parseKomik(document: Document): KomikDetail {
        val url = document.selectFirst("link[rel='canonical']")?.attr("href") ?: ""
        val slug = getLastPathSegment(
            url
        ) ?: ""
        val id = slug

        val title = document.selectFirst("h1")?.text()
        val genreLinkList = arrayListOf<GenreLink>()
        val genres = document.select("#Informasi ul.genre li")

        for (genre in genres) {
            val urlGenre = genre.attr("url") ?: ""
            genreLinkList.add(
                GenreLink(
                    title = genre.text(),
                    slug = getLastPathSegment(url) ?: "",
                    url = urlGenre,
                )
            )
        }

        val similarList = arrayListOf<SimilarTitle>()
        val similars = document.select("#Spoiler>div")
        for (similar in similars) {
            val urlSimilar = similar.selectFirst("a")?.attr("href") ?: ""
            similarList.add(
                SimilarTitle(
                    title = similar.selectFirst(".h4")?.text()?.trim() ?: "",
                    img = similar.selectFirst("img")?.attr("data-src") ?: "",
                    genre = null,
                    type = "",
                    isColored = similar.selectFirst(".berwarna") != null,
                    slug = getLastPathSegment(urlSimilar) ?: "",
                    url = urlSimilar
                )
            )
        }

        val chapterList = arrayListOf<Chapter>()
        val chapters = document.select("#Daftar_Chapter tbody tr")
        chapters.removeAt(0)
        for (chapter in chapters) {
            val urlChapter = chapter.selectFirst("a")?.attr("href") ?: ""
            chapterList.add(
                Chapter(
                    title = chapter.selectFirst("a")?.text()
                        ?.replace(title ?: "", "")?.trim()
                        ?: "",
                    date = parseDateString(
                        chapter.selectFirst(".tanggalseries")?.text()?.trim() ?: "",
                        "DD/MM/YYYY",
                        Locale.US
                    ),
                    slug = getLastPathSegment(urlChapter) ?: "",
                    id = getLastPathSegment(urlChapter) ?: "",
                    mangaId = id.toString(),
                    url = urlChapter,
                )
            )
        }


        val urlRegex = Regex("""url\((.*?)\)""")
        val matchResult = urlRegex.find(document.html())
        val banner = matchResult?.groupValues?.get(1) ?: ""

        val table = document.select(".inftable tbody tr")
        val type =
            table.find { el -> el.text().startsWith("Jenis Komik") }?.selectFirst("td:nth-child(2)")
                ?.text() ?: ""
        return KomikDetail(
            id = id.toString(),
            slug = getLastPathSegment(
                url
            ) ?: "",
            url = url,
            title = title?.trim() ?: "",
            img = document.selectFirst("#Informasi img")?.attr("src") ?: "",
            banner = banner,
            type = type,
            description = document.selectFirst("p.desc")?.text()?.trim() ?: "",
            score = null,
            genreLinks = genreLinkList,
            similar = similarList,
            chapters = chapterList,
        )
    }

    override suspend fun getDetailKomik(id: String): KomikDetail {
        val body: ResponseBody = api.getKomikBySlug(id)
        val document = Jsoup.parse(body.string())
        return parseKomik(document)
    }

    override suspend fun getChapterList(id: String): List<Chapter> {
        val body = api.getKomikBySlug(id)
        val document = Jsoup.parse(body.string())
        val title = document.selectFirst("h1")?.text()

        val chapterList = arrayListOf<Chapter>()
        val chapters = document.select("#Daftar_Chapter tbody tr")
        chapters.removeAt(0)
        for (chapter in chapters) {
            val urlChapter = chapter.selectFirst("a")?.attr("href") ?: ""
            chapterList.add(
                Chapter(
                    title = chapter.selectFirst("a")?.text()
                        ?.replace(title ?: "", "")?.trim()
                        ?: "",
                    date = parseDateString(
                        chapter.selectFirst(".tanggalseries")?.text()?.trim() ?: "",
                        "DD/MM/YYYY",
                        Locale.US
                    ),
                    slug = getLastPathSegment(urlChapter) ?: "",
                    id = getLastPathSegment(urlChapter) ?: "",
                    mangaId = id.toString(),
                    url = urlChapter,
                )
            )
        }
        return chapterList
    }


    private fun parseChapter(document: Document): ChapterApi {
        val imgs = document.select("#Baca_Komik img")
        val imgList = arrayListOf<String>()
        for (img in imgs) {
            imgList.add(img.attr("src") ?: "")
        }

        val delimiter = "Chapter"
        val chapterTitle =
            delimiter + " " + (document.selectFirst("#Judul h1")?.text()?.trim() ?: "").split(
                delimiter
            ).getOrNull(1)?.trim()

        val url = document.selectFirst("link[rel='canonical']")?.attr("href") ?: ""
        val slug =
            getLastPathSegment(url) ?: ""

        val mangaSlug =
            getLastPathSegment(document.selectFirst(".perapih .s1 a")?.attr("href") ?: "") ?: ""
        return ChapterApi(
            id = slug,
            imgs = imgList,
            title = chapterTitle,
            slug = slug,
            mangaId = mangaSlug,
            mangaSlug = mangaSlug,
            disqusConfig = getVoidScansDisqus(document.html())
        )
    }

    override suspend fun getChapter(id: String): ChapterApi {
        return getChapterBySlug(id)
    }

    override suspend fun getChapterBySlug(slug: String): ChapterApi {
        val body = api.getChapterBySlug(slug)
        val document = Jsoup.parse(body.string())
        return parseChapter(document)
    }

    override suspend fun search(query: String, page: Int): SearchResult {
        val body = api.search(page, query)
        val document = Jsoup.parse(body.string())
        val searchItems = arrayListOf<SearchResult.ExactMatch>()
        val searchList = document.select(".daftar > div.bge")
        for (search in searchList) {
            val url = search.selectFirst("a")?.attr("href") ?: ""
            searchItems.add(
                SearchResult.ExactMatch(
                    title = search.selectFirst("h3")?.text()?.trim() ?: "",
                    img = search.selectFirst("img")?.attr("data-src") ?: "",
                    score = null,
                    type = search.selectFirst(".tpe1_inf b")?.text() ?: "",
                    isColored = search.selectFirst(".berwarna") != null,
                    isComplete = search.selectFirst(".status.Completed") != null,
                    isHot = search.selectFirst(".hotx") != null,
                    url = url,
                    slug = getLastPathSegment(url) ?: ""
                )
            )
        }

        return SearchResult.SearchList(
            page = page,
            result = searchItems,
            hasNext = document.selectFirst(".pag-nav .next") != null
        )
    }

    override suspend fun searchByGenre(genreList: List<Genre>, page: Int): GenreSearch {
        var genre = genreList.getOrNull(0)
        if (genre === null) genre = Genre("fantasy", "Fantasy")
        val document = fetch {
            api.searchByGenre(genre.id, page)
        }

        val genreListElements = document.select("ul.genre li")
        val genreListResult = arrayListOf<Genre>()
        for (genreEl in genreListElements) {
            val url = genreEl.selectFirst("a")?.attr("href") ?: ""
            genreListResult.add(
                Genre(
                    id = getLastPathSegment(url) ?: "",
                    title = genreEl.text()
                )
            )
        }

        val searchItems = arrayListOf<KomikSearchResult>()
        val searchList = document.select(".daftar > div.bge")
        for (search in searchList) {
            val url = search.selectFirst("a")?.attr("href") ?: ""
            searchItems.add(
                KomikSearchResult(
                    title = search.selectFirst("h3")?.text()?.trim() ?: "",
                    img = search.selectFirst("img")?.attr("data-src") ?: "",
                    score = null,
                    type = search.selectFirst(".tpe1_inf b")?.text() ?: "",
                    isColored = search.selectFirst(".berwarna") != null,
                    isComplete = search.selectFirst(".status.Completed") != null,
                    isHot = search.selectFirst(".hotx") != null,
                    url = url,
                    slug = getLastPathSegment(url) ?: ""
                )
            )
        }

        val hasNext = document.selectFirst(".pag-nav .next") != null
        return GenreSearch(
            genreList = genreListResult,
            page = page,
            hasNext = hasNext,
            result = searchItems
        )
    }

    override suspend fun getLatestUpdate(page: Int): LatestUpdatePage {
        val body = api.getLatestKomik(page)
        val document = Jsoup.parse(body.string())
        val latestUpdateElemets = document.select(".daftar > div.bge")
        val latestUpdate = arrayListOf<LatestUpdate>()

        for (latest in latestUpdateElemets) {
            val url = latest.selectFirst("a")?.attr("href") ?: ""
            val chapter = latest.select(".new1").getOrNull(1)
            val chapters = arrayListOf<ChapterUpdate>()

            if (chapter != null) {
                val cUrl = chapter.selectFirst("a")?.attr("href") ?: ""
                chapters.add(
                    ChapterUpdate(
                        title = chapter.selectFirst("span:nth-child(2)")?.text()?.trim() ?: "",
                        slug = getLastPathSegment(cUrl) ?: "",
                        url = cUrl,
                        date = null
                    )
                )
            }

            latestUpdate.add(
                LatestUpdate(
                    title = latest.selectFirst("h3")?.text()?.trim() ?: "",
                    img = latest.selectFirst("img")?.attr("data-src") ?: "",
                    description = "",
                    slug = getLastPathSegment(url) ?: "",
                    url = url,
                    chapters = chapters
                )
            )
        }

        return LatestUpdatePage(
            page = page,
            result = latestUpdate,
            hasNext = false
        )
    }
}