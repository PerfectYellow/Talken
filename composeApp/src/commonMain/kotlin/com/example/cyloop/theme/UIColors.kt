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

// Telegram-inspired Light Colors
val TelegramBlue = Color(0xFF2481CC)
val TelegramLightBlue = Color(0xFF50A2E3)
val TelegramBgLight = Color(0xFFFFFFFF)
val TelegramSurfaceLight = Color(0xFFF1F1F1)
val TelegramTextLight = Color(0xFF000000)
val TelegramTextSecondaryLight = Color(0xFF8E8E93)

// Telegram-inspired Dark Colors
val TelegramBgDark = Color(0xFF0E1621)
val TelegramSurfaceDark = Color(0xFF17212B)
val TelegramSurfaceVariantDark = Color(0xFF242F3D)
val TelegramTextDark = Color(0xFFFFFFFF)
val TelegramTextSecondaryDark = Color(0xFF7F91A4)
val TelegramAccentDark = Color(0xFF64B5F6)

private val DarkColorScheme = darkColorScheme(
    primary = TelegramAccentDark,
    secondary = TelegramLightBlue,
    tertiary = Color(0xFF82B1FF),
    background = TelegramBgDark,
    surface = TelegramSurfaceDark,
    surfaceVariant = TelegramSurfaceVariantDark,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = TelegramTextDark,
    onSurface = TelegramTextDark,
    onSurfaceVariant = TelegramTextSecondaryDark,
    primaryContainer = Color(0xFF1D2733),
    onPrimaryContainer = Color.White,
    error = Color(0xFFCF6679),
    onError = Color.Black
)

private val LightColorScheme = lightColorScheme(
    primary = TelegramBlue,
    secondary = TelegramLightBlue,
    tertiary = Color(0xFF40B7E0),
    background = TelegramBgLight,
    surface = TelegramSurfaceLight,
    surfaceVariant = Color(0xFFEBEDF0),
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
        Brush.verticalGradient(listOf(Color(0xFF1D2733), Color(0xFF0E1621)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFE3F2FD), Color(0xFFFFFFFF)))
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
