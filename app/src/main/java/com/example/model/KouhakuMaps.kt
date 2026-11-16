package com.example.model

data class PortalLink(
    val targetMapId: String,
    val targetX: Int,
    val targetY: Int
)

data class RegionalMap(
    val id: String,
    val name: String,
    val subtitle: String,
    val width: Int = 11,
    val height: Int = 9,
    val rows: List<String>,
    val portals: Map<TileType, PortalLink>,
    val wildPoolDay: List<Int>,
    val wildPoolNight: List<Int>,
    val minWildLevel: Int,
    val maxWildLevel: Int
) {
    fun tileAt(x: Int, y: Int): TileType {
        if (y !in 0 until height || x !in 0 until width) return TileType.MAPLE_TREE
        val ch = rows.getOrNull(y)?.getOrNull(x) ?: 'T'
        return when (ch) {
            '.' -> TileType.GRASS_PATH
            'g' -> TileType.TALL_GRASS
            'T' -> TileType.MAPLE_TREE
            '~' -> TileType.WATER
            '=' -> TileType.BRIDGE
            'H' -> TileType.TORII_GATE
            'B' -> TileType.SHRINE_ALTAR_BRASS
            'S' -> TileType.SHRINE_ALTAR_SILVER
            'L' -> TileType.LANTERN_HEAL
            'K' -> TileType.DOJO_MASTER_BRASS
            'M' -> TileType.DOJO_MASTER_SILVER
            'R' -> TileType.GRANDMASTER_RYOMA
            '#' -> TileType.STONE_WALL
            'N' -> TileType.PORTAL_NORTH
            'V' -> TileType.PORTAL_SOUTH
            'E' -> TileType.PORTAL_EAST
            'W' -> TileType.PORTAL_WEST
            'A' -> TileType.ACORN_TREE
            else -> TileType.GRASS_PATH
        }
    }
}

object KouhakuMaps {
    val maps: Map<String, RegionalMap> = mapOf(
        "momiji_hamlet" to RegionalMap(
            id = "momiji_hamlet",
            name = "Momiji Hamlet",
            subtitle = "Autumn Lantern Village",
            rows = listOf(
                "TTTTTHNHTTT",
                "T..A..H...T",
                "T.###...L.T",
                "T.###.....T",
                "T.....ggg.T",
                "T.~~~=ggg.T",
                "T.~~~=....T",
                "T.........T",
                "TTTTTTTTTTT"
            ),
            portals = mapOf(
                TileType.PORTAL_NORTH to PortalLink("crimson_maple_route", 5, 7)
            ),
            wildPoolDay = listOf(1, 3, 5, 9),
            wildPoolNight = listOf(7, 1, 3, 10),
            minWildLevel = 4,
            maxWildLevel = 7
        ),
        "crimson_maple_route" to RegionalMap(
            id = "crimson_maple_route",
            name = "Crimson Maple Trail",
            subtitle = "Crossroads of Brass & Silver",
            rows = listOf(
                "TTTTTHNHTTT",
                "T.ggg...A.T",
                "T.ggg.H...T",
                "W.....L...E",
                "T...~~~...T",
                "T.gg~~~gg.T",
                "T.gg===gg.T",
                "T.........T",
                "TTTTTHVHTTT"
            ),
            portals = mapOf(
                TileType.PORTAL_SOUTH to PortalLink("momiji_hamlet", 5, 1),
                TileType.PORTAL_WEST to PortalLink("brass_chime_city", 9, 4),
                TileType.PORTAL_EAST to PortalLink("moonlit_silver_lake", 1, 4),
                TileType.PORTAL_NORTH to PortalLink("bellflower_sanctum", 5, 7)
            ),
            wildPoolDay = listOf(5, 9, 10, 12, 14),
            wildPoolNight = listOf(7, 10, 12, 16),
            minWildLevel = 7,
            maxWildLevel = 12
        ),
        "brass_chime_city" to RegionalMap(
            id = "brass_chime_city",
            name = "Brass Chime Sanctuary",
            subtitle = "Roost of the Sun-Brass Bell",
            rows = listOf(
                "#####HB####",
                "#..H...H..#",
                "#..#...#..#",
                "#..K...L..#",
                "#.........E",
                "#A.ggggg..#",
                "#..ggggg..#",
                "#.........#",
                "###########"
            ),
            portals = mapOf(
                TileType.PORTAL_EAST to PortalLink("crimson_maple_route", 1, 3)
            ),
            wildPoolDay = listOf(1, 12, 16, 9),
            wildPoolNight = listOf(7, 12, 13, 16),
            minWildLevel = 11,
            maxWildLevel = 16
        ),
        "moonlit_silver_lake" to RegionalMap(
            id = "moonlit_silver_lake",
            name = "Moonlit Silver Lake",
            subtitle = "Shrine of the Tidal Silver Bell",
            rows = listOf(
                "~~~~~HS~~~~",
                "~~~..=..~~~",
                "~~..~~~..~~",
                "~..M===L..~",
                "W.........~",
                "~..ggggg.A~",
                "~~.ggggg.~~",
                "~~~.....~~~",
                "~~~~~~~~~~~"
            ),
            portals = mapOf(
                TileType.PORTAL_WEST to PortalLink("crimson_maple_route", 9, 3)
            ),
            wildPoolDay = listOf(3, 14, 9, 10),
            wildPoolNight = listOf(14, 15, 7, 8),
            minWildLevel = 12,
            maxWildLevel = 18
        ),
        "bellflower_sanctum" to RegionalMap(
            id = "bellflower_sanctum",
            name = "Bellflower Spire Summit",
            subtitle = "Grandmaster's Twin-Bell Sanctum",
            rows = listOf(
                "#####HR####",
                "#..H...H..#",
                "#A.ggggg.A#",
                "#..ggggg..#",
                "#~~~===~~~#",
                "#..ggggg..#",
                "#..L...H..#",
                "#.........#",
                "#####HV####"
            ),
            portals = mapOf(
                TileType.PORTAL_SOUTH to PortalLink("crimson_maple_route", 5, 1)
            ),
            wildPoolDay = listOf(2, 4, 6, 11, 13, 16),
            wildPoolNight = listOf(8, 11, 13, 15, 16),
            minWildLevel = 16,
            maxWildLevel = 23
        )
    )

    fun getMap(mapId: String): RegionalMap =
        maps[mapId] ?: maps.getValue("momiji_hamlet")
}
