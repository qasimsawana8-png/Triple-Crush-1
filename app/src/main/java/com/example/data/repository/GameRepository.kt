package com.example.data.repository

import com.example.data.db.LevelRecordEntity
import com.example.data.db.PlayerProgressDao
import com.example.data.db.PlayerProgressEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class GameRepository(private val dao: PlayerProgressDao) {

    val playerProgress: Flow<PlayerProgressEntity?> = dao.getPlayerProgress()
    val allLevelRecords: Flow<List<LevelRecordEntity>> = dao.getAllLevelRecords()

    suspend fun initializeIfEmpty() {
        val current = dao.getPlayerProgressDirect()
        if (current == null) {
            dao.insertOrUpdatePlayerProgress(
                PlayerProgressEntity(
                    id = 1,
                    highestLevelUnlocked = 1,
                    currentLevel = 1,
                    totalCoins = 200,
                    totalStars = 0,
                    boosterUndoCount = 3,
                    boosterShuffleCount = 3,
                    boosterHintCount = 3,
                    boosterVacuumCount = 2,
                    activeTileTheme = "fruits",
                    activeBackground = "sleek",
                    soundEnabled = true,
                    hapticsEnabled = true
                )
            )
        }
    }

    suspend fun completeLevel(levelNumber: Int, starsEarned: Int, score: Int, timeSeconds: Int) {
        val existingRecord = dao.getLevelRecord(levelNumber)
        val oldStars = existingRecord?.stars ?: 0
        val newStars = maxOf(oldStars, starsEarned)
        val newHighScore = maxOf(existingRecord?.highScore ?: 0, score)
        val newCompletedCount = (existingRecord?.completedCount ?: 0) + 1
        val bestTime = if (existingRecord?.bestTimeSeconds != null && existingRecord.bestTimeSeconds > 0) {
            minOf(existingRecord.bestTimeSeconds, timeSeconds)
        } else {
            timeSeconds
        }

        dao.insertOrUpdateLevelRecord(
            LevelRecordEntity(
                levelNumber = levelNumber,
                stars = newStars,
                highScore = newHighScore,
                completedCount = newCompletedCount,
                bestTimeSeconds = bestTime
            )
        )

        // Update player global progress
        val progress = dao.getPlayerProgressDirect() ?: PlayerProgressEntity()
        val starsDiff = maxOf(0, newStars - oldStars)
        val coinsEarned = 20 + (starsEarned * 10)
        val nextLevel = maxOf(progress.highestLevelUnlocked, levelNumber + 1)

        dao.insertOrUpdatePlayerProgress(
            progress.copy(
                highestLevelUnlocked = nextLevel,
                totalStars = progress.totalStars + starsDiff,
                totalCoins = progress.totalCoins + coinsEarned
            )
        )
    }

    suspend fun updateCurrentLevel(levelNumber: Int) {
        val progress = dao.getPlayerProgressDirect() ?: return
        dao.insertOrUpdatePlayerProgress(progress.copy(currentLevel = levelNumber))
    }

    suspend fun useBooster(boosterName: String): Boolean {
        val progress = dao.getPlayerProgressDirect() ?: return false
        return when (boosterName) {
            "Undo" -> {
                if (progress.boosterUndoCount > 0) {
                    dao.insertOrUpdatePlayerProgress(progress.copy(boosterUndoCount = progress.boosterUndoCount - 1))
                    true
                } else false
            }
            "Shuffle" -> {
                if (progress.boosterShuffleCount > 0) {
                    dao.insertOrUpdatePlayerProgress(progress.copy(boosterShuffleCount = progress.boosterShuffleCount - 1))
                    true
                } else false
            }
            "Magic Wand" -> {
                if (progress.boosterHintCount > 0) {
                    dao.insertOrUpdatePlayerProgress(progress.copy(boosterHintCount = progress.boosterHintCount - 1))
                    true
                } else false
            }
            "Vacuum" -> {
                if (progress.boosterVacuumCount > 0) {
                    dao.insertOrUpdatePlayerProgress(progress.copy(boosterVacuumCount = progress.boosterVacuumCount - 1))
                    true
                } else false
            }
            else -> false
        }
    }

    suspend fun buyBooster(boosterName: String, cost: Int = 60): Boolean {
        val progress = dao.getPlayerProgressDirect() ?: return false
        if (progress.totalCoins < cost) return false

        val updated = when (boosterName) {
            "Undo" -> progress.copy(totalCoins = progress.totalCoins - cost, boosterUndoCount = progress.boosterUndoCount + 1)
            "Shuffle" -> progress.copy(totalCoins = progress.totalCoins - cost, boosterShuffleCount = progress.boosterShuffleCount + 1)
            "Magic Wand" -> progress.copy(totalCoins = progress.totalCoins - cost, boosterHintCount = progress.boosterHintCount + 1)
            "Vacuum" -> progress.copy(totalCoins = progress.totalCoins - cost, boosterVacuumCount = progress.boosterVacuumCount + 1)
            else -> progress
        }
        dao.insertOrUpdatePlayerProgress(updated)
        return true
    }

    suspend fun setTileTheme(themeId: String) {
        dao.updateTileTheme(themeId)
    }

    suspend fun setBackground(bgId: String) {
        dao.updateBackground(bgId)
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        dao.updateSoundEnabled(enabled)
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        dao.updateHapticsEnabled(enabled)
    }

    suspend fun addRewardedCoins(amount: Int) {
        val progress = dao.getPlayerProgressDirect() ?: return
        dao.insertOrUpdatePlayerProgress(progress.copy(totalCoins = progress.totalCoins + amount))
    }

    suspend fun grantFreeBooster(boosterName: String) {
        val progress = dao.getPlayerProgressDirect() ?: return
        val updated = when (boosterName) {
            "Undo" -> progress.copy(boosterUndoCount = progress.boosterUndoCount + 1)
            "Shuffle" -> progress.copy(boosterShuffleCount = progress.boosterShuffleCount + 1)
            "Magic Wand" -> progress.copy(boosterHintCount = progress.boosterHintCount + 1)
            "Vacuum" -> progress.copy(boosterVacuumCount = progress.boosterVacuumCount + 1)
            else -> progress
        }
        dao.insertOrUpdatePlayerProgress(updated)
    }
}
