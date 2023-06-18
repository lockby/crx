package com.crstlnz.komikchino.data.api

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface KomikScrapeAPI {
    @GET("/")
    suspend fun getHome(): ResponseBody

    @GET("page/{page}")
    suspend fun search(@Path("page") page: Int = 1, @Query("s") search: String): ResponseBody

    @GET("komik/{slug}")
    suspend fun getKomikPage(@Path("slug") slug: String): ResponseBody
}

interface KiryuuScrapeAPI {
    @GET("/")
    suspend fun getHome(): ResponseBody

    @GET("page/{page}")
    suspend fun search(@Path("page") page: Int = 1, @Query("s") search: String): ResponseBody

    @GET("manga/{slug}")
    suspend fun getKomikPage(@Path("slug") slug: String): ResponseBody

    @GET("/")
    suspend fun getKomikById(@Query("p") id: Int): ResponseBody

    @GET("/")
    suspend fun getChapter(@Query("p") id: Int): ResponseBody
}
