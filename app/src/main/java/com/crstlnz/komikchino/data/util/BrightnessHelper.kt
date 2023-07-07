package com.crstlnz.komikchino.data.util

import android.content.Context
import android.provider.Settings

const val maxBrightness = 255F
fun getCurrentBrightness(context: Context): Float {
    return Settings.System.getInt(
        context.contentResolver,
        Settings.System.SCREEN_BRIGHTNESS
    ) / maxBrightness
}

fun setBrightness(context: Context, brightness: Float) {
    val brightnessMode = Settings.System.getInt(
        context.contentResolver,
        Settings.System.SCREEN_BRIGHTNESS_MODE
    )

    if (brightnessMode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
        // If automatic brightness is enabled, disable it first
        Settings.System.putInt(
            context.contentResolver,
            Settings.System.SCREEN_BRIGHTNESS_MODE,
            Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
        )
    }


    val brightnessValue = (brightness * maxBrightness).toInt()

    Settings.System.putInt(
        context.contentResolver,
        Settings.System.SCREEN_BRIGHTNESS,
        brightnessValue
    )
}