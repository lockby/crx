package com.crstlnz.komikchino.data.util

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources.getSystem
import com.crstlnz.komikchino.config.AppSettings
import com.crstlnz.komikchino.data.api.Bahasa
import com.crstlnz.komikchino.data.api.KomikServer
import com.crstlnz.komikchino.data.model.DisqusConfig
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.Cookie
import java.io.File
import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder
import java.text.SimpleDateFormat
import android.util.Base64
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern
import kotlin.math.log10
import kotlin.math.pow

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

fun isPlural(num: Long, isEnglish: Boolean = true): String {
    if (!isEnglish || num == 1L) return ""
    return "s"
}

fun formatRelativeDate(date: Date): String {
    val isEnglish = AppSettings.komikServer!!.bahasa == Bahasa.ENGLISH
    val now = Calendar.getInstance().time
    val difference = now.time - date.time

    val seconds = difference / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    val weeks = days / 7
    val months = days / 30
    val years = days / 365

    val SECONDS = if (isEnglish) "second" else "detik"
    val MINUTES = if (isEnglish) "minute" else "menit"
    val HOURS = if (isEnglish) "hour" else "jam"
    val DAYS = if (isEnglish) "day" else "hari"
    val WEEKS = if (isEnglish) "week" else "minggu"
    val MONTHS = if (isEnglish) "month" else "bulan"
    val YEARS = if (isEnglish) "year" else "tahun"

    val suffix = if (isEnglish) "ago" else "lalu"

    return when {
        years >= 1 -> "$years $YEARS${isPlural(years, isEnglish)} $suffix"
        months >= 1 -> "$months $MONTHS${isPlural(months, isEnglish)} $suffix"
        weeks >= 1 -> "$weeks $WEEKS${isPlural(weeks, isEnglish)} $suffix"
        days >= 1 -> "$days $DAYS${isPlural(days, isEnglish)} $suffix"
        hours >= 1 -> "$hours $HOURS${isPlural(hours, isEnglish)} $suffix"
        minutes >= 1 -> "$minutes $MINUTES${isPlural(minutes, isEnglish)} $suffix"
        else -> "$seconds $SECONDS${isPlural(seconds, isEnglish)} $suffix"
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

fun extractDomain(url: String): String? {
    try {
        val uri = URI(url)
        val host = uri.host
        if (host != null) {
            val parts = host.split(".")
            return parts.takeLast(2).joinToString(".")
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

fun getAppVersion(context: Context): String {
    val packageName = context.packageName
    try {
        val packageInfo = context.packageManager.getPackageInfo(packageName, 0)
        return packageInfo.versionName
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    }
    return ""
}


fun parseKiryuUpdateTime(timeExpression: String): Date? {
    val regex = "(\\d+)\\s+(detik|menit|jam|hari|minggu|bulan|tahun)\\s+ago".toRegex()
    val matchResult = regex.find(timeExpression)

    if (matchResult != null) {
        val (value, unit) = matchResult.destructured
        val now = Calendar.getInstance()
        return when (unit) {
            "detik" -> {
                now.add(Calendar.SECOND, -value.toInt())
                now.time
            }

            "menit" -> {
                now.add(Calendar.MINUTE, -value.toInt())
                now.time
            }

            "jam" -> {
                now.add(Calendar.HOUR_OF_DAY, -value.toInt())
                now.time
            }

            "hari" -> {
                now.add(Calendar.DAY_OF_YEAR, -value.toInt())
                now.time
            }

            "minggu" -> {
                now.add(Calendar.WEEK_OF_YEAR, -value.toInt())
                now.time
            }

            "bulan" -> {
                now.add(Calendar.MONTH, -value.toInt())
                now.time
            }

            "tahun" -> {
                now.add(Calendar.YEAR, -value.toInt())
                now.time
            }

            else -> null
        }
    }
    return null
}

fun parseRelativeTime(relativeTime: String): Date {
    val calendar = Calendar.getInstance()
    when {
        relativeTime.contains("second") -> {
            val seconds = relativeTime.split(" ")[0].toInt()
            calendar.add(Calendar.SECOND, -seconds)
        }

        relativeTime.contains("minute") -> {
            val minutes = relativeTime.split(" ")[0].toInt()
            calendar.add(Calendar.MINUTE, -minutes)
        }

        relativeTime.contains("hour") -> {
            val hours = relativeTime.split(" ")[0].toInt()
            calendar.add(Calendar.HOUR, -hours)
        }

        relativeTime.contains("day") -> {
            val days = relativeTime.split(" ")[0].toInt()
            calendar.add(Calendar.DAY_OF_YEAR, -days)
        }

        relativeTime.contains("week") -> {
            val weeks = relativeTime.split(" ")[0].toInt()
            calendar.add(Calendar.WEEK_OF_YEAR, -weeks)
        }

        relativeTime.contains("month") -> {
            val months = relativeTime.split(" ")[0].toInt()
            calendar.add(Calendar.MONTH, -months)
        }

        relativeTime.contains("year") -> {
            val years = relativeTime.split(" ")[0].toInt()
            calendar.add(Calendar.YEAR, -years)
        }
    }
    return calendar.time
}

fun getVoidScansDisqus(string: String): DisqusConfig? {
    val identifierPattern = Pattern.compile("\"disqusIdentifier\":\"([^\"]+)\"")
    val urlPattern = Pattern.compile("\"disqusUrl\":\"([^\"]+)\"")

    val identifierMatcher = identifierPattern.matcher(string)
    val urlMatcher = urlPattern.matcher(string)

    var id: String? = null
    var url: String? = null

    if (identifierMatcher.find()) {
        id = identifierMatcher.group(1)
    }

    if (urlMatcher.find()) {
        url = urlMatcher.group(1)
    }

    return if (id != null && url != null) {
        DisqusConfig(
            identifier = id,
            url = url
        )
    } else {
        null
    }
}


fun getCacheFolderSize(context: Context, folderPath: String): Long {
    val cacheDir = context.cacheDir.resolve("image_cache")
    return getFolderSize(cacheDir)
}

fun getFolderSize(directory: File): Long {
    var size: Long = 0

    directory.listFiles()?.forEach { file ->
        size += if (file.isDirectory) {
            getFolderSize(file)
        } else {
            file.length()
        }
    }

    return size
}

fun formatSize(size: Long): String {
    if (size <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
    return String.format("%.2f %s", size / 1024.0.pow(digitGroups.toDouble()), units[digitGroups])
}

fun clearCache(context: Context, cachePath: String) {
    val cacheDir = context.cacheDir.resolve(cachePath)
    if (cacheDir.exists() && cacheDir.isDirectory) {
        val cacheFiles = cacheDir.listFiles()
        if (cacheFiles != null) {
            for (file in cacheFiles) {
                file.delete()
            }
        }
    }
}

fun parseCookieString(cookies: String, url: String): List<Cookie>? {
    val updatedCookies = mutableListOf<Cookie>()
    val cookieArray = cookies.split(";")
    for (cookieItem in cookieArray) {
        val cookiePair = cookieItem.trim().split("=")
        if (cookiePair.size == 2) {
            val cookieName = cookiePair[0]
            val cookieValue = cookiePair[1]
            val cookie =
                Cookie.Builder().domain(extractDomain(url) ?: "").path("/").name(cookieName)
                    .value(cookieValue).build()
            updatedCookies.add(cookie)
        }
    }

    if (updatedCookies.isEmpty()) return null
    return updatedCookies
}

fun getCurrentDateString(): String {
    return SimpleDateFormat("dd MMMM yyyy HH:mm:ss", Locale("id", "ID")).format(Date())
}

fun decodeBase64(text: String): String {
    return Base64.encodeToString(text.toByteArray(), Base64.DEFAULT) ?: "="
//    return Base64.getEncoder().encodeToString(text.toByteArray())
}