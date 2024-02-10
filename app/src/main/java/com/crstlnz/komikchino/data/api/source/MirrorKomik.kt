package com.crstlnz.komikchino.data.api.source

import android.content.Context
import com.crstlnz.komikchino.data.api.KomikClient
import com.crstlnz.komikchino.data.api.KomikServer
import com.crstlnz.komikchino.data.api.ScraperBase
import com.crstlnz.komikchino.data.model.Chapter
import com.crstlnz.komikchino.data.model.ChapterApi
import com.crstlnz.komikchino.data.model.ChapterUpdate
import com.crstlnz.komikchino.data.model.DisqusConfig
import com.crstlnz.komikchino.data.model.FeaturedComic
import com.crstlnz.komikchino.data.model.Genre
import com.crstlnz.komikchino.data.model.GenreLink
import com.crstlnz.komikchino.data.model.GenreSearch
import com.crstlnz.komikchino.data.model.HomeData
import com.crstlnz.komikchino.data.model.KomikDetail
import com.crstlnz.komikchino.data.model.KomikSearchResult
import com.crstlnz.komikchino.data.model.LatestUpdate
import com.crstlnz.komikchino.data.model.LatestUpdatePage
import com.crstlnz.komikchino.data.model.OpenType
import com.crstlnz.komikchino.data.model.SearchResult
import com.crstlnz.komikchino.data.model.Section
import com.crstlnz.komikchino.data.model.SectionComic
import com.crstlnz.komikchino.data.model.SimilarTitle
import com.crstlnz.komikchino.data.util.StorageHelper
import com.crstlnz.komikchino.data.util.extractIdsFromOnClick
import com.crstlnz.komikchino.data.util.getBackgroundImage
import com.crstlnz.komikchino.data.util.getLastPathSegment
import com.crstlnz.komikchino.data.util.parseRelativeTimeIndonesia
import com.fasterxml.jackson.databind.type.TypeFactory
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import retrofit2.Response


class MirrorKomik(context: Context) : ScraperBase {
    private val api = KomikClient.getMirrorKomikClient()
    private val DIVIDER = "@!*!@"
    private val CACHE_KEY = "mirrorkomik-genres"
    private val USERNAME = "cocotmu"
    private val PASSWORD = "caricolor123"
    private val storage = StorageHelper<List<Genre>>(
        context,
        "GENRE-LIST-C",
        TypeFactory.defaultInstance()
            .constructParametricType(List::class.java, Genre::class.java),
        86400000L
    )

    override fun getChapterUrl(slug: String): String {
        return "${KomikServer.MIRRORKOMIK.url}chapter/$slug"
    }

    override fun getChapterUrlById(id: String): String {
        return getChapterUrl(id)
    }
    override fun getDetailKomikUrl(slug: String): String {
        return "${KomikServer.MIRRORKOMIK.url}${slug.replace(DIVIDER, "/")}"
    }

    private fun generateSlug(url: String): String {
        return url.trim("/".single()).replace("/", DIVIDER)
    }

    private suspend fun checkLogin(body: Response<ResponseBody>): Boolean {
        return if (body.raw().request.url.toString() == "https://mirrorkomik.net/login") {
            val loginDocument = Jsoup.parse(body.body()?.string() ?: "")
            val csrf =
                loginDocument.selectFirst("input[name='csrf_test_name']")?.attr("value") ?: ""
            api.login(csrf, USERNAME, PASSWORD)
            true
        } else {
            false
        }
    }

