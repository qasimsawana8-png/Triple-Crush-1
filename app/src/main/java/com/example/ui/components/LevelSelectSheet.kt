package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LevelInfo
import com.example.ui.theme.SleekOnBackground
import com.example.ui.theme.SleekOutline
import com.example.ui.theme.SleekPrimary
import com.example.ui.theme.SleekPrimaryContainer
import com.example.ui.theme.SleekSurface
import com.example.ui.theme.SleekSurfaceContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelSelectSheet(
    levels: List<LevelInfo>,
    currentLevel: Int,
    onLevelSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SleekSurface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 20.dp)
                .testTag("level_select_sheet")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🗺️ Level Map",
                    color = SleekOnBackground,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SleekPrimaryContainer)
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = SleekPrimary)
                }
            }

            Text(
                text = "Complete levels to unlock new patterns & themes",
                color = SleekOnBackground.copy(alpha = 0.7f),
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(levels) { level ->
                    LevelTileItem(
                        level = level,
                        isCurrent = level.levelNumber == currentLevel,
                        onClick = {
                            if (level.isUnlocked) {
                                onLevelSelected(level.levelNumber)
                                onDismiss()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LevelTileItem(
    level: LevelInfo,
    isCurrent: Boolean,
    onClick: () -> Unit
) {
    val itemShape = RoundedCornerShape(16.dp)

    Box(
        modifier = Modifier
            .size(72.dp)
            .shadow(if (level.isUnlocked) 4.dp else 1.dp, itemShape, ambientColor = Color(0x111D1B20))
            .clip(itemShape)
            .background(
                when {
                    isCurrent -> SleekPrimary
                    level.isUnlocked -> Color.White
                    else -> SleekSurfaceContainer
                }
            )
            .border(
                width = if (isCurrent) 2.dp else 1.dp,
                color = if (isCurrent) SleekPrimary else if (level.isUnlocked) SleekOutline else SleekOutline.copy(alpha = 0.5f),
                shape = itemShape
            )
            .clickable(enabled = level.isUnlocked, onClick = onClick)
            .testTag("level_item_${level.levelNumber}"),
        contentAlignment = Alignment.Center
    ) {
        if (!level.isUnlocked) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Locked",
                tint = SleekOutline,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${level.levelNumber}",
                    color = if (isCurrent) Color.White else SleekPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Stars row
                Row {
                    for (i in 1..3) {
                        val earned = i <= level.starsEarned
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (earned) Color(0xFFFFB300) else if (isCurrent) Color.White.copy(alpha = 0.4f) else SleekOutline,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}

