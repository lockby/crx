package com.crstlnz.komikchino.data.util

import android.content.Context
import android.content.res.Resources.getSystem
import android.graphics.Bitmap
import android.renderscript.Allocation
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.renderscript.Element
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import jp.wasabeef.glide.transformations.internal.FastBlur
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
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


fun parseRelativeDate(relativeDate: String): Date? {
    val calendar = Calendar.getInstance()
    calendar.time = Date()

    try {
        when {
            relativeDate.contains("tahun") -> {
                val yearsAgo = relativeDate.split(" ")[0].toInt()
                calendar.add(Calendar.YEAR, -yearsAgo)
            }

            relativeDate.contains("bulan") -> {
                val monthsAgo = relativeDate.split(" ")[0].toInt()
                calendar.add(Calendar.MONTH, -monthsAgo)
            }

            relativeDate.contains("minggu") -> {
                val weeksAgo = relativeDate.split(" ")[0].toInt()
                calendar.add(Calendar.WEEK_OF_YEAR, -weeksAgo)
            }

            relativeDate.contains("hari") -> {
                val daysAgo = relativeDate.split(" ")[0].toInt()
                calendar.add(Calendar.DAY_OF_MONTH, -daysAgo)
            }

            relativeDate.contains("jam") -> {
                val hoursAgo = relativeDate.split(" ")[0].toInt()
                calendar.add(Calendar.HOUR_OF_DAY, -hoursAgo)
            }

            relativeDate.contains("menit") -> {
                val minutesAgo = relativeDate.split(" ")[0].toInt()
                calendar.add(Calendar.MINUTE, -minutesAgo)
            }

            relativeDate.contains("detik") -> {
                val secondsAgo = relativeDate.split(" ")[0].toInt()
                calendar.add(Calendar.SECOND, -secondsAgo)
            }

            else -> throw IllegalArgumentException("Format waktu relatif tidak valid")
        }
    } catch (e: Exception) {
        return null
    }
    return calendar.time
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

fun parseDateString(dateString: String, pattern: String = "MMMM d, yyyy"): Date? {
    val format = SimpleDateFormat(pattern, Locale("id", "ID"))
    return try {
        format.parse(dateString)
    } catch (e: Exception) {
        null
    }
}

fun getIdFromUrl(url: String): String? {
    return getQuery(url, "id")
}

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

class BlurTransformation(private val radius: Int = 25) :
    BitmapTransformation() {

    override fun transform(
        pool: BitmapPool,
        toTransform: Bitmap,
        outWidth: Int,
        outHeight: Int
    ): Bitmap {
        return FastBlur.blur(toTransform, radius, true)
    }

    override fun hashCode(): Int {
        return ID.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is BlurTransformation
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(ID_BYTES)
    }

    companion object {
        private const val ID = "com.example.BlurTransformation"
        private val ID_BYTES = ID.toByteArray()
    }
}