    override suspend fun getHome(): HomeData {
        val body = api.getHome()
        val document = Jsoup.parse(body.string())
        val featureds = document.select("aside .grd")
        val featuredList = arrayListOf<FeaturedComic>()
        for (featured in featureds) {
            val link = featured.selectFirst(".popunder")
            if (link !== null) {
                val url = link.attr("href") ?: ""
                val genreLinkList = arrayListOf<GenreLink>()
                val genre =
                    featured.selectFirst(".tpe1_inf")?.text()?.trim()?.split(" ")?.getOrNull(1)
                        ?: ""
                if (genre !== "") {
                    genreLinkList.add(
                        GenreLink(
                            title = genre,
                            url = "/Genre/${genre}",
                            slug = genre
                        )
                    )
                }

                val dataType = url.trim("/".single()).split("/")
                featuredList.add(
                    FeaturedComic(
                        title = featured.selectFirst("a h4")?.text()?.trim() ?: "",
                        url = url,
                        description = featured.selectFirst("p")?.text()?.trim()
                            ?: "No description.",
                        genreLink = genreLinkList,
                        type = dataType.getOrNull(0) ?: "",
                        img = featured.selectFirst("img")?.attr("data-src") ?: "",
                        slug = generateSlug(url),
                        score = null
                    )
                )
            }
        }

        val sectionList = arrayListOf<SectionComic>()
        val firstComic = document.selectFirst(".popunder")
        val comics = (document.selectFirst(".popunder + section")?.select(".at")
            ?: emptyList()).toMutableList()
        if (firstComic !== null) comics.add(0, firstComic)
        for (comic in comics) {
            val url = comic.selectFirst("a")?.attr("href") ?: ""
            sectionList.add(
                SectionComic(
                    title = comic.selectFirst("a h3")?.text()?.trim() ?: "",
                    url = url,
                    type = comic.selectFirst("img + div")?.className() ?: "",
                    img = comic.selectFirst("img")?.attr("data-src")
                        ?: getBackgroundImage(document.selectFirst("style")?.html() ?: ""),
                    slug = getLastPathSegment(url) ?: "",
                    score = null,
                    chapterString = "",
                    openType = OpenType.CHAPTER
                )
            )
        }

        val section = Section(
            title = "Populer",
            list = sectionList
        )

        return HomeData(
            featured = featuredList, sections = listOf(section)
        )
    }

