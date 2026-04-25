package com.polyscores.kenya.presentation.ui.theme

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

// ==================== COLOR PALETTE ====================
// Inspired by Kenyan flag colors and sports themes

val KenyaGreen = Color(0xFF009900)
val KenyaRed = Color(0xFF990000)
val KenyaBlack = Color(0xFF000000)
val KenyaWhite = Color(0xFFFFFFFF)

val Primary = KenyaGreen
val PrimaryVariant = Color(0xFF007700)
val Secondary = KenyaRed
val SecondaryVariant = Color(0xFF770000)

val Success = Color(0xFF4CAF50)
val Error = Color(0xFFF44336)
val Warning = Color(0xFFFF9800)
val Info = Color(0xFF2196F3)

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = KenyaWhite,
    primaryContainer = PrimaryVariant,
    onPrimaryContainer = KenyaWhite,
    secondary = Secondary,
    onSecondary = KenyaWhite,
    secondaryContainer = SecondaryVariant,
    onSecondaryContainer = KenyaWhite,
    tertiary = Color(0xFFFFD700),
    onTertiary = KenyaBlack,
    background = Color(0xFF121212),
    onBackground = KenyaWhite,
    surface = Color(0xFF1E1E1E),
    onSurface = KenyaWhite,
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFE0E0E0),
    error = Error,
    onError = KenyaWhite
)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = KenyaWhite,
    primaryContainer = Color(0xFFCCFFCC),
    onPrimaryContainer = Color(0xFF003300),
    secondary = Secondary,
    onSecondary = KenyaWhite,
    secondaryContainer = Color(0xFFFFD6D6),
    onSecondaryContainer = Color(0xFF330000),
    tertiary = Color(0xFFFFD700),
    onTertiary = KenyaBlack,
    background = Color(0xFFFFFBFF),
    onBackground = KenyaBlack,
    surface = KenyaWhite,
    onSurface = KenyaBlack,
    surfaceVariant = Color(0xFFF0F0F0),
    onSurfaceVariant = Color(0xFF424242),
    error = Error,
    onError = KenyaWhite
)

@Composable
fun PolyScoresTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
