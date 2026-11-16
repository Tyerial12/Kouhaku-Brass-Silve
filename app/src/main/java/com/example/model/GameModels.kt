package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.ElementBrassColor
import com.example.ui.theme.ElementEarthColor
import com.example.ui.theme.ElementFlameColor
import com.example.ui.theme.ElementGaleColor
import com.example.ui.theme.ElementSpiritColor
import com.example.ui.theme.ElementTideColor

enum class ElementAffinity(val displayName: String, val color: Color) {
    FLAME("Flame", ElementFlameColor),
    TIDE("Tide", ElementTideColor),
    GALE("Gale", ElementGaleColor),
    EARTH("Earth", ElementEarthColor),
    BRASS("Brass", ElementBrassColor),
    SPIRIT("Spirit", ElementSpiritColor);

    fun effectivenessAgainst(defender: ElementAffinity): Float {
        return when (this) {
            FLAME -> when (defender) {
                GALE, BRASS -> 1.6f
                TIDE, EARTH -> 0.65f
                else -> 1.0f
            }
            TIDE -> when (defender) {
                FLAME, EARTH -> 1.6f
                GALE, SPIRIT -> 0.65f
                else -> 1.0f
            }
            GALE -> when (defender) {
                TIDE, EARTH -> 1.6f
                FLAME, BRASS -> 0.65f
                else -> 1.0f
            }
            EARTH -> when (defender) {
                FLAME, BRASS -> 1.6f
                GALE, TIDE -> 0.65f
                else -> 1.0f
            }
            BRASS -> when (defender) {
                SPIRIT, GALE -> 1.6f
                FLAME, EARTH -> 0.65f
                else -> 1.0f
            }
            SPIRIT -> when (defender) {
                TIDE, SPIRIT -> 1.6f
                BRASS -> 0.65f
                else -> 1.0f
            }
        }
    }
}

enum class TimeOfDay(
    val label: String,
    val japaneseSub: String,
    val overlayColor: Color,
    val skyTint: Color,
    val encounterRateBonus: Float
) {
    DAWN("Dawn Mist", "Akatsuki", Color(0x22FFB07C), Color(0xFF47374D), 1.0f),
    DAY("Autumn Sun", "Hiru", Color(0x00000000), Color(0xFF3E6B48), 1.0f),
    TWILIGHT("Crimson Dusk", "Tasogare", Color(0x38D94E34), Color(0xFF592A28), 1.15f),
    NIGHT("Silver Moon", "Yoru", Color(0x52141E38), Color(0xFF192236), 1.25f);

    fun next(): TimeOfDay = entries[(ordinal + 1) % entries.size]
}

enum class Direction(val dx: Int, val dy: Int, val label: String) {
    UP(0, -1, "North"),
    DOWN(0, 1, "South"),
    LEFT(-1, 0, "West"),
    RIGHT(1, 0, "East")
}

enum class TileType(val walkable: Boolean, val description: String) {
    GRASS_PATH(true, "Cobblestone & packed earth path"),
    TALL_GRASS(true, "Rustling autumn silver-grass where wild spirits hide"),
    MAPLE_TREE(false, "Ancient crimson momiji maple tree"),
    WATER(false, "Deep mirror-like shrine pond"),
    BRIDGE(true, "Vermilion lacquered wooden bridge"),
    TORII_GATE(true, "Sacred vermilion Torii archway"),
    SHRINE_ALTAR_BRASS(true, "Sun-Brass Roost Altar — resonates with the Clear Brass Bell"),
    SHRINE_ALTAR_SILVER(true, "Moon-Silver Whirlpool Altar — resonates with the Tidal Silver Bell"),
    LANTERN_HEAL(true, "Sacred Stone Lantern — fully restores party HP & Spirit"),
    DOJO_MASTER_BRASS(true, "Dojo Master Kagerou — Keeper of the Clear Brass Bell"),
    DOJO_MASTER_SILVER(true, "Dojo Master Suiren — Keeper of the Tidal Silver Bell"),
    GRANDMASTER_RYOMA(true, "Elder Grandmaster Ryoma — Champion of Bellflower Sanctum"),
    STONE_WALL(false, "Carved temple courtyard wall"),
    PORTAL_NORTH(true, "Path leading North"),
    PORTAL_SOUTH(true, "Path leading South"),
    PORTAL_EAST(true, "Path leading East"),
    PORTAL_WEST(true, "Path leading West"),
    ACORN_TREE(true, "Wild Spirit-Acorn tree — shake once per visit for crafting acorns")
}

enum class CaptureOrbType(
    val displayName: String,
    val acornRecipeName: String,
    val baseMultiplier: Float,
    val description: String,
    val primaryColor: Color
) {
    SPIRIT_ORB(
        "Spirit Orb",
        "Standard Shrine Vessel",
        1.0f,
        "Balancedlacquered vessel for taming wild Kouhaku spirits.",
        Color(0xFFD94E34)
    ),
    EMBER_ORB(
        "Crimson Ember Orb",
        "Crafted from Crimson Acorn",
        1.8f,
        "2.2x capture rate against Flame & Gale spirits, or during Twilight.",
        Color(0xFFF3702B)
    ),
    DUSK_ORB(
        "Moonlit Dusk Orb",
        "Crafted from Dusk Acorn",
        1.8f,
        "2.5x capture rate during Night or against Spirit & Tide beasts.",
        Color(0xFF6B52D9)
    ),
    HEAVY_BRASS_ORB(
        "Heavy Brass Orb",
        "Crafted from Brass Acorn",
        2.0f,
        "2.4x capture rate against Brass, Earth, and Guardian spirits.",
        Color(0xFFD49B35)
    )
}

data class MoveTechnique(
    val id: Int,
    val name: String,
    val element: ElementAffinity,
    val power: Int,
    val healFraction: Float = 0f,
    val description: String
)

data class MonsterSpecies(
    val id: Int,
    val name: String,
    val title: String,
    val primaryElement: ElementAffinity,
    val secondaryElement: ElementAffinity? = null,
    val baseHp: Int,
    val baseAtk: Int,
    val baseDef: Int,
    val baseSpd: Int,
    val catchRate: Int, // 30 (legendary) to 220 (common)
    val evolvesToId: Int? = null,
    val evolveLevel: Int? = null,
    val preferredTime: TimeOfDay? = null,
    val isGuardianLegend: Boolean = false,
    val lore: String,
    val moveIds: List<Int>,
    val primaryColor: Color,
    val secondaryColor: Color,
    val accentColor: Color,
    val pixelRows: List<String> // 12x12 pixel grid ('0'=transparent, '1'=primary, '2'=secondary, '3'=accent, '4'=eye/dark)
)

enum class BottomGadgetTab {
    CONTROLS,
    SPIRIT_WALKER,
    SHRINE_BELLS,
    PARTY_COMPENDIUM
}
