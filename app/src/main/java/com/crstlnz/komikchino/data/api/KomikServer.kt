package com.crstlnz.komikchino.data.api

import com.crstlnz.komikchino.data.api.source.Kiryuu
import com.crstlnz.komikchino.data.api.source.Mangakatana
import com.crstlnz.komikchino.data.api.source.Manhwalist
import com.crstlnz.komikchino.data.api.source.VoidScans

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

fun getScraper(databaseKey: KomikServer): ScraperBase {
    return when (databaseKey) {
        KomikServer.KIRYUU -> {
            Kiryuu()
        }

        KomikServer.MANGAKATANA -> {
            Mangakatana()
        }

        KomikServer.VOIDSCANS -> {
            VoidScans()
        }

        KomikServer.MANHWALIST -> {
            Manhwalist()
        }
    }
}