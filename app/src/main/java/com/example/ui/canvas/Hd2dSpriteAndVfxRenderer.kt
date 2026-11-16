package com.example.ui.canvas

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.model.ElementAffinity
import com.example.model.MonsterSpecies
import com.example.model.TimeOfDay
import com.example.ui.theme.HeartBrassBright
import com.example.ui.theme.SoulSpireBright
import com.example.ui.theme.ToriiVermilion
import com.example.viewmodel.CombatVfxEvent
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * HD-2D Multi-Layered Shaded Monster Sprite Renderer:
 * 1. Radial elemental aura glow + soft ground shadow
 * 2. Crisp dark ink contour outline pass around the monster silhouette
 * 3. Smoothly shaded gradient body geometry with rounded bevels
 * 4. Specular rim highlights & eye catchlights
 */
fun DrawScope.drawMonsterPixelSprite(
    species: MonsterSpecies,
    left: Float,
    top: Float,
    spriteWidth: Float,
    spriteHeight: Float,
    animPhase: Float = 0.5f
) {
    val rows = species.pixelRows
    val gridH = rows.size
    val gridW = rows.firstOrNull()?.length ?: 12
    val cellW = (spriteWidth / gridW).coerceAtLeast(1f)
    val cellH = (spriteHeight / gridH).coerceAtLeast(1f)
    val center = Offset(left + spriteWidth * 0.5f, top + spriteHeight * 0.52f)
    val radius = min(spriteWidth, spriteHeight) * 0.58f

    // 1. Soft Elemental Aura Halo behind sprite
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                species.primaryElement.color.copy(alpha = 0.32f + animPhase * 0.12f),
                species.primaryColor.copy(alpha = 0.10f),
                Color.Transparent
            ),
            center = center,
            radius = radius * 1.15f
        ),
        radius = radius * 1.15f,
        center = center
    )

    // 2. Crisp Dark Ink Silhouette Outline Pass (HD-2D Illustrated Contour)
    val outlinePad = (min(cellW, cellH) * 0.34f).coerceAtLeast(1.4f)
    val cornerRad = CornerRadius(cellW * 0.38f, cellH * 0.38f)
    for (r in 0 until gridH) {
        val rowStr = rows[r]
        for (c in 0 until minOf(gridW, rowStr.length)) {
            if (rowStr[c] != '0') {
                drawRoundRect(
                    color = Color(0xFF100C1C),
                    topLeft = Offset(
                        left + c * cellW - outlinePad,
                        top + r * cellH - outlinePad
                    ),
                    size = Size(cellW + outlinePad * 2f, cellH + outlinePad * 2f),
                    cornerRadius = CornerRadius(cellW * 0.48f, cellH * 0.48f)
                )
            }
        }
    }

    // 3. Smoothly Shaded Multi-Layered Body & Specular Highlights Pass
    val bodyGradient = Brush.linearGradient(
        colors = listOf(
            species.primaryColor,
            species.primaryColor.copy(
                red = (species.primaryColor.red * 0.78f).coerceIn(0f, 1f),
                green = (species.primaryColor.green * 0.78f).coerceIn(0f, 1f),
                blue = (species.primaryColor.blue * 0.85f).coerceIn(0f, 1f)
            )
        ),
        start = Offset(left, top),
        end = Offset(left + spriteWidth, top + spriteHeight)
    )

    for (r in 0 until gridH) {
        val rowStr = rows[r]
        for (c in 0 until minOf(gridW, rowStr.length)) {
            val code = rowStr[c]
            if (code == '0') continue

            val cx = left + c * cellW
            val cy = top + r * cellH
            val isTopEdge = r == 0 || rows[r - 1].getOrNull(c) == '0'
            val isBottomEdge = r == gridH - 1 || rows.getOrNull(r + 1)?.getOrNull(c) == '0'

            when (code) {
                '1' -> {
                    drawRoundRect(
                        brush = bodyGradient,
                        topLeft = Offset(cx, cy),
                        size = Size(cellW + 0.8f, cellH + 0.8f),
                        cornerRadius = cornerRad
                    )
                }
                '2' -> {
                    drawRoundRect(
                        color = species.secondaryColor,
                        topLeft = Offset(cx, cy),
                        size = Size(cellW + 0.8f, cellH + 0.8f),
                        cornerRadius = cornerRad
                    )
                }
                '3' -> {
                    // Luminous crest / elemental gem accent
                    drawRoundRect(
                        color = species.accentColor,
                        topLeft = Offset(cx, cy),
                        size = Size(cellW + 0.8f, cellH + 0.8f),
                        cornerRadius = cornerRad
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.55f),
                        radius = min(cellW, cellH) * 0.28f,
                        center = Offset(cx + cellW * 0.45f, cy + cellH * 0.45f)
                    )
                }
                '4' -> {
                    // Expressive gloss eye with white specular catchlight
                    drawRoundRect(
                        color = Color(0xFF141020),
                        topLeft = Offset(cx, cy),
                        size = Size(cellW + 0.8f, cellH + 0.8f),
                        cornerRadius = cornerRad
                    )
                    drawCircle(
                        color = Color.White,
                        radius = min(cellW, cellH) * 0.26f,
                        center = Offset(cx + cellW * 0.35f, cy + cellH * 0.35f)
                    )
                }
            }

            // Specular Rim Lighting on upper contours
            if (isTopEdge && code != '4') {
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.34f),
                    topLeft = Offset(cx + cellW * 0.08f, cy + cellH * 0.06f),
                    size = Size(cellW * 0.84f, cellH * 0.32f),
                    cornerRadius = CornerRadius(cellW * 0.2f, cellH * 0.2f)
                )
            }
            // Ambient Occlusion shading on lower contours
            if (isBottomEdge && code != '4') {
                drawRoundRect(
                    color = Color(0x40090612),
                    topLeft = Offset(cx, cy + cellH * 0.65f),
                    size = Size(cellW + 0.5f, cellH * 0.35f),
                    cornerRadius = CornerRadius(cellW * 0.2f, cellH * 0.2f)
                )
            }
        }
    }
}

