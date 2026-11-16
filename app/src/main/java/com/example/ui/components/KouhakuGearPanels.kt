package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.OwnedMonsterEntity
import com.example.model.CaptureOrbType
import com.example.model.Direction
import com.example.model.KouhakuMaps
import com.example.model.MonsterCatalog
import com.example.model.MoveTechnique
import com.example.ui.canvas.drawMonsterPixelSprite
import com.example.ui.theme.GlassBorderBrush
import com.example.ui.theme.GlassSubtleBorderBrush
import com.example.ui.theme.GlassSurfaceTranslucent
import com.example.ui.theme.HandheldChassisDark
import com.example.ui.theme.HandheldPanelElevated
import com.example.ui.theme.HeartBrassBright
import com.example.ui.theme.HeartBrassGold
import com.example.ui.theme.JadeHpGreen
import com.example.ui.theme.RetroLcdParchment
import com.example.ui.theme.SoulSpireBright
import com.example.ui.theme.SoulSpireSilver
import com.example.ui.theme.SpiritWalkerDarkInk
import com.example.ui.theme.SpiritWalkerSage
import com.example.ui.theme.ToriiVermilion
import com.example.viewmodel.BattleState
import com.example.viewmodel.GameEngineViewModel
import com.example.viewmodel.GameUiState

@Composable
fun BattleCommandDeck(
    uiState: GameUiState,
    battle: BattleState,
    viewModel: GameEngineViewModel,
    modifier: Modifier = Modifier
) {
    val leadSpecies = uiState.leadSpecies ?: MonsterCatalog.speciesList.first()
    val moves = MonsterCatalog.getMovesForSpecies(leadSpecies)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "ELEMENTAL COMBAT DECK • ${uiState.leadMonster?.nickname?.uppercase() ?: "PARTNER"}",
            style = MaterialTheme.typography.titleSmall,
            color = HeartBrassBright
        )

        val chunkedMoves = moves.chunked(2)
        for (rowMoves in chunkedMoves) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (move in rowMoves) {
                    MoveStrikeButton(
                        move = move,
                        enemyPrimary = battle.enemySpecies.primaryElement,
                        onClick = { viewModel.executePlayerMove(move) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Text(
            text = "SPIRIT VESSELS & REMEDIES",
            style = MaterialTheme.typography.labelMedium,
            color = SoulSpireBright
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OrbQuickButton(
                label = "Spirit (${uiState.save.spiritOrbs})",
                color = CaptureOrbType.SPIRIT_ORB.primaryColor,
                tag = "throw_spirit_orb",
                onClick = { viewModel.throwCaptureOrb(CaptureOrbType.SPIRIT_ORB) },
                modifier = Modifier.weight(1f)
            )
            OrbQuickButton(
                label = "Ember (${uiState.save.emberOrbs})",
                color = CaptureOrbType.EMBER_ORB.primaryColor,
                tag = "throw_ember_orb",
                onClick = { viewModel.throwCaptureOrb(CaptureOrbType.EMBER_ORB) },
                modifier = Modifier.weight(1f)
            )
            OrbQuickButton(
                label = "Dusk (${uiState.save.duskOrbs})",
                color = CaptureOrbType.DUSK_ORB.primaryColor,
                tag = "throw_dusk_orb",
                onClick = { viewModel.throwCaptureOrb(CaptureOrbType.DUSK_ORB) },
                modifier = Modifier.weight(1f)
            )
            OrbQuickButton(
                label = "Brass (${uiState.save.heavyBrassOrbs})",
                color = CaptureOrbType.HEAVY_BRASS_ORB.primaryColor,
                tag = "throw_brass_orb",
                onClick = { viewModel.throwCaptureOrb(CaptureOrbType.HEAVY_BRASS_ORB) },
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalButton(
                onClick = { viewModel.useHealingTea() },
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .testTag("battle_heal_tea_button"),
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = JadeHpGreen.copy(alpha = 0.25f))
            ) {
                Text(
                    text = "Herbal Tea (${uiState.save.healingTeas})",
                    style = MaterialTheme.typography.labelLarge,
                    color = RetroLcdParchment
                )
            }
            OutlinedButton(
                onClick = { viewModel.fleeFromBattle() },
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .testTag("battle_flee_button")
            ) {
                Text(
                    text = "Retreat",
                    style = MaterialTheme.typography.labelLarge,
                    color = ToriiVermilion
                )
            }
        }
    }
}

@Composable
private fun MoveStrikeButton(
    move: MoveTechnique,
    enemyPrimary: com.example.model.ElementAffinity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val eff = move.element.effectivenessAgainst(enemyPrimary)
    val effTag = when {
        move.power == 0 -> "HEAL 42%"
        eff > 1.2f -> "1.6x SUPER"
        eff < 0.85f -> "0.6x RESIST"
        else -> "PWR ${move.power}"
    }

    Surface(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .testTag("move_button_${move.id}"),
        color = GlassSurfaceTranslucent,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            Brush.linearGradient(
                listOf(move.element.color, move.element.color.copy(alpha = 0.45f))
            )
        )
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)) {
            Text(
                text = move.name,
                style = MaterialTheme.typography.labelLarge,
                color = RetroLcdParchment,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = move.element.displayName.uppercase(),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = move.element.color
                )
                Text(
                    text = effTag,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (eff > 1.2f) JadeHpGreen else SoulSpireBright
                )
            }
        }
    }
}

