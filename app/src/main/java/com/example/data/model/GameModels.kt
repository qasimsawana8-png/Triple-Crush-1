package com.example.data.model

enum class TileCategory(val title: String) {
    FRUIT("Juicy Fruits"),
    DESSERT("Sweet Desserts"),
    ANIMAL("Cute Animals"),
    NATURE("Zen Nature"),
    GEM("Royal Gems")
}

data class TileType(
    val id: String,
    val name: String,
    val symbol: String, // Emoji or high-contrast character
    val category: TileCategory,
    val primaryColor: Long, // 0xFF...
    val secondaryColor: Long
)

data class Tile(
    val id: String,
    val typeId: String,
    val type: TileType,
    val layer: Int, // 0 is bottom, higher is on top
    val col: Float, // horizontal grid coordinate (e.g. 0.0, 0.5, 1.0, etc.)
    val row: Float, // vertical grid coordinate
    val isSelectable: Boolean = true,
    val isMatched: Boolean = false,
    val inTray: Boolean = false,
    val animationOffsetKey: Long = System.currentTimeMillis()
)

enum class BoosterType(val title: String, val description: String, val iconName: String) {
    UNDO("Undo", "Return last picked tile to board", "Undo"),
    SHUFFLE("Shuffle", "Rearrange all tiles on board", "Shuffle"),
    HINT("Magic Wand", "Auto-match 3 identical tiles", "AutoAwesome"),
    VACUUM("Vacuum", "Clear 3 tiles from tray to save game", "DeleteSweep")
}

enum class TileTheme(val id: String, val title: String, val previewEmoji: String, val category: TileCategory) {
    FRUITS("fruits", "Juicy Fruits", "🍓🍉🍌", TileCategory.FRUIT),
    DESSERTS("desserts", "Sweet Treats", "🍰🍩🍦", TileCategory.DESSERT),
    ANIMALS("animals", "Cute Pets", "🐶🐱🐼", TileCategory.ANIMAL),
    NATURE("nature", "Zen Garden", "🌸🌻🍀", TileCategory.NATURE),
    GEMS("gems", "Royal Jewels", "💎👑🔮", TileCategory.GEM)
}

enum class BoardBackground(
    val id: String,
    val title: String,
    val topGradientColor: Long,
    val bottomGradientColor: Long,
    val trayColor: Long,
    val accentColor: Long
) {
    SLEEK_LAVENDER(
        "sleek",
        "Sleek Lavender",
        0xFFFEF7FF,
        0xFFF3EDF7,
        0xFFF3EDF7,
        0xFF6750A4
    ),
    SLEEK_PURPLE(
        "purple",
        "Royal Violet",
        0xFF2A1F45,
        0xFF161028,
        0xFF3B2F5C,
        0xFFD0BCFF
    ),
    ZEN_GARDEN(
        "zen",
        "Sleek Mint",
        0xFFE8F5E9,
        0xFFC8E6C9,
        0xFFDCEDC8,
        0xFF2E7D32
    ),
    DEEP_OCEAN(
        "ocean",
        "Sleek Cyan",
        0xFFE0F7FA,
        0xFFB2EBF2,
        0xFFB2DFDB,
        0xFF00838F
    ),
    WARM_SUNSET(
        "sunset",
        "Sleek Peach",
        0xFFFFF3E0,
        0xFFFFE0B2,
        0xFFFFCCBC,
        0xFFE65100
    )
}

data class LevelInfo(
    val levelNumber: Int,
    val targetTilesCount: Int,
    val layerCount: Int,
    val starsEarned: Int = 0,
    val isUnlocked: Boolean = false,
    val bestScore: Int = 0
)

enum class GameStatus {
    PLAYING,
    PAUSED,
    LEVEL_WON,
    GAME_OVER
}

data class Particle(
    val id: Long,
    val startX: Float,
    val startY: Float,
    val vx: Float,
    val vy: Float,
    val color: Long,
    val size: Float,
    val alpha: Float = 1f,
    val rotation: Float = 0f,
    val emoji: String? = null
)
