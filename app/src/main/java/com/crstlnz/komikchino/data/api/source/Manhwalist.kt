package com.crstlnz.komikchino.data.api.source

import com.crstlnz.komikchino.data.api.KomikClient
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
import com.crstlnz.komikchino.data.util.parseRelativeTime
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.util.Locale
import java.util.regex.Pattern

class Manhwalist : ScraperBase {
    private val api = KomikClient.getManhwalistClient()

    private fun extractNumericValue(title: String): Int {
        val numericRegex = Regex("\\d+")
        val numericPart = numericRegex.find(title)?.value
        return numericPart?.toIntOrNull() ?: 0
    }

    private fun sortChapter(): Comparator<Chapter> {
        return Comparator<Chapter> { c1, c2 ->
            if (c1.date != null && c2.date != null && c1.date.time != c2.date.time) {
                if (c1.date.time > c2.date.time) {
                    -1
                } else {
                    1
                }
            } else {
                val numericValue1 = extractNumericValue(c1.title)
                val numericValue2 = extractNumericValue(c2.title)
                if (numericValue1 != numericValue2) {
                    numericValue2 - numericValue1
                } else {
                    c2.title.compareTo(c1.title)
                }
            }
        }
    }

    override suspend fun getHome(): HomeData {
        val body = api.getHome()
        val document = Jsoup.parse(body.string())
        val featureds = document.select("#content .owl-carousel .slide-item")
        val featuredList = arrayListOf<FeaturedComic>()
        for (featured in featureds) {
            val url = featured.selectFirst(".poster a")?.attr("href") ?: ""
            // mangakatana tak ade genre di home
            val genreLinkList = arrayListOf<GenreLink>()
            val genres = featured.select(".extras .extra-category a")
            for (genre in genres) {
                genreLinkList.add(
                    GenreLink(
                        title = genre.text() ?: "",
                        url = genre.attr("href") ?: "",
                        slug = getLastPathSegment(genres.attr("href") ?: "") ?: ""
                    )
                )
            }
            featuredList.add(
                FeaturedComic(
                    title = featured.selectFirst(".title .ellipsis a")?.text()?.trim() ?: "",
                    url = url,
                    description = featured.selectFirst(".excerpt > p:nth-child(3)")?.text()?.trim()
                        ?: "No description.",
                    genreLink = genreLinkList,
                    type = featured.selectFirst(".title .release-year")?.text()?.trim() ?: "",
                    img = featured.selectFirst(".poster img")?.attr("src") ?: "",
                    slug = getLastPathSegment(url) ?: "",
                    score = featured.selectFirst(".rating .vote")?.text()?.toFloatOrNull()
                )
            )
        }

        val sectionList = arrayListOf<SectionComic>()
        val comics = document.select(".hothome .listupd .bs")

        for (comic in comics) {
            val url = comic.selectFirst(".bsx a")?.attr("href") ?: ""
            sectionList.add(
                SectionComic(
                    title = comic.selectFirst(".bigor .tt")?.text()?.trim() ?: "",
                    url = url,
                    type = comic.selectFirst(".limit .type")?.classNames()?.toList()?.getOrNull(1)
                        ?.toString() ?: "",
                    img = comic.selectFirst(".limit img")?.attr("src") ?: "",
                    slug = getLastPathSegment(url) ?: "",
                    score = comic.selectFirst(".rating .numscore")?.text()?.trim()
                        ?.toFloatOrNull() ?: 0f,
                    chapterString = comic.selectFirst(".bigor .epxs")?.text()?.trim() ?: ""
                )
            )
        }

        val section = Section(
            title = document.selectFirst(".hothome .releases h2")?.text()?.trim()
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

    override suspend fun getLatestUpdate(page: Int): LatestUpdatePage {
        val body = api.getLatestUpdate(page)
        val document = Jsoup.parse(body.string())
        val latestUpdateBox = document.select(".postbody .listupd")
        val latestUpdateElemets = latestUpdateBox.getOrNull(1)?.select(".bs")
            ?: listOf()
        val latestUpdate = arrayListOf<LatestUpdate>()

        for (latest in latestUpdateElemets) {
            val url = latest.selectFirst(".bsx a")?.attr("href") ?: ""
            val chapterElements = latest.select(".bigor ul li")
            val chapters = arrayListOf<ChapterUpdate>()

            for (chapter in chapterElements) {
                val cUrl = chapter.selectFirst("a")?.attr("href") ?: ""
                chapters.add(
                    ChapterUpdate(
                        title = chapter.selectFirst("a")?.text()?.trim() ?: "",
                        slug = getLastPathSegment(cUrl) ?: "",
                        url = cUrl,
                        date = parseRelativeTime(chapter.selectFirst("span")?.text() ?: "")
                    )
                )
            }
            latestUpdate.add(
                LatestUpdate(
                    title = latest.selectFirst(".bigor .tt")?.text() ?: "",
                    img = latest.selectFirst("img")?.attr("src") ?: "",
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
            hasNext = document.selectFirst(".hpage .r") != null
        )
    }

    override suspend fun search(query: String, page: Int): SearchResult {
        val body = api.search(page, query)
        val document = Jsoup.parse(body.string())
        val searchItems = arrayListOf<SearchResult.ExactMatch>()
        val searchList = document.select(".postbody .bs")
        for (search in searchList) {
            val url = search.selectFirst("a")?.attr("href") ?: ""
            searchItems.add(
                SearchResult.ExactMatch(
                    title = search.selectFirst(".bigor .tt")?.text()?.trim() ?: "",
                    img = search.selectFirst(".limit img.ts-post-image")?.attr("src") ?: "",
                    score = search.selectFirst(".rating .numscore")?.text()?.trim()?.toFloatOrNull()
                        ?: 0f,
                    type = search.selectFirst(".limit .type")?.classNames()?.toList()?.getOrNull(1)
                        ?: if (search.selectFirst(".novelabel") != null) "Novel" else "",
                    isColored = search.selectFirst(".colored") != null,
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
            hasNext = document.selectFirst(".pagination .next") != null
        )
    }


    private fun parseKomik(document: Document): KomikDetail {
        val table = document.select(".infotable tbody tr")
        val id = document.selectFirst(".bookmark")?.attr("data-id")?.toIntOrNull() ?: 0
        val title = document.selectFirst("#titlemove .entry-title")?.text()
        val genreLinkList = arrayListOf<GenreLink>()
        val genres = document.select(".wd-full .mgen a")

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
        val similars = document.select(".bixbox .bs")
        for (similar in similars) {
            val url = similar.selectFirst(".bsx a")?.attr("href") ?: ""
            similarList.add(
                SimilarTitle(
                    title = similar.selectFirst(".tt")?.text()?.trim() ?: "",
                    img = similar.selectFirst("a img")?.attr("src") ?: "",
                    genre = null,
                    type = similar.selectFirst(".limit .type")?.classNames()?.toList()?.getOrNull(1)
                        ?.toString()
                        ?: if (similar.selectFirst(".novelabel") != null) "Novel" else "",
                    isColored = similar.selectFirst(".colored") != null,
                    slug = getLastPathSegment(url) ?: "",
                    url = url
                )
            )
        }

        val chapterList = arrayListOf<Chapter>()
        val chapters = document.select("#chapterlist ul li")

        for (chapter in chapters.reversed()) {
            val url = chapter.selectFirst(".eph-num a")?.attr("href") ?: ""
            chapterList.add(
                Chapter(
                    title = chapter.selectFirst(".chbox .chapternum")?.text()
                        ?.replace(title ?: "", "")?.trim()
                        ?: "",
                    date = parseDateString(
                        chapter.selectFirst(".eph-num .chapterdate")?.text()?.trim() ?: "",
                        "MMMM d, yyyy",
                        Locale.ENGLISH
                    ),
                    slug = getLastPathSegment(url) ?: "",
                    id = getLastPathSegment(url) ?: "",
                    mangaId = id.toString(),
                    url = url,
                )
            )
        }


        val url = document.selectFirst("link[rel='canonical']")?.attr("href") ?: ""
        return KomikDetail(
            id = id.toString(),
            slug = getLastPathSegment(
                url
            ) ?: "",
            url = url,
            title = title?.trim() ?: "",
            img = document.selectFirst("#content .thumb img")?.attr("src") ?: "",
            banner = document.selectFirst("#content .thumb img")?.attr("src") ?: "",
            type = document.selectFirst(".tsinfo .imptdt:nth-child(2) a")?.text()?.trim() ?: "",
            description = document.selectFirst(".entry-content p")?.text()?.trim() ?: "",
            score = document.selectFirst(".rating .num")?.text()?.trim()?.toFloatOrNull() ?: 0f,
            genreLinks = genreLinkList,
            similar = similarList,
            chapters = chapterList.sortedWith(sortChapter()),
            disqusConfig = getVoidScansDisqus(document.html())
        )
    }

    override suspend fun getDetailKomik(id: String): KomikDetail {
        val intId = id.toIntOrNull()
        val body: ResponseBody = if (intId != null) {
            api.getKomikById(id)
        } else {
            api.getKomikBySlug(id)
        }
        val document = Jsoup.parse(body.string())
        return parseKomik(document)
    }

    override suspend fun getChapterList(id: String): List<Chapter> {
        val body = api.getKomikById(id)
        val document = Jsoup.parse(body.string())
        val title = document.selectFirst("#single_book .heading")?.text()
        val mangaId = document.selectFirst(".bookmark")?.attr("data-id")
        val chapterList = arrayListOf<Chapter>()
        val chapters = document.select("#chapterlist ul li")

        for (chapter in chapters.reversed()) {
            val url = chapter.selectFirst(".eph-num a")?.attr("href") ?: ""
            chapterList.add(
                Chapter(
                    title = chapter.selectFirst(".chbox .chapternum")?.text()
                        ?.replace(title ?: "", "")?.trim()
                        ?: "",
                    date = parseDateString(
                        chapter.selectFirst(".eph-num .chapterdate")?.text()?.trim() ?: "",
                        "MMMM d, yyyy",
                        Locale.ENGLISH
                    ),
                    slug = getLastPathSegment(url) ?: "",
                    id = getLastPathSegment(url) ?: "",
                    mangaId = mangaId ?: id,
                    url = url,
                )
            )
        }
        return chapterList.sortedWith(sortChapter())
    }


    private fun parseChapter(document: Document): ChapterApi {
        val imgs = document.select("#readerarea img")
        val imgList = arrayListOf<String>()
        for (img in imgs) {
            imgList.add(img.attr("src") ?: "")
        }

        val breadCrumbs = document.select(".ts-breadcrumb li")
        val mangaTitle = breadCrumbs.getOrNull(1)?.selectFirst("span")?.text()?.trim()
        val chapterTitle =
            document.selectFirst(".readingnavtop .chpnw")?.text()?.trim() ?: ""


        val slug =
            getLastPathSegment(breadCrumbs.getOrNull(2)?.selectFirst("a")?.attr("href") ?: "") ?: ""

        val regex = "var post_id = (\\d+);"
        val pattern = Pattern.compile(regex)
        val matcher = pattern.matcher(document.select("script").html())

        var mangaId: String = ""
        if (matcher.find()) {
            mangaId = matcher.group(1)?.toString() ?: ""
        }

        val mangaSlug =
            getLastPathSegment(document.selectFirst(".allc a")?.attr("href") ?: "") ?: ""
        return ChapterApi(
            id = slug,
            imgs = imgList,
            title = chapterTitle,
            slug = slug,
            mangaId = mangaId,
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

    override suspend fun searchByGenre(genreList: List<Genre>, page: Int): GenreSearch {
        val document = fetch {
            api.searchByGenre(genreList.map { it.id }, page)
        }

        val genreListElements = document.select(".quickfilter .genrez li")
        val genreListResult = arrayListOf<Genre>()
        for (genre in genreListElements) {
            genreListResult.add(
                Genre(
                    id = genre.selectFirst("input")?.attr("value")?.trim() ?: "",
                    title = genre.selectFirst("label")?.text()?.trim() ?: ""
                )
            )
        }

        val searchItems = arrayListOf<KomikSearchResult>()
        val searchList = document.select(".postbody .bs")
        for (search in searchList) {
            val url = search.selectFirst("a")?.attr("href") ?: ""
            searchItems.add(
                KomikSearchResult(
                    title = search.selectFirst(".bigor .tt")?.text()?.trim() ?: "",
                    img = search.selectFirst(".limit img.ts-post-image")?.attr("src") ?: "",
                    score = search.selectFirst(".rating .numscore")?.text()?.trim()?.toFloatOrNull()
                        ?: 0f,
                    type = search.selectFirst(".limit .type")?.classNames()?.toList()?.getOrNull(1)
                        ?: if (search.selectFirst(".novelabel") != null) "Novel" else "",
                    isColored = search.selectFirst(".colored") != null,
                    isComplete = search.selectFirst(".status.Completed") != null,
                    isHot = search.selectFirst(".hotx") != null,
                    url = url,
                    slug = getLastPathSegment(url) ?: ""
                )
            )
        }

        val hasNext = document.selectFirst(".hpage .r") != null
        return GenreSearch(
            genreList = genreListResult,
            page = page,
            hasNext = hasNext,
            result = searchItems
        )
    }
}