/**
 * Renders volumetric sunbeams/moonbeams and floating luminous particles
 * across the HD-2D viewport.
 */
fun DrawScope.drawHd2dSunbeamsAndParticles(
    timeOfDay: TimeOfDay,
    animPhase: Float
) {
    val rayColor = when (timeOfDay) {
        TimeOfDay.DAWN -> Color(0x2EFFC078)
        TimeOfDay.DAY -> Color(0x26FFF5C0)
        TimeOfDay.TWILIGHT -> Color(0x30FF6A45)
        TimeOfDay.NIGHT -> Color(0x2872C8F8)
    }

    // 3 Diagonal Crepuscular God-Rays sweeping from top-left sky
    for (rayIdx in 0..2) {
        val startX = size.width * (0.08f + rayIdx * 0.30f + animPhase * 0.03f)
        val rayWidthTop = size.width * 0.13f
        val rayWidthBottom = size.width * 0.26f
        val path = Path().apply {
            moveTo(startX, 0f)
            lineTo(startX + rayWidthTop, 0f)
            lineTo(startX + rayWidthBottom + size.width * 0.18f, size.height)
            lineTo(startX + size.width * 0.14f, size.height)
            close()
        }
        drawPath(
            path = path,
            brush = Brush.linearGradient(
                colors = listOf(rayColor, rayColor.copy(alpha = 0.05f), Color.Transparent),
                start = Offset(startX, 0f),
                end = Offset(startX + size.width * 0.2f, size.height)
            )
        )
    }

    // Drifting Luminous Motes (Glowing Fireflies at Night / Sunlit Maple Embers by Day)
    val coreMoteColor = if (timeOfDay == TimeOfDay.NIGHT) Color(0xFF72F2EB) else Color(0xFFFFB84D)
    val haloMoteColor = if (timeOfDay == TimeOfDay.NIGHT) SoulSpireBright else ToriiVermilion

    for (i in 0..9) {
        val mx = ((i * 89 + (animPhase * 22f).toInt()) % 100) / 100f * size.width
        val my = ((i * 47 + (animPhase * 16f).toInt()) % 100) / 100f * size.height
        val center = Offset(mx, my)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    coreMoteColor.copy(alpha = 0.85f),
                    haloMoteColor.copy(alpha = 0.28f),
                    Color.Transparent
                ),
                center = center,
                radius = 14f
            ),
            radius = 14f,
            center = center
        )
        drawCircle(
            color = Color.White,
            radius = 2.4f,
            center = center
        )
    }
}

/**
 * Renders animated elemental combat VFX bursts, shockwave rings, and particle rays on hit.
 */
fun DrawScope.drawCombatVfxBurst(
    vfx: CombatVfxEvent,
    targetCenter: Offset,
    animPhase: Float
) {
    val primaryColor = vfx.element.color
    val secondaryColor = when (vfx.element) {
        ElementAffinity.FLAME -> Color(0xFFFFE066)
        ElementAffinity.TIDE -> Color(0xFFB3F0FF)
        ElementAffinity.GALE -> Color(0xFFD4FFE4)
        ElementAffinity.EARTH -> Color(0xFFFFD180)
        ElementAffinity.BRASS -> HeartBrassBright
        ElementAffinity.SPIRIT -> Color(0xFFF0B3FF)
    }

    val maxRadius = min(size.width, size.height) * 0.26f
    val pulseRadius = maxRadius * (0.55f + animPhase * 0.45f)

    // Outer Bloom Explosion
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.85f),
                secondaryColor.copy(alpha = 0.65f),
                primaryColor.copy(alpha = 0.35f),
                Color.Transparent
            ),
            center = targetCenter,
            radius = pulseRadius
        ),
        radius = pulseRadius,
        center = targetCenter
    )

    // Shockwave Ring
    drawCircle(
        color = secondaryColor,
        radius = pulseRadius * 0.82f,
        center = targetCenter,
        style = Stroke(width = 4.5f)
    )

    // 8 Radial Elemental Particle Sparks
    for (i in 0 until 8) {
        val angleRad = Math.toRadians((i * 45.0) + (animPhase * 35.0))
        val dist = pulseRadius * 0.72f
        val px = targetCenter.x + (cos(angleRad) * dist).toFloat()
        val py = targetCenter.y + (sin(angleRad) * dist).toFloat()
        drawCircle(
            color = primaryColor,
            radius = 7.5f,
            center = Offset(px, py)
        )
        drawCircle(
            color = Color.White,
            radius = 3.5f,
            center = Offset(px, py)
        )
    }
}
