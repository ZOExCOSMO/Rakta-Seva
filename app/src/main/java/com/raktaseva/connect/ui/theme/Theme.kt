package com.raktaseva.connect.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary          = RedPrimary,
    onPrimary        = TextPrimary,
    primaryContainer = RedSurface,
    background       = BgDark,
    surface          = BgCard,
    surfaceVariant   = BgElevated,
    onBackground     = TextPrimary,
    onSurface        = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline          = BorderSubtle,
    error            = RedLight,
)

@Composable
fun RaktaSevaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography  = Typography,
        content     = content
    )
}