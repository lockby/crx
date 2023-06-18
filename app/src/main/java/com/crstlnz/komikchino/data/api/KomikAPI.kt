package com.crstlnz.komikchino.data.api

import com.crstlnz.komikchino.data.model.KomikDetailAPI
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path

interface KomikAPI {
    @GET("manga/{id}")
    suspend fun getKomikDetail(@Path("id") id: Int): KomikDetailAPI
}