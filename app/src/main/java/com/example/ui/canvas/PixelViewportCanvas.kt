package com.example.ui.canvas

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MonsterCatalog
import com.example.model.TileType
import com.example.model.TimeOfDay
import com.example.ui.theme.AmberHpWarn
import com.example.ui.theme.GlassBorderBrush
import com.example.ui.theme.GlassSurfaceTranslucent
import com.example.ui.theme.HandheldChassisDark
import com.example.ui.theme.HeartBrassBright
import com.example.ui.theme.HeartBrassGold
import com.example.ui.theme.JadeHpGreen
import com.example.ui.theme.RetroLcdParchment
import com.example.ui.theme.SoulSpireBright
import com.example.ui.theme.SoulSpireSilver
import com.example.ui.theme.ToriiVermilion
import com.example.viewmodel.BattleState
import com.example.viewmodel.GameUiState

@Composable
fun TopHandheldViewport(
    uiState: GameUiState,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "hd2d_pulse")
    val animPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bob_phase"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .border(2.dp, GlassBorderBrush, RoundedCornerShape(14.dp))
            .background(HandheldChassisDark)
    ) {
        if (uiState.battleState != null) {
            BattleHd2dScreen(
                uiState = uiState,
                battle = uiState.battleState,
                animPhase = animPhase
            )
        } else {
            OverworldHd2dScreen(
                uiState = uiState,
                animPhase = animPhase
            )
        }

        // Sacred Bell Resonance Golden / Silver Bloom Flash Overlay
        uiState.bellResonanceColorHex?.let { hex ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(hex).copy(alpha = 0.55f))
            )
        }
    }
}

