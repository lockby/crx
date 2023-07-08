package com.crstlnz.komikchino.data.api

//enum class KomikServer(val value: String) {
//    KIRYUU("kiryuu"), MANGAKATANA("mangakatana"), VOIDSCANS("voidscans")
//}
enum class Bahasa {
    ENGLISH, INDONESIA
}

enum class KomikServer(
    val value: String,
    val url: String,
    val multiGenreSearch: Boolean = false,
    val bahasa: Bahasa = Bahasa.INDONESIA
) {
    KIRYUU("kiryuu", "https://kiryuu.id/", true),
    MANGAKATANA("mangakatana", "https://mangakatana.com/", true, Bahasa.ENGLISH),
    VOIDSCANS("voidscans", "https://void-scans.com/", true, Bahasa.ENGLISH),
    MANHWALIST("manhwalist", "https://manhwalist.xyz/", true)
}