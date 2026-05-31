package com.example.cyloop.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Updated Dark Colors based on image
val CyLoopDarkBg = Color(0xFF0B0B15)
val CyLoopDarkSurface = Color(0xFF151525)
val CyLoopDarkSurfaceVariant = Color(0xFF1E1E2E)
val CyLoopGold = Color(0xFFF5D45E)
val CyLoopPurple = Color(0xFF9E86FF)
val CyLoopTextDark = Color(0xFFFFFFFF)
val CyLoopTextSecondaryDark = Color(0xFF8E8E93)

// Light Mode Colors
val TelegramBlue = Color(0xFF2481CC)
val TelegramLightBlue = Color(0xFF50A2E3)
val TelegramTextLight = Color(0xFF000000)
val TelegramTextSecondaryLight = Color(0xFF636366)

private val DarkColorScheme = darkColorScheme(
    primary = CyLoopGold,
    secondary = CyLoopPurple,
    tertiary = Color(0xFF82B1FF),
    background = CyLoopDarkBg,
    surface = CyLoopDarkSurface,
    surfaceVariant = CyLoopDarkSurfaceVariant,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = CyLoopTextDark,
    onSurface = CyLoopTextDark,
    onSurfaceVariant = CyLoopTextSecondaryDark,
    primaryContainer = Color(0xFF1D2733),
    onPrimaryContainer = Color.White,
    error = Color(0xFFCF6679),
    onError = Color.Black
)

private val LightColorScheme = lightColorScheme(
    primary = TelegramBlue,
    secondary = TelegramLightBlue,
    tertiary = Color(0xFF40B7E0),
    background = Color(0xFFF2F2F7),
    surface = Color.White,
    surfaceVariant = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = TelegramTextLight,
    onSurface = TelegramTextLight,
    onSurfaceVariant = TelegramTextSecondaryLight,
    primaryContainer = Color(0xFFE3F2FD),
    onPrimaryContainer = TelegramBlue,
    error = Color(0xFFB00020),
    onError = Color.White
)

@Composable
fun getAppBackgroundBrush(): Brush {
    return if (isSystemInDarkTheme()) {
        Brush.verticalGradient(listOf(CyLoopDarkBg, Color(0xFF05050A)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFF2F2F7), Color(0xFFE5E5EA)))
    }
}

@Composable
fun CyLoopTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography,
        content = {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                content()
            }
        }
    )
}
