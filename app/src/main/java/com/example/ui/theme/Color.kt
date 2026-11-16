package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Sleek Modern Glassmorphic Handheld Chassis & HD-2D Luminous Palette
val HandheldChassisDark = Color(0xFF0E0C16)
val HandheldBezelSurface = Color(0xFF171424)
val HandheldPanelElevated = Color(0xFF231F36)
val GlassSurfaceTranslucent = Color(0xCC1E1A30)
val GlassCardHighlight = Color(0x33FFFFFF)

val HeartBrassGold = Color(0xFFF5B942)
val HeartBrassBright = Color(0xFFFFDF7E)
val SoulSpireSilver = Color(0xFF72C8F8)
val SoulSpireBright = Color(0xFFC4ECFF)

val ToriiVermilion = Color(0xFFFF5740)
val MapleCrimson = Color(0xFFD63230)
val JadeHpGreen = Color(0xFF2EE58D)
val AmberHpWarn = Color(0xFFFFB838)

val RetroLcdParchment = Color(0xFFF4F1FA)
val RetroLcdInk = Color(0xFF121824)
val SpiritWalkerSage = Color(0xFF162736) // Backlit Holo-LCD Cyan/Slate
val SpiritWalkerDarkInk = Color(0xFF72F2EB) // Glowing Holo Cyan Ink

// Elemental Affinity Badge Colors (Vibrant HD-2D Neon/Luminous)
val ElementFlameColor = Color(0xFFFF663B)
val ElementTideColor = Color(0xFF38A8FF)
val ElementGaleColor = Color(0xFF3CE38E)
val ElementEarthColor = Color(0xFFE09F46)
val ElementBrassColor = Color(0xFFF7C948)
val ElementSpiritColor = Color(0xFFB66DFF)

val GlassBorderBrush = Brush.linearGradient(
    colors = listOf(
        HeartBrassGold.copy(alpha = 0.75f),
        SoulSpireSilver.copy(alpha = 0.55f)
    )
)

val GlassSubtleBorderBrush = Brush.linearGradient(
    colors = listOf(
        SoulSpireSilver.copy(alpha = 0.45f),
        HeartBrassGold.copy(alpha = 0.35f)
    )
)
