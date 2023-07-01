package com.crstlnz.komikchino.data.model


sealed class SearchResult {
    data class SearchList(
        var page: Int = 1,
        var result: List<ExactMatch> = listOf(),
        var hasNext: Boolean = false
    ) : SearchResult()

    data class ExactMatch(
        var title: String = "",
        var img: String = "",
        var score: Float? = 0f,
        var type: String = "",
        var isColored: Boolean = false,
        var isComplete: Boolean = false,
        var isHot: Boolean = false,
        var url: String = "",
        var slug: String = ""
    ) : SearchResult()
}
//
//data class SearchQuery(
//    var page: Int = 1,
//    var result: List<SearchItem> = listOf(),
//    var hasNext: Boolean = false,
//)
//
//data class SearchItem(
//    var title: String = "",
//    var img: String = "",
//    var score: Float? = 0f,
//    var type: String = "",
//    var isColored: Boolean = false,
//    var isComplete: Boolean = false,
//    var isHot: Boolean = false,
//    var url: String = "",
//    var slug: String = ""
//)