package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Tile
import com.example.ui.theme.SleekOutline
import com.example.ui.theme.SleekPrimary
import com.example.ui.theme.SleekSurfaceVariant

@Composable
fun TileView(
    tile: Tile,
    tileSize: Dp = 56.dp,
    isMatching: Boolean = false,
    isHighlighted: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isMatching) 1.22f else if (isHighlighted) 1.10f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "tileScale"
    )

    val elevation = when {
        tile.inTray -> 2.dp
        tile.isSelectable -> (3 + tile.layer * 2).dp
        else -> 1.dp
    }

    val shape = RoundedCornerShape(14.dp)
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .size(tileSize)
            .scale(scale)
            .shadow(
                elevation = elevation,
                shape = shape,
                clip = false,
                ambientColor = Color(0x221D1B20),
                spotColor = Color(0x336750A4)
            )
            .clip(shape)
            .background(
                if (tile.isSelectable || tile.inTray) Color(0xFFFFFFFF)
                else Color(0xFFE8DEF8).copy(alpha = 0.75f)
            )
            .border(
                width = if (isHighlighted) 2.5.dp else 1.dp,
                color = when {
                    isHighlighted -> SleekPrimary
                    isMatching -> Color(0xFF00C853)
                    tile.isSelectable || tile.inTray -> SleekSurfaceVariant
                    else -> SleekOutline
                },
                shape = shape
            )
            .then(
                if (onClick != null && (tile.isSelectable || tile.inTray)) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = ripple(bounded = true, color = SleekPrimary.copy(alpha = 0.2f)),
                        onClick = onClick
                    )
                } else Modifier
            )
            .testTag("tile_${tile.typeId}_${tile.id}"),
        contentAlignment = Alignment.Center
    ) {
        // Sleek tactile bottom 3D bevel (border-b-4 style in HTML design)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val bevelColor = if (tile.isSelectable || tile.inTray) Color(0xFFE2DCED) else Color(0xFFCAC4D0)
                    drawLine(
                        color = bevelColor,
                        start = Offset(4f, size.height - 3.5f),
                        end = Offset(size.width - 4f, size.height - 3.5f),
                        strokeWidth = 4f
                    )
                }
        )

        // Tile emoji / icon
        Text(
            text = tile.type.symbol,
            fontSize = (tileSize.value * 0.52f).sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .offset(y = if (tile.isSelectable || tile.inTray) (-2).dp else (-1).dp)
        )

        // Sleek subtle category indicator dot in corner
        Box(
            modifier = Modifier
                .size(6.dp)
                .align(Alignment.TopEnd)
                .offset(x = (-6).dp, y = 6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(tile.type.primaryColor).copy(alpha = if (tile.isSelectable) 0.85f else 0.4f))
        )

        // Dimming overlay if blocked by tiles above
        if (!tile.isSelectable && !tile.inTray) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x3349454F))
            )
        }
    }
}

