package com.crstlnz.komikchino.data.util

import android.content.res.Resources.getSystem
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.net.URLDecoder
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

//val Int.dp: Int get() = (this / getSystem().displayMetrics.density).toInt()
val Int.px: Int get() = (this * getSystem().displayMetrics.density).toInt()

fun getLastPathSegment(url: String): String? {
    val regex = "/([^/]+)/?$".toRegex()
    val matchResult = regex.find(url)
    return matchResult?.groupValues?.get(1)
}

fun getLastTwoSegments(url: String): String {
    val sanitizedUrl = url.removeSuffix("/")
    val segments = sanitizedUrl.split("/")
    val lastTwoSegments = segments.takeLast(2)
    return lastTwoSegments.joinToString("/")
}
fun formatRelativeDate(date: Date): String {
    val now = Calendar.getInstance().time
    val difference = now.time - date.time

    val seconds = difference / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    val weeks = days / 7
    val months = days / 30
    val years = days / 365

    return when {
        years >= 1 -> "$years tahun yang lalu"
        months >= 1 -> "$months bulan yang lalu"
        weeks >= 1 -> "$weeks minggu yang lalu"
        days >= 1 -> "$days hari yang lalu"
        hours >= 1 -> "$hours jam yang lalu"
        minutes >= 1 -> "$minutes menit yang lalu"
        else -> "$seconds detik yang lalu"
    }
}

fun parseDateString(
    dateString: String, pattern: String = "MMMM d, yyyy", locale: Locale = Locale("id", "ID")
): Date? {
    val format = SimpleDateFormat(pattern, locale)
    return try {
        format.parse(dateString)
    } catch (e: Exception) {
        null
    }
}

fun isToday(date: Date): Boolean {
    val currentDate = Calendar.getInstance()
    val calendar = Calendar.getInstance()
    calendar.time = date

    val currentYear = currentDate.get(Calendar.YEAR)
    val currentMonth = currentDate.get(Calendar.MONTH)
    val currentDay = currentDate.get(Calendar.DAY_OF_MONTH)

    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    return currentYear == year && currentMonth == month && currentDay == day
}

//fun getIdFromUrl(url: String): String? {
//    return getQuery(url, "id")
//}

fun getQuery(url: String, queryName: String): String? {
    val query = url.substringAfterLast("?")
    val idParameter = query.substringAfter("${queryName}=")

    return if (idParameter.isNotEmpty()) {
        idParameter.substringBefore("&")
    } else {
        null
    }
}

fun getBackgroundImage(styleAttribute: String): String {
    val startIndex = styleAttribute.indexOf("url('") + 5
    val endIndex = styleAttribute.indexOf("')")
    return styleAttribute.substring(startIndex, endIndex)
}


val mapper = jacksonObjectMapper()
fun <T> convertToStringURL(data: T): String {
    return try {
        URLEncoder.encode(mapper.writeValueAsString(data), "utf-8")
    } catch (e: Exception) {
        ""
    }
}

fun <T> decodeStringURL(data: String, type: JavaType? = null): T? {
    @Suppress("UNCHECKED_CAST")
    if (type == null) return URLDecoder.decode(data, "utf-8") as T
    return try {
        mapper.readValue<T>(URLDecoder.decode(data, "utf-8"), type)
    } catch (e: Exception) {
        null
    }
}

fun parseMangaKatanaChapterImages(html: String): List<String> {
    return try {
        val regex = Regex("""var thzq=\[(.*?)];""")
        val matchResult = regex.find(html)
        val str = matchResult?.groupValues?.get(1)?.trim()
        str?.split(",")?.map { it.trim().removeSurrounding("'") }?.filter { it.isNotEmpty() }
            ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }

}