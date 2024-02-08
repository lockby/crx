package com.crstlnz.komikchino.data.util

import android.content.Context
import android.content.SharedPreferences
import com.crstlnz.komikchino.config.AppSettings
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Response
import java.net.URLEncoder


class EmptyCookieJar : CookieJar {
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return emptyList()
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {

    }

}


class CustomCookieJar(context: Context) : CookieJar {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("CookiePrefs", Context.MODE_PRIVATE)

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val cookieValue = cookies.joinToString(",") { it.toString() }
        sharedPreferences.edit()
            .putString(URLEncoder.encode(removeSubdomain(url.host), "UTF-8"), cookieValue)
            .apply()
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val cookieValue =
            sharedPreferences.getString(URLEncoder.encode(removeSubdomain(url.host), "UTF-8"), null)
                ?.split(",")
        return cookieValue?.mapNotNull {
            Cookie.parse(url, it)
        } ?: emptyList()
    }

    private fun removeSubdomain(host: String): String {
        val parts = host.split("\\.".toRegex())
        return if (parts.size > 2) {
            parts.subList(1, parts.size).joinToString(".")
        } else {
            host
        }
    }

    // Add a method to update cookies
    fun updateCookies(url: HttpUrl, cookies: List<Cookie>?) {
        if (cookies != null)
            this.saveFromResponse(url, cookies)
    }
}

class RequestHeaderInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val modifiedRequest = originalRequest.newBuilder()
            .header("User-Agent", AppSettings.userAgent)
            .build()
        return chain.proceed(modifiedRequest)
    }
}