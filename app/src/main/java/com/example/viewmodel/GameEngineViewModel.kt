package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.KouhakuRepository
import com.example.data.OwnedMonsterEntity
import com.example.data.PlayerSaveEntity
import com.example.model.BottomGadgetTab
import com.example.model.CaptureOrbType
import com.example.model.Direction
import com.example.model.ElementAffinity
import com.example.model.KouhakuMaps
import com.example.model.MonsterCatalog
import com.example.model.MonsterSpecies
import com.example.model.MoveTechnique
import com.example.model.RegionalMap
import com.example.model.TileType
import com.example.model.TimeOfDay
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CombatVfxEvent(
    val element: ElementAffinity,
    val moveName: String,
    val isTargetingEnemy: Boolean,
    val damageAmount: Int,
    val isCritical: Boolean = false,
    val isHeal: Boolean = false
)

data class BattleState(
    val isTrainerBattle: Boolean = false,
    val trainerTitle: String? = null,
    val trainerRewardType: String? = null, // "BRASS_DOJO", "SILVER_DOJO", "GRANDMASTER"
    val enemySpecies: MonsterSpecies,
    val enemyLevel: Int,
    val enemyCurrentHp: Int,
    val enemyMaxHp: Int,
    val battleLog: String,
    val lastEffectMultiplier: Float = 1.0f,
    val activeVfx: CombatVfxEvent? = null,
    val orbShakeCount: Int = 0,
    val isAnimatingOrb: Boolean = false,
    val battleFinished: Boolean = false,
    val playerWon: Boolean = false
)

private data class EphemeralEngineState(
    val activeTab: BottomGadgetTab = BottomGadgetTab.CONTROLS,
    val statusBannerText: String = "Welcome to Momiji Hamlet! Tap D-Pad to explore with your partner.",
    val followerReactionText: String = "Your partner spirit gazes warmly at the autumn maple leaves!",
    val followerHeartPulse: Boolean = false,
    val battleState: BattleState? = null,
    val bellResonanceColorHex: Long? = null,
    val harvestedAcornCoords: Set<String> = emptySet(),
    val showCompendiumDialog: Boolean = false
)

data class GameUiState(
    val isLoading: Boolean = true,
    val save: PlayerSaveEntity = PlayerSaveEntity(),
    val currentMap: RegionalMap = KouhakuMaps.getMap("momiji_hamlet"),
    val partyMonsters: List<OwnedMonsterEntity> = emptyList(),
    val sanctuaryMonsters: List<OwnedMonsterEntity> = emptyList(),
    val leadMonster: OwnedMonsterEntity? = null,
    val leadSpecies: MonsterSpecies? = null,
    val ownedSpeciesIds: Set<Int> = emptySet(),
    val timeOfDay: TimeOfDay = TimeOfDay.TWILIGHT,
    val playerDirection: Direction = Direction.DOWN,
    val activeTab: BottomGadgetTab = BottomGadgetTab.CONTROLS,
    val statusBannerText: String = "",
    val followerReactionText: String = "",
    val followerHeartPulse: Boolean = false,
    val battleState: BattleState? = null,
    val bellResonanceColorHex: Long? = null,
    val showCompendiumDialog: Boolean = false
)

