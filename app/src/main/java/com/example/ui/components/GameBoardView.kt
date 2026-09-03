package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.data.model.Tile

private data class BoardBounds(
    val minCol: Float,
    val maxCol: Float,
    val minRow: Float,
    val maxRow: Float
)

@Composable
fun GameBoardView(
    tiles: List<Tile>,
    currentLevel: Int,
    highlightedTileIds: Set<String>,
    onTileClick: (Tile) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("game_board"),
        contentAlignment = Alignment.Center
    ) {
        if (tiles.isEmpty()) return@BoxWithConstraints

        // Fixed bounds per level to keep layout completely stable during gameplay
        val bounds = remember(currentLevel) {
            BoardBounds(
                minCol = tiles.minOfOrNull { it.col } ?: 0.5f,
                maxCol = tiles.maxOfOrNull { it.col } ?: 5.5f,
                minRow = tiles.minOfOrNull { it.row } ?: 0.5f,
                maxRow = tiles.maxOfOrNull { it.row } ?: 5.5f
            )
        }

        val gridCols = (bounds.maxCol - bounds.minCol + 1.1f).coerceAtLeast(4.5f)
        val gridRows = (bounds.maxRow - bounds.minRow + 1.1f).coerceAtLeast(4.5f)

        val availWidth = maxWidth - 16.dp
        val availHeight = maxHeight - 16.dp

        val tileSizeByWidth = availWidth / gridCols
        val tileSizeByHeight = availHeight / gridRows
        val calculatedTileSize = minOf(tileSizeByWidth, tileSizeByHeight, 58.dp).coerceAtLeast(42.dp)

        val totalBoardWidth = calculatedTileSize * gridCols
        val totalBoardHeight = calculatedTileSize * gridRows

        Box(
            modifier = Modifier
                .size(totalBoardWidth, totalBoardHeight)
                .align(Alignment.Center)
        ) {
            // Sort tiles by layer ascending so higher layer renders on top
            val sortedTiles = tiles.sortedWith(
                compareBy<Tile> { it.layer }
                    .thenBy { it.row }
                    .thenBy { it.col }
            )

            for (tile in sortedTiles) {
                val targetOffsetX = (tile.col - bounds.minCol) * calculatedTileSize.value
                val targetOffsetY = (tile.row - bounds.minRow) * calculatedTileSize.value - (tile.layer * 3.5f)

                val animOffsetX by animateDpAsState(
                    targetValue = targetOffsetX.dp,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "tileX_${tile.id}"
                )
                val animOffsetY by animateDpAsState(
                    targetValue = targetOffsetY.dp,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "tileY_${tile.id}"
                )

                TileView(
                    tile = tile,
                    tileSize = calculatedTileSize,
                    isHighlighted = tile.id in highlightedTileIds,
                    onClick = { onTileClick(tile) },
                    modifier = Modifier
                        .zIndex(tile.layer * 100f + tile.row * 10f + tile.col)
                        .offset(x = animOffsetX, y = animOffsetY)
                )
            }
        }
    }
}
