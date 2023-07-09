package com.crstlnz.komikchino

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.crstlnz.komikchino.config.AppSettings
import com.crstlnz.komikchino.data.api.KomikServer
import com.crstlnz.komikchino.data.api.getScraper
import com.crstlnz.komikchino.data.model.HomeData
import com.crstlnz.komikchino.data.util.CustomCookieJar
import com.crstlnz.komikchino.data.util.HttpErrorInterceptor
import com.crstlnz.komikchino.data.util.RequestHeaderInterceptor
import com.crstlnz.komikchino.data.util.parseCookieString
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.test.runTest
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class ScraperTest {

    init {
        AppSettings.userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36"
        AppSettings.cookieJar = CustomCookieJar(ApplicationProvider.getApplicationContext())
        AppSettings.customHttpClient = OkHttpClient.Builder()
            .followRedirects(true) // Enable automatic following of redirects
            .followSslRedirects(true)
            .cookieJar(AppSettings.cookieJar)
            .addInterceptor(HttpErrorInterceptor())
            .addInterceptor(RequestHeaderInterceptor())
            .addInterceptor(UrlLoggingInterceptor())
            .build()
        setCookies(
            "cf_clearance=jwRCSe1Fc8yGUV0yfQUcqJKZ1wOzXVqHcLH3YCUcI6o-1688905897-0-250",
            KomikServer.VOIDSCANS.url
        )
    }

    private suspend fun scrapeHome(): MutableMap<KomikServer, HomeData> {
        val results = mutableMapOf<KomikServer, HomeData>()
        for (server in KomikServer.values()) {
            val scraper = getScraper(server)
            results[server] = scraper.getHome()
        }

        return results
    }

    private fun setCookies(cookies: String, url: String) {
        (AppSettings.cookieJar as CustomCookieJar).updateCookies(
            url.toHttpUrl(), parseCookieString(cookies, url)
        )
    }

    @Test
    fun checkHome() = runTest {
        val data = scrapeHome()
        for ((server, homeData) in data.entries) {
            Log.d("HOME TESTING", server.value)
            Log.d("HOME TESTING", homeData.toString())
        }
    }
}