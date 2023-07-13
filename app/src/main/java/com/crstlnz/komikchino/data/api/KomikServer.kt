package com.crstlnz.komikchino.data.api

import androidx.annotation.DrawableRes
import com.crstlnz.komikchino.R
import com.crstlnz.komikchino.data.api.source.CosmicScans
import com.crstlnz.komikchino.data.api.source.Kiryuu
import com.crstlnz.komikchino.data.api.source.Mangakatana
import com.crstlnz.komikchino.data.api.source.Manhwalist
import com.crstlnz.komikchino.data.api.source.VoidScans

//enum class KomikServer(val value: String) {
//    KIRYUU("kiryuu"), MANGAKATANA("mangakatana"), VOIDSCANS("voidscans")
//}
enum class Bahasa(@DrawableRes val icon: Int, val title: String) {
    ENGLISH(R.drawable.english, "English"), INDONESIA(R.drawable.indonesia, "Indonesia")
}


enum class KomikServer(
    val title: String,
    val value: String,
    val url: String,
    val multiGenreSearch: Boolean = false,
    val bahasa: Bahasa = Bahasa.INDONESIA
) {
    KIRYUU("Kiryuu", "kiryuu", "https://kiryuu.id/", true),
    MANGAKATANA("MangaKatana", "mangakatana", "https://mangakatana.com/", true, Bahasa.ENGLISH),
    VOIDSCANS("Void Scans", "voidscans", "https://void-scans.com/", true, Bahasa.ENGLISH),
    MANHWALIST("Manhwalist", "manhwalist", "https://manhwalist.xyz/", true),
    COSMICSCANS("Cosmic Scans", "cosmicscans", "https://cosmicscans.com/", true, Bahasa.ENGLISH)
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

        KomikServer.COSMICSCANS -> {
            CosmicScans()
        }
    }
}