@Composable
private fun OverworldHd2dScreen(
    uiState: GameUiState,
    animPhase: Float
) {
    val map = uiState.currentMap
    val smoothBob = (animPhase - 0.5f) * 5f

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val tileW = size.width / map.width.toFloat()
            val tileH = size.height / map.height.toFloat()

            // 1. Draw HD-2D Richly Shaded 11x9 Tilemap with Water Reflections & Foliage Depth
            for (y in 0 until map.height) {
                for (x in 0 until map.width) {
                    val tile = map.tileAt(x, y)
                    drawHd2dOverworldTile(
                        tile = tile,
                        left = x * tileW,
                        top = y * tileH,
                        w = tileW,
                        h = tileH,
                        animPhase = animPhase,
                        timeOfDay = uiState.timeOfDay
                    )
                }
            }

            // 2. Draw Walking Lead Partner Monster with Smooth HD-2D Shading & Outline
            uiState.leadSpecies?.let { followerSpecies ->
                val fx = uiState.save.followerX * tileW
                val fy = uiState.save.followerY * tileH + smoothBob
                drawOval(
                    color = Color(0x7705040A),
                    topLeft = Offset(fx + tileW * 0.14f, fy + tileH * 0.74f),
                    size = Size(tileW * 0.72f, tileH * 0.24f)
                )
                drawMonsterPixelSprite(
                    species = followerSpecies,
                    left = fx + tileW * 0.06f,
                    top = fy + tileH * 0.04f,
                    spriteWidth = tileW * 0.88f,
                    spriteHeight = tileH * 0.88f,
                    animPhase = animPhase
                )
                if (uiState.followerHeartPulse || (uiState.leadMonster?.amity ?: 0) >= 130) {
                    val heartCenter = Offset(fx + tileW * 0.5f, fy - tileH * 0.06f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(ToriiVermilion, Color.Transparent),
                            center = heartCenter,
                            radius = tileW * 0.28f
                        ),
                        radius = tileW * 0.28f,
                        center = heartCenter
                    )
                    drawCircle(
                        color = ToriiVermilion,
                        radius = tileW * 0.10f,
                        center = heartCenter
                    )
                }
            }

            // 3. Draw HD-2D Shaded Player Traveller Sprite
            val px = uiState.save.tileX * tileW
            val py = uiState.save.tileY * tileH
            drawOval(
                color = Color(0x7705040A),
                topLeft = Offset(px + tileW * 0.18f, py + tileH * 0.76f),
                size = Size(tileW * 0.64f, tileH * 0.22f)
            )
            drawHd2dPlayerTravellerSprite(
                left = px + tileW * 0.12f,
                top = py + tileH * 0.06f,
                w = tileW * 0.76f,
                h = tileH * 0.86f
            )

            // 4. Time-of-Day Color Grading + Volumetric Sunbeams + Point-Light Lantern Bloom
            drawRect(
                color = uiState.timeOfDay.overlayColor,
                topLeft = Offset.Zero,
                size = size
            )
            drawHd2dSunbeamsAndParticles(
                timeOfDay = uiState.timeOfDay,
                animPhase = animPhase
            )

            // Edge Vignette for HD-2D Diorama Depth
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Transparent,
                        Color(0x6606040C)
                    ),
                    center = Offset(size.width * 0.5f, size.height * 0.5f),
                    radius = size.width * 0.72f
                ),
                topLeft = Offset.Zero,
                size = size
            )
        }

        // Sleek Glassmorphic Top HUD Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            color = GlassSurfaceTranslucent,
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorderBrush)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${map.name.uppercase()} • ${uiState.timeOfDay.label.uppercase()}",
                    style = MaterialTheme.typography.labelMedium,
                    color = HeartBrassBright,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (uiState.save.brassEmblemEarned) "★BRASS" else "☆BRASS",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (uiState.save.brassEmblemEarned) HeartBrassGold else Color.Gray
                    )
                    Text(
                        text = if (uiState.save.silverEmblemEarned) "★SILVER" else "☆SILVER",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (uiState.save.silverEmblemEarned) SoulSpireBright else Color.Gray
                    )
                }
            }
        }

        // Sleek Glassmorphic Bottom Status Strip inside Top Viewport
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(8.dp),
            color = GlassSurfaceTranslucent,
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorderBrush)
        ) {
            Text(
                text = uiState.statusBannerText,
                style = MaterialTheme.typography.bodySmall,
                color = RetroLcdParchment,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun BattleHd2dScreen(
    uiState: GameUiState,
    battle: BattleState,
    animPhase: Float
) {
    val lead = uiState.leadMonster
    val leadSpecies = uiState.leadSpecies ?: MonsterCatalog.speciesList.first()
    val smoothBob = (animPhase - 0.5f) * 7f

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // 1. HD-2D Gradient Sky Backdrop with Celestial Sun / Silver Moon Bloom
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0E0B1A),
                        uiState.timeOfDay.skyTint,
                        Color(0xFF282032)
                    ),
                    startY = 0f,
                    endY = size.height * 0.62f
                ),
                topLeft = Offset.Zero,
                size = Size(size.width, size.height * 0.62f)
            )

            val celestialCenter = Offset(size.width * 0.80f, size.height * 0.16f)
            val celestialColor = if (uiState.timeOfDay == TimeOfDay.NIGHT) SoulSpireBright else HeartBrassBright
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        celestialColor.copy(alpha = 0.65f),
                        celestialColor.copy(alpha = 0.18f),
                        Color.Transparent
                    ),
                    center = celestialCenter,
                    radius = size.width * 0.22f
                ),
                radius = size.width * 0.22f,
                center = celestialCenter
            )
            drawCircle(
                color = celestialColor,
                radius = size.width * 0.045f,
                center = celestialCenter
            )

            // Distant Mountain Silhouettes
            drawRoundRect(
                color = Color(0xFF161324),
                topLeft = Offset(-size.width * 0.05f, size.height * 0.44f),
                size = Size(size.width * 0.65f, size.height * 0.24f),
                cornerRadius = CornerRadius(80f, 80f)
            )
            drawRoundRect(
                color = Color(0xFF1D182E),
                topLeft = Offset(size.width * 0.38f, size.height * 0.40f),
                size = Size(size.width * 0.70f, size.height * 0.26f),
                cornerRadius = CornerRadius(90f, 90f)
            )

            // 2. Ground Battle Arena with Depth Gradient
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF23302A),
                        Color(0xFF141B1E)
                    ),
                    startY = size.height * 0.56f,
                    endY = size.height
                ),
                topLeft = Offset(0f, size.height * 0.56f),
                size = Size(size.width, size.height * 0.44f)
            )

            // 3. Glowing Elemental Enemy Platform (Top Right)
            val enemyPadOrigin = Offset(size.width * 0.53f, size.height * 0.38f)
            val enemyPadSize = Size(size.width * 0.40f, size.height * 0.15f)
            drawOval(
                color = Color(0x8807050E),
                topLeft = Offset(enemyPadOrigin.x, enemyPadOrigin.y + 6f),
                size = enemyPadSize
            )
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        battle.enemySpecies.primaryElement.color.copy(alpha = 0.45f),
                        Color(0xFF283330)
                    ),
                    center = Offset(enemyPadOrigin.x + enemyPadSize.width * 0.5f, enemyPadOrigin.y + enemyPadSize.height * 0.5f),
                    radius = enemyPadSize.width * 0.55f
                ),
                topLeft = enemyPadOrigin,
                size = enemyPadSize
            )
            drawOval(
                color = battle.enemySpecies.primaryElement.color.copy(alpha = 0.8f),
                topLeft = enemyPadOrigin,
                size = enemyPadSize,
                style = Stroke(width = 2.5f)
            )

            // 4. Glowing Elemental Player Platform (Bottom Left)
            val playerPadOrigin = Offset(size.width * 0.05f, size.height * 0.65f)
            val playerPadSize = Size(size.width * 0.44f, size.height * 0.17f)
            drawOval(
                color = Color(0x8807050E),
                topLeft = Offset(playerPadOrigin.x, playerPadOrigin.y + 6f),
                size = playerPadSize
            )
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        leadSpecies.primaryElement.color.copy(alpha = 0.45f),
                        Color(0xFF283330)
                    ),
                    center = Offset(playerPadOrigin.x + playerPadSize.width * 0.5f, playerPadOrigin.y + playerPadSize.height * 0.5f),
                    radius = playerPadSize.width * 0.55f
                ),
                topLeft = playerPadOrigin,
                size = playerPadSize
            )
            drawOval(
                color = leadSpecies.primaryElement.color.copy(alpha = 0.8f),
                topLeft = playerPadOrigin,
                size = playerPadSize,
                style = Stroke(width = 2.5f)
            )

            // 5. Draw Enemy Monster or Capture Vessel Resonance Animation
            val enemySpriteSize = minOf(size.width, size.height) * 0.35f
            val enemyCenter = Offset(size.width * 0.73f, size.height * 0.32f)
            if (battle.isAnimatingOrb) {
                val shakeX = if (battle.orbShakeCount % 2 == 0) -9f else 9f
                val orbCenter = Offset(enemyCenter.x + shakeX, enemyCenter.y + 12f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(HeartBrassBright, ToriiVermilion.copy(alpha = 0.4f), Color.Transparent),
                        center = orbCenter,
                        radius = 48f
                    ),
                    radius = 48f,
                    center = orbCenter
                )
                drawCircle(color = Color(0xFF120E1C), radius = 25f, center = orbCenter)
                drawCircle(color = ToriiVermilion, radius = 22f, center = orbCenter)
                drawCircle(color = HeartBrassBright, radius = 9f, center = orbCenter)
            } else {
                drawMonsterPixelSprite(
                    species = battle.enemySpecies,
                    left = size.width * 0.59f,
                    top = size.height * 0.13f + smoothBob,
                    spriteWidth = enemySpriteSize,
                    spriteHeight = enemySpriteSize,
                    animPhase = animPhase
                )
            }

            // 6. Draw Player Lead Monster
            val playerSpriteSize = minOf(size.width, size.height) * 0.37f
            val playerCenter = Offset(size.width * 0.26f, size.height * 0.58f)
            drawMonsterPixelSprite(
                species = leadSpecies,
                left = size.width * 0.11f,
                top = size.height * 0.40f - smoothBob,
                spriteWidth = playerSpriteSize,
                spriteHeight = playerSpriteSize,
                animPhase = animPhase
            )

            // 7. Volumetric Sunbeams + Active Elemental Combat VFX Burst
            drawHd2dSunbeamsAndParticles(
                timeOfDay = uiState.timeOfDay,
                animPhase = animPhase
            )

            battle.activeVfx?.let { vfx ->
                val targetPos = if (vfx.isTargetingEnemy) enemyCenter else playerCenter
                drawCombatVfxBurst(
                    vfx = vfx,
                    targetCenter = targetPos,
                    animPhase = animPhase
                )
            }
        }

        // Floating Damage / Heal Badge when Combat VFX is active
        battle.activeVfx?.let { vfx ->
            Surface(
                modifier = Modifier
                    .align(if (vfx.isTargetingEnemy) Alignment.TopEnd else Alignment.CenterStart)
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                color = vfx.element.color.copy(alpha = 0.92f),
                shape = RoundedCornerShape(50),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White)
            ) {
                Text(
                    text = if (vfx.isHeal) "+${vfx.damageAmount} HP"
                    else "-${vfx.damageAmount} HP" + (if (vfx.isCritical) " ★CRIT!" else ""),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        // Sleek Glassmorphic Enemy HUD Card (Top Left)
        BattleGlassHudCard(
            name = battle.enemySpecies.name,
            level = battle.enemyLevel,
            currentHp = battle.enemyCurrentHp,
            maxHp = battle.enemyMaxHp,
            elementText = battle.enemySpecies.primaryElement.displayName,
            elementColor = battle.enemySpecies.primaryElement.color,
            isCaughtSpecies = uiState.ownedSpeciesIds.contains(battle.enemySpecies.id),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .width(196.dp)
        )

        // Sleek Glassmorphic Player Lead HUD Card (Bottom Right above battle log)
        if (lead != null) {
            BattleGlassHudCard(
                name = lead.nickname,
                level = lead.level,
                currentHp = lead.currentHp,
                maxHp = lead.maxHp,
                elementText = "${leadSpecies.primaryElement.displayName} • ♥${lead.amity}",
                elementColor = leadSpecies.primaryElement.color,
                isCaughtSpecies = false,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 8.dp, bottom = 48.dp)
                    .width(206.dp)
            )
        }

        // Glassmorphic Battle Log Banner at bottom of Top Screen
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(8.dp),
            color = GlassSurfaceTranslucent,
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorderBrush)
        ) {
            Text(
                text = battle.battleLog,
                style = MaterialTheme.typography.bodySmall,
                color = RetroLcdParchment,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun BattleGlassHudCard(
    name: String,
    level: Int,
    currentHp: Int,
    maxHp: Int,
    elementText: String,
    elementColor: Color,
    isCaughtSpecies: Boolean,
    modifier: Modifier = Modifier
) {
    val hpRatio = (currentHp.toFloat() / maxHp.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
    val barColor = when {
        hpRatio > 0.5f -> JadeHpGreen
        hpRatio > 0.22f -> AmberHpWarn
        else -> ToriiVermilion
    }

    Surface(
        modifier = modifier,
        color = GlassSurfaceTranslucent,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, elementColor.copy(alpha = 0.85f))
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = (if (isCaughtSpecies) "● " else "") + name.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = RetroLcdParchment,
                    maxLines = 1
                )
                Text(
                    text = "Lv.$level",
                    style = MaterialTheme.typography.labelMedium,
                    color = HeartBrassBright
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { hpRatio },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = barColor,
                trackColor = Color(0xFF2D283E)
            )
            Spacer(modifier = Modifier.height(3.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = elementText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = elementColor
                )
                Text(
                    text = "$currentHp/$maxHp HP",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = RetroLcdParchment
                )
            }
        }
    }
}

