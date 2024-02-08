package com.crstlnz.komikchino.data.util

import android.util.Log
import com.crstlnz.komikchino.config.AppSettings
import kotlinx.coroutines.flow.update
import okhttp3.Interceptor
import okhttp3.Response
import org.jsoup.Jsoup
import java.io.IOException


class HttpErrorInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        // check cloudflare block
        if (!response.isSuccessful) {
            Log.d("FETCH ERROR HOST", response.request.url.toString())
        }
        if (!response.isSuccessful && isBlocked(response)) {
            AppSettings.cloudflareState.update {
                it.copy(
                    isBlocked = true, key = it.key + 1, url = chain.request().url.toString()
                )
            }
        } else if (AppSettings.cloudflareState.value.tryCount > 0) {
            AppSettings.cloudflareState.update {
                it.copy(
                    mustManualTrigger = false, isBlocked = false, key = 0, tryCount = 0
                )
            }
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