package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.PlayerProgressEntity
import com.example.ui.theme.SleekOnBackground
import com.example.ui.theme.SleekOutline
import com.example.ui.theme.SleekPrimary
import com.example.ui.theme.SleekPrimaryContainer
import com.example.ui.theme.SleekSurfaceVariant
import com.example.ui.viewmodel.GameUiState

@Composable
fun GameTopBar(
    uiState: GameUiState,
    playerProgress: PlayerProgressEntity?,
    onPauseClick: () -> Unit,
    onLevelsClick: () -> Unit,
    onThemesClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Level Selector Button (Sleek pill badge)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .shadow(2.dp, RoundedCornerShape(20.dp), ambientColor = Color(0x111D1B20))
                    .clip(RoundedCornerShape(20.dp))
                    .background(SleekPrimaryContainer)
                    .border(1.dp, SleekPrimary.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                    .clickable { onLevelsClick() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("level_select_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Map,
                    contentDescription = "Levels",
                    tint = SleekPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Level ${uiState.currentLevel}",
                    color = SleekPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            // Score & Timer (Sleek white pill)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .shadow(2.dp, RoundedCornerShape(20.dp), ambientColor = Color(0x111D1B20))
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, SleekOutline, RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "⭐ ${uiState.score}",
                    color = Color(0xFFF57F17),
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "⏱️ ${formatTime(uiState.timeElapsedSeconds)}",
                    color = SleekOnBackground,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Coins & Quick Actions
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Coins pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .shadow(2.dp, RoundedCornerShape(20.dp), ambientColor = Color(0x111D1B20))
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFFFF8E1))
                        .border(1.dp, Color(0xFFFFD54F), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .testTag("coins_indicator")
                ) {
                    Text(text = "🪙", fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${playerProgress?.totalCoins ?: 0}",
                        color = Color(0xFFE65100),
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Themes (Sleek white circle button)
                IconButton(
                    onClick = onThemesClick,
                    modifier = Modifier
                        .size(36.dp)
                        .shadow(2.dp, CircleShape, ambientColor = Color(0x111D1B20))
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, SleekOutline, CircleShape)
                        .testTag("theme_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Themes",
                        tint = SleekPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Settings (Sleek white circle button)
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier
                        .size(36.dp)
                        .shadow(2.dp, CircleShape, ambientColor = Color(0x111D1B20))
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, SleekOutline, CircleShape)
                        .testTag("settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = SleekPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Animated Combo Streak Banner
        AnimatedVisibility(
            visible = uiState.comboMessage != null,
            enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .shadow(6.dp, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(
                                    SleekPrimary,
                                    Color(0xFF7E57C2),
                                    Color(0xFFAB47BC)
                                )
                            )
                        )
                        .border(1.5.dp, SleekPrimaryContainer, RoundedCornerShape(16.dp))
                        .padding(horizontal = 18.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = uiState.comboMessage ?: "",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

private fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format("%02d:%02d", m, s)
}