@Composable
private fun OrbQuickButton(
    label: String,
    color: Color,
    tag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .testTag(tag),
        color = color.copy(alpha = 0.22f),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, color)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = RetroLcdParchment,
                maxLines = 1
            )
        }
    }
}

@Composable
fun ExplorerControlDeck(
    uiState: GameUiState,
    viewModel: GameEngineViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Glassmorphic Walking Partner Reaction Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.talkToWalkingPartner() }
                .testTag("partner_mood_card"),
            color = GlassSurfaceTranslucent,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.2.dp, GlassBorderBrush)
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                uiState.leadSpecies?.let { spec ->
                    Canvas(modifier = Modifier.size(38.dp)) {
                        drawMonsterPixelSprite(spec, 0f, 0f, size.width, size.height)
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "WALKING PARTNER: ${uiState.leadMonster?.nickname?.uppercase() ?: "NONE"}",
                            style = MaterialTheme.typography.labelLarge,
                            color = HeartBrassBright
                        )
                        Text(
                            text = "♥ Amity ${uiState.leadMonster?.amity ?: 0}/255",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ToriiVermilion
                        )
                    }
                    Text(
                        text = uiState.followerReactionText,
                        style = MaterialTheme.typography.bodySmall,
                        color = RetroLcdParchment,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Sleek Glass D-Pad + Glowing Handheld Action Deck Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                DpadButton(
                    icon = Icons.Default.KeyboardArrowUp,
                    contentDesc = "Move North",
                    tag = "dpad_up",
                    onClick = { viewModel.movePlayer(Direction.UP) }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    DpadButton(
                        icon = Icons.Default.KeyboardArrowLeft,
                        contentDesc = "Move West",
                        tag = "dpad_left",
                        onClick = { viewModel.movePlayer(Direction.LEFT) }
                    )
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(HandheldChassisDark)
                            .border(1.dp, GlassSubtleBorderBrush, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${uiState.save.stepCount}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = HeartBrassBright
                        )
                    }
                    DpadButton(
                        icon = Icons.Default.KeyboardArrowRight,
                        contentDesc = "Move East",
                        tag = "dpad_right",
                        onClick = { viewModel.movePlayer(Direction.RIGHT) }
                    )
                }
                DpadButton(
                    icon = Icons.Default.KeyboardArrowDown,
                    contentDesc = "Move South",
                    tag = "dpad_down",
                    onClick = { viewModel.movePlayer(Direction.DOWN) }
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = { viewModel.interactWithEnvironment() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("action_a_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = ToriiVermilion),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("A • INSPECT / CHALLENGE", style = MaterialTheme.typography.labelLarge, color = Color.White)
                }

                FilledTonalButton(
                    onClick = { viewModel.talkToWalkingPartner() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("talk_partner_button"),
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = HeartBrassGold.copy(alpha = 0.25f)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Favorite, contentDescription = null, tint = HeartBrassBright, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("B • TALK TO PARTNER", style = MaterialTheme.typography.labelLarge, color = RetroLcdParchment)
                }

                OutlinedButton(
                    onClick = { viewModel.cycleTimeOfDay() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("cycle_time_button"),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.WbTwilight, contentDescription = null, tint = SoulSpireBright, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("LIGHT: ${uiState.timeOfDay.label.uppercase()}", style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        // Glassmorphic Regional Fast Travel Radar
        Text(
            text = "SHRINE WIND FAST-TRAVEL RADAR",
            style = MaterialTheme.typography.labelMedium,
            color = SoulSpireBright
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(KouhakuMaps.maps.values.toList()) { mapItem ->
                val isCurrent = mapItem.id == uiState.currentMap.id
                Surface(
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { viewModel.fastTravelToMap(mapItem.id) }
                        .testTag("travel_${mapItem.id}"),
                    color = if (isCurrent) HeartBrassGold.copy(alpha = 0.28f) else GlassSurfaceTranslucent,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.2.dp,
                        if (isCurrent) GlassBorderBrush else GlassSubtleBorderBrush
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Explore,
                            contentDescription = null,
                            tint = if (isCurrent) HeartBrassBright else SoulSpireBright,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = mapItem.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = RetroLcdParchment
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DpadButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDesc: String,
    tag: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag(tag),
        color = GlassSurfaceTranslucent,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, GlassBorderBrush)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDesc,
                tint = RetroLcdParchment,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
fun SpiritWalkerPedometerPanel(
    uiState: GameUiState,
    viewModel: GameEngineViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Modern Backlit Holo-LCD Pedometer Screen
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, GlassBorderBrush, RoundedCornerShape(14.dp)),
            color = SpiritWalkerSage,
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(66.dp)
                        .border(1.5.dp, SpiritWalkerDarkInk.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    uiState.leadSpecies?.let { spec ->
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawMonsterPixelSprite(spec, 0f, 0f, size.width, size.height)
                        }
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "SPIRIT-WALKER HOLO-LCD",
                        style = MaterialTheme.typography.titleSmall,
                        color = SpiritWalkerDarkInk
                    )
                    Text(
                        text = "TOTAL STEPS: ${uiState.save.stepCount}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = RetroLcdParchment
                    )
                    Text(
                        text = "DOWSING POOL: ${uiState.save.unredeemedSteps} / 50 STEPS",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = HeartBrassBright
                    )
                    Text(
                        text = "Walking with ${uiState.leadMonster?.nickname ?: "Partner"} (Amity ♥${uiState.leadMonster?.amity ?: 0})",
                        fontSize = 11.sp,
                        color = SoulSpireBright
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.simulateBriskWalk() },
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .testTag("walker_stroll_button"),
                colors = ButtonDefaults.buttonColors(containerColor = HeartBrassGold)
            ) {
                Icon(Icons.AutoMirrored.Filled.DirectionsWalk, contentDescription = null, tint = HandheldChassisDark)
                Spacer(modifier = Modifier.width(6.dp))
                Text("+25 STROLL STEPS", style = MaterialTheme.typography.labelLarge, color = HandheldChassisDark)
            }

            Button(
                onClick = { viewModel.dowseStepRewards() },
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .testTag("walker_dowse_button"),
                colors = ButtonDefaults.buttonColors(containerColor = ToriiVermilion)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("DOWSE (50 STEPS)", style = MaterialTheme.typography.labelLarge, color = Color.White)
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = GlassSurfaceTranslucent),
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassSubtleBorderBrush),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "AMITY BOND PERKS (HEARTGOLD / SOULSILVER STYLE)",
                    style = MaterialTheme.typography.labelMedium,
                    color = HeartBrassBright
                )
                Text(
                    text = "• Walking together in the overworld or Holo-Walker raises Amity (up to 255).\n" +
                        "• Amity 140+: Partner may endure a knockout blow at 1 HP in battle.\n" +
                        "• Amity 150+: +22% chance for resonant critical strikes & bonus Acorn gifts.",
                    style = MaterialTheme.typography.bodySmall,
                    color = RetroLcdParchment
                )
            }
        }
    }
}

@Composable
fun SacredBellAndForgePanel(
    uiState: GameUiState,
    viewModel: GameEngineViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "SACRED BELL SHRINE RESONANCE",
            style = MaterialTheme.typography.titleSmall,
            color = HeartBrassBright
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = GlassSurfaceTranslucent),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, HeartBrassGold)
            ) {
                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("CLEAR BRASS BELL", style = MaterialTheme.typography.labelMedium, color = HeartBrassBright)
                    Text(
                        text = if (uiState.save.brassEmblemEarned) "Unlocked! Ready to summon Lv.24 HOUOU-KIN."
                        else "Locked: Defeat Master Kagerou in Brass Sanctuary.",
                        style = MaterialTheme.typography.bodySmall,
                        color = RetroLcdParchment
                    )
                    Button(
                        onClick = { viewModel.ringSacredBell(isBrassBell = true) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("ring_brass_bell_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = HeartBrassGold)
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = HandheldChassisDark, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("TOLL BRASS", style = MaterialTheme.typography.labelLarge, color = HandheldChassisDark)
                    }
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = GlassSurfaceTranslucent),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, SoulSpireSilver)
            ) {
                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("TIDAL SILVER BELL", style = MaterialTheme.typography.labelMedium, color = SoulSpireBright)
                    Text(
                        text = if (uiState.save.silverEmblemEarned) "Unlocked! Ready to summon Lv.24 RYUJIN-GIN."
                        else "Locked: Defeat Master Suiren at Silver Lake.",
                        style = MaterialTheme.typography.bodySmall,
                        color = RetroLcdParchment
                    )
                    Button(
                        onClick = { viewModel.ringSacredBell(isBrassBell = false) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("ring_silver_bell_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = SoulSpireSilver)
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = HandheldChassisDark, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("TOLL SILVER", style = MaterialTheme.typography.labelLarge, color = HandheldChassisDark)
                    }
                }
            }
        }

        Text(
            text = "SPIRIT ACORN KILN • CUSTOM CAPTURE ORB FORGE",
            style = MaterialTheme.typography.titleSmall,
            color = SoulSpireBright
        )
        Text(
            text = "Acorns Held: Crimson (${uiState.save.crimsonAcorns}) • Dusk (${uiState.save.duskAcorns}) • Brass (${uiState.save.brassAcorns})",
            style = MaterialTheme.typography.bodySmall,
            color = RetroLcdParchment
        )

        ForgeRecipeRow(
            orbType = CaptureOrbType.EMBER_ORB,
            ownedOrbs = uiState.save.emberOrbs,
            acornCount = uiState.save.crimsonAcorns,
            onCraft = { viewModel.craftCaptureOrb(CaptureOrbType.EMBER_ORB) }
        )
        ForgeRecipeRow(
            orbType = CaptureOrbType.DUSK_ORB,
            ownedOrbs = uiState.save.duskOrbs,
            acornCount = uiState.save.duskAcorns,
            onCraft = { viewModel.craftCaptureOrb(CaptureOrbType.DUSK_ORB) }
        )
        ForgeRecipeRow(
            orbType = CaptureOrbType.HEAVY_BRASS_ORB,
            ownedOrbs = uiState.save.heavyBrassOrbs,
            acornCount = uiState.save.brassAcorns,
            onCraft = { viewModel.craftCaptureOrb(CaptureOrbType.HEAVY_BRASS_ORB) }
        )
    }
}

