package com.crstlnz.komikchino.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController

private val DarkColorScheme = darkColors(
    primary = Black2,
    secondary = Black2,
    primaryVariant = Blue,
    background = Black1,
    onPrimary = WhiteGray,
    onSurface = WhiteGray,
    onError = WhiteGray,
    onSecondary = WhiteGray,
    onBackground = WhiteGray,
)


@Composable
fun KomikChinoTheme(
//    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        val systemUiController = rememberSystemUiController()
        SideEffect {
            systemUiController.setSystemBarsColor(
                color = Color(android.graphics.Color.TRANSPARENT),
                darkIcons = false
            )
        }
    }



    MaterialTheme(
        colors = colorScheme,
        typography = Typography,
        content = content
    )
}