    override suspend fun getLatestUpdate(page: Int): LatestUpdatePage {
        val body = api.getLatestUpdate(page)
        val document = Jsoup.parse(body.string())
        val latestUpdateElemets = document.select("main .content > .container > div > div")
        val latestUpdate = arrayListOf<LatestUpdate>()

        for (latest in latestUpdateElemets) {
            val url = latest.selectFirst("a")?.attr("href") ?: ""
            val chapterElements = latest.select("ul.chapter li")
            val chapters = arrayListOf<ChapterUpdate>()

            for (chapter in chapterElements) {
                val cUrl = chapter.selectFirst("a")?.attr("href") ?: ""
                chapters.add(
                    ChapterUpdate(
                        title = chapter.selectFirst("a")?.text()?.trim() ?: "",
                        slug = getLastPathSegment(cUrl) ?: "",
                        url = cUrl,
                        date = parseRelativeTimeIndonesia(
                            chapter.selectFirst(".date")?.text() ?: ""
                        )
                    )
                )
            }
            latestUpdate.add(
                LatestUpdate(
                    title = latest.selectFirst(".title a")?.text() ?: "",
                    img = latest.selectFirst("img")?.attr("data-src") ?: "",
                    description = "",
                    slug = generateSlug(url),
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

    override suspend fun search(query: String, page: Int): SearchResult {
        val body = api.search(page, query)
        val document = Jsoup.parse(body.string())
        val searchItems = arrayListOf<SearchResult.ExactMatch>()
        val searchList = document.select("article section.whites .animepost")
        for (search in searchList) {
            val url = search.selectFirst("a")?.attr("href") ?: ""
            searchItems.add(
                SearchResult.ExactMatch(
                    title = search.selectFirst("a .tt")?.text()?.trim() ?: "",
                    img = search.selectFirst("a img")?.attr("data-src") ?: "",
                    type = url.trim("/".single()).split("/").getOrNull(0)?.trim() ?: "",
                    url = url,
                    slug = generateSlug(url)
                )
            )
        }

        return SearchResult.SearchList(
            page = page,
            result = searchItems,
            hasNext = document.selectFirst("[aria-label=\"Next\"]") != null
        )
    }


    private fun parseKomik(document: Document, slug: String): KomikDetail {
        val tableInfoList = document.select(".inftable tr")
        val typeInfo =
            tableInfoList.find { it.text().startsWith("Jenis") }?.select("td")?.getOrNull(1)?.text()
                ?.trim()
        val title = document.selectFirst("article header h1")?.text()
        val genreLinkList = arrayListOf<GenreLink>()
        val genres = document.select("ul.genre a")
        for (genre in genres) {
            val url = genre.attr("url") ?: ""
            genreLinkList.add(
                GenreLink(
                    title = genre.text(),
                    slug = getLastPathSegment(url) ?: "",
                    url = url,
                )
            )
        }

        val similarList = arrayListOf<SimilarTitle>()
        val similars = document.select("aside .grd")
        for (similar in similars) {
            val link = similar.selectFirst(".popunder")
            if (link !== null) {
                val url = link.attr("href") ?: ""
                val genreList = arrayListOf<GenreLink>()
                val genre =
                    similar.selectFirst(".tpe1_inf")?.text()?.trim()?.split(" ")?.getOrNull(1) ?: ""
                if (genre !== "") {
                    genreList.add(
                        GenreLink(
                            title = genre,
                            url = "/Genre/${genre}",
                            slug = genre
                        )
                    )
                }

                val dataType = url.trim("/".single()).split("/")
                similarList.add(
                    SimilarTitle(
                        title = similar.selectFirst("a h4")?.text()?.trim() ?: "",
                        url = url,
                        genre = genreList.getOrNull(0)?.title ?: "",
                        type = dataType.getOrNull(0) ?: "",
                        img = similar.selectFirst("img")?.attr("data-src") ?: "",
                        slug = generateSlug(url),
                    )
                )
            }
        }

        val chapterList = arrayListOf<Chapter>()
        val chapters = document.select(".bxcl ul li")
        for (chapter in chapters) {
            val url = chapter.selectFirst("a")?.attr("href") ?: ""
            chapterList.add(
                Chapter(
                    title = chapter.selectFirst("a")?.text()
                        ?.replace(title ?: "", "")?.trim()
                        ?: "",
                    date = null,
                    slug = getLastPathSegment(url) ?: "",
                    id = getLastPathSegment(url) ?: "",
                    mangaId = slug,
                    url = url,
                )
            )
        }


        val url = document.selectFirst("link[rel='canonical']")?.attr("href") ?: ""
        val komikId = document.selectFirst(".subscribe button")?.attr("data-komik")
        return KomikDetail(
            id = slug,
            slug = slug,
            url = url,
            title = title?.trim() ?: "",
            img = document.selectFirst("article .ims img")?.attr("src") ?: "",
            banner = getBackgroundImage(document.selectFirst("style")?.html() ?: "") ?: "",
            type = typeInfo ?: "",
            description = document.selectFirst("#Sinopsis p")?.text()?.trim() ?: "",
            score = null,
            genreLinks = genreLinkList,
            similar = similarList,
            chapters = chapterList,
            disqusConfig = DisqusConfig(
                url = "https://mirrorkomik.net/${typeInfo?.lowercase()}/${komikId}",
                identifier = "https://mirrorkomik.net/${typeInfo?.lowercase()}/${komikId}"
            )
        )
    }

    override suspend fun getDetailKomik(id: String): KomikDetail {
        val urlData = id.split(DIVIDER)
        var body = api.getKomikBySlug(urlData.getOrNull(0) ?: "", urlData.getOrNull(1) ?: "")
        if (checkLogin(body)) {
            body = api.getKomikBySlug(urlData.getOrNull(0) ?: "", urlData.getOrNull(1) ?: "")
        }

        val document = Jsoup.parse(body.body()?.string() ?: "")
        return parseKomik(document, id)
    }

    override suspend fun getChapterList(id: String): List<Chapter> {
        val urlData = id.split(DIVIDER)
        var body = api.getKomikBySlug(urlData.getOrNull(0) ?: "", urlData.getOrNull(1) ?: "")
        if (checkLogin(body)) {
            body = api.getKomikBySlug(urlData.getOrNull(0) ?: "", urlData.getOrNull(1) ?: "")
        }
        val document = Jsoup.parse(body.body()?.string() ?: "")
        val title = document.selectFirst("article header h1")?.text()
        val chapterList = arrayListOf<Chapter>()
        val chapters = document.select(".bxcl ul li")
        for (chapter in chapters) {
            val url = chapter.selectFirst("a")?.attr("href") ?: ""
            chapterList.add(
                Chapter(
                    title = chapter.selectFirst("a")?.text()
                        ?.replace(title ?: "", "")?.trim()
                        ?: "",
                    date = null,
                    slug = getLastPathSegment(url) ?: "",
                    id = getLastPathSegment(url) ?: "",
                    mangaId = id,
                    url = url,
                )
            )
        }
        return chapterList
    }


    private suspend fun parseChapter(document: Document, slug: String): ChapterApi {
        val komikChapterId =
            extractIdsFromOnClick(document.selectFirst("#thisch")?.attr("onclick") ?: "")
        if (komikChapterId === null) throw Error("No Id!")
        val (komikId, chapterId) = komikChapterId
        val imgJSON = api.listChapterImages(komikId, chapterId)
        val imgList = arrayListOf<String>()
        for (img in imgJSON) {
            if (!img.contains("iklan") && !img.contains("jajan")) imgList.add(img ?: "")
        }

        val breadCrumbs = document.select("ul.perapih.brd li")
        val mangaTitle = breadCrumbs.getOrNull(1)?.selectFirst("span")?.text()?.trim()
        val chapterTitle =
            document.selectFirst("#Deskripsi h1")?.text()
                ?.trim()?.replace(mangaTitle ?: "", "") ?: ""

        val mangaUrl: String = breadCrumbs.getOrNull(1)?.selectFirst("a")?.attr("href") ?: ""
        return ChapterApi(
            id = slug,
            imgs = imgList,
            title = chapterTitle,
            slug = slug,
            mangaId = generateSlug(mangaUrl),
            mangaSlug = generateSlug(mangaUrl),
            disqusConfig = DisqusConfig(
                url = "https://mirrorkomik.net/chapter/${chapterId}",
                identifier = "https://mirrorkomik.net/${
                    generateSlug(mangaUrl).split(DIVIDER).getOrNull(0)?.lowercase()
                }/${komikId}"
            )
        )
    }

    override suspend fun getChapter(id: String): ChapterApi {
        return getChapterBySlug(id)
    }

    override suspend fun getChapterBySlug(slug: String): ChapterApi {
        var body = api.getChapterBySlug(slug)
        if (checkLogin(body)) {
            body = api.getChapterBySlug(slug)
        }
        val document = Jsoup.parse(body.body()?.string() ?: "")
        return parseChapter(document, slug)
    }

    override suspend fun searchByGenre(genreList: List<Genre>, page: Int): GenreSearch {
        var genre = genreList.getOrNull(0)
        if (genre === null) genre = Genre("action", "Action")
        val document = fetch {
            api.searchByGenre(genre.id, page)
        }

        val genresCache = storage.get<List<Genre>>(CACHE_KEY)
        val genreListResult = arrayListOf<Genre>()
        if (genresCache !== null) {
            genreListResult.addAll(genresCache)
        } else {
            val genreDocument = fetch {
                api.searchNoGenre()
            }
            val genreElements = genreDocument.select("ul.genre li.genre")
            genreListResult.clear()
            for (gnr in genreElements) {
                genreListResult.add(
                    Genre(
                        id = getLastPathSegment(gnr.selectFirst("a")?.attr("href") ?: "") ?: "",
                        title = gnr.selectFirst("a")?.text()?.trim() ?: "",
                    )
                )
            }
            storage.set<List<Genre>>(CACHE_KEY, genreListResult)
        }

        val searchItems = arrayListOf<KomikSearchResult>()
        val searchList = document.select("article section .animepost")
        for (search in searchList) {
            val url = search.selectFirst("a")?.attr("href") ?: ""
            searchItems.add(
                KomikSearchResult(
                    title = search.selectFirst(".bigors .tt")?.text()?.trim() ?: "",
                    img = search.selectFirst(".limit img")?.attr("data-src") ?: "",
                    type = url.trim("/".single()).split("/").getOrNull(0) ?: "",
                    url = url,
                    slug = generateSlug(url)
                )
            )
        }

        val hasNext =
            document.selectFirst("[aria-label=\"Next\"]") != null || document.selectFirst("ul.pagination li.active + li") != null
        return GenreSearch(
            genreList = genreListResult,
            page = page,
            hasNext = hasNext,
            result = searchItems
        )
    }
}