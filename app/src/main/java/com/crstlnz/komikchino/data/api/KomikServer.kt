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
    VOIDSCANS("voidscans", "https://void-scans.com/", true)
}

fun getServerUrl(server: KomikServer): String {
    return when (server) {
        KomikServer.KIRYUU -> "https://kiryuu.id/"
        KomikServer.MANGAKATANA -> "https://mangakatana.com/"
        KomikServer.VOIDSCANS -> "https://void-scans.com/"
    }
}
