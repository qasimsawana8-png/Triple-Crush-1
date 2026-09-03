package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.PlayerProgressEntity
import com.example.ui.theme.SleekOutline
import com.example.ui.theme.SleekPrimary
import com.example.ui.theme.SleekPrimaryContainer
import com.example.ui.theme.SleekSurfaceVariant

@Composable
fun BoosterBar(
    playerProgress: PlayerProgressEntity?,
    onUndoClick: () -> Unit,
    onShuffleClick: () -> Unit,
    onHintClick: () -> Unit,
    onVacuumClick: () -> Unit,
    onBuyBoosterClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val undoCount = playerProgress?.boosterUndoCount ?: 0
    val shuffleCount = playerProgress?.boosterShuffleCount ?: 0
    val hintCount = playerProgress?.boosterHintCount ?: 0
    val vacuumCount = playerProgress?.boosterVacuumCount ?: 0

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BoosterButton(
            title = "Undo",
            icon = Icons.Default.Undo,
            count = undoCount,
            accentColor = Color(0xFF3F51B5),
            testTag = "booster_undo",
            onClick = {
                if (undoCount > 0) onUndoClick() else onBuyBoosterClick("Undo")
            }
        )

        BoosterButton(
            title = "Shuffle",
            icon = Icons.Default.Shuffle,
            count = shuffleCount,
            accentColor = Color(0xFF2E7D32),
            testTag = "booster_shuffle",
            onClick = {
                if (shuffleCount > 0) onShuffleClick() else onBuyBoosterClick("Shuffle")
            }
        )

        BoosterButton(
            title = "Wand",
            icon = Icons.Default.AutoAwesome,
            count = hintCount,
            accentColor = SleekPrimary,
            testTag = "booster_hint",
            onClick = {
                if (hintCount > 0) onHintClick() else onBuyBoosterClick("Magic Wand")
            }
        )

        BoosterButton(
            title = "Vacuum",
            icon = Icons.Default.CleaningServices,
            count = vacuumCount,
            accentColor = Color(0xFFC2185B),
            testTag = "booster_vacuum",
            onClick = {
                if (vacuumCount > 0) onVacuumClick() else onBuyBoosterClick("Vacuum")
            }
        )
    }
}

@Composable
private fun BoosterButton(
    title: String,
    icon: ImageVector,
    count: Int,
    accentColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val buttonShape = RoundedCornerShape(16.dp)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = false, radius = 28.dp),
                onClick = onClick
            )
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier.size(56.dp, 52.dp),
            contentAlignment = Alignment.Center
        ) {
            // Sleek White tactile button with subtle bevel
            Box(
                modifier = Modifier
                    .size(48.dp, 48.dp)
                    .shadow(
                        elevation = 3.dp,
                        shape = buttonShape,
                        ambientColor = Color(0x1A1D1B20),
                        spotColor = Color(0x2A6750A4)
                    )
                    .clip(buttonShape)
                    .background(Color.White)
                    .border(1.dp, SleekOutline.copy(alpha = 0.8f), buttonShape)
                    .drawBehind {
                        // 3D tactile bottom edge
                        drawLine(
                            color = Color(0xFFE2DCED),
                            start = Offset(4f, size.height - 2.5f),
                            end = Offset(size.width - 4f, size.height - 2.5f),
                            strokeWidth = 3f
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Count badge or "+" buy badge
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 2.dp, y = (-2).dp)
                    .shadow(2.dp, CircleShape)
                    .clip(CircleShape)
                    .background(if (count > 0) SleekPrimary else Color(0xFFFF8F00))
                    .border(1.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (count > 0) "$count" else "+",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = title,
            color = SleekPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