private fun DrawScope.drawHd2dPlayerTravellerSprite(
    left: Float,
    top: Float,
    w: Float,
    h: Float
) {
    val rows = listOf(
        "00111100",
        "01122110",
        "00343400",
        "00333300",
        "01211210",
        "11211211",
        "00122100",
        "00400400"
    )
    val cellW = w / 8f
    val cellH = h / 8f
    val pad = 1.4f

    // Crisp dark outline pass
    for (r in rows.indices) {
        for (c in 0 until 8) {
            if (rows[r][c] != '0') {
                drawRoundRect(
                    color = Color(0xFF100C1C),
                    topLeft = Offset(left + c * cellW - pad, top + r * cellH - pad),
                    size = Size(cellW + pad * 2f, cellH + pad * 2f),
                    cornerRadius = CornerRadius(cellW * 0.45f, cellH * 0.45f)
                )
            }
        }
    }

    // Smoothly shaded garment & face pass
    for (r in rows.indices) {
        for (c in 0 until 8) {
            val col = when (rows[r][c]) {
                '1' -> ToriiVermilion
                '2' -> HeartBrassBright
                '3' -> Color(0xFFFFDFC4)
                '4' -> Color(0xFF181424)
                else -> null
            }
            if (col != null) {
                drawRoundRect(
                    color = col,
                    topLeft = Offset(left + c * cellW, top + r * cellH),
                    size = Size(cellW + 0.6f, cellH + 0.6f),
                    cornerRadius = CornerRadius(cellW * 0.38f, cellH * 0.38f)
                )
            }
        }
    }
}

