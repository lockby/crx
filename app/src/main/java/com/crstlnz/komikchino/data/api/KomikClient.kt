package com.crstlnz.komikchino.data.api

import com.crstlnz.komikchino.config.AppSettings
import com.crstlnz.komikchino.data.api.client.GithubAPI
import com.crstlnz.komikchino.data.api.client.KiryuuScrapeAPI
import com.crstlnz.komikchino.data.api.client.MangaKatanaScrapeAPI
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
            .baseUrl("https://kiryuu.id/")
            .client(AppSettings.customHttpClient)
            .build()
            .create(KiryuuScrapeAPI::class.java)
    }

    fun getMangaKatanaClient(): MangaKatanaScrapeAPI {
        return retrofitScraper
            .baseUrl("https://mangakatana.com/")
            .client(AppSettings.customHttpClient)
            .build()
            .create(MangaKatanaScrapeAPI::class.java)
    }

    fun getVoidScansClient(): VoidScansScrapeAPI {
        return retrofitScraper
            .baseUrl("https://void-scans.com/")
            .client(AppSettings.customHttpClient)
            .build()
            .create(VoidScansScrapeAPI::class.java)
    }
}