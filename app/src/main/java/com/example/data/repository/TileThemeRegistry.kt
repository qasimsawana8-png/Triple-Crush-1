package com.example.data.repository

import com.example.data.model.TileCategory
import com.example.data.model.TileTheme
import com.example.data.model.TileType

object TileThemeRegistry {

    private val fruitTiles = listOf(
        TileType("f1", "Strawberry", "🍓", TileCategory.FRUIT, 0xFFFF1744, 0xFFFFEBEE),
        TileType("f2", "Watermelon", "🍉", TileCategory.FRUIT, 0xFF00E676, 0xFFE8F5E9),
        TileType("f3", "Banana", "🍌", TileCategory.FRUIT, 0xFFFFD600, 0xFFFFFDE7),
        TileType("f4", "Grapes", "🍇", TileCategory.FRUIT, 0xFFAA00FF, 0xFFF3E5F5),
        TileType("f5", "Avocado", "🥑", TileCategory.FRUIT, 0xFF64DD17, 0xFFF1F8E9),
        TileType("f6", "Red Apple", "🍎", TileCategory.FRUIT, 0xFFFF5252, 0xFFFFEBEE),
        TileType("f7", "Pineapple", "🍍", TileCategory.FRUIT, 0xFFFFAB00, 0xFFFFF8E1),
        TileType("f8", "Cherries", "🍒", TileCategory.FRUIT, 0xFFD50000, 0xFFFFEBEE),
        TileType("f9", "Lemon", "🍋", TileCategory.FRUIT, 0xFFFFEA00, 0xFFFFFDE7),
        TileType("f10", "Orange", "🍊", TileCategory.FRUIT, 0xFFFF6D00, 0xFFFFF3E0),
        TileType("f11", "Kiwi", "🥝", TileCategory.FRUIT, 0xFF76FF03, 0xFFF9FBE7),
        TileType("f12", "Peach", "🍑", TileCategory.FRUIT, 0xFFFF8A80, 0xFFFBE9E7)
    )

    private val dessertTiles = listOf(
        TileType("d1", "Shortcake", "🍰", TileCategory.DESSERT, 0xFFFF4081, 0xFFFCE4EC),
        TileType("d2", "Donut", "🍩", TileCategory.DESSERT, 0xFF8D6E63, 0xFFEFEBE9),
        TileType("d3", "Ice Cream", "🍦", TileCategory.DESSERT, 0xFF00E5FF, 0xFFE0F7FA),
        TileType("d4", "Cupcake", "🧁", TileCategory.DESSERT, 0xFFFF80AB, 0xFFFCE4EC),
        TileType("d5", "Birthday Cake", "🎂", TileCategory.DESSERT, 0xFFFFD700, 0xFFFFFDE7),
        TileType("d6", "Pancakes", "🥞", TileCategory.DESSERT, 0xFFFFB74D, 0xFFFFF3E0),
        TileType("d7", "Pudding", "🍮", TileCategory.DESSERT, 0xFFFFC107, 0xFFFFF8E1),
        TileType("d8", "Cookie", "🍪", TileCategory.DESSERT, 0xFF795548, 0xFFEFEBE9),
        TileType("d9", "Lollipop", "🍭", TileCategory.DESSERT, 0xFFE040FB, 0xFFF3E5F5),
        TileType("d10", "Chocolate", "🍫", TileCategory.DESSERT, 0xFF4E342E, 0xFFD7CCC8),
        TileType("d11", "Dango", "🍡", TileCategory.DESSERT, 0xFF81C784, 0xFFE8F5E9),
        TileType("d12", "Croissant", "🥐", TileCategory.DESSERT, 0xFFFFB300, 0xFFFFF8E1)
    )

