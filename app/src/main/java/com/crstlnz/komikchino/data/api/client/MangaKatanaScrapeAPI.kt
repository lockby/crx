package com.crstlnz.komikchino.data.api.client

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MangaKatanaScrapeAPI {
    @GET("/")
    suspend fun getHome(): ResponseBody

    @GET("page/{page}")
    suspend fun search(
        @Path("page") page: Int = 1,
        @Query("search") search: String,
        @Query("search_by") searchBy: String = "book_name"
    ): ResponseBody

    @GET("page/{page}")
    suspend fun getLatestUpdate(@Path("page") page: Int = 1): ResponseBody

    @GET("manga/{slug}")
    suspend fun getKomikPage(@Path("slug") slug: String): ResponseBody

    @GET("manga/{slug}/{id}")
    suspend fun getChapter(@Path("slug") slug: String, @Path("id") id: String): ResponseBody

    @GET("manga/page/{page}")
    suspend fun searchByGenre(
        @Path("page") page: Int = 1,
        @Query("include") include: String,
        @Query("include_mode") includeMode: String = "and",
        @Query("order") order: String = "latest",
        @Query("filter") filter: String = "1"
    ): ResponseBody
}