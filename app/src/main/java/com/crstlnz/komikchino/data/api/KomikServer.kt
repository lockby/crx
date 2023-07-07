package com.crstlnz.komikchino.data.api

//enum class KomikServer(val value: String) {
//    KIRYUU("kiryuu"), MANGAKATANA("mangakatana"), VOIDSCANS("voidscans")
//}

enum class KomikServer(
    val value: String,
    val url: String,
    val multiGenreSearch: Boolean = false
) {
    KIRYUU("kiryuu", "https://kiryuu.id/", true),
    MANGAKATANA("mangakatana", "https://mangakatana.com/", true),
    VOIDSCANS("voidscans", "https://void-scans.com/", true),
    MANHWALIST("manhwalist", "https://manhwalist.xyz/", true)
}