    private val animalTiles = listOf(
        TileType("a1", "Puppy", "🐶", TileCategory.ANIMAL, 0xFFFF9800, 0xFFFFF3E0),
        TileType("a2", "Kitty", "🐱", TileCategory.ANIMAL, 0xFFFFB74D, 0xFFFFF8E1),
        TileType("a3", "Panda", "🐼", TileCategory.ANIMAL, 0xFF37474F, 0xFFECEFF1),
        TileType("a4", "Fox", "🦊", TileCategory.ANIMAL, 0xFFFF5722, 0xFFFBE9E7),
        TileType("a5", "Lion", "🦁", TileCategory.ANIMAL, 0xFFFFC107, 0xFFFFF8E1),
        TileType("a6", "Bunny", "🐰", TileCategory.ANIMAL, 0xFFF06292, 0xFFFCE4EC),
        TileType("a7", "Penguin", "🐧", TileCategory.ANIMAL, 0xFF0288D1, 0xFFE1F5FE),
        TileType("a8", "Koala", "🐨", TileCategory.ANIMAL, 0xFF78909C, 0xFFECEFF1),
        TileType("a9", "Frog", "🐸", TileCategory.ANIMAL, 0xFF4CAF50, 0xFFE8F5E9),
        TileType("a10", "Monkey", "🐵", TileCategory.ANIMAL, 0xFF8D6E63, 0xFFEFEBE9),
        TileType("a11", "Tiger", "🐯", TileCategory.ANIMAL, 0xFFFF6F00, 0xFFFFF3E0),
        TileType("a12", "Bear", "🐻", TileCategory.ANIMAL, 0xFF5D4037, 0xFFD7CCC8)
    )

    private val natureTiles = listOf(
        TileType("n1", "Sakura", "🌸", TileCategory.NATURE, 0xFFF48FB1, 0xFFFCE4EC),
        TileType("n2", "Sunflower", "🌻", TileCategory.NATURE, 0xFFFFD600, 0xFFFFFDE7),
        TileType("n3", "Clover", "🍀", TileCategory.NATURE, 0xFF00C853, 0xFFE8F5E9),
        TileType("n4", "Mushroom", "🍄", TileCategory.NATURE, 0xFFFF3D00, 0xFFFFEBEE),
        TileType("n5", "Maple", "🍁", TileCategory.NATURE, 0xFFFF6E40, 0xFFFBE9E7),
        TileType("n6", "Hibiscus", "🌺", TileCategory.NATURE, 0xFFE91E63, 0xFFFCE4EC),
        TileType("n7", "Cactus", "🌵", TileCategory.NATURE, 0xFF43A047, 0xFFE8F5E9),
        TileType("n8", "Rose", "🌹", TileCategory.NATURE, 0xFFC2185B, 0xFFFFEBEE),
        TileType("n9", "Bamboo", "🎋", TileCategory.NATURE, 0xFF2E7D32, 0xFFE8F5E9),
        TileType("n10", "Palm", "🌴", TileCategory.NATURE, 0xFF00897B, 0xFFE0F2F1),
        TileType("n11", "Tulip", "🌷", TileCategory.NATURE, 0xFFFF4081, 0xFFFCE4EC),
        TileType("n12", "Crystal", "🌿", TileCategory.NATURE, 0xFF66BB6A, 0xFFE8F5E9)
    )

    private val gemTiles = listOf(
        TileType("g1", "Diamond", "💎", TileCategory.GEM, 0xFF00B0FF, 0xFFE1F5FE),
        TileType("g2", "Crown", "👑", TileCategory.GEM, 0xFFFFD700, 0xFFFFFDE7),
        TileType("g3", "Crystal Ball", "🔮", TileCategory.GEM, 0xFF9C27B0, 0xFFF3E5F5),
        TileType("g4", "Ring", "💍", TileCategory.GEM, 0xFF00E5FF, 0xFFE0F7FA),
        TileType("g5", "Coin", "🪙", TileCategory.GEM, 0xFFFFAB00, 0xFFFFF8E1),
        TileType("g6", "Trophy", "🏆", TileCategory.GEM, 0xFFFFC400, 0xFFFFFDE7),
        TileType("g7", "Key", "🗝️", TileCategory.GEM, 0xFFFF9100, 0xFFFFF3E0),
        TileType("g8", "Thunder Gem", "⚡", TileCategory.GEM, 0xFFFFEA00, 0xFFFFFDE7),
        TileType("g9", "Heart Jewel", "💖", TileCategory.GEM, 0xFFFF1744, 0xFFFFEBEE),
        TileType("g10", "Star Ruby", "🌟", TileCategory.GEM, 0xFFFFD600, 0xFFFFFDE7),
        TileType("g11", "Magic Urn", "🏺", TileCategory.GEM, 0xFF8D6E63, 0xFFEFEBE9),
        TileType("g12", "Planet Gem", "🪐", TileCategory.GEM, 0xFF7C4DFF, 0xFFEDE7F6)
    )

    fun getTilesForTheme(themeId: String): List<TileType> {
        return when (themeId) {
            "desserts" -> dessertTiles
            "animals" -> animalTiles
            "nature" -> natureTiles
            "gems" -> gemTiles
            else -> fruitTiles
        }
    }

    fun getAllThemes(): List<TileTheme> = TileTheme.values().toList()
}
