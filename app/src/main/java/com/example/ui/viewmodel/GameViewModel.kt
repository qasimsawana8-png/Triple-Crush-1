package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.GameAudioPlayer
import com.example.data.db.AppDatabase
import com.example.data.db.LevelRecordEntity
import com.example.data.db.PlayerProgressEntity
import com.example.data.model.BoardBackground
import com.example.data.model.GameStatus
import com.example.data.model.LevelInfo
import com.example.data.model.Particle
import com.example.data.model.Tile
import com.example.data.model.TileTheme
import com.example.data.repository.GameRepository
import com.example.data.repository.TileThemeRegistry
import com.example.logic.LevelGenerator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

data class GameUiState(
    val currentLevel: Int = 1,
    val gameStatus: GameStatus = GameStatus.PLAYING,
    val boardTiles: List<Tile> = emptyList(),
    val trayTiles: List<Tile> = emptyList(),
    val maxTrayCapacity: Int = 7,
    val score: Int = 0,
    val comboStreak: Int = 0,
    val comboMessage: String? = null,
    val starsEarned: Int = 0,
    val timeElapsedSeconds: Int = 0,
    val movesCount: Int = 0,
    val activeTileTheme: TileTheme = TileTheme.FRUITS,
    val activeBackground: BoardBackground = BoardBackground.SLEEK_LAVENDER,
    val particles: List<Particle> = emptyList(),
    val matchingTileIds: Set<String> = emptySet(),
    val isTrayFullWarning: Boolean = false,
    val highlightedTileIds: Set<String> = emptySet()
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GameRepository
    val audioPlayer: GameAudioPlayer = GameAudioPlayer()

    val playerProgress: StateFlow<PlayerProgressEntity?>
    val levelRecords: StateFlow<List<LevelRecordEntity>>

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val moveHistory = mutableListOf<Tile>()
    private var timerJob: Job? = null
    private var comboResetJob: Job? = null

    init {
        val db = AppDatabase.getDatabase(application)
        repository = GameRepository(db.playerProgressDao())

        playerProgress = repository.playerProgress.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null
        )

        levelRecords = repository.allLevelRecords.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        viewModelScope.launch {
            repository.initializeIfEmpty()
            playerProgress.collect { progress ->
                if (progress != null) {
                    audioPlayer.isSoundEnabled = progress.soundEnabled
                    val theme = TileTheme.values().find { it.id == progress.activeTileTheme } ?: TileTheme.FRUITS
                    val bg = BoardBackground.values().find { it.id == progress.activeBackground } ?: BoardBackground.SLEEK_LAVENDER
                    _uiState.value = _uiState.value.copy(
                        activeTileTheme = theme,
                        activeBackground = bg
                    )
                }
            }
        }

        startLevel(1)
    }

    fun startLevel(levelNumber: Int) {
        timerJob?.cancel()
        moveHistory.clear()

        val theme = _uiState.value.activeTileTheme
        val availableTypes = TileThemeRegistry.getTilesForTheme(theme.id)
        val newBoardTiles = LevelGenerator.generateLevel(levelNumber, availableTypes)

        _uiState.value = _uiState.value.copy(
            currentLevel = levelNumber,
            gameStatus = GameStatus.PLAYING,
            boardTiles = newBoardTiles,
            trayTiles = emptyList(),
            score = 0,
            comboStreak = 0,
            comboMessage = null,
            starsEarned = 0,
            timeElapsedSeconds = 0,
            movesCount = 0,
            particles = emptyList(),
            matchingTileIds = emptySet(),
            isTrayFullWarning = false,
            highlightedTileIds = emptySet()
        )

        viewModelScope.launch {
            repository.updateCurrentLevel(levelNumber)
        }

        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.gameStatus == GameStatus.PLAYING) {
                delay(1000)
                _uiState.value = _uiState.value.copy(
                    timeElapsedSeconds = _uiState.value.timeElapsedSeconds + 1
                )
            }
        }
    }

    fun selectTile(tile: Tile) {
        collectTileToTray(tile, bypassSelectable = false)
    }

    private fun collectTileToTray(tile: Tile, bypassSelectable: Boolean = false) {
        val currentState = _uiState.value
        if (currentState.gameStatus != GameStatus.PLAYING) return
        if (currentState.matchingTileIds.isNotEmpty()) return // Prevent taps while 3-match animation is resolving
        if (!bypassSelectable && !tile.isSelectable) return
        if (tile.inTray || tile.isMatched) return
        if (currentState.trayTiles.size >= currentState.maxTrayCapacity) {
            _uiState.value = currentState.copy(isTrayFullWarning = true)
            return
        }

        audioPlayer.playTileTap()

        // Move tile from board to tray
        val updatedBoard = currentState.boardTiles.filter { it.id != tile.id }
        val refreshedBoard = LevelGenerator.updateSelectableStates(updatedBoard)

        val tileInTray = tile.copy(
            inTray = true,
            isSelectable = false,
            animationOffsetKey = System.currentTimeMillis()
        )
        moveHistory.add(tile)

        // Insert tile into tray, grouping with matching types if possible
        val currentTray = currentState.trayTiles.toMutableList()
        val matchIndex = currentTray.indexOfLast { it.typeId == tile.typeId }
        if (matchIndex != -1) {
            currentTray.add(matchIndex + 1, tileInTray)
        } else {
            currentTray.add(tileInTray)
        }

        _uiState.value = currentState.copy(
            boardTiles = refreshedBoard,
            trayTiles = currentTray,
            movesCount = currentState.movesCount + 1,
            isTrayFullWarning = false,
            highlightedTileIds = emptySet()
        )

        audioPlayer.playTileSlide()

        // Check for 3-match in tray
        checkForMatch(currentTray, refreshedBoard)
    }

    private fun checkForMatch(currentTray: List<Tile>, currentBoard: List<Tile>) {
        val grouped = currentTray.groupBy { it.typeId }
        val matchedGroup = grouped.entries.firstOrNull { it.value.size >= 3 }

        if (matchedGroup != null) {
            val matchingTiles = matchedGroup.value.take(3)
            val matchIds = matchingTiles.map { it.id }.toSet()

            // Highlight & trigger sparkle match
            _uiState.value = _uiState.value.copy(
                matchingTileIds = matchIds
            )

            val newCombo = _uiState.value.comboStreak + 1
            val comboBonus = newCombo * 50
            val scoreGain = 100 + comboBonus

            audioPlayer.playMatch(newCombo)
            triggerMatchParticles(matchingTiles.first())

            val comboMsg = when {
                newCombo >= 4 -> "🔥 MEGA CRUSH x$newCombo!"
                newCombo >= 3 -> "⚡ SUPER MATCH x$newCombo!"
                newCombo >= 2 -> "✨ COMBO x$newCombo!"
                else -> "TRIPLE CRUSH!"
            }

            viewModelScope.launch {
                delay(260) // smooth visual pop before removing
                moveHistory.removeAll { it.id in matchIds }
                val remainingTray = _uiState.value.trayTiles.filter { it.id !in matchIds }

                _uiState.value = _uiState.value.copy(
                    trayTiles = remainingTray,
                    matchingTileIds = emptySet(),
                    score = _uiState.value.score + scoreGain,
                    comboStreak = newCombo,
                    comboMessage = comboMsg
                )

                resetComboTimer()

                // Check Win Condition
                if (_uiState.value.boardTiles.isEmpty() && remainingTray.isEmpty()) {
                    handleLevelWin()
                } else if (remainingTray.isNotEmpty()) {
                    // Check cascading match
                    checkForMatch(remainingTray, _uiState.value.boardTiles)
                }
            }
        } else {
            // Check Loss Condition (tray full with 7 tiles and no match)
            if (currentTray.size >= _uiState.value.maxTrayCapacity) {
                viewModelScope.launch {
                    delay(300)
                    if (_uiState.value.trayTiles.size >= _uiState.value.maxTrayCapacity &&
                        _uiState.value.matchingTileIds.isEmpty() &&
                        _uiState.value.gameStatus == GameStatus.PLAYING
                    ) {
                        _uiState.value = _uiState.value.copy(
                            gameStatus = GameStatus.GAME_OVER
                        )
                        audioPlayer.playGameOver()
                    }
                }
            }
        }
    }

    private fun resetComboTimer() {
        comboResetJob?.cancel()
        comboResetJob = viewModelScope.launch {
            delay(3500)
            _uiState.value = _uiState.value.copy(
                comboStreak = 0,
                comboMessage = null
            )
        }
    }

    private fun handleLevelWin() {
        timerJob?.cancel()
        audioPlayer.playWinFanfare()

        val time = _uiState.value.timeElapsedSeconds
        val moves = _uiState.value.movesCount
        val stars = when {
            time < 45 -> 3
            time < 90 -> 2
            else -> 1
        }

        _uiState.value = _uiState.value.copy(
            gameStatus = GameStatus.LEVEL_WON,
            starsEarned = stars
        )

        viewModelScope.launch {
            repository.completeLevel(
                levelNumber = _uiState.value.currentLevel,
                starsEarned = stars,
                score = _uiState.value.score,
                timeSeconds = time
            )
            triggerWinConfetti()
        }
    }

    fun useUndo() {
        val currentState = _uiState.value
        if (currentState.matchingTileIds.isNotEmpty()) return
        if (currentState.trayTiles.isEmpty()) return

        val tileToUndo = moveHistory.findLast { historyTile ->
            currentState.trayTiles.any { it.id == historyTile.id }
        } ?: currentState.trayTiles.lastOrNull() ?: return

        viewModelScope.launch {
            val success = repository.useBooster("Undo")
            if (success) {
                audioPlayer.playBoosterWhoosh()
                moveHistory.removeAll { it.id == tileToUndo.id }
                val currentTray = _uiState.value.trayTiles.filter { it.id != tileToUndo.id }
                val restoredBoard = _uiState.value.boardTiles + tileToUndo.copy(
                    inTray = false,
                    isMatched = false
                )
                val updatedBoard = LevelGenerator.updateSelectableStates(restoredBoard)
                val wasGameOver = _uiState.value.gameStatus == GameStatus.GAME_OVER
                _uiState.value = _uiState.value.copy(
                    boardTiles = updatedBoard,
                    trayTiles = currentTray,
                    gameStatus = if (wasGameOver) GameStatus.PLAYING else _uiState.value.gameStatus,
                    isTrayFullWarning = false
                )
                if (wasGameOver) {
                    startTimer()
                }
            }
        }
    }

    fun useShuffle() {
        val currentState = _uiState.value
        if (currentState.matchingTileIds.isNotEmpty()) return
        if (currentState.gameStatus != GameStatus.PLAYING) return
        val board = currentState.boardTiles
        if (board.isEmpty()) return

        viewModelScope.launch {
            val success = repository.useBooster("Shuffle")
            if (success) {
                audioPlayer.playBoosterWhoosh()
                val currentBoard = _uiState.value.boardTiles
                val types = currentBoard.map { it.type }.shuffled()
                val shuffledBoard = currentBoard.mapIndexed { index, tile ->
                    tile.copy(
                        type = types[index],
                        typeId = types[index].id,
                        animationOffsetKey = System.currentTimeMillis() + index
                    )
                }
                val updatedBoard = LevelGenerator.updateSelectableStates(shuffledBoard)
                _uiState.value = _uiState.value.copy(boardTiles = updatedBoard)
            }
        }
    }

    fun useHint() {
        val currentState = _uiState.value
        if (currentState.matchingTileIds.isNotEmpty()) return
        if (currentState.gameStatus != GameStatus.PLAYING) return
        val board = currentState.boardTiles
        val tray = currentState.trayTiles
        if (board.isEmpty()) return

        viewModelScope.launch {
            val trayCounts = tray.groupBy { it.typeId }
            var targetTypeId: String? = null
            var neededFromBoard = 0

            // Priority 1: 2 in tray, need 1 from board
            for ((typeId, inTray) in trayCounts) {
                val onBoardCount = board.count { it.typeId == typeId }
                if (inTray.size == 2 && onBoardCount >= 1 && (tray.size + 1) <= currentState.maxTrayCapacity) {
                    targetTypeId = typeId
                    neededFromBoard = 1
                    break
                }
            }

            // Priority 2: 1 in tray, need 2 from board
            if (targetTypeId == null) {
                for ((typeId, inTray) in trayCounts) {
                    val onBoardCount = board.count { it.typeId == typeId }
                    if (inTray.size == 1 && onBoardCount >= 2 && (tray.size + 2) <= currentState.maxTrayCapacity) {
                        targetTypeId = typeId
                        neededFromBoard = 2
                        break
                    }
                }
            }

            // Priority 3: 0 in tray, need 3 from board
            if (targetTypeId == null) {
                val candidate = board.groupBy { it.typeId }.entries.firstOrNull { it.value.size >= 3 }
                if (candidate != null && (tray.size + 3) <= currentState.maxTrayCapacity) {
                    targetTypeId = candidate.key
                    neededFromBoard = 3
                }
            }

            // Fallback: pick any available on board
            if (targetTypeId == null) {
                val candidate = board.groupBy { it.typeId }.entries.firstOrNull { it.value.isNotEmpty() }
                if (candidate != null && tray.size < currentState.maxTrayCapacity) {
                    targetTypeId = candidate.key
                    neededFromBoard = minOf(candidate.value.size, currentState.maxTrayCapacity - tray.size)
                }
            }

            if (targetTypeId == null || neededFromBoard <= 0) return@launch

            val success = repository.useBooster("Magic Wand")
            if (success) {
                audioPlayer.playBoosterWhoosh()
                val targetTiles = board.filter { it.typeId == targetTypeId }
                    .sortedByDescending { it.isSelectable }
                    .take(neededFromBoard)

                val targetIds = targetTiles.map { it.id }.toSet()
                _uiState.value = _uiState.value.copy(highlightedTileIds = targetIds)

                for (t in targetTiles) {
                    delay(140)
                    collectTileToTray(t, bypassSelectable = true)
                }
            }
        }
    }

    fun useVacuum() {
        val currentState = _uiState.value
        if (currentState.matchingTileIds.isNotEmpty()) return
        if (currentState.trayTiles.isEmpty()) return

        viewModelScope.launch {
            val success = repository.useBooster("Vacuum")
            if (success) {
                audioPlayer.playBoosterWhoosh()
                // Take up to 3 tiles from tray and return them safely to board
                val currentTray = _uiState.value.trayTiles
                val countToReturn = minOf(3, currentTray.size)
                val tilesToReturn = currentTray.takeLast(countToReturn)
                val remainingTray = currentTray.dropLast(countToReturn)

                val maxLayer = _uiState.value.boardTiles.maxOfOrNull { it.layer } ?: 0
                val returnedTiles = tilesToReturn.mapIndexed { idx, tile ->
                    tile.copy(
                        inTray = false,
                        isMatched = false,
                        layer = maxLayer + 1,
                        row = 5.2f,
                        col = 1.5f + idx * 1.2f,
                        isSelectable = true
                    )
                }
                val updatedBoard = LevelGenerator.updateSelectableStates(_uiState.value.boardTiles + returnedTiles)
                val wasGameOver = _uiState.value.gameStatus == GameStatus.GAME_OVER
                _uiState.value = _uiState.value.copy(
                    boardTiles = updatedBoard,
                    trayTiles = remainingTray,
                    gameStatus = GameStatus.PLAYING,
                    isTrayFullWarning = false
                )
                if (wasGameOver) {
                    startTimer()
                }
            }
        }
    }

    fun reviveWithVacuum(onFailure: (String) -> Unit = {}) {
        val currentState = _uiState.value
        if (currentState.trayTiles.isEmpty()) return

        viewModelScope.launch {
            val count = playerProgress.value?.boosterVacuumCount ?: 0
            if (count > 0) {
                useVacuum()
            } else {
                val coins = playerProgress.value?.totalCoins ?: 0
                if (coins >= 60) {
                    val bought = repository.buyBooster("Vacuum", 60)
                    if (bought) {
                        audioPlayer.playCoinEarn()
                        useVacuum()
                    }
                } else {
                    onFailure("Not enough coins to revive! (Need 60 🪙)")
                }
            }
        }
    }

    fun buyBooster(boosterName: String) {
        viewModelScope.launch {
            val bought = repository.buyBooster(boosterName, 60)
            if (bought) {
                audioPlayer.playCoinEarn()
            }
        }
    }

    fun claimRewardedCoins(amount: Int = 50) {
        viewModelScope.launch {
            repository.addRewardedCoins(amount)
            audioPlayer.playCoinEarn()
        }
    }

    fun claimRewardedBooster(boosterName: String) {
        viewModelScope.launch {
            repository.grantFreeBooster(boosterName)
            audioPlayer.playCoinEarn()
        }
    }

    fun reviveWithRewardedAd() {
        val currentState = _uiState.value
        if (currentState.trayTiles.isEmpty()) return

        audioPlayer.playBoosterWhoosh()
        // Return 3 tiles from tray back to board for free!
        val currentTray = currentState.trayTiles
        val countToReturn = minOf(3, currentTray.size)
        val tilesToReturn = currentTray.takeLast(countToReturn)
        val remainingTray = currentTray.dropLast(countToReturn)

        val maxLayer = currentState.boardTiles.maxOfOrNull { it.layer } ?: 0
        val returnedTiles = tilesToReturn.mapIndexed { idx, tile ->
            tile.copy(
                inTray = false,
                isMatched = false,
                layer = maxLayer + 1,
                row = 5.2f,
                col = 1.5f + idx * 1.2f,
                isSelectable = true
            )
        }
        val updatedBoard = LevelGenerator.updateSelectableStates(_uiState.value.boardTiles + returnedTiles)
        _uiState.value = _uiState.value.copy(
            boardTiles = updatedBoard,
            trayTiles = remainingTray,
            gameStatus = GameStatus.PLAYING,
            isTrayFullWarning = false
        )
        startTimer()
    }

    fun setTileTheme(theme: TileTheme) {
        viewModelScope.launch {
            repository.setTileTheme(theme.id)
            _uiState.value = _uiState.value.copy(activeTileTheme = theme)
            // Restart current level with new theme
            startLevel(_uiState.value.currentLevel)
        }
    }

    fun setBackground(bg: BoardBackground) {
        viewModelScope.launch {
            repository.setBackground(bg.id)
            _uiState.value = _uiState.value.copy(activeBackground = bg)
        }
    }

    fun toggleSound() {
        val current = playerProgress.value?.soundEnabled ?: true
        viewModelScope.launch {
            repository.setSoundEnabled(!current)
        }
    }

    fun toggleHaptics() {
        val current = playerProgress.value?.hapticsEnabled ?: true
        viewModelScope.launch {
            repository.setHapticsEnabled(!current)
        }
    }

    fun restartCurrentLevel() {
        startLevel(_uiState.value.currentLevel)
    }

    fun nextLevel() {
        startLevel(_uiState.value.currentLevel + 1)
    }

    fun pauseGame() {
        if (_uiState.value.gameStatus == GameStatus.PLAYING) {
            _uiState.value = _uiState.value.copy(gameStatus = GameStatus.PAUSED)
            timerJob?.cancel()
        }
    }

    fun resumeGame() {
        if (_uiState.value.gameStatus == GameStatus.PAUSED) {
            _uiState.value = _uiState.value.copy(gameStatus = GameStatus.PLAYING)
            startTimer()
        }
    }

    private fun triggerMatchParticles(tile: Tile) {
        val newParticles = (0..16).map { i ->
            val angle = (i.toFloat() / 16f) * 2f * Math.PI.toFloat()
            val speed = Random.nextFloat() * 180f + 60f
            Particle(
                id = System.currentTimeMillis() + i,
                startX = 0.5f,
                startY = 0.85f,
                vx = (kotlin.math.cos(angle.toDouble()) * speed).toFloat(),
                vy = (kotlin.math.sin(angle.toDouble()) * speed).toFloat(),
                color = tile.type.primaryColor,
                size = Random.nextFloat() * 12f + 8f,
                emoji = if (Random.nextBoolean()) tile.type.symbol else "✨"
            )
        }
        _uiState.value = _uiState.value.copy(
            particles = _uiState.value.particles + newParticles
        )
        viewModelScope.launch {
            delay(1200)
            _uiState.value = _uiState.value.copy(
                particles = emptyList()
            )
        }
    }

    private fun triggerWinConfetti() {
        val confettiColors = listOf(0xFFFFD700, 0xFFFF4081, 0xFF00E676, 0xFF00B0FF, 0xFFFF9100)
        val confetti = (0..35).map { i ->
            Particle(
                id = System.currentTimeMillis() + i,
                startX = Random.nextFloat(),
                startY = -0.1f,
                vx = (Random.nextFloat() - 0.5f) * 120f,
                vy = Random.nextFloat() * 200f + 100f,
                color = confettiColors.random(),
                size = Random.nextFloat() * 14f + 8f,
                emoji = listOf("⭐", "🎉", "🌟", "👑", "💎").random()
            )
        }
        _uiState.value = _uiState.value.copy(
            particles = confetti
        )
    }

    fun getAllLevels(): List<LevelInfo> {
        val progress = playerProgress.value
        val records = levelRecords.value.associateBy { it.levelNumber }
        val highestUnlocked = progress?.highestLevelUnlocked ?: 1

        return (1..40).map { lvl ->
            val record = records[lvl]
            LevelInfo(
                levelNumber = lvl,
                targetTilesCount = (18 + (lvl % 10) * 3),
                layerCount = (2 + (lvl % 10) / 3),
                starsEarned = record?.stars ?: 0,
                isUnlocked = lvl <= highestUnlocked,
                bestScore = record?.highScore ?: 0
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        comboResetJob?.cancel()
    }
}
