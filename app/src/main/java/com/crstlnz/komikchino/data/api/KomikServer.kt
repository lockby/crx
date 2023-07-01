package com.crstlnz.komikchino.data.api

enum class KomikServer(val value: String) {
    KIRYUU("kiryuu"), MANGAKATANA("mangakatana"), VOIDSCANS("voidscans")
}

fun getServerUrl(server: KomikServer): String {
    return when (server) {
        KomikServer.KIRYUU -> "https://kiryuu.id/"
        KomikServer.MANGAKATANA -> "https://mangakatana.com/"
        KomikServer.VOIDSCANS -> "https://void-scans.com/"
    }
}