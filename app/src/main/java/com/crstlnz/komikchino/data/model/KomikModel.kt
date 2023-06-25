package com.crstlnz.komikchino.data.model

import java.util.Date

data class KomikDetail(
    val id: String = "",
    val slug: String = "",
    val title: String = "",
    val img: String = "",
    val banner: String = "",
    val type: String = "",
    val description: String = "",
    val score: Float? = 0f,
    val genre: List<Genre> = listOf<Genre>(),
    val similar: List<SimilarTitle> = listOf<SimilarTitle>(),
    val chapters: List<Chapter> = listOf<Chapter>()
)

data class Chapter(
    val id: String?,
    val mangaId: String?,
    val title: String = "",
    val date: Date? = null,
    val slug: String = "",
    val url: String = ""
)


data class SimilarTitle(
    val title: String = "",
    val img: String = "",
    val genre: String?,
    val type: String = "",
    val isColored: Boolean = false,
    val slug: String = "",
    val url: String = ""
)


//@JsonIgnoreProperties(ignoreUnknown = true)
//data class KomikDetailAPI(
//    @JsonProperty("id") var id: Int? = null,
//    @JsonProperty("date") var date: String? = null,
//    @JsonProperty("date_gmt") var dateGmt: String? = null,
//    @JsonProperty("guid") var guid: Guid? = Guid(),
//    @JsonProperty("modified") var modified: String? = null,
//    @JsonProperty("modified_gmt") var modifiedGmt: String? = null,
//    @JsonProperty("slug") var slug: String? = null,
//    @JsonProperty("status") var status: String? = null,
//    @JsonProperty("type") var type: String? = null,
//    @JsonProperty("link") var link: String? = null,
//    @JsonProperty("title") var title: Title? = Title(),
//    @JsonProperty("content") var content: Content? = Content(),
//    @JsonProperty("featured_media") var featuredMedia: Int? = null,
//    @JsonProperty("parent") var parent: Int? = null,
//    @JsonProperty("comment_status") var commentStatus: String? = null,
//    @JsonProperty("ping_status") var pingStatus: String? = null,
//    @JsonProperty("template") var template: String? = null,
//)
//
//
//data class Guid(
//    @JsonProperty("rendered") var rendered: String? = null
//)
//
//data class Title(
//    @JsonProperty("rendered") var rendered: String? = null
//)
//
//data class Content(
//    @JsonProperty("rendered") var rendered: String? = null,
//    @JsonProperty("protected") var protected: Boolean? = null
//)