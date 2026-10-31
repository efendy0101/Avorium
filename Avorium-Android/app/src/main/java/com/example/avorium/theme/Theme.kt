package com.example.avorium.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Avorium Okyanus KaranlÄ±k Tema â€” sabit olarak kullanÄ±lÄ±r
private val AvoriumDarkColorScheme = darkColorScheme(
    primary            = OceanBlue60,
    onPrimary          = Color.White,
    primaryContainer   = NavyContainer,
    onPrimaryContainer = OceanBlue80,
    secondary          = Teal60,
    onSecondary        = Color.White,
    secondaryContainer = Color(0xFF004D59),
    onSecondaryContainer = Teal80,
    tertiary           = GoldenAmber,
    onTertiary         = Color(0xFF2D1900),
    background         = DeepNavy,
    onBackground       = SeaWhite,
    surface            = NavySurface,
    onSurface          = SeaWhite,
    surfaceVariant     = NavyCard,
    onSurfaceVariant   = SeaLight,
    surfaceTint        = OceanBlue60,
    outline            = SeaDim,
    outlineVariant     = Color(0xFF1E3A52),
    error              = ErrorRed,
    onError            = Color.White,
    errorContainer     = Color(0xFF410002),
    onErrorContainer   = Color(0xFFFFDAD6),
    inverseSurface     = SeaLight,
    inverseOnSurface   = NavySurface,
    inversePrimary     = OceanBlueDark,
    scrim              = Color(0x99000000),
)

@Composable
fun AvoriumTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = AvoriumDarkColorScheme,
        typography  = Typography,
        content     = content
    )
}

