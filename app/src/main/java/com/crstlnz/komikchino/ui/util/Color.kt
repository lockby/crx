package com.crstlnz.komikchino.ui.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

fun lightenColor(color: Color, percentage: Int): Color {
    val factor = percentage / 100f
    val argb = color.toArgb()
    val red = ((argb shr 16) and 0xFF) * (1 + factor)
    val green = ((argb shr 8) and 0xFF) * (1 + factor)
    val blue = (argb and 0xFF) * (1 + factor)
    val alpha = (argb shr 24) and 0xFF
    val lightenedArgb = (alpha shl 24) or
            ((red.coerceAtMost(255f).toInt() and 0xFF) shl 16) or
            ((green.coerceAtMost(255f).toInt() and 0xFF) shl 8) or
            (blue.coerceAtMost(255f).toInt() and 0xFF)
    return Color(lightenedArgb)
}

fun lightenColor(color: Int, percentage: Int): Int {
    val factor = percentage / 100f
    val red = ((color shr 16) and 0xFF) * (1 + factor)
    val green = ((color shr 8) and 0xFF) * (1 + factor)
    val blue = (color and 0xFF) * (1 + factor)
    val alpha = (color ushr 24)
    return (alpha shl 24) or (red.toInt() shl 16) or (green.toInt() shl 8) or blue.toInt()
}