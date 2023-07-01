package com.crstlnz.komikchino.data.util

import android.util.Log
import com.crstlnz.komikchino.config.AppSettings
import com.crstlnz.komikchino.config.CloudflareState
import kotlinx.coroutines.flow.update
import okhttp3.Interceptor
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import retrofit2.HttpException
import java.io.IOException


class HttpErrorInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        // check cloudflare block
        if (!response.isSuccessful && isBlocked(response)) {
            AppSettings.cloudflareTry += 1
            if (AppSettings.cloudflareTry <= 5) {
                AppSettings.cloudflareState.update {
                    CloudflareState(
                        isBlocked = true,
                        key = it.key + 1
                    )
                }
            }
        } else {
            AppSettings.cloudflareTry = 0
        }
        return response
    }

    private fun isBlocked(e: Response): Boolean {
        if (e.code != 403) return false
        if (e.headers["Cf-Mitigated"] == "challenge") return true
        val document = Jsoup.parse(e.body?.string() ?: "")
        return document.selectFirst("#challenge-form") != null || document.selectFirst("#challenge-error-title") != null
    }
}