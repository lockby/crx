package com.crstlnz.komikchino.data.api.source

import com.crstlnz.komikchino.data.api.KomikClient
import com.crstlnz.komikchino.data.model.Chapter
import com.crstlnz.komikchino.data.model.ChapterModel
import com.crstlnz.komikchino.data.model.FeaturedComic
import com.crstlnz.komikchino.data.model.Genre
import com.crstlnz.komikchino.data.model.HomeData
import com.crstlnz.komikchino.data.model.KomikDetail
import com.crstlnz.komikchino.data.model.SearchItem
import com.crstlnz.komikchino.data.model.SearchQuery
import com.crstlnz.komikchino.data.model.Section
import com.crstlnz.komikchino.data.model.SectionComic
import com.crstlnz.komikchino.data.model.SimilarTitle
import com.crstlnz.komikchino.data.util.getBackgroundImage
import com.crstlnz.komikchino.data.util.getLastPathSegment
import com.crstlnz.komikchino.data.util.getQuery
import com.crstlnz.komikchino.data.util.parseDateString
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.util.Locale
import java.util.regex.Pattern

class Kiryuu : ScraperBase {
    private val api = KomikClient.getKiryuuClient()
    override suspend fun getHome(): HomeData {
        val body = api.getHome()
        val document = Jsoup.parse(body.string())
        val featureds = document.select(".slider-wrapper .swiper-slide")
        val featuredList = arrayListOf<FeaturedComic>()
        for (featured in featureds) {
            val url = featured.selectFirst(".sliderinfo .sliderinfolimit a")?.attr("href") ?: ""
            val genreList = arrayListOf<Genre>()
            val genres = featured.select(".metas-slider-genres .metas-genres-values a")
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
                    title = featured.selectFirst(".sliderinfo .name")?.text()?.trim() ?: "",
                    url = url,
                    description = featured.selectFirst(".sliderinfo .desc")?.text()?.trim() ?: "",
                    genre = genreList,
                    type = featured.selectFirst(".metas-slider-type .meta-type-values")?.text()
                        ?.trim() ?: "",
                    img = getBackgroundImage(
                        featured.selectFirst(".bigbanner")?.attr("style") ?: ""
                    ),
                    slug = getLastPathSegment(url) ?: "",
                    score = featured.selectFirst(".metas-slider-score .meta-score-values")?.text()
                        ?.trim()?.toFloatOrNull() ?: 0f
                )
            )
        }

        val sectionList = arrayListOf<SectionComic>()
        val comics = document.select(".popularslider .bs")

        for (comic in comics) {
            val url = comic.selectFirst(".bsx a")?.attr("href") ?: ""
            sectionList.add(
                SectionComic(
                    title = comic.selectFirst(".bigor .tt")?.text()?.trim() ?: "",
                    url = url,
                    type = comic.selectFirst(".limit .type")?.classNames()?.toList()?.getOrNull(1)
                        ?.toString()
                        ?: if (comic.selectFirst(".novelabel") != null) "Novel" else "",
                    img = comic.selectFirst("img")?.attr("src") ?: "",
                    slug = getLastPathSegment(url) ?: "",
                    score = comic.selectFirst(".rating .numscore")?.text()?.trim()
                        ?.toFloatOrNull() ?: 0f,
                    chapterString = comic.selectFirst(".bigor .epxs")?.text()?.trim() ?: ""
                )
            )
        }
        return HomeData(
            featured = featuredList, sections = listOf(Section(
                title = document.selectFirst("#content .hotslid .releases")?.text()?.trim()
                    ?.split(" ")?.joinToString(" ") { it.capitalize(Locale.ROOT) } ?: "",
                list = sectionList
            ))
        )
    }

    override suspend fun search(query: String, page: Int): SearchQuery {
        val body = api.search(page, query)
        val document = Jsoup.parse(body.string())
        val searchItems = arrayListOf<SearchItem>()
        val searchList = document.select(".postbody .bs")
        for (search in searchList) {
            val url = search.selectFirst("a")?.attr("href") ?: ""
            searchItems.add(
                SearchItem(
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

        return SearchQuery(
            page = page,
            result = searchItems,
            hasNext = document.selectFirst(".pagination .next") != null
        )
    }

    private fun parseKomik(document: Document): KomikDetail {
        val table = document.select(".infotable tbody tr")
        val id = document.selectFirst(".bookmark")?.attr("data-id")?.toIntOrNull() ?: 0
        val title = document.selectFirst(".seriestuheader .entry-title")?.text()
        val genreList = arrayListOf<Genre>()
        val genres = document.select(".seriestugenre a")

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

        for (chapter in chapters) {
            val url = chapter.selectFirst(".eph-num a")?.attr("href") ?: ""
            val dlUrl = chapter.selectFirst(".dt a")?.attr("href") ?: ""
            chapterList.add(
                Chapter(
                    title = chapter.selectFirst(".chbox .chapternum")?.text()
                        ?.replace(title ?: "", "")?.trim()
                        ?: "",
                    date = parseDateString(
                        chapter.selectFirst(".eph-num .chapterdate")?.text()?.trim() ?: ""
                    ),
                    slug = getLastPathSegment(url) ?: "",
                    id = getQuery(dlUrl, "id")?.toString(),
                    mangaId = id.toString(),
                    url = url,
                )
            )
        }


        return KomikDetail(
            id = id.toString(),
            slug = getLastPathSegment(
                document.selectFirst("link[rel='canonical']")?.attr("href") ?: ""
            ) ?: "",
            title = title
                ?.replace("Bahasa Indonesia", "")?.trim() ?: "",
            img = document.selectFirst(".seriestucontent .thumb img")?.attr("src") ?: "",
            banner = getBackgroundImage(
                document.selectFirst(".bigcover .bigbanner")?.attr("style") ?: ""
            ),
            type = table.getOrNull(1)?.select("td")?.getOrNull(1)?.text()?.trim() ?: "",
            description = document.selectFirst(".entry-content p")?.text()?.trim() ?: "",
            score = document.selectFirst(".rating .num")?.text()?.trim()?.toFloatOrNull() ?: 0f,
            genre = genreList,
            similar = similarList,
            chapters = chapterList
        )
    }

//    override suspend fun getDetailKomik(slug: String): KomikDetail {
//        val body = api.getKomikPage(slug)
//        val document = Jsoup.parse(body.string())
//        return parseKomik(document)
//    }

    override suspend fun getDetailKomik(id: String): KomikDetail {
        val intId = id.toIntOrNull()
        val body: ResponseBody = if (intId != null) {
            api.getKomikById(id)
        } else {
            api.getKomikPage(id)
        }
        val document = Jsoup.parse(body.string())
        return parseKomik(document)
    }

    override suspend fun getChapterList(id: String): List<Chapter> {
        val body = api.getKomikById(id)
        val document = Jsoup.parse(body.string())

        val chapterList = arrayListOf<Chapter>()
        val chapters = document.select("#chapterlist ul li")

        for (chapter in chapters) {
            val url = chapter.selectFirst(".eph-num a")?.attr("href") ?: ""
            val dlUrl = chapter.selectFirst(".dt a")?.attr("href") ?: ""
            chapterList.add(
                Chapter(
                    title = chapter.selectFirst(".chbox .chapternum")?.text() ?: "",
                    date = parseDateString(
                        chapter.selectFirst(".eph-num .chapterdate")?.text()?.trim() ?: ""
                    ),
                    slug = getLastPathSegment(url) ?: "",
                    id = getQuery(dlUrl, "id"),
                    mangaId = document.selectFirst(".bookmark")?.attr("data-id")?.toString(),
                    url = url,
                )
            )
        }

        return chapterList
    }

    override suspend fun getChapter(id: String): ChapterModel {
        val body = api.getChapter(id)
        val document = Jsoup.parse(body.string())
        val imgs = document.select("#readerarea img")
        val imgList = arrayListOf<String>()
        for (img in imgs) {
            imgList.add(img.attr("src") ?: "")
        }

        val breadCrumbs = document.select(".ts-breadcrumb li")
        val mangaTitle = breadCrumbs.getOrNull(1)?.selectFirst("span")?.text()?.trim()
        val chapterTitle =
            breadCrumbs.getOrNull(2)?.selectFirst("span")?.text()?.replace(mangaTitle ?: "", "")
                ?.trim() ?: ""

        val slug =
            getLastPathSegment(breadCrumbs.getOrNull(2)?.selectFirst("a")?.attr("href") ?: "") ?: ""

        val regex = "var post_id = (\\d+);"
        val pattern = Pattern.compile(regex)
        val matcher = pattern.matcher(document.select("script").html())

        var mangaId: String = ""
        if (matcher.find()) {
            mangaId = matcher.group(1)?.toString() ?: ""
        }
        return ChapterModel(
            id = id,
            imgs = imgList,
            title = chapterTitle,
            slug = slug,
            mangaId = mangaId,
        )
    }

}