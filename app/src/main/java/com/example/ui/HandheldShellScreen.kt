package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.CatchingPokemon
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.model.BottomGadgetTab
import com.example.model.MonsterCatalog
import com.example.ui.canvas.TopHandheldViewport
import com.example.ui.canvas.drawMonsterPixelSprite
import com.example.ui.components.BattleCommandDeck
import com.example.ui.components.ExplorerControlDeck
import com.example.ui.components.PartyAndCompendiumPanel
import com.example.ui.components.SacredBellAndForgePanel
import com.example.ui.components.SpiritWalkerPedometerPanel
import com.example.ui.theme.GlassBorderBrush
import com.example.ui.theme.GlassSubtleBorderBrush
import com.example.ui.theme.GlassSurfaceTranslucent
import com.example.ui.theme.HandheldBezelSurface
import com.example.ui.theme.HandheldChassisDark
import com.example.ui.theme.HandheldPanelElevated
import com.example.ui.theme.HeartBrassBright
import com.example.ui.theme.HeartBrassGold
import com.example.ui.theme.JadeHpGreen
import com.example.ui.theme.RetroLcdParchment
import com.example.ui.theme.SoulSpireBright
import com.example.ui.theme.SoulSpireSilver
import com.example.ui.theme.ToriiVermilion
import com.example.viewmodel.GameEngineViewModel
import com.example.viewmodel.GameUiState

@Composable
fun HandheldShellScreen(
    viewModel: GameEngineViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler(
        enabled = uiState.showCompendiumDialog || uiState.activeTab != BottomGadgetTab.CONTROLS
    ) {
        if (uiState.showCompendiumDialog) {
            viewModel.toggleCompendiumDialog(false)
        } else {
            viewModel.selectTab(BottomGadgetTab.CONTROLS)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = HandheldChassisDark,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            HandheldChassisDark,
                            Color(0xFF151224),
                            HandheldChassisDark
                        )
                    )
                )
                .padding(innerPadding)
        ) {
            if (!uiState.isLoading && !uiState.save.starterChosen) {
                StarterSelectionScreen(
                    onSelectStarter = { speciesId ->
                        viewModel.chooseStarterMonster(speciesId)
                    }
                )
            } else {
                AdaptiveDualScreenConsole(
                    uiState = uiState,
                    viewModel = viewModel
                )
            }

            if (uiState.showCompendiumDialog) {
                KouhakuCompendiumModal(
                    ownedSpeciesIds = uiState.ownedSpeciesIds,
                    onDismiss = { viewModel.toggleCompendiumDialog(false) }
                )
            }
        }
    }
}

@Composable
private fun AdaptiveDualScreenConsole(
    uiState: GameUiState,
    viewModel: GameEngineViewModel
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        val isWideTablet = maxWidth > 640.dp

        if (isWideTablet) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TopHandheldViewport(
                    uiState = uiState,
                    modifier = Modifier
                        .weight(1.05f)
                        .fillMaxHeight()
                )
                BottomKouhakuGearShell(
                    uiState = uiState,
                    viewModel = viewModel,
                    modifier = Modifier
                        .weight(0.95f)
                        .fillMaxHeight()
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TopHandheldViewport(
                    uiState = uiState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.47f)
                )

                HandheldHingeBar(uiState = uiState)

                BottomKouhakuGearShell(
                    uiState = uiState,
                    viewModel = viewModel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.53f)
                )
            }
        }
    }
}

@Composable
private fun HandheldHingeBar(uiState: GameUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(20.dp)
            .clip(RoundedCornerShape(50))
            .background(GlassSurfaceTranslucent)
            .border(1.dp, GlassBorderBrush, RoundedCornerShape(50))
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(4) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(SoulSpireBright.copy(alpha = 0.7f))
                )
            }
        }
        Text(
            text = "KOUHAKU: BRASS & SILVER • HOLO DECK",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = HeartBrassBright
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(if (uiState.battleState != null) ToriiVermilion else JadeHpGreen)
            )
            Text(
                text = "SYNC",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = SoulSpireBright
            )
        }
    }
}

