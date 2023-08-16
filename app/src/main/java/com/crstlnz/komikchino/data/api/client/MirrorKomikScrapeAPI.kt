package com.crstlnz.komikchino.data.api.client

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MirrorKomikScrapeAPI {
    @GET("/")
    suspend fun getHome(): ResponseBody

    @GET("cari")
    suspend fun search(
        @Query("page_komik_info") page: Int = 1,
        @Query("s") search: String
    ): ResponseBody

    @GET("list-update")
    suspend fun getLatestUpdate(@Query("page_komik_info") page: Int = 1): ResponseBody

    @GET("{type}/{slug}")
    suspend fun getKomikBySlug(
        @Path("type") type: String,
        @Path("slug") slug: String
    ): Response<ResponseBody>

    @GET("/chapter/{slug}")
    suspend fun getChapterBySlug(@Path("slug") slug: String): Response<ResponseBody>

    @GET("/genre/{genre}")
    suspend fun searchByGenre(
        @Path("genre") genre: String,
        @Query("page_komik_info") page: Int,
    ): ResponseBody

    @FormUrlEncoded
    @POST("/login")
    suspend fun login(
        @Field("csrf_test_name") csrf: String,
        @Field("login") username: String,
        @Field("password") password: String,
    ): ResponseBody

    @GET("/genre/=")
    suspend fun searchNoGenre(): ResponseBody

    @Headers("X-Requested-With: XMLHttpRequest")
    @GET("/chapter/listchap,{komikId},{chapterId}")
    suspend fun listChapterImages(
        @Path("komikId") komikId: String,
        @Path("chapterId") chapterId: String
    ): List<String>
}
