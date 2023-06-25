package com.crstlnz.komikchino.data.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit

object KomikClient {
    private val retrofitScraper = Retrofit.Builder()
//    private val BASE_URL = "https://komikcast.net/"
//    private val BASE_URL_API = "https://komikcast.net/wp-json/wp/v2/"
//    private val retrofit = Retrofit.Builder()
//        .addConverterFactory(JacksonConverterFactory.create())
//        .baseUrl(BASE_URL_API)
//        .build()

    private val customHTTPClient: OkHttpClient = OkHttpClient.Builder()
        .followRedirects(true) // Enable automatic following of redirects
        .followSslRedirects(true)
        .build()

    fun getKiryuuClient(): KiryuuScrapeAPI {
        return retrofitScraper
            .baseUrl("https://kiryuu.id/")
            .client(customHTTPClient)
            .build()
            .create(KiryuuScrapeAPI::class.java)
    }

    fun getMangaKatanaClient(): MangaKatanaScrapeAPI {
        return retrofitScraper
            .baseUrl("https://mangakatana.com/")
            .client(customHTTPClient)
            .build()
            .create(MangaKatanaScrapeAPI::class.java)
    }
}