@Composable
private fun BottomKouhakuGearShell(
    uiState: GameUiState,
    viewModel: GameEngineViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .border(1.5.dp, GlassBorderBrush, RoundedCornerShape(14.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        HandheldBezelSurface.copy(alpha = 0.94f),
                        HandheldChassisDark.copy(alpha = 0.98f)
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Box(modifier = Modifier.weight(1f)) {
            val battle = uiState.battleState
            if (battle != null) {
                BattleCommandDeck(
                    uiState = uiState,
                    battle = battle,
                    viewModel = viewModel
                )
            } else {
                when (uiState.activeTab) {
                    BottomGadgetTab.CONTROLS -> ExplorerControlDeck(uiState, viewModel)
                    BottomGadgetTab.SPIRIT_WALKER -> SpiritWalkerPedometerPanel(uiState, viewModel)
                    BottomGadgetTab.SHRINE_BELLS -> SacredBellAndForgePanel(uiState, viewModel)
                    BottomGadgetTab.PARTY_COMPENDIUM -> PartyAndCompendiumPanel(uiState, viewModel)
                }
            }
        }

        // Sleek Glassmorphic Mode Switcher Bar with Glowing Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xDD0D0B14))
                .padding(horizontal = 6.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            GadgetNavTabButton(
                label = "D-Pad",
                icon = Icons.Default.Gamepad,
                selected = uiState.activeTab == BottomGadgetTab.CONTROLS,
                tag = "tab_controls",
                onClick = { viewModel.selectTab(BottomGadgetTab.CONTROLS) },
                modifier = Modifier.weight(1f)
            )
            GadgetNavTabButton(
                label = "Walker",
                icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                selected = uiState.activeTab == BottomGadgetTab.SPIRIT_WALKER,
                tag = "tab_walker",
                onClick = { viewModel.selectTab(BottomGadgetTab.SPIRIT_WALKER) },
                modifier = Modifier.weight(1f)
            )
            GadgetNavTabButton(
                label = "Bells",
                icon = Icons.Default.NotificationsActive,
                selected = uiState.activeTab == BottomGadgetTab.SHRINE_BELLS,
                tag = "tab_bells",
                onClick = { viewModel.selectTab(BottomGadgetTab.SHRINE_BELLS) },
                modifier = Modifier.weight(1f)
            )
            GadgetNavTabButton(
                label = "Party",
                icon = Icons.Default.CatchingPokemon,
                selected = uiState.activeTab == BottomGadgetTab.PARTY_COMPENDIUM,
                tag = "tab_party",
                onClick = { viewModel.selectTab(BottomGadgetTab.PARTY_COMPENDIUM) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun GadgetNavTabButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
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
        color = if (selected) HeartBrassGold.copy(alpha = 0.25f) else GlassSurfaceTranslucent,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.2.dp,
            if (selected) GlassBorderBrush else GlassSubtleBorderBrush
        )
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) HeartBrassBright else SoulSpireBright,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) HeartBrassBright else RetroLcdParchment,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun StarterSelectionScreen(
    onSelectStarter: (Int) -> Unit
) {
    val starters = listOf(
        MonsterCatalog.getSpecies(1), // Kitsunebi
        MonsterCatalog.getSpecies(3), // Kappadrop
        MonsterCatalog.getSpecies(5)  // Tanukigale
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(2.dp, GlassBorderBrush)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.img_kouhaku_shrine_banner_1794839720747),
                    contentDescription = "Kouhaku Region Brass & Silver Towers",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color(0xCC0D0A18))
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(14.dp)
                ) {
                    Text(
                        text = "KOUHAKU: BRASS & SILVER",
                        style = MaterialTheme.typography.headlineMedium,
                        color = HeartBrassBright
                    )
                    Text(
                        text = "Sacred Bell Chronicles • Choose Your Partner Spirit",
                        style = MaterialTheme.typography.bodySmall,
                        color = RetroLcdParchment
                    )
                }
            }
        }

        Text(
            text = "Elder Ren of Momiji Hamlet offers three luminous Spirit Vessels. Your chosen partner will walk alongside you with dynamic elemental auras!",
            style = MaterialTheme.typography.bodyMedium,
            color = RetroLcdParchment,
            textAlign = TextAlign.Center
        )

        starters.forEach { species ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectStarter(species.id) }
                    .testTag("select_starter_${species.id}"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = GlassSurfaceTranslucent),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, species.primaryElement.color)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(HandheldChassisDark)
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawMonsterPixelSprite(species, 0f, 0f, size.width, size.height)
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = species.name.uppercase(),
                                style = MaterialTheme.typography.titleMedium,
                                color = HeartBrassBright
                            )
                            Text(
                                text = species.primaryElement.displayName.uppercase(),
                                style = MaterialTheme.typography.labelMedium,
                                color = species.primaryElement.color
                            )
                        }
                        Text(
                            text = species.title,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = SoulSpireBright
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = species.lore,
                            style = MaterialTheme.typography.bodySmall,
                            color = RetroLcdParchment
                        )
                    }

                    Button(
                        onClick = { onSelectStarter(species.id) },
                        modifier = Modifier.minimumInteractiveComponentSize(),
                        colors = ButtonDefaults.buttonColors(containerColor = species.primaryElement.color)
                    ) {
                        Text("Choose", style = MaterialTheme.typography.labelLarge, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun KouhakuCompendiumModal(
    ownedSpeciesIds: Set<Int>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(530.dp),
            color = HandheldBezelSurface,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(2.dp, GlassBorderBrush)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "KOUHAKU COMPENDIUM (${ownedSpeciesIds.size}/18)",
                        style = MaterialTheme.typography.titleMedium,
                        color = HeartBrassBright
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("close_compendium_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close Compendium", tint = RetroLcdParchment)
                    }
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(MonsterCatalog.speciesList, key = { it.id }) { species ->
                        val isTamed = ownedSpeciesIds.contains(species.id)
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = GlassSurfaceTranslucent,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.2.dp,
                                if (isTamed) GlassBorderBrush else GlassSubtleBorderBrush
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Canvas(modifier = Modifier.size(46.dp)) {
                                    drawMonsterPixelSprite(species, 0f, 0f, size.width, size.height)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "#${species.id.toString().padStart(2, '0')} ${species.name}",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = if (isTamed) HeartBrassBright else RetroLcdParchment
                                        )
                                        Text(
                                            text = if (isTamed) "★ TAMED" else species.primaryElement.displayName,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isTamed) HeartBrassGold else species.primaryElement.color
                                        )
                                    }
                                    Text(
                                        text = species.lore,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SoulSpireBright
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