@Composable
private fun ForgeRecipeRow(
    orbType: CaptureOrbType,
    ownedOrbs: Int,
    acornCount: Int,
    onCraft: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = GlassSurfaceTranslucent,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, orbType.primaryColor.copy(alpha = 0.75f))
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${orbType.displayName} (Bag: $ownedOrbs)",
                    style = MaterialTheme.typography.labelLarge,
                    color = orbType.primaryColor
                )
                Text(
                    text = orbType.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = RetroLcdParchment
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            FilledTonalButton(
                onClick = onCraft,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("craft_${orbType.name.lowercase()}"),
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = orbType.primaryColor.copy(alpha = 0.28f))
            ) {
                Text("Forge +2 ($acornCount)", style = MaterialTheme.typography.labelLarge, color = RetroLcdParchment)
            }
        }
    }
}

@Composable
fun PartyAndCompendiumPanel(
    uiState: GameUiState,
    viewModel: GameEngineViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PARTY (${uiState.partyMonsters.size}/6) • DEX (${uiState.ownedSpeciesIds.size}/18)",
                style = MaterialTheme.typography.titleSmall,
                color = HeartBrassBright
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilledTonalButton(
                    onClick = { viewModel.toggleCompendiumDialog(true) },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("open_compendium_button"),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Compendium", style = MaterialTheme.typography.labelLarge)
                }
                OutlinedButton(
                    onClick = { viewModel.resetChronicleSave() },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("reset_save_button"),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = "Reset Chronicle", modifier = Modifier.size(15.dp))
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(uiState.partyMonsters, key = { it.uid }) { monster ->
                PartyMonsterRowCard(
                    monster = monster,
                    isLead = monster.uid == uiState.leadMonster?.uid,
                    onMakeLead = { viewModel.makeMonsterWalkingLead(monster) }
                )
            }
            if (uiState.sanctuaryMonsters.isNotEmpty()) {
                item {
                    Text(
                        text = "SHRINE SANCTUARY BOX (${uiState.sanctuaryMonsters.size})",
                        style = MaterialTheme.typography.labelMedium,
                        color = SoulSpireBright,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
                items(uiState.sanctuaryMonsters, key = { it.uid }) { monster ->
                    PartyMonsterRowCard(
                        monster = monster,
                        isLead = false,
                        onMakeLead = { viewModel.makeMonsterWalkingLead(monster) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PartyMonsterRowCard(
    monster: OwnedMonsterEntity,
    isLead: Boolean,
    onMakeLead: () -> Unit
) {
    val species = MonsterCatalog.getSpecies(monster.speciesId)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onMakeLead)
            .testTag("party_monster_${monster.uid}"),
        color = if (isLead) HeartBrassGold.copy(alpha = 0.22f) else GlassSurfaceTranslucent,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (isLead) GlassBorderBrush else Brush.linearGradient(
                listOf(species.primaryElement.color.copy(alpha = 0.7f), HandheldPanelElevated)
            )
        )
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Canvas(modifier = Modifier.size(40.dp)) {
                drawMonsterPixelSprite(species, 0f, 0f, size.width, size.height)
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${monster.nickname} (Lv.${monster.level})",
                        style = MaterialTheme.typography.labelLarge,
                        color = RetroLcdParchment
                    )
                    Text(
                        text = if (isLead) "★ WALKING LEAD" else "TAP TO WALK",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isLead) HeartBrassBright else SoulSpireBright
                    )
                }
                Text(
                    text = "${species.primaryElement.displayName} • HP ${monster.currentHp}/${monster.maxHp} • Amity ♥${monster.amity} • EXP ${monster.exp}/${monster.level * 35}",
                    style = MaterialTheme.typography.bodySmall,
                    color = SoulSpireBright
                )
            }
        }
    }
}
