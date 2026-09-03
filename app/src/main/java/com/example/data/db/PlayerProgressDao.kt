package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerProgressDao {

    @Query("SELECT * FROM player_progress WHERE id = 1")
    fun getPlayerProgress(): Flow<PlayerProgressEntity?>

    @Query("SELECT * FROM player_progress WHERE id = 1")
    suspend fun getPlayerProgressDirect(): PlayerProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePlayerProgress(progress: PlayerProgressEntity)

    @Query("SELECT * FROM level_records ORDER BY levelNumber ASC")
    fun getAllLevelRecords(): Flow<List<LevelRecordEntity>>

    @Query("SELECT * FROM level_records WHERE levelNumber = :levelNumber")
    suspend fun getLevelRecord(levelNumber: Int): LevelRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLevelRecord(record: LevelRecordEntity)

    @Query("UPDATE player_progress SET totalCoins = totalCoins + :amount WHERE id = 1")
    suspend fun addCoins(amount: Int)

    @Query("UPDATE player_progress SET activeTileTheme = :themeId WHERE id = 1")
    suspend fun updateTileTheme(themeId: String)

    @Query("UPDATE player_progress SET activeBackground = :bgId WHERE id = 1")
    suspend fun updateBackground(bgId: String)

    @Query("UPDATE player_progress SET soundEnabled = :enabled WHERE id = 1")
    suspend fun updateSoundEnabled(enabled: Boolean)

    @Query("UPDATE player_progress SET hapticsEnabled = :enabled WHERE id = 1")
    suspend fun updateHapticsEnabled(enabled: Boolean)
}
