package com.example.logic

import com.example.data.model.Tile
import com.example.data.model.TileType
import kotlin.math.abs
import kotlin.random.Random

data class TilePosition(
    val col: Float,
    val row: Float,
    val layer: Int
)

object LevelGenerator {

    /**
     * Checks if tile at index `targetIdx` is blocked by any other remaining tile in `tiles`.
     */
    fun isTileBlocked(tile: Tile, remainingTiles: List<Tile>): Boolean {
        if (tile.inTray || tile.isMatched) return false
        for (other in remainingTiles) {
            if (other.id == tile.id || other.inTray || other.isMatched) continue
            if (other.layer > tile.layer) {
                val dx = abs(other.col - tile.col)
                val dy = abs(other.row - tile.row)
                // If the top tile overlaps substantially (bounding box 0.88 grid units)
                if (dx < 0.88f && dy < 0.88f) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * Recalculates selectable status for all remaining tiles on board.
     */
    fun updateSelectableStates(tiles: List<Tile>): List<Tile> {
        val boardTiles = tiles.filter { !it.inTray && !it.isMatched }
        return tiles.map { tile ->
            if (tile.inTray || tile.isMatched) {
                tile
            } else {
                val blocked = isTileBlocked(tile, boardTiles)
                tile.copy(isSelectable = !blocked)
            }
        }
    }

    fun generateLevel(levelNumber: Int, availableTileTypes: List<TileType>): List<Tile> {
        val positions = getPositionsForLevel(levelNumber)
        val totalCount = positions.size
        // Ensure total count is divisible by 3
        val adjustedCount = (totalCount / 3) * 3
        val finalPositions = positions.take(adjustedCount).shuffled(Random(levelNumber * 31L))

        val numTriplets = adjustedCount / 3
        // Distinct tile types for this level based on level difficulty
        val numDistinctTypes = (4 + (levelNumber / 3)).coerceAtMost(availableTileTypes.size).coerceAtLeast(3)
        val selectedTypes = availableTileTypes.shuffled(Random(levelNumber * 17L)).take(numDistinctTypes)

        // Distribute triplets evenly among selected types
        val assignedTypes = mutableListOf<TileType>()
        for (i in 0 until numTriplets) {
            val type = selectedTypes[i % selectedTypes.size]
            assignedTypes.add(type)
            assignedTypes.add(type)
            assignedTypes.add(type)
        }
        assignedTypes.shuffle(Random(levelNumber * 43L))

        val rawTiles = mutableListOf<Tile>()
        for (i in 0 until adjustedCount) {
            val pos = finalPositions[i]
            val type = assignedTypes[i]
            val tile = Tile(
                id = "tile_${levelNumber}_${i}_${pos.layer}_${pos.col}_${pos.row}",
                typeId = type.id,
                type = type,
                layer = pos.layer,
                col = pos.col,
                row = pos.row,
                isSelectable = true,
                isMatched = false,
                inTray = false
            )
            rawTiles.add(tile)
        }

        return updateSelectableStates(rawTiles)
    }

    private fun getPositionsForLevel(level: Int): List<TilePosition> {
        val positions = mutableListOf<TilePosition>()
        when (level % 10) {
            1 -> {
                // Level 1: Gentle Starter (18 tiles, 2 layers, centered)
                // Layer 0: 3x4 grid
                for (r in 1..3) {
                    for (c in 1..4) {
                        positions.add(TilePosition(c.toFloat(), r.toFloat(), 0))
                    }
                }
                // Layer 1: 2x3 centered
                for (r in 1..2) {
                    for (c in 2..3) {
                        positions.add(TilePosition(c + 0.5f, r + 0.5f, 1))
                    }
                }
                // Plus two on top
                positions.add(TilePosition(2.5f, 1.5f, 2))
                positions.add(TilePosition(3.5f, 1.5f, 2))
            }
            2 -> {
                // Level 2: Diamond Stack (24 tiles, 3 layers)
                // Layer 0: Diamond shape
                val diamondCoords = listOf(
                    Pair(3f, 0.5f),
                    Pair(2f, 1.5f), Pair(3f, 1.5f), Pair(4f, 1.5f),
                    Pair(1f, 2.5f), Pair(2f, 2.5f), Pair(3f, 2.5f), Pair(4f, 2.5f), Pair(5f, 2.5f),
                    Pair(2f, 3.5f), Pair(3f, 3.5f), Pair(4f, 3.5f),
                    Pair(3f, 4.5f)
                )
                diamondCoords.forEach { positions.add(TilePosition(it.first, it.second, 0)) }
                // Layer 1: Inner diamond
                val innerDiamond = listOf(
                    Pair(3f, 1.5f),
                    Pair(2.5f, 2.5f), Pair(3.5f, 2.5f),
                    Pair(3f, 3.5f),
                    Pair(2f, 2.5f), Pair(4f, 2.5f),
                    Pair(3f, 2.5f)
                )
                innerDiamond.forEach { positions.add(TilePosition(it.first, it.second, 1)) }
                // Layer 2: Center peak
                positions.add(TilePosition(3f, 2.5f, 2))
                positions.add(TilePosition(2.5f, 2f, 2))
                positions.add(TilePosition(3.5f, 2f, 2))
                positions.add(TilePosition(3f, 3f, 2))
            }
            3 -> {
                // Level 3: Cross & Fortress (27 tiles, 3 layers)
                // Layer 0: Cross
                for (c in 1..5) {
                    positions.add(TilePosition(c.toFloat(), 2.5f, 0))
                    positions.add(TilePosition(c.toFloat(), 3.5f, 0))
                }
                for (r in 1..5) {
                    positions.add(TilePosition(3.0f, r.toFloat(), 0))
                }
                // Corners
                positions.add(TilePosition(1.5f, 1.5f, 0))
                positions.add(TilePosition(4.5f, 1.5f, 0))
                positions.add(TilePosition(1.5f, 4.5f, 0))
                positions.add(TilePosition(4.5f, 4.5f, 0))

                // Layer 1
                positions.add(TilePosition(2.5f, 2.5f, 1))
                positions.add(TilePosition(3.5f, 2.5f, 1))
                positions.add(TilePosition(2.5f, 3.5f, 1))
                positions.add(TilePosition(3.5f, 3.5f, 1))

                // Layer 2
                positions.add(TilePosition(3.0f, 3.0f, 2))
                positions.add(TilePosition(2.5f, 3.0f, 2))
                positions.add(TilePosition(3.5f, 3.0f, 2))
            }
            4 -> {
                // Level 4: Pyramid Mountain (30 tiles, 4 layers)
                // Layer 0: 4x4 base
                for (r in 1..4) {
                    for (c in 1..4) {
                        positions.add(TilePosition(c.toFloat(), r.toFloat(), 0))
                    }
                }
                // Layer 1: 3x3
                for (r in 1..3) {
                    for (c in 1..3) {
                        positions.add(TilePosition(c + 0.5f, r + 0.5f, 1))
                    }
                }
                // Layer 2: 2x2
                for (r in 1..2) {
                    for (c in 1..2) {
                        positions.add(TilePosition(c + 1.0f, r + 1.0f, 2))
                    }
                }
                // Layer 3: Top cap
                positions.add(TilePosition(2.5f, 2.5f, 3))
            }
            5 -> {
                // Level 5: Sweet Heart (33 tiles, 3 layers)
                val heartBase = listOf(
                    Pair(2f, 1f), Pair(4f, 1f),
                    Pair(1.5f, 2f), Pair(2.5f, 2f), Pair(3.5f, 2f), Pair(4.5f, 2f),
                    Pair(1.5f, 3f), Pair(2.5f, 3f), Pair(3.5f, 3f), Pair(4.5f, 3f),
                    Pair(2f, 4f), Pair(3f, 4f), Pair(4f, 4f),
                    Pair(2.5f, 5f), Pair(3.5f, 5f),
                    Pair(3f, 6f)
                )
                heartBase.forEach { positions.add(TilePosition(it.first, it.second, 0)) }
                // Layer 1: Inner heart
                val innerHeart = listOf(
                    Pair(2f, 2f), Pair(4f, 2f),
                    Pair(2.5f, 2.5f), Pair(3.5f, 2.5f),
                    Pair(2.5f, 3.5f), Pair(3.5f, 3.5f),
                    Pair(3f, 4.5f),
                    Pair(3f, 2f), Pair(3f, 3f),
                    Pair(2f, 3f), Pair(4f, 3f)
                )
                innerHeart.forEach { positions.add(TilePosition(it.first, it.second, 1)) }
                // Layer 2: Center core
                positions.add(TilePosition(3f, 2.5f, 2))
                positions.add(TilePosition(3f, 3.5f, 2))
                positions.add(TilePosition(2.5f, 3f, 2))
                positions.add(TilePosition(3.5f, 3f, 2))
                positions.add(TilePosition(3f, 3f, 3))
                positions.add(TilePosition(3f, 2f, 3))
            }
            6 -> {
                // Level 6: Twin Towers (36 tiles, 4 layers)
                // Left tower base
                for (r in 1..4) {
                    for (c in 1..2) {
                        positions.add(TilePosition(c.toFloat(), r.toFloat(), 0))
                    }
                }
                // Right tower base
                for (r in 1..4) {
                    for (c in 4..5) {
                        positions.add(TilePosition(c.toFloat(), r.toFloat(), 0))
                    }
                }
                // Bridge base
                positions.add(TilePosition(2.5f, 2.5f, 0))
                positions.add(TilePosition(3.5f, 2.5f, 0))

                // Left tower layer 1
                for (r in 1..3) {
                    positions.add(TilePosition(1.5f, r + 0.5f, 1))
                }
                // Right tower layer 1
                for (r in 1..3) {
                    positions.add(TilePosition(4.5f, r + 0.5f, 1))
                }
                // Bridge layer 1
                positions.add(TilePosition(3.0f, 2.5f, 1))

                // Left tower layer 2
                positions.add(TilePosition(1.5f, 1.5f, 2))
                positions.add(TilePosition(1.5f, 2.5f, 2))
                // Right tower layer 2
                positions.add(TilePosition(4.5f, 1.5f, 2))
                positions.add(TilePosition(4.5f, 2.5f, 2))

                // Spire peaks
                positions.add(TilePosition(1.5f, 2.0f, 3))
                positions.add(TilePosition(4.5f, 2.0f, 3))
                positions.add(TilePosition(3.0f, 2.5f, 2))
                positions.add(TilePosition(3.0f, 3.5f, 2))
            }
            7 -> {
                // Level 7: Butterfly Wings (39 tiles, 3 layers)
                val wingCoords = listOf(
                    // Left wing
                    Pair(1f, 1f), Pair(2f, 1f),
                    Pair(0.5f, 2f), Pair(1.5f, 2f), Pair(2.5f, 2f),
                    Pair(0.5f, 3f), Pair(1.5f, 3f), Pair(2.5f, 3f),
                    Pair(1f, 4f), Pair(2f, 4f),
                    // Right wing
                    Pair(4f, 1f), Pair(5f, 1f),
                    Pair(3.5f, 2f), Pair(4.5f, 2f), Pair(5.5f, 2f),
                    Pair(3.5f, 3f), Pair(4.5f, 3f), Pair(5.5f, 3f),
                    Pair(4f, 4f), Pair(5f, 4f),
                    // Body
                    Pair(3f, 1.5f), Pair(3f, 2.5f), Pair(3f, 3.5f)
                )
                wingCoords.forEach { positions.add(TilePosition(it.first, it.second, 0)) }

                // Layer 1
                val wingL1 = listOf(
                    Pair(1.5f, 1.5f), Pair(1f, 2.5f), Pair(2f, 2.5f), Pair(1.5f, 3.5f),
                    Pair(4.5f, 1.5f), Pair(4f, 2.5f), Pair(5f, 2.5f), Pair(4.5f, 3.5f),
                    Pair(3f, 2f), Pair(3f, 3f)
                )
                wingL1.forEach { positions.add(TilePosition(it.first, it.second, 1)) }

                // Layer 2
                positions.add(TilePosition(1.5f, 2.5f, 2))
                positions.add(TilePosition(4.5f, 2.5f, 2))
                positions.add(TilePosition(3f, 2.5f, 2))
                positions.add(TilePosition(1.5f, 2.0f, 2))
                positions.add(TilePosition(4.5f, 2.0f, 2))
                positions.add(TilePosition(3f, 1.5f, 2))
            }
            8 -> {
                // Level 8: Zen Mandala (42 tiles, 4 layers)
                for (r in 1..5) {
                    for (c in 1..5) {
                        if (abs(r - 3) + abs(c - 3) <= 3) {
                            positions.add(TilePosition(c.toFloat(), r.toFloat(), 0))
                        }
                    }
                }
                for (r in 1..4) {
                    for (c in 1..4) {
                        if (abs(r - 2.5f) + abs(c - 2.5f) <= 2.2f) {
                            positions.add(TilePosition(c + 0.5f, r + 0.5f, 1))
                        }
                    }
                }
                positions.add(TilePosition(2.5f, 2.5f, 2))
                positions.add(TilePosition(3.5f, 2.5f, 2))
                positions.add(TilePosition(2.5f, 3.5f, 2))
                positions.add(TilePosition(3.5f, 3.5f, 2))
                positions.add(TilePosition(3.0f, 3.0f, 3))
                positions.add(TilePosition(2.0f, 3.0f, 2))
                positions.add(TilePosition(4.0f, 3.0f, 2))
            }
            9 -> {
                // Level 9: Honeycomb Hive (45 tiles, 4 layers)
                for (r in 1..5) {
                    val offset = if (r % 2 == 0) 0.5f else 0.0f
                    for (c in 1..5) {
                        positions.add(TilePosition(c + offset, r.toFloat(), 0))
                    }
                }
                for (r in 1..4) {
                    val offset = if (r % 2 == 1) 0.5f else 0.0f
                    for (c in 1..4) {
                        positions.add(TilePosition(c + 0.5f + offset, r + 0.5f, 1))
                    }
                }
                positions.add(TilePosition(2.5f, 2.5f, 2))
                positions.add(TilePosition(3.5f, 2.5f, 2))
                positions.add(TilePosition(3.0f, 3.5f, 2))
                positions.add(TilePosition(3.0f, 2.0f, 3))
                positions.add(TilePosition(3.0f, 3.0f, 3))
            }
            else -> {
                // Level 10 & Multiples: Castle Fortress (48 tiles, 5 layers)
                for (r in 1..5) {
                    for (c in 1..5) {
                        positions.add(TilePosition(c.toFloat(), r.toFloat(), 0))
                    }
                }
                for (r in 1..4) {
                    for (c in 1..4) {
                        positions.add(TilePosition(c + 0.5f, r + 0.5f, 1))
                    }
                }
                for (r in 2..3) {
                    for (c in 2..3) {
                        positions.add(TilePosition(c.toFloat(), r.toFloat(), 2))
                        positions.add(TilePosition(c + 0.5f, r + 0.5f, 3))
                    }
                }
                positions.add(TilePosition(3.0f, 3.0f, 4))
                positions.add(TilePosition(2.5f, 2.5f, 4))
                positions.add(TilePosition(3.5f, 3.5f, 4))
            }
        }
        return positions
    }
}