private fun DrawScope.drawHd2dOverworldTile(
    tile: TileType,
    left: Float,
    top: Float,
    w: Float,
    h: Float,
    animPhase: Float,
    timeOfDay: TimeOfDay
) {
    // 1. Richly Shaded Ground / Water Base
    when (tile) {
        TileType.WATER -> {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF1B4978), Color(0xFF2869A8)),
                    startY = top,
                    endY = top + h
                ),
                topLeft = Offset(left, top),
                size = Size(w + 0.8f, h + 0.8f)
            )
            // Specular Water Reflections & Shimmering Caustics
            val waveShift = w * (0.12f + animPhase * 0.24f)
            drawRoundRect(
                color = SoulSpireBright.copy(alpha = 0.42f),
                topLeft = Offset(left + waveShift, top + h * 0.28f),
                size = Size(w * 0.52f, h * 0.14f),
                cornerRadius = CornerRadius(8f, 8f)
            )
            drawRoundRect(
                color = Color.White.copy(alpha = 0.28f),
                topLeft = Offset(left + w * 0.42f - waveShift * 0.5f, top + h * 0.64f),
                size = Size(w * 0.38f, h * 0.10f),
                cornerRadius = CornerRadius(8f, 8f)
            )
        }
        TileType.BRIDGE -> {
            // Water reflection underneath bridge
            drawRect(color = Color(0xFF1B4978), topLeft = Offset(left, top), size = Size(w + 0.8f, h + 0.8f))
            // Lacquered Vermilion Bridge Planks with bevel shadow
            drawRoundRect(
                color = Color(0xFF6B1E14),
                topLeft = Offset(left + w * 0.04f, top + h * 0.08f),
                size = Size(w * 0.92f, h * 0.86f),
                cornerRadius = CornerRadius(6f, 6f)
            )
            drawRoundRect(
                color = Color(0xFFD94A34),
                topLeft = Offset(left + w * 0.06f, top + h * 0.10f),
                size = Size(w * 0.88f, h * 0.78f),
                cornerRadius = CornerRadius(6f, 6f)
            )
        }
        TileType.STONE_WALL -> {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF4A4458), Color(0xFF2A2634)),
                    startY = top,
                    endY = top + h
                ),
                topLeft = Offset(left, top),
                size = Size(w + 0.8f, h + 0.8f)
            )
            drawRoundRect(
                color = Color(0xFF635C75),
                topLeft = Offset(left + w * 0.08f, top + h * 0.08f),
                size = Size(w * 0.84f, h * 0.42f),
                cornerRadius = CornerRadius(5f, 5f)
            )
        }
        else -> {
            // Lush Autumn Moss Meadow Base
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF3E633A), Color(0xFF325230)),
                    start = Offset(left, top),
                    end = Offset(left + w, top + h)
                ),
                topLeft = Offset(left, top),
                size = Size(w + 0.8f, h + 0.8f)
            )
        }
    }

    val tileCenter = Offset(left + w * 0.5f, top + h * 0.5f)

    // 2. Detailed HD-2D Props, Foliage, and Dynamic Point-Light Bloom
    when (tile) {
        TileType.GRASS_PATH -> {
            drawRoundRect(
                color = Color(0xFF9E845C),
                topLeft = Offset(left + w * 0.10f, top + h * 0.12f),
                size = Size(w * 0.80f, h * 0.80f),
                cornerRadius = CornerRadius(10f, 10f)
            )
            drawRoundRect(
                color = Color(0xFFD4BC94),
                topLeft = Offset(left + w * 0.14f, top + h * 0.14f),
                size = Size(w * 0.72f, h * 0.70f),
                cornerRadius = CornerRadius(9f, 9f)
            )
        }
        TileType.TALL_GRASS -> {
            val sway = (animPhase - 0.5f) * 3.5f
            for (blade in 0..2) {
                val bx = left + w * (0.18f + blade * 0.25f) + sway
                drawRoundRect(
                    color = Color(0xFF213D20),
                    topLeft = Offset(bx, top + h * 0.26f),
                    size = Size(w * 0.16f, h * 0.56f),
                    cornerRadius = CornerRadius(5f, 5f)
                )
                drawRoundRect(
                    color = HeartBrassGold,
                    topLeft = Offset(bx, top + h * 0.16f),
                    size = Size(w * 0.16f, h * 0.22f),
                    cornerRadius = CornerRadius(5f, 5f)
                )
            }
        }
        TileType.MAPLE_TREE -> {
            // Soft Ambient Canopy Shadow
            drawOval(
                color = Color(0x66080610),
                topLeft = Offset(left + w * 0.08f, top + h * 0.62f),
                size = Size(w * 0.84f, h * 0.34f)
            )
            // Trunk
            drawRoundRect(
                color = Color(0xFF3E2416),
                topLeft = Offset(left + w * 0.36f, top + h * 0.52f),
                size = Size(w * 0.28f, h * 0.42f),
                cornerRadius = CornerRadius(4f, 4f)
            )
            // Multi-layered Shaded Crimson & Amber Momiji Canopy with Dark Outline
            drawCircle(
                color = Color(0xFF140D1C),
                radius = w * 0.44f,
                center = Offset(left + w * 0.5f, top + h * 0.38f)
            )
            drawCircle(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFF6E3A), Color(0xFFB82925)),
                    startY = top,
                    endY = top + h * 0.7f
                ),
                radius = w * 0.40f,
                center = Offset(left + w * 0.5f, top + h * 0.38f)
            )
            drawCircle(
                color = Color(0xFFFFB347).copy(alpha = 0.75f),
                radius = w * 0.22f,
                center = Offset(left + w * 0.42f, top + h * 0.28f)
            )
        }
        TileType.TORII_GATE, TileType.PORTAL_NORTH, TileType.PORTAL_SOUTH, TileType.PORTAL_EAST, TileType.PORTAL_WEST -> {
            // Warm Torii Lantern Bloom
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(ToriiVermilion.copy(alpha = 0.35f), Color.Transparent),
                    center = tileCenter,
                    radius = w * 0.75f
                ),
                radius = w * 0.75f,
                center = tileCenter
            )
            // Outlined Vermilion Torii Archway
            drawRoundRect(
                color = Color(0xFF140D1C),
                topLeft = Offset(left + w * 0.06f, top + h * 0.10f),
                size = Size(w * 0.88f, h * 0.22f),
                cornerRadius = CornerRadius(5f, 5f)
            )
            drawRoundRect(
                color = ToriiVermilion,
                topLeft = Offset(left + w * 0.08f, top + h * 0.12f),
                size = Size(w * 0.84f, h * 0.18f),
                cornerRadius = CornerRadius(4f, 4f)
            )
            drawRoundRect(
                color = ToriiVermilion,
                topLeft = Offset(left + w * 0.18f, top + h * 0.24f),
                size = Size(w * 0.15f, h * 0.68f),
                cornerRadius = CornerRadius(3f, 3f)
            )
            drawRoundRect(
                color = ToriiVermilion,
                topLeft = Offset(left + w * 0.67f, top + h * 0.24f),
                size = Size(w * 0.15f, h * 0.68f),
                cornerRadius = CornerRadius(3f, 3f)
            )
        }
        TileType.SHRINE_ALTAR_BRASS -> {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        HeartBrassBright.copy(alpha = 0.65f),
                        HeartBrassGold.copy(alpha = 0.25f),
                        Color.Transparent
                    ),
                    center = tileCenter,
                    radius = w * 0.95f
                ),
                radius = w * 0.95f,
                center = tileCenter
            )
            drawRoundRect(
                color = HeartBrassGold,
                topLeft = Offset(left + w * 0.14f, top + h * 0.14f),
                size = Size(w * 0.72f, h * 0.72f),
                cornerRadius = CornerRadius(8f, 8f)
            )
            drawCircle(color = Color.White, radius = w * 0.18f, center = tileCenter)
        }
        TileType.SHRINE_ALTAR_SILVER -> {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        SoulSpireBright.copy(alpha = 0.65f),
                        SoulSpireSilver.copy(alpha = 0.25f),
                        Color.Transparent
                    ),
                    center = tileCenter,
                    radius = w * 0.95f
                ),
                radius = w * 0.95f,
                center = tileCenter
            )
            drawRoundRect(
                color = SoulSpireSilver,
                topLeft = Offset(left + w * 0.14f, top + h * 0.14f),
                size = Size(w * 0.72f, h * 0.72f),
                cornerRadius = CornerRadius(8f, 8f)
            )
            drawCircle(color = Color.White, radius = w * 0.18f, center = tileCenter)
        }
        TileType.LANTERN_HEAL -> {
            // Dynamic Point-Light Bloom Halo around Sacred Lantern
            val bloomScale = if (timeOfDay == TimeOfDay.NIGHT || timeOfDay == TimeOfDay.TWILIGHT) 1.15f else 0.85f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xAAFFE07D),
                        Color(0x44FF8833),
                        Color.Transparent
                    ),
                    center = tileCenter,
                    radius = w * bloomScale
                ),
                radius = w * bloomScale,
                center = tileCenter
            )
            drawRoundRect(
                color = Color(0xFF6E6A7C),
                topLeft = Offset(left + w * 0.26f, top + h * 0.16f),
                size = Size(w * 0.48f, h * 0.72f),
                cornerRadius = CornerRadius(6f, 6f)
            )
            drawCircle(
                color = Color(0xFFFFF0A8),
                radius = w * 0.18f,
                center = Offset(left + w * 0.5f, top + h * 0.42f)
            )
        }
        TileType.DOJO_MASTER_BRASS, TileType.DOJO_MASTER_SILVER, TileType.GRANDMASTER_RYOMA -> {
            val robeColor = when (tile) {
                TileType.DOJO_MASTER_BRASS -> HeartBrassGold
                TileType.DOJO_MASTER_SILVER -> SoulSpireSilver
                else -> Color(0xFFB66DFF)
            }
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(robeColor.copy(alpha = 0.45f), Color.Transparent),
                    center = tileCenter,
                    radius = w * 0.7f
                ),
                radius = w * 0.7f,
                center = tileCenter
            )
            drawRoundRect(
                color = Color(0xFF120E1C),
                topLeft = Offset(left + w * 0.20f, top + h * 0.16f),
                size = Size(w * 0.60f, h * 0.74f),
                cornerRadius = CornerRadius(8f, 8f)
            )
            drawRoundRect(
                color = robeColor,
                topLeft = Offset(left + w * 0.24f, top + h * 0.20f),
                size = Size(w * 0.52f, h * 0.66f),
                cornerRadius = CornerRadius(7f, 7f)
            )
            drawCircle(
                color = Color(0xFFFFDFC4),
                radius = w * 0.16f,
                center = Offset(left + w * 0.5f, top + h * 0.34f)
            )
        }
        TileType.ACORN_TREE -> {
            drawCircle(
                color = Color(0xFF2B5E38),
                radius = w * 0.40f,
                center = Offset(left + w * 0.5f, top + h * 0.42f)
            )
            drawCircle(
                color = HeartBrassBright,
                radius = w * 0.14f,
                center = Offset(left + w * 0.5f, top + h * 0.42f)
            )
        }
        else -> {}
    }
}
