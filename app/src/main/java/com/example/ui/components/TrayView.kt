package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Tile
import com.example.ui.theme.SleekOutline
import com.example.ui.theme.SleekPrimary
import com.example.ui.theme.SleekPrimaryContainer
import com.example.ui.theme.SleekSurfaceContainer
import com.example.ui.theme.SleekSurfaceVariant

@Composable
fun TrayView(
    trayTiles: List<Tile>,
    matchingTileIds: Set<String>,
    maxCapacity: Int = 7,
    isWarning: Boolean = false,
    trayColorHex: Long = 0xFFF3EDF7,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "warningPulse")
    val warningAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "warningAlpha"
    )

    val trayShape = RoundedCornerShape(20.dp)
    val slotShape = RoundedCornerShape(12.dp)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .shadow(
                elevation = 6.dp,
                shape = trayShape,
                ambientColor = Color(0x1A1D1B20),
                spotColor = Color(0x2A6750A4)
            )
            .clip(trayShape)
            .background(Color(trayColorHex))
            .border(
                width = if (isWarning || trayTiles.size >= 6) 2.5.dp else 1.5.dp,
                color = if (trayTiles.size >= 6) Color(0xFFBA1A1A).copy(alpha = warningAlpha)
                else SleekOutline,
                shape = trayShape
            )
            .padding(horizontal = 10.dp, vertical = 10.dp)
            .testTag("tray_dock")
    ) {
        val totalSpacing = 6.dp * (maxCapacity - 1)
        val availableWidth = maxWidth - 20.dp - totalSpacing
        val slotSize = (availableWidth / maxCapacity).coerceAtMost(48.dp).coerceAtLeast(36.dp)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (index in 0 until maxCapacity) {
                val tile = trayTiles.getOrNull(index)

                Box(
                    modifier = Modifier
                        .size(slotSize, slotSize + 4.dp)
                        .clip(slotShape)
                        .background(SleekPrimaryContainer.copy(alpha = 0.5f))
                        .border(
                            width = 1.dp,
                            color = SleekOutline.copy(alpha = 0.6f),
                            shape = slotShape
                        )
                        .drawBehind {
                            // Soft inner inset line
                            drawLine(
                                color = Color(0x221D1B20),
                                start = Offset(2f, 2f),
                                end = Offset(size.width - 2f, 2f),
                                strokeWidth = 1.5f
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (tile != null) {
                        TileView(
                            tile = tile,
                            tileSize = slotSize,
                            isMatching = tile.id in matchingTileIds,
                            onClick = null
                        )
                    }
                }
            }
        }
    }
}

