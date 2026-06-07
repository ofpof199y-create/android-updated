package com.youtubescript.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Blue40,
    onPrimary = WarmGray99,
    primaryContainer = Blue90,
    onPrimaryContainer = Blue10,
    secondary = DarkBlue40,
    onSecondary = WarmGray99,
    secondaryContainer = Blue90,
    onSecondaryContainer = DarkBlue10,
    surface = WarmGray99,
    onSurface = WarmGray10,
    surfaceVariant = WarmGray95,
    onSurfaceVariant = WarmGray20,
    background = WarmGray99,
    onBackground = WarmGray10,
    error = ErrorRed,
    onError = WarmGray99,
    outline = Blue80
)

private val DarkColorScheme = darkColorScheme(
    primary = Blue80,
    onPrimary = Blue20,
    primaryContainer = Blue30,
    onPrimaryContainer = Blue90,
    secondary = Blue80,
    onSecondary = DarkBlue20,
    secondaryContainer = DarkBlue30,
    onSecondaryContainer = Blue90,
    surface = WarmGray10,
    onSurface = WarmGray90,
    surfaceVariant = WarmGray20,
    onSurfaceVariant = WarmGray90,
    background = DarkBlue10,
    onBackground = WarmGray90,
    error = Color(0xFFEF9A9A),
    onError = DarkBlue10,
    outline = Blue40
)

@Composable
fun YouTubeScriptTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
