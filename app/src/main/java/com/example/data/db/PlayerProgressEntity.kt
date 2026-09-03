package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_progress")
data class PlayerProgressEntity(
    @PrimaryKey val id: Int = 1, // Single record for player global state
    val highestLevelUnlocked: Int = 1,
    val currentLevel: Int = 1,
    val totalCoins: Int = 150,
    val totalStars: Int = 0,
    val boosterUndoCount: Int = 3,
    val boosterShuffleCount: Int = 3,
    val boosterHintCount: Int = 3,
    val boosterVacuumCount: Int = 2,
    val activeTileTheme: String = "fruits",
    val activeBackground: String = "sleek",
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true
)

@Entity(tableName = "level_records")
data class LevelRecordEntity(
    @PrimaryKey val levelNumber: Int,
    val stars: Int = 0, // 0..3
    val highScore: Int = 0,
    val completedCount: Int = 0,
    val bestTimeSeconds: Int = 0
)