class GameEngineViewModel(
    private val repository: KouhakuRepository
) : ViewModel() {

    private val ephemeralState = MutableStateFlow(EphemeralEngineState())

    val uiState: StateFlow<GameUiState> = combine(
        repository.playerSaveFlow,
        repository.monstersFlow,
        ephemeralState
    ) { saveNullable, allMonsters, eph ->
        val save = saveNullable ?: PlayerSaveEntity()
        val party = allMonsters.filter { it.partySlot != null }.sortedBy { it.partySlot }
        val sanctuary = allMonsters.filter { it.partySlot == null }
        val lead = party.firstOrNull { it.currentHp > 0 } ?: party.firstOrNull()
        val leadSpec = lead?.let { MonsterCatalog.getSpecies(it.speciesId) }
        val tod = runCatching { TimeOfDay.valueOf(save.timeOfDay) }.getOrDefault(TimeOfDay.TWILIGHT)
        val dir = runCatching { Direction.valueOf(save.facing) }.getOrDefault(Direction.DOWN)

        GameUiState(
            isLoading = saveNullable == null,
            save = save,
            currentMap = KouhakuMaps.getMap(save.mapId),
            partyMonsters = party,
            sanctuaryMonsters = sanctuary,
            leadMonster = lead,
            leadSpecies = leadSpec,
            ownedSpeciesIds = allMonsters.map { it.speciesId }.toSet(),
            timeOfDay = tod,
            playerDirection = dir,
            activeTab = eph.activeTab,
            statusBannerText = eph.statusBannerText,
            followerReactionText = eph.followerReactionText,
            followerHeartPulse = eph.followerHeartPulse,
            battleState = eph.battleState,
            bellResonanceColorHex = eph.bellResonanceColorHex,
            showCompendiumDialog = eph.showCompendiumDialog
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GameUiState()
    )

    init {
        viewModelScope.launch {
            repository.ensureInitialized()
        }
    }

    fun selectTab(tab: BottomGadgetTab) {
        ephemeralState.update { it.copy(activeTab = tab) }
    }

    fun toggleCompendiumDialog(show: Boolean) {
        ephemeralState.update { it.copy(showCompendiumDialog = show) }
    }

    fun chooseStarterMonster(speciesId: Int, customPlayerName: String = "Akira") {
        viewModelScope.launch {
            val species = MonsterCatalog.getSpecies(speciesId)
            val level = 5
            val maxHp = calculateMaxHp(species.baseHp, level)
            val starter = OwnedMonsterEntity(
                speciesId = species.id,
                nickname = species.name,
                level = level,
                currentHp = maxHp,
                maxHp = maxHp,
                exp = 0,
                amity = 95,
                partySlot = 0
            )
            repository.addMonster(starter)
            val currentSave = repository.ensureInitialized()
            val updatedSave = currentSave.copy(
                playerName = customPlayerName.ifBlank { "Akira" },
                starterChosen = true,
                mapId = "momiji_hamlet",
                tileX = 5,
                tileY = 6,
                followerX = 5,
                followerY = 7
            )
            repository.savePlayer(updatedSave)
            ephemeralState.update {
                it.copy(
                    statusBannerText = "${species.name} emerged from its Spirit Vessel to walk beside you!",
                    followerReactionText = "${species.name}'s elemental aura shimmers warmly behind your footsteps!"
                )
            }
        }
    }

    fun movePlayer(direction: Direction) {
        val state = uiState.value
        if (!state.save.starterChosen || state.battleState != null) return

        val map = state.currentMap
        val oldX = state.save.tileX
        val oldY = state.save.tileY
        val targetX = oldX + direction.dx
        val targetY = oldY + direction.dy
        val targetTile = map.tileAt(targetX, targetY)

        if (!targetTile.walkable) {
            viewModelScope.launch {
                repository.savePlayer(state.save.copy(facing = direction.name))
            }
            ephemeralState.update {
                it.copy(statusBannerText = "Blocked by ${targetTile.description}.")
            }
            return
        }

        viewModelScope.launch {
            val portal = map.portals[targetTile]
            if (portal != null) {
                val destMap = KouhakuMaps.getMap(portal.targetMapId)
                val newSave = state.save.copy(
                    mapId = destMap.id,
                    tileX = portal.targetX,
                    tileY = portal.targetY,
                    facing = direction.name,
                    followerX = portal.targetX - direction.dx,
                    followerY = portal.targetY - direction.dy,
                    stepCount = state.save.stepCount + 1,
                    unredeemedSteps = state.save.unredeemedSteps + 1
                )
                repository.savePlayer(newSave)
                ephemeralState.update {
                    it.copy(
                        statusBannerText = "Entered ${destMap.name} — ${destMap.subtitle}.",
                        followerReactionText = "${state.leadMonster?.nickname ?: "Partner"} admires the scenery of ${destMap.name}!"
                    )
                }
                return@launch
            }

            val newSteps = state.save.stepCount + 1
            val newUnredeemed = state.save.unredeemedSteps + 1

            state.leadMonster?.let { lead ->
                if (newSteps % 10 == 0 && lead.amity < 255) {
                    repository.updateMonster(lead.copy(amity = min(255, lead.amity + 3)))
                }
            }

            val updatedSave = state.save.copy(
                tileX = targetX,
                tileY = targetY,
                facing = direction.name,
                followerX = oldX,
                followerY = oldY,
                stepCount = newSteps,
                unredeemedSteps = newUnredeemed
            )
            repository.savePlayer(updatedSave)

            when (targetTile) {
                TileType.LANTERN_HEAL -> {
                    repository.healAllPartyMonsters()
                    ephemeralState.update {
                        it.copy(
                            statusBannerText = "Sacred Stone Lantern's radiant bloom restored your entire party's HP!",
                            followerHeartPulse = true
                        )
                    }
                }
                TileType.TALL_GRASS -> {
                    val roll = Random.nextFloat()
                    val threshold = 0.27f * state.timeOfDay.encounterRateBonus
                    if (roll < threshold) {
                        triggerWildEncounter(map, state.timeOfDay)
                    } else {
                        ephemeralState.update {
                            it.copy(statusBannerText = "Sunlit autumn grass sways gently... ($newSteps steps)")
                        }
                    }
                }
                else -> {
                    ephemeralState.update {
                        it.copy(statusBannerText = "${map.name} ($targetX, $targetY) • ${targetTile.description}")
                    }
                }
            }
        }
    }

    fun fastTravelToMap(mapId: String) {
        val state = uiState.value
        if (!state.save.starterChosen || state.battleState != null) return
        val destMap = KouhakuMaps.getMap(mapId)
        viewModelScope.launch {
            repository.savePlayer(
                state.save.copy(
                    mapId = destMap.id,
                    tileX = 5,
                    tileY = 4,
                    followerX = 5,
                    followerY = 5,
                    facing = Direction.DOWN.name
                )
            )
            ephemeralState.update {
                it.copy(statusBannerText = "Shrine Wind carried you to ${destMap.name}!")
            }
        }
    }

    fun interactWithEnvironment() {
        val state = uiState.value
        if (!state.save.starterChosen || state.battleState != null) return

        val map = state.currentMap
        val standingTile = map.tileAt(state.save.tileX, state.save.tileY)
        val frontX = state.save.tileX + state.playerDirection.dx
        val frontY = state.save.tileY + state.playerDirection.dy
        val facingTile = map.tileAt(frontX, frontY)

        if (frontX == state.save.followerX && frontY == state.save.followerY) {
            talkToWalkingPartner()
            return
        }

        val activeTile = if (standingTile != TileType.GRASS_PATH && standingTile != TileType.TALL_GRASS) {
            standingTile
        } else {
            facingTile
        }

        viewModelScope.launch {
            when (activeTile) {
                TileType.LANTERN_HEAL -> {
                    repository.healAllPartyMonsters()
                    ephemeralState.update {
                        it.copy(statusBannerText = "Rested at the Sacred Lantern. All party monsters are at full vigor!")
                    }
                }
                TileType.ACORN_TREE -> {
                    val key = "${map.id}_${state.save.tileX}_${state.save.tileY}"
                    val save = state.save
                    val updated = save.copy(
                        crimsonAcorns = save.crimsonAcorns + 1,
                        duskAcorns = save.duskAcorns + 1,
                        brassAcorns = save.brassAcorns + (if (Random.nextBoolean()) 1 else 0)
                    )
                    repository.savePlayer(updated)
                    ephemeralState.update {
                        it.copy(
                            harvestedAcornCoords = it.harvestedAcornCoords + key,
                            statusBannerText = "Shook the Spirit-Acorn Tree! Found Crimson & Dusk Acorns for the Orb Forge!"
                        )
                    }
                }
                TileType.DOJO_MASTER_BRASS -> {
                    startTrainerBattle(
                        trainerTitle = "Dojo Master Kagerou (Brass Sanctuary)",
                        rewardType = "BRASS_DOJO",
                        speciesId = 13,
                        level = 15
                    )
                }
                TileType.DOJO_MASTER_SILVER -> {
                    startTrainerBattle(
                        trainerTitle = "Dojo Master Suiren (Moonlit Lake)",
                        rewardType = "SILVER_DOJO",
                        speciesId = 15,
                        level = 17
                    )
                }
                TileType.GRANDMASTER_RYOMA -> {
                    startTrainerBattle(
                        trainerTitle = "Elder Grandmaster Ryoma (Bellflower Summit)",
                        rewardType = "GRANDMASTER",
                        speciesId = 16,
                        level = 22
                    )
                }
                TileType.SHRINE_ALTAR_BRASS -> ringSacredBell(isBrassBell = true)
                TileType.SHRINE_ALTAR_SILVER -> ringSacredBell(isBrassBell = false)
                else -> talkToWalkingPartner()
            }
        }
    }

    fun talkToWalkingPartner() {
        val state = uiState.value
        val lead = state.leadMonster ?: return

        viewModelScope.launch {
            val foundGift = Random.nextFloat() < 0.30f
            val amityTier = when {
                lead.amity >= 180 -> "Soul-Bonded"
                lead.amity >= 120 -> "Devoted"
                else -> "Friendly"
            }

            val reactions = listOf(
                "${lead.nickname} nuzzles your sleeve happily! ($amityTier • Amity ${lead.amity}/255)",
                "${lead.nickname} basks in the ${state.timeOfDay.label} rays and hums a shrine melody!",
                "${lead.nickname} is playfully chasing glowing autumn embers behind you!",
                "${lead.nickname}'s aura flares brightly as it guards you in ${state.currentMap.name}!"
            )

            var msg = reactions.random()
            if (foundGift) {
                val save = state.save
                repository.savePlayer(save.copy(crimsonAcorns = save.crimsonAcorns + 1))
                msg = "${lead.nickname} tugged your cloak and handed you a luminous Crimson Acorn it found!"
            }

            val newAmity = min(255, lead.amity + 4)
            repository.updateMonster(lead.copy(amity = newAmity))

            ephemeralState.update {
                it.copy(
                    followerReactionText = msg,
                    statusBannerText = msg,
                    followerHeartPulse = !it.followerHeartPulse
                )
            }
        }
    }

    fun simulateBriskWalk() {
        val state = uiState.value
        if (!state.save.starterChosen) return
        viewModelScope.launch {
            val save = state.save
            val updated = save.copy(
                stepCount = save.stepCount + 25,
                unredeemedSteps = save.unredeemedSteps + 25
            )
            repository.savePlayer(updated)
            state.leadMonster?.let { lead ->
                repository.updateMonster(lead.copy(amity = min(255, lead.amity + 5)))
            }
            ephemeralState.update {
                it.copy(
                    statusBannerText = "Holo-Walker logged +25 brisk steps with ${state.leadMonster?.nickname ?: "your partner"}!",
                    followerReactionText = "${state.leadMonster?.nickname ?: "Partner"} matches your stride with radiant energy!"
                )
            }
        }
    }

    fun dowseStepRewards() {
        val state = uiState.value
        if (state.save.unredeemedSteps < 50) {
            ephemeralState.update {
                it.copy(statusBannerText = "Need at least 50 unredeemed Holo-Walker steps to dowse for Shrine treasures!")
            }
            return
        }
        viewModelScope.launch {
            val save = state.save
            val updatedSave = save.copy(
                unredeemedSteps = save.unredeemedSteps - 50,
                spiritOrbs = save.spiritOrbs + 1,
                crimsonAcorns = save.crimsonAcorns + 1,
                duskAcorns = save.duskAcorns + 1,
                brassAcorns = save.brassAcorns + 1,
                healingTeas = save.healingTeas + 1
            )
            repository.savePlayer(updatedSave)

            state.leadMonster?.let { lead ->
                val boosted = grantExpToMonster(lead, 45)
                repository.updateMonster(boosted.copy(amity = min(255, boosted.amity + 8)))
            }

            ephemeralState.update {
                it.copy(
                    statusBannerText = "Holo-Walker Dowsing: Found +1 Spirit Orb, +3 Acorns, +1 Healing Tea & Partner EXP!"
                )
            }
        }
    }

    fun cycleTimeOfDay() {
        val state = uiState.value
        val nextTime = state.timeOfDay.next()
        viewModelScope.launch {
            repository.savePlayer(state.save.copy(timeOfDay = nextTime.name))
            ephemeralState.update {
                it.copy(
                    statusBannerText = "Kouhaku lighting shifted to ${nextTime.label} (${nextTime.japaneseSub}). Dynamic rays & wild spawns updated!"
                )
            }
        }
    }

    fun craftCaptureOrb(orbType: CaptureOrbType) {
        val state = uiState.value
        val save = state.save
        viewModelScope.launch {
            when (orbType) {
                CaptureOrbType.EMBER_ORB -> {
                    if (save.crimsonAcorns <= 0) {
                        ephemeralState.update { it.copy(statusBannerText = "Need 1 Crimson Acorn to forge a Crimson Ember Orb!") }
                        return@launch
                    }
                    repository.savePlayer(save.copy(crimsonAcorns = save.crimsonAcorns - 1, emberOrbs = save.emberOrbs + 2))
                }
                CaptureOrbType.DUSK_ORB -> {
                    if (save.duskAcorns <= 0) {
                        ephemeralState.update { it.copy(statusBannerText = "Need 1 Dusk Acorn to forge a Moonlit Dusk Orb!") }
                        return@launch
                    }
                    repository.savePlayer(save.copy(duskAcorns = save.duskAcorns - 1, duskOrbs = save.duskOrbs + 2))
                }
                CaptureOrbType.HEAVY_BRASS_ORB -> {
                    if (save.brassAcorns <= 0) {
                        ephemeralState.update { it.copy(statusBannerText = "Need 1 Brass Acorn to forge a Heavy Brass Orb!") }
                        return@launch
                    }
                    repository.savePlayer(save.copy(brassAcorns = save.brassAcorns - 1, heavyBrassOrbs = save.heavyBrassOrbs + 2))
                }
                CaptureOrbType.SPIRIT_ORB -> return@launch
            }
            ephemeralState.update {
                it.copy(statusBannerText = "Shrine Kiln forged +2 ${orbType.displayName}s!")
            }
        }
    }

    fun ringSacredBell(isBrassBell: Boolean) {
        val state = uiState.value
        if (state.battleState != null) return

        viewModelScope.launch {
            if (isBrassBell) {
                if (!state.save.brassEmblemEarned) {
                    ephemeralState.update {
                        it.copy(statusBannerText = "Defeat Dojo Master Kagerou in Brass Chime Sanctuary to earn the Clear Brass Bell!")
                    }
                    return@launch
                }
                ephemeralState.update {
                    it.copy(
                        bellResonanceColorHex = 0xFFF5B942,
                        statusBannerText = "The Clear Brass Bell tolls across the autumn sky... HOUOU-KIN descends!"
                    )
                }
                delay(550)
                val houou = MonsterCatalog.getSpecies(17)
                val maxHp = calculateMaxHp(houou.baseHp, 24)
                ephemeralState.update {
                    it.copy(
                        bellResonanceColorHex = null,
                        battleState = BattleState(
                            isTrainerBattle = false,
                            enemySpecies = houou,
                            enemyLevel = 24,
                            enemyCurrentHp = maxHp,
                            enemyMaxHp = maxHp,
                            battleLog = "Sacred Guardian HOUOU-KIN descended in a radiant storm of golden feathers!"
                        )
                    )
                }
            } else {
                if (!state.save.silverEmblemEarned) {
                    ephemeralState.update {
                        it.copy(statusBannerText = "Defeat Dojo Master Suiren at Moonlit Silver Lake to earn the Tidal Silver Bell!")
                    }
                    return@launch
                }
                ephemeralState.update {
                    it.copy(
                        bellResonanceColorHex = 0xFF72C8F8,
                        statusBannerText = "The Tidal Silver Bell echoes beneath the silver waves... RYUJIN-GIN rises!"
                    )
                }
                delay(550)
                val ryujin = MonsterCatalog.getSpecies(18)
                val maxHp = calculateMaxHp(ryujin.baseHp, 24)
                ephemeralState.update {
                    it.copy(
                        bellResonanceColorHex = null,
                        battleState = BattleState(
                            isTrainerBattle = false,
                            enemySpecies = ryujin,
                            enemyLevel = 24,
                            enemyCurrentHp = maxHp,
                            enemyMaxHp = maxHp,
                            battleLog = "Sacred Guardian RYUJIN-GIN surged from the luminous silver abyss!"
                        )
                    )
                }
            }
        }
    }

    private fun triggerWildEncounter(map: RegionalMap, timeOfDay: TimeOfDay) {
        val pool = if (timeOfDay == TimeOfDay.NIGHT || timeOfDay == TimeOfDay.TWILIGHT) {
            map.wildPoolNight
        } else {
            map.wildPoolDay
        }
        val speciesId = pool.random()
        val species = MonsterCatalog.getSpecies(speciesId)
        val level = Random.nextInt(map.minWildLevel, map.maxWildLevel + 1)
        val maxHp = calculateMaxHp(species.baseHp, level)

        ephemeralState.update {
            it.copy(
                battleState = BattleState(
                    isTrainerBattle = false,
                    enemySpecies = species,
                    enemyLevel = level,
                    enemyCurrentHp = maxHp,
                    enemyMaxHp = maxHp,
                    battleLog = "A wild ${species.name} (Lv.$level) emerged from the ${timeOfDay.label} grass!"
                )
            )
        }
    }

    private fun startTrainerBattle(
        trainerTitle: String,
        rewardType: String,
        speciesId: Int,
        level: Int
    ) {
        val species = MonsterCatalog.getSpecies(speciesId)
        val maxHp = calculateMaxHp(species.baseHp, level)
        ephemeralState.update {
            it.copy(
                battleState = BattleState(
                    isTrainerBattle = true,
                    trainerTitle = trainerTitle,
                    trainerRewardType = rewardType,
                    enemySpecies = species,
                    enemyLevel = level,
                    enemyCurrentHp = maxHp,
                    enemyMaxHp = maxHp,
                    battleLog = "$trainerTitle sent out ${species.name} (Lv.$level)!"
                )
            )
        }
    }

    fun executePlayerMove(move: MoveTechnique) {
        val state = uiState.value
        val battle = state.battleState ?: return
        if (battle.battleFinished || battle.isAnimatingOrb) return
        val lead = state.leadMonster ?: return
        val leadSpecies = state.leadSpecies ?: return

        viewModelScope.launch {
            if (move.healFraction > 0f && move.power == 0) {
                val healAmt = max(8, (lead.maxHp * move.healFraction).toInt())
                val newPlayerHp = min(lead.maxHp, lead.currentHp + healAmt)
                repository.updateMonster(lead.copy(currentHp = newPlayerHp))
                ephemeralState.update {
                    it.copy(
                        battleState = battle.copy(
                            activeVfx = CombatVfxEvent(
                                element = move.element,
                                moveName = move.name,
                                isTargetingEnemy = false,
                                damageAmount = healAmt,
                                isHeal = true
                            ),
                            battleLog = "${lead.nickname} brewed ${move.name} and restored +$healAmt HP!"
                        )
                    )
                }
                delay(550)
                executeEnemyCounterTurn(lead.copy(currentHp = newPlayerHp), leadSpecies)
                return@launch
            }

            val effPrimary = move.element.effectivenessAgainst(battle.enemySpecies.primaryElement)
            val effSecondary = battle.enemySpecies.secondaryElement?.let {
                move.element.effectivenessAgainst(it)
            } ?: 1.0f
            val totalEff = effPrimary * effSecondary
            val stab = if (move.element == leadSpecies.primaryElement || move.element == leadSpecies.secondaryElement) 1.25f else 1.0f
            val amityCrit = if (lead.amity >= 150 && Random.nextFloat() < 0.22f) 1.4f else 1.0f

            val rawDmg = (((lead.level * 0.45f + 2f) * move.power * (leadSpecies.baseAtk.toFloat() / battle.enemySpecies.baseDef.toFloat())) / 14f) + 4f
            val finalDmg = max(4, (rawDmg * totalEff * stab * amityCrit).toInt())
            val newEnemyHp = max(0, battle.enemyCurrentHp - finalDmg)

            var updatedLead = lead
            if (move.healFraction > 0f) {
                val drainHeal = max(4, (finalDmg * move.healFraction).toInt())
                updatedLead = lead.copy(currentHp = min(lead.maxHp, lead.currentHp + drainHeal))
                repository.updateMonster(updatedLead)
            }

            val effNote = when {
                totalEff > 1.2f -> " Super Effective!"
                totalEff < 0.85f -> " Resisted..."
                else -> ""
            }
            val critNote = if (amityCrit > 1.0f) " [Amity Crit!]" else ""

            ephemeralState.update {
                it.copy(
                    battleState = battle.copy(
                        enemyCurrentHp = newEnemyHp,
                        lastEffectMultiplier = totalEff,
                        activeVfx = CombatVfxEvent(
                            element = move.element,
                            moveName = move.name,
                            isTargetingEnemy = true,
                            damageAmount = finalDmg,
                            isCritical = amityCrit > 1.0f || totalEff > 1.2f
                        ),
                        battleLog = "${updatedLead.nickname} unleashed ${move.name}! (-$finalDmg HP)$effNote$critNote"
                    )
                )
            }

            delay(560)

            if (newEnemyHp <= 0) {
                handleBattleVictory(battle, updatedLead)
            } else {
                executeEnemyCounterTurn(updatedLead, leadSpecies)
            }
        }
    }

    private suspend fun executeEnemyCounterTurn(currentLead: OwnedMonsterEntity, leadSpecies: MonsterSpecies) {
        val battle = ephemeralState.value.battleState ?: return
        if (battle.battleFinished) return

        val enemyMoves = MonsterCatalog.getMovesForSpecies(battle.enemySpecies).filter { it.power > 0 }
        val chosenMove = enemyMoves.randomOrNull() ?: MonsterCatalog.moves.getValue(1)

        val eff = chosenMove.element.effectivenessAgainst(leadSpecies.primaryElement)
        val rawDmg = (((battle.enemyLevel * 0.4f + 2f) * chosenMove.power * (battle.enemySpecies.baseAtk.toFloat() / leadSpecies.baseDef.toFloat())) / 16f) + 3f
        var dmg = max(3, (rawDmg * eff).toInt())

        var amityEndured = false
        if (currentLead.currentHp - dmg <= 0 && currentLead.amity >= 140 && Random.nextFloat() < 0.5f) {
            dmg = currentLead.currentHp - 1
            amityEndured = true
        }

        val newPlayerHp = max(0, currentLead.currentHp - dmg)
        val updatedLead = currentLead.copy(currentHp = newPlayerHp)
        repository.updateMonster(updatedLead)

        if (newPlayerHp <= 0) {
            val allParty = repository.getMonstersOnce().filter { it.partySlot != null }
            val nextHealthy = allParty.firstOrNull { it.uid != currentLead.uid && it.currentHp > 0 }
            if (nextHealthy != null) {
                ephemeralState.update {
                    it.copy(
                        battleState = battle.copy(
                            activeVfx = null,
                            battleLog = "${currentLead.nickname} fainted! ${nextHealthy.nickname} stepped into the arena!"
                        )
                    )
                }
            } else {
                repository.healAllPartyMonsters()
                ephemeralState.update {
                    it.copy(
                        battleState = null,
                        statusBannerText = "Your party rested by a Sacred Lantern and recovered their strength!"
                    )
                }
            }
        } else {
            val endureText = if (amityEndured) " ${currentLead.nickname} endured at 1 HP for your sake!" else ""
            ephemeralState.update {
                it.copy(
                    battleState = battle.copy(
                        activeVfx = CombatVfxEvent(
                            element = chosenMove.element,
                            moveName = chosenMove.name,
                            isTargetingEnemy = false,
                            damageAmount = dmg,
                            isCritical = eff > 1.2f
                        ),
                        battleLog = "${battle.enemySpecies.name} struck with ${chosenMove.name} (-$dmg HP)!$endureText"
                    )
                )
            }
            delay(520)
            ephemeralState.update { state ->
                state.copy(battleState = state.battleState?.copy(activeVfx = null))
            }
        }
    }

    private suspend fun handleBattleVictory(
        battle: BattleState,
        lead: OwnedMonsterEntity
    ) {
        val expGain = battle.enemyLevel * (if (battle.isTrainerBattle) 26 else 18)
        val leveledMonster = grantExpToMonster(lead, expGain)
        repository.updateMonster(leveledMonster)

        val save = uiState.value.save
        var rewardBanner = "Defeated ${battle.enemySpecies.name}! ${leveledMonster.nickname} gained +$expGain EXP"
        if (leveledMonster.level > lead.level) {
            rewardBanner += " & grew to Lv.${leveledMonster.level}!"
        }
        if (leveledMonster.speciesId != lead.speciesId) {
            rewardBanner += " Evolved into ${leveledMonster.nickname}!"
        }

        when (battle.trainerRewardType) {
            "BRASS_DOJO" -> {
                repository.savePlayer(save.copy(brassEmblemEarned = true, heavyBrassOrbs = save.heavyBrassOrbs + 2))
                rewardBanner = "Earned the SUN-BRASS EMBLEM & CLEAR BRASS BELL from Master Kagerou!"
            }
            "SILVER_DOJO" -> {
                repository.savePlayer(save.copy(silverEmblemEarned = true, duskOrbs = save.duskOrbs + 2))
                rewardBanner = "Earned the MOON-SILVER EMBLEM & TIDAL SILVER BELL from Master Suiren!"
            }
            "GRANDMASTER" -> {
                repository.savePlayer(
                    save.copy(
                        grandmasterDefeated = true,
                        heavyBrassOrbs = save.heavyBrassOrbs + 3,
                        duskOrbs = save.duskOrbs + 3
                    )
                )
                rewardBanner = "Triumph over Grandmaster Ryoma! You are hailed as Sovereign of Kouhaku!"
            }
        }

        ephemeralState.update {
            it.copy(
                battleState = null,
                statusBannerText = rewardBanner,
                followerReactionText = "${leveledMonster.nickname} beams with pride after winning the battle!"
            )
        }
    }

    fun throwCaptureOrb(orbType: CaptureOrbType) {
        val state = uiState.value
        val battle = state.battleState ?: return
        if (battle.isTrainerBattle) {
            ephemeralState.update {
                it.copy(
                    battleState = battle.copy(
                        battleLog = "Cannot tame another Dojo Master's bonded partner spirit!"
                    )
                )
            }
            return
        }

        val save = state.save
        val availableCount = when (orbType) {
            CaptureOrbType.SPIRIT_ORB -> save.spiritOrbs
            CaptureOrbType.EMBER_ORB -> save.emberOrbs
            CaptureOrbType.DUSK_ORB -> save.duskOrbs
            CaptureOrbType.HEAVY_BRASS_ORB -> save.heavyBrassOrbs
        }
        if (availableCount <= 0) {
            ephemeralState.update {
                it.copy(
                    battleState = battle.copy(
                        battleLog = "Out of ${orbType.displayName}s! Craft more in the Acorn Forge or Dowse steps."
                    )
                )
            }
            return
        }

        viewModelScope.launch {
            val updatedSave = when (orbType) {
                CaptureOrbType.SPIRIT_ORB -> save.copy(spiritOrbs = save.spiritOrbs - 1)
                CaptureOrbType.EMBER_ORB -> save.copy(emberOrbs = save.emberOrbs - 1)
                CaptureOrbType.DUSK_ORB -> save.copy(duskOrbs = save.duskOrbs - 1)
                CaptureOrbType.HEAVY_BRASS_ORB -> save.copy(heavyBrassOrbs = save.heavyBrassOrbs - 1)
            }
            repository.savePlayer(updatedSave)

            val enemy = battle.enemySpecies
            val specialtyMult = when (orbType) {
                CaptureOrbType.SPIRIT_ORB -> 1.0f
                CaptureOrbType.EMBER_ORB ->
                    if (enemy.primaryElement.name in listOf("FLAME", "GALE") || state.timeOfDay == TimeOfDay.TWILIGHT) 2.2f else 1.3f
                CaptureOrbType.DUSK_ORB ->
                    if (state.timeOfDay == TimeOfDay.NIGHT || enemy.primaryElement.name in listOf("SPIRIT", "TIDE")) 2.5f else 1.3f
                CaptureOrbType.HEAVY_BRASS_ORB ->
                    if (enemy.isGuardianLegend || enemy.primaryElement.name in listOf("BRASS", "EARTH")) 2.4f else 1.4f
            }

            val hpFactor = (3f * battle.enemyMaxHp - 2f * battle.enemyCurrentHp) / (3f * battle.enemyMaxHp)
            val catchScore = hpFactor * (enemy.catchRate / 255f) * specialtyMult
            val caught = Random.nextFloat() < min(0.95f, catchScore + 0.25f)

            for (shake in 1..3) {
                ephemeralState.update {
                    it.copy(
                        battleState = battle.copy(
                            activeVfx = null,
                            isAnimatingOrb = true,
                            orbShakeCount = shake,
                            battleLog = "Threw ${orbType.displayName}... Resonance pulse $shake..."
                        )
                    )
                }
                delay(320)
            }

            if (caught) {
                val partySize = state.partyMonsters.size
                val slot = if (partySize < 6) partySize else null
                val newMonster = OwnedMonsterEntity(
                    speciesId = enemy.id,
                    nickname = enemy.name,
                    level = battle.enemyLevel,
                    currentHp = battle.enemyMaxHp,
                    maxHp = battle.enemyMaxHp,
                    exp = 0,
                    amity = 85,
                    partySlot = slot
                )
                repository.addMonster(newMonster)

                val destNote = if (slot != null) "added to Party Slot #${slot + 1}" else "sent to Shrine Sanctuary Box"
                ephemeralState.update {
                    it.copy(
                        battleState = null,
                        statusBannerText = "Gotcha! ${enemy.name} (Lv.${battle.enemyLevel}) was tamed and $destNote!"
                    )
                }
            } else {
                ephemeralState.update {
                    it.copy(
                        battleState = battle.copy(
                            isAnimatingOrb = false,
                            orbShakeCount = 0,
                            battleLog = "Wild ${enemy.name} burst free from the ${orbType.displayName}!"
                        )
                    )
                }
                delay(400)
                val lead = state.leadMonster
                val leadSpec = state.leadSpecies
                if (lead != null && leadSpec != null) {
                    executeEnemyCounterTurn(lead, leadSpec)
                }
            }
        }
    }

    fun useHealingTea() {
        val state = uiState.value
        if (state.save.healingTeas <= 0) {
            ephemeralState.update {
                it.copy(statusBannerText = "No Herbal Healing Teas left! Dowse steps on the Holo-Walker to find more.")
            }
            return
        }
        val lead = state.leadMonster ?: return
        viewModelScope.launch {
            repository.savePlayer(state.save.copy(healingTeas = state.save.healingTeas - 1))
            val healed = lead.copy(currentHp = lead.maxHp, amity = min(255, lead.amity + 6))
            repository.updateMonster(healed)

            val bState = state.battleState
            if (bState != null) {
                ephemeralState.update {
                    it.copy(
                        battleState = bState.copy(
                            activeVfx = CombatVfxEvent(
                                element = ElementAffinity.GALE,
                                moveName = "Herbal Tea",
                                isTargetingEnemy = false,
                                damageAmount = lead.maxHp - lead.currentHp,
                                isHeal = true
                            ),
                            battleLog = "Sip of Shrine Herbal Tea restored ${lead.nickname} to full HP!"
                        )
                    )
                }
                delay(500)
                ephemeralState.update { s -> s.copy(battleState = s.battleState?.copy(activeVfx = null)) }
            } else {
                ephemeralState.update {
                    it.copy(statusBannerText = "Shared Shrine Herbal Tea with ${lead.nickname}! HP fully restored & Amity rose.")
                }
            }
        }
    }

    fun makeMonsterWalkingLead(targetMonster: OwnedMonsterEntity) {
        val state = uiState.value
        val currentLead = state.partyMonsters.firstOrNull { it.partySlot == 0 }
        viewModelScope.launch {
            if (currentLead != null && currentLead.uid != targetMonster.uid) {
                repository.updateMonster(currentLead.copy(partySlot = targetMonster.partySlot ?: 1))
            }
            repository.updateMonster(targetMonster.copy(partySlot = 0))
            ephemeralState.update {
                it.copy(
                    statusBannerText = "${targetMonster.nickname} is now your Walking Partner in the overworld!",
                    followerReactionText = "${targetMonster.nickname} eagerly steps out to walk behind you!",
                    activeTab = if (state.battleState != null) BottomGadgetTab.CONTROLS else it.activeTab
                )
            }
        }
    }

    fun fleeFromBattle() {
        val state = uiState.value
        val battle = state.battleState ?: return
        if (battle.isTrainerBattle) {
            ephemeralState.update {
                it.copy(
                    battleState = battle.copy(
                        battleLog = "You cannot turn your back on an official Dojo Master challenge!"
                    )
                )
            }
            return
        }
        ephemeralState.update {
            it.copy(
                battleState = null,
                statusBannerText = "Retreated safely back to the trail with ${state.leadMonster?.nickname ?: "your partner"}."
            )
        }
    }

    fun resetChronicleSave() {
        viewModelScope.launch {
            repository.resetSaveForNewJourney()
            ephemeralState.update {
                EphemeralEngineState(
                    statusBannerText = "Chronicle reset. Choose your new Kouhaku partner spirit!"
                )
            }
        }
    }

    private fun grantExpToMonster(monster: OwnedMonsterEntity, gainedExp: Int): OwnedMonsterEntity {
        var totalExp = monster.exp + gainedExp
        var newLevel = monster.level
        var speciesId = monster.speciesId
        var nickname = monster.nickname

        var expThreshold = newLevel * 35
        while (totalExp >= expThreshold && newLevel < 50) {
            totalExp -= expThreshold
            newLevel += 1
            expThreshold = newLevel * 35
        }

        val currentSpecies = MonsterCatalog.getSpecies(speciesId)
        val evolveLv = currentSpecies.evolveLevel
        val evolveTarget = currentSpecies.evolvesToId
        if (evolveLv != null && evolveTarget != null && newLevel >= evolveLv) {
            val evolvedSpecies = MonsterCatalog.getSpecies(evolveTarget)
            if (nickname == currentSpecies.name) {
                nickname = evolvedSpecies.name
            }
            speciesId = evolvedSpecies.id
        }

        val finalSpecies = MonsterCatalog.getSpecies(speciesId)
        val newMaxHp = calculateMaxHp(finalSpecies.baseHp, newLevel)
        val hpDelta = max(0, newMaxHp - monster.maxHp)

        return monster.copy(
            speciesId = speciesId,
            nickname = nickname,
            level = newLevel,
            maxHp = newMaxHp,
            currentHp = min(newMaxHp, monster.currentHp + hpDelta),
            exp = totalExp
        )
    }

    companion object {
        fun calculateMaxHp(baseHp: Int, level: Int): Int {
            return ((baseHp * 2 * level) / 38) + level + 18
        }

        fun provideFactory(repository: KouhakuRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return GameEngineViewModel(repository) as T
                }
            }
    }
}
