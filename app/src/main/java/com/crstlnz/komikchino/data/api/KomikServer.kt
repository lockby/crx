package com.crstlnz.komikchino.data.api

import android.content.Context
import androidx.annotation.DrawableRes
import com.crstlnz.komikchino.R
import com.crstlnz.komikchino.data.api.source.CosmicScansIndonesia
import com.crstlnz.komikchino.data.api.source.Kiryuu
import com.crstlnz.komikchino.data.api.source.KomikuId
import com.crstlnz.komikchino.data.api.source.Mangakatana
import com.crstlnz.komikchino.data.api.source.Manhwalist
import com.crstlnz.komikchino.data.api.source.MirrorKomik
import com.crstlnz.komikchino.data.api.source.VoidScans

enum class Bahasa(@DrawableRes val icon: Int, val title: String) {
    ENGLISH(R.drawable.english, "English"), INDONESIA(R.drawable.indonesia, "Indonesia")
}


enum class KomikServer(
    val title: String,
    val value: String,
    val url: String,
    val multiGenreSearch: Boolean = false,
    val bahasa: Bahasa = Bahasa.INDONESIA,
    val haveComment: Boolean = true
) {
    KIRYUU("Kiryuu", "kiryuu", "https://kiryuu.id/", true),
    MANGAKATANA("MangaKatana", "mangakatana", "https://mangakatana.com/", true, Bahasa.ENGLISH),
    VOIDSCANS("Void Scans", "voidscans", "https://hivetoon.com/", true, Bahasa.ENGLISH),
    MANHWALIST("Manhwalist", "manhwalist", "https://manhwalist.com/", true),
    MIRRORKOMIK("MirrorKomik", "mirrorkomik", "https://mirrorkomik.co/", false, Bahasa.INDONESIA),

    //    COSMICSCANS("Cosmic Scans", "cosmicscans", "https://cosmic-scans.com/", true, Bahasa.ENGLISH),
    COSMICSCANSINDO(
        "Cosmic Scans Indo",
        "cosmicscansindo",
        "https://cosmicscans.id/",
        true,
        Bahasa.INDONESIA
    ),
    KOMIKUID("Komiku Id", "komikuid", "https://komiku.id/", false, Bahasa.INDONESIA, false)
}

fun getScraper(databaseKey: KomikServer, context: Context): ScraperBase {
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

//        KomikServer.COSMICSCANS -> {
//            CosmicScans()
//        }

        KomikServer.COSMICSCANSINDO -> {
            CosmicScansIndonesia()
        }

        KomikServer.MIRRORKOMIK -> {
            MirrorKomik(context)
        }

        KomikServer.KOMIKUID -> {
            KomikuId()
        }
    }
}