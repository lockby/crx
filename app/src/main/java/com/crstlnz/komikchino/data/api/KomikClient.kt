package com.crstlnz.komikchino.data.api

import com.crstlnz.komikchino.config.AppSettings
import com.crstlnz.komikchino.data.api.client.CosmicScansIndonesiaScrapeAPI
import com.crstlnz.komikchino.data.api.client.CosmicScansScrapeAPI
import com.crstlnz.komikchino.data.api.client.GithubAPI
import com.crstlnz.komikchino.data.api.client.KiryuuScrapeAPI
import com.crstlnz.komikchino.data.api.client.KomikuScrapeAPI
import com.crstlnz.komikchino.data.api.client.MangaKatanaScrapeAPI
import com.crstlnz.komikchino.data.api.client.ManhwalistScrapeAPI
import com.crstlnz.komikchino.data.api.client.MirrorKomikScrapeAPI
import com.crstlnz.komikchino.data.api.client.VoidScansScrapeAPI
import com.crstlnz.komikchino.data.api.source.CosmicScansIndonesia
import okhttp3.CipherSuite
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory


object ApiClient {
    private val GITHUB_API = "https://api.github.com/"
    private val githubClient = Retrofit.Builder()
        .addConverterFactory(JacksonConverterFactory.create())
        .baseUrl(GITHUB_API)
        .build()

    fun getGithubClient(): GithubAPI {
        return githubClient.create(GithubAPI::class.java)
    }
}

object KomikClient {
    private val retrofitScraper = Retrofit.Builder()


//    private val BASE_URL = "https://komikcast.net/"
//    private val BASE_URL_API = "https://komikcast.net/wp-json/wp/v2/"
//    private val retrofit = Retrofit.Builder()
//        .addConverterFactory(JacksonConverterFactory.create())
//        .baseUrl(BASE_URL_API)
//        .build()


    fun getKiryuuClient(): KiryuuScrapeAPI {
        return retrofitScraper
            .baseUrl(KomikServer.KIRYUU.url)
            .client(AppSettings.customHttpClient)
            .build()
            .create(KiryuuScrapeAPI::class.java)
    }

    fun getMangaKatanaClient(): MangaKatanaScrapeAPI {
        return retrofitScraper
            .baseUrl(KomikServer.MANGAKATANA.url)
            .client(AppSettings.customHttpClient)
            .build()
            .create(MangaKatanaScrapeAPI::class.java)
    }

    fun getVoidScansClient(): VoidScansScrapeAPI {
        return retrofitScraper
            .baseUrl(KomikServer.VOIDSCANS.url)
            .client(AppSettings.customHttpClient)
            .build()
            .create(VoidScansScrapeAPI::class.java)
    }

    fun getManhwalistClient(): ManhwalistScrapeAPI {
        return retrofitScraper
            .baseUrl(KomikServer.MANHWALIST.url)
            .client(AppSettings.customHttpClient)
            .build()
            .create(ManhwalistScrapeAPI::class.java)
    }

    fun getCosmicScansClient(): CosmicScansScrapeAPI {
        return retrofitScraper
            .baseUrl(KomikServer.COSMICSCANS.url)
            .client(AppSettings.customHttpClient)
            .build()
            .create(CosmicScansScrapeAPI::class.java)
    }

    fun getCosmicScansIndonesiaClient(): CosmicScansIndonesiaScrapeAPI {
        return retrofitScraper
            .baseUrl(KomikServer.COSMICSCANSINDO.url)
            .client(AppSettings.customHttpClient)
            .build()
            .create(CosmicScansIndonesiaScrapeAPI::class.java)
    }

    fun getMirrorKomikClient(): MirrorKomikScrapeAPI {
        return retrofitScraper
            .addConverterFactory(JacksonConverterFactory.create())
            .baseUrl(KomikServer.MIRRORKOMIK.url)
            .client(AppSettings.customHttpClient)
            .build()
            .create(MirrorKomikScrapeAPI::class.java)
    }

    fun getKomikuIdClient(): KomikuScrapeAPI {
        return retrofitScraper
            .addConverterFactory(JacksonConverterFactory.create())
            .baseUrl(KomikServer.KOMIKUID.url)
            .client(AppSettings.customHttpClient)
            .build()
            .create(KomikuScrapeAPI::class.java)
    }
}