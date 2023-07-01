package com.crstlnz.komikchino.data.util

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException


class LoggingInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()

        // Log the request URL
        System.out.println("Request URL: " + request.url)

        // Log the cookies
        val cookies: String? = request.header("Cookie")
        Log.d("COOKIES", "Cookies: $cookies")
        // Proceed with the request
        return chain.proceed(request)
    }
}