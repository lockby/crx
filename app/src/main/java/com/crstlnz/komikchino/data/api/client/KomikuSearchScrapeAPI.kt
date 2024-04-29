package com.crstlnz.komikchino.data.api.client

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface KomikuSearchScrapeAPI {
    @GET("/page/{page}")
    suspend fun search(
        @Path("page") page: Int,
        @Query("s") search: String,
        @Query("post_type") postType: String = "manga"
    ): ResponseBody
}