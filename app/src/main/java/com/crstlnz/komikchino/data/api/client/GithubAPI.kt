package com.crstlnz.komikchino.data.api.client

import com.crstlnz.komikchino.data.model.GithubModel
import retrofit2.http.GET

interface GithubAPI {
    @GET("repos/lockby/crx/releases/latest")
    suspend fun getReleases(): GithubModel
}
