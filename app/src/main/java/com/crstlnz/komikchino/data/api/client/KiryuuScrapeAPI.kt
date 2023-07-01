package com.crstlnz.komikchino.data.api.client

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface KiryuuScrapeAPI {
    @GET("/")
    suspend fun getHome(): ResponseBody

    @GET("page/{page}")
    suspend fun search(@Path("page") page: Int = 1, @Query("s") search: String): ResponseBody

    @GET("page/{page}")
    suspend fun getLatestUpdate(@Path("page") page: Int = 1): ResponseBody

    @GET("manga/{slug}")
    suspend fun getKomikPage(@Path("slug") slug: String): ResponseBody

    @GET("/")
    suspend fun getKomikById(@Query("p") id: String): ResponseBody

    @GET("/")
    suspend fun getChapter(@Query("p") id: String): ResponseBody

    @GET("/{slug}")
    suspend fun getChapterBySlug(@Path("slug") slug: String): ResponseBody
}
