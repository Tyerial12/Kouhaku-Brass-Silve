package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val KouhakuConsoleColorScheme = darkColorScheme(
    primary = HeartBrassGold,
    onPrimary = Color(0xFF1A1204),
    primaryContainer = Color(0xFF4A3612),
    onPrimaryContainer = HeartBrassBright,
    secondary = SoulSpireSilver,
    onSecondary = Color(0xFF0F1922),
    secondaryContainer = Color(0xFF263747),
    onSecondaryContainer = SoulSpireBright,
    tertiary = ToriiVermilion,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF561D13),
    onTertiaryContainer = Color(0xFFFFB4A8),
    background = HandheldChassisDark,
    onBackground = RetroLcdParchment,
    surface = HandheldBezelSurface,
    onSurface = RetroLcdParchment,
    surfaceVariant = HandheldPanelElevated,
    onSurfaceVariant = SoulSpireBright
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = KouhakuConsoleColorScheme,
        typography = Typography,
        content = content
    )
}
