package com.example.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ads.AdMobManager
import com.example.data.model.GameStatus
import com.example.ui.components.AdBannerView
import com.example.ui.components.BoosterBar
import com.example.ui.components.ConfettiOverlay
import com.example.ui.components.GameBoardView
import com.example.ui.components.GameOverDialog
import com.example.ui.components.GameTopBar
import com.example.ui.components.LevelSelectSheet
import com.example.ui.components.SettingsDialog
import com.example.ui.components.ThemeShopSheet
import com.example.ui.components.TrayView
import com.example.ui.components.WinDialog
import com.example.ui.viewmodel.GameViewModel
import kotlinx.coroutines.launch

@Composable
fun TripleCrushGameScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playerProgress by viewModel.playerProgress.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showLevelSelect by remember { mutableStateOf(false) }
    var showThemeShop by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    val bg = uiState.activeBackground

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(bg.topGradientColor),
                        Color(bg.topGradientColor).copy(alpha = 0.88f),
                        Color(bg.bottomGradientColor)
                    )
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                GameTopBar(
                    uiState = uiState,
                    playerProgress = playerProgress,
                    onPauseClick = { viewModel.pauseGame() },
                    onLevelsClick = { showLevelSelect = true },
                    onThemesClick = { showThemeShop = true },
                    onSettingsClick = { showSettings = true },
                    modifier = Modifier.statusBarsPadding()
                )
            },
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TrayView(
                        trayTiles = uiState.trayTiles,
                        matchingTileIds = uiState.matchingTileIds,
                        maxCapacity = uiState.maxTrayCapacity,
                        isWarning = uiState.isTrayFullWarning,
                        trayColorHex = bg.trayColor
                    )

                    BoosterBar(
                        playerProgress = playerProgress,
                        onUndoClick = {
                            if (playerProgress?.hapticsEnabled == true) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            viewModel.useUndo()
                        },
                        onShuffleClick = {
                            if (playerProgress?.hapticsEnabled == true) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            viewModel.useShuffle()
                        },
                        onHintClick = {
                            if (playerProgress?.hapticsEnabled == true) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            viewModel.useHint()
                        },
                        onVacuumClick = {
                            if (playerProgress?.hapticsEnabled == true) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            viewModel.useVacuum()
                        },
                        onBuyBoosterClick = { boosterName ->
                            val coins = playerProgress?.totalCoins ?: 0
                            if (coins >= 60) {
                                viewModel.buyBooster(boosterName)
                                scope.launch {
                                    snackbarHostState.showSnackbar("Bought 1 $boosterName booster! (-60 🪙)")
                                }
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Not enough coins! Need 60 🪙 (You have $coins 🪙)")
                                }
                            }
                        }
                    )

                    // AdMob Banner Ad View
                    AdBannerView()

                    Spacer(modifier = Modifier.height(2.dp))
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                GameBoardView(
                    tiles = uiState.boardTiles,
                    currentLevel = uiState.currentLevel,
                    highlightedTileIds = uiState.highlightedTileIds,
                    onTileClick = { tile ->
                        if (playerProgress?.hapticsEnabled == true) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        viewModel.selectTile(tile)
                    }
                )
            }
        }

        // Particle & Confetti Layer
        ConfettiOverlay(particles = uiState.particles)

        // Win Dialog
        if (uiState.gameStatus == GameStatus.LEVEL_WON) {
            WinDialog(
                uiState = uiState,
                onNextLevel = {
                    if (activity != null) {
                        AdMobManager.instance.showInterstitialAd(activity) {
                            viewModel.nextLevel()
                        }
                    } else {
                        viewModel.nextLevel()
                    }
                },
                onRestart = {
                    if (activity != null) {
                        AdMobManager.instance.showInterstitialAd(activity) {
                            viewModel.restartCurrentLevel()
                        }
                    } else {
                        viewModel.restartCurrentLevel()
                    }
                },
                onLevelSelect = { showLevelSelect = true }
            )
        }

        // Game Over Dialog
        if (uiState.gameStatus == GameStatus.GAME_OVER) {
            GameOverDialog(
                vacuumCount = playerProgress?.boosterVacuumCount ?: 0,
                onWatchAdToRevive = {
                    if (activity != null) {
                        AdMobManager.instance.showRewardedAd(
                            activity = activity,
                            onUserEarnedReward = {
                                viewModel.reviveWithRewardedAd()
                                scope.launch {
                                    snackbarHostState.showSnackbar("Revived for free! 🎉")
                                }
                            },
                            onAdDismissedOrFailed = {}
                        )
                    } else {
                        viewModel.reviveWithRewardedAd()
                    }
                },
                onReviveWithVacuum = {
                    viewModel.reviveWithVacuum { errorMsg ->
                        scope.launch {
                            snackbarHostState.showSnackbar(errorMsg)
                        }
                    }
                },
                onRestart = {
                    if (activity != null) {
                        AdMobManager.instance.showInterstitialAd(activity) {
                            viewModel.restartCurrentLevel()
                        }
                    } else {
                        viewModel.restartCurrentLevel()
                    }
                },
                onLevelSelect = { showLevelSelect = true }
            )
        }

        // Level Select Bottom Sheet
        if (showLevelSelect) {
            LevelSelectSheet(
                levels = viewModel.getAllLevels(),
                currentLevel = uiState.currentLevel,
                onLevelSelected = { lvl ->
                    if (activity != null && lvl != uiState.currentLevel) {
                        AdMobManager.instance.showInterstitialAd(activity) {
                            viewModel.startLevel(lvl)
                        }
                    } else {
                        viewModel.startLevel(lvl)
                    }
                },
                onDismiss = { showLevelSelect = false }
            )
        }

        // Theme Shop Bottom Sheet
        if (showThemeShop) {
            ThemeShopSheet(
                activeTheme = uiState.activeTileTheme,
                activeBackground = uiState.activeBackground,
                totalCoins = playerProgress?.totalCoins ?: 0,
                onSelectTheme = { theme -> viewModel.setTileTheme(theme) },
                onSelectBackground = { bgTheme -> viewModel.setBackground(bgTheme) },
                onWatchAdForCoins = {
                    if (activity != null) {
                        AdMobManager.instance.showRewardedAd(
                            activity = activity,
                            onUserEarnedReward = { amount ->
                                val reward = if (amount > 0) amount else 50
                                viewModel.claimRewardedCoins(reward)
                                scope.launch {
                                    snackbarHostState.showSnackbar("You earned +$reward Coins! 🪙")
                                }
                            }
                        )
                    } else {
                        viewModel.claimRewardedCoins(50)
                    }
                },
                onDismiss = { showThemeShop = false }
            )
        }

        // Settings Dialog
        if (showSettings) {
            SettingsDialog(
                playerProgress = playerProgress,
                onToggleSound = { viewModel.toggleSound() },
                onToggleHaptics = { viewModel.toggleHaptics() },
                onRestartLevel = { viewModel.restartCurrentLevel() },
                onDismiss = { showSettings = false }
            )
        }
    }
}
