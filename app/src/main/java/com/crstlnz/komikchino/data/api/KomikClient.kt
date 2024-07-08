package com.crstlnz.komikchino.data.api

import com.crstlnz.komikchino.config.AppSettings
import com.crstlnz.komikchino.data.api.client.CosmicScansIndonesiaScrapeAPI
import com.crstlnz.komikchino.data.api.client.CosmicScansScrapeAPI
import com.crstlnz.komikchino.data.api.client.GithubAPI
import com.crstlnz.komikchino.data.api.client.KiryuuScrapeAPI
import com.crstlnz.komikchino.data.api.client.KomikuScrapeAPI
import com.crstlnz.komikchino.data.api.client.KomikuSearchScrapeAPI
import com.crstlnz.komikchino.data.api.client.MangaKatanaScrapeAPI
import com.crstlnz.komikchino.data.api.client.ManhwalistScrapeAPI
import com.crstlnz.komikchino.data.api.client.MirrorKomikScrapeAPI
import com.crstlnz.komikchino.data.api.client.VoidScansScrapeAPI
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

class KomikClient<T>(
    val server: KomikServer,
    apiClass: Class<T>,
    val baseUrl: String = server.url,
    builder: (Retrofit.Builder) -> Retrofit.Builder = {
        it
    }
) {
    val api: T =
        builder(Retrofit.Builder().baseUrl(baseUrl).client(AppSettings.customHttpClient)).build()
            .create(apiClass)
}

object KomikClients {
    private val retrofitScraper = Retrofit.Builder()


//    private val BASE_URL = "https://komikcast.net/"
//    private val BASE_URL_API = "https://komikcast.net/wp-json/wp/v2/"
//    private val retrofit = Retrofit.Builder()
//        .addConverterFactory(JacksonConverterFactory.create())
//        .baseUrl(BASE_URL_API)
//        .build()


    fun getKiryuuClient(): KomikClient<KiryuuScrapeAPI> {
        return KomikClient(
            KomikServer.KIRYUU,
            KiryuuScrapeAPI::class.java
        )
    }

    fun getMangaKatanaClient(): KomikClient<MangaKatanaScrapeAPI> {
        return KomikClient(
            KomikServer.MANGAKATANA,
            MangaKatanaScrapeAPI::class.java
        )
    }

    fun getVoidScansClient(): KomikClient<VoidScansScrapeAPI> {
        return KomikClient(
            KomikServer.VOIDSCANS,
            VoidScansScrapeAPI::class.java
        )
    }

    fun getManhwalistClient(): KomikClient<ManhwalistScrapeAPI> {
        return KomikClient(
            KomikServer.MANHWALIST,
            ManhwalistScrapeAPI::class.java
        )
    }

//    fun getCosmicScansClient(): CosmicScansScrapeAPI {
//        return retrofitScraper
//            .baseUrl(KomikServer.COSMICSCANS.url)
//            .client(AppSettings.customHttpClient)
//            .build()
//            .create(CosmicScansScrapeAPI::class.java)
//    }

    fun getCosmicScansIndonesiaClient(): KomikClient<CosmicScansIndonesiaScrapeAPI> {
        return KomikClient(
            KomikServer.COSMICSCANSINDO,
            CosmicScansIndonesiaScrapeAPI::class.java
        )
    }

    fun getMirrorKomikClient(): KomikClient<MirrorKomikScrapeAPI> {
        return KomikClient(
            KomikServer.MIRRORKOMIK,
            MirrorKomikScrapeAPI::class.java
        ) {
            it.addConverterFactory(JacksonConverterFactory.create())
        }
    }

    fun getKomikuIdClient(): KomikClient<KomikuScrapeAPI> {
        return KomikClient(
            KomikServer.KOMIKUID,
            KomikuScrapeAPI::class.java
        ) {
            it.addConverterFactory(JacksonConverterFactory.create())
        }
    }

    fun getKomikuIdSearchClient(): KomikClient<KomikuSearchScrapeAPI> {
        return KomikClient(
            KomikServer.KOMIKUID,
            KomikuSearchScrapeAPI::class.java,
            "https://api.komiku.id/"
        ) {
            it.addConverterFactory(JacksonConverterFactory.create())
        }
    }
}