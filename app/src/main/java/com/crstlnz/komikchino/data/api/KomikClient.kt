package com.crstlnz.komikchino.data.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory

object KomikClient {
    private val BASE_URL = "https://komikcast.net/"
    private val BASE_URL_API = "https://komikcast.net/wp-json/wp/v2/"
    private val retrofitScraper = Retrofit.Builder()


    private val retrofit = Retrofit.Builder()
        .addConverterFactory(JacksonConverterFactory.create())
        .baseUrl(BASE_URL_API)
        .build()

    fun getScraperClient(baseUrl: String = BASE_URL): KomikScrapeAPI {
        return retrofitScraper
            .baseUrl(baseUrl)
            .build()
            .create(KomikScrapeAPI::class.java)
    }

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

    fun getAPIClient(): KomikAPI {
        return retrofit.create(KomikAPI::class.java)
    }
}