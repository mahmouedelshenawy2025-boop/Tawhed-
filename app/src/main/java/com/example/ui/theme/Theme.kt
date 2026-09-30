package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = IslamicGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD0EADA),
    onPrimaryContainer = IslamicGreen,
    secondary = IslamicGold,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFFFBEBC9),
    onSecondaryContainer = Color(0xFF4A3408),
    background = CreamBackground,
    onBackground = TextDark,
    surface = CreamCard,
    onSurface = TextDark,
    surfaceVariant = CreamSurface,
    onSurfaceVariant = TextMuted,
    outline = CreamBorder
)

private val DarkColorScheme = darkColorScheme(
    primary = IslamicGoldLight,
    onPrimary = Color.Black,
    primaryContainer = IslamicGreenLight,
    onPrimaryContainer = Color.White,
    secondary = IslamicGold,
    onSecondary = Color.Black,
    background = Color(0xFF121714),
    onBackground = Color(0xFFE8ECE9),
    surface = Color(0xFF1B231F),
    onSurface = Color(0xFFE8ECE9),
    surfaceVariant = Color(0xFF242E29),
    onSurfaceVariant = Color(0xFFBAC5BE),
    outline = Color(0xFF38453F)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Keep default bright for clear black Arabic text
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
