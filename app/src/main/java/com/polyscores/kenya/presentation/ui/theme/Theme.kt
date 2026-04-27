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
// Kitale National Polytechnic Brand Colors

val KitaleMaroon = Color(0xFF7B241C)
val KitaleMaroonDark = Color(0xFF511812)
val KitaleMaroonLight = Color(0xFFE57373) // Lighter red/maroon for dark mode contrast
val KitaleGold = Color(0xFFF1C40F)
val KitaleBlack = Color(0xFF1E1E1E)
val KitaleWhite = Color(0xFFFFFFFF)

val Primary = KitaleMaroon
val PrimaryVariant = KitaleMaroonDark
val Secondary = KitaleGold
val SecondaryVariant = Color(0xFFD4AC0D)

val Success = Color(0xFF2ECC71)
val Error = Color(0xFFE74C3C)
val Warning = Color(0xFFF39C12)
val Info = Color(0xFF3498DB)

private val DarkColorScheme = darkColorScheme(
    primary = KitaleMaroonLight,
    onPrimary = KitaleWhite,
    primaryContainer = PrimaryVariant,
    onPrimaryContainer = KitaleWhite,
    secondary = Secondary,
    onSecondary = KitaleWhite,
    secondaryContainer = SecondaryVariant,
    onSecondaryContainer = KitaleWhite,
    tertiary = Color(0xFFFFD700),
    onTertiary = KitaleBlack,
    background = Color(0xFF121212),
    onBackground = KitaleWhite,
    surface = Color(0xFF1E1E1E),
    onSurface = KitaleWhite,
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFE0E0E0),
    error = Error,
    onError = KitaleWhite
)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = KitaleWhite,
    primaryContainer = Color(0xFFF9EBEA),
    onPrimaryContainer = Color(0xFF511812),
    secondary = Secondary,
    onSecondary = KitaleBlack,
    secondaryContainer = Color(0xFFFCF3CF),
    onSecondaryContainer = Color(0xFF7D6608),
    tertiary = Color(0xFFFFD700),
    onTertiary = KitaleBlack,
    background = Color(0xFFF4F6F7), // A light, cool gray to make white cards pop
    onBackground = KitaleBlack,
    surface = KitaleWhite,
    onSurface = KitaleBlack,
    surfaceVariant = KitaleWhite, // Pure white for cards
    onSurfaceVariant = Color(0xFF424242),
    error = Error,
    onError = KitaleWhite
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
