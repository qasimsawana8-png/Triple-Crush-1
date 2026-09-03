package com.example.ui.components

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BoardBackground
import com.example.data.model.TileTheme
import com.example.data.repository.TileThemeRegistry
import com.example.ui.theme.SleekOnBackground
import com.example.ui.theme.SleekOutline
import com.example.ui.theme.SleekPrimary
import com.example.ui.theme.SleekPrimaryContainer
import com.example.ui.theme.SleekSurface
import com.example.ui.theme.SleekSurfaceContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeShopSheet(
    activeTheme: TileTheme,
    activeBackground: BoardBackground,
    totalCoins: Int,
    onSelectTheme: (TileTheme) -> Unit,
    onSelectBackground: (BoardBackground) -> Unit,
    onWatchAdForCoins: () -> Unit,
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
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
                .testTag("theme_shop_sheet")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🎨 Themes & Styles",
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

            Spacer(modifier = Modifier.height(14.dp))

            // Free Coins Banner Card with AdMob Rewarded Ad
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFFFFF3E0), Color(0xFFFFECB3))))
                    .border(1.5.dp, Color(0xFFFFB300), RoundedCornerShape(16.dp))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🪙 $totalCoins Coins", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color(0xFFE65100))
                    }
                    Text(text = "Watch short ad to get +50 Coins", fontSize = 12.sp, color = Color(0xFF6D4C41))
                }

                androidx.compose.material3.Button(
                    onClick = onWatchAdForCoins,
                    shape = RoundedCornerShape(12.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF8F00)
                    ),
                    modifier = Modifier.testTag("watch_ad_coins_btn")
                ) {
                    Text(text = "🎬 +50 🪙", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "TILE THEMES",
                color = SleekPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                for (theme in TileThemeRegistry.getAllThemes()) {
                    val isSelected = theme.id == activeTheme.id
                    val sampleTiles = TileThemeRegistry.getTilesForTheme(theme.id).take(5)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(if (isSelected) 4.dp else 1.dp, RoundedCornerShape(16.dp), ambientColor = Color(0x111D1B20))
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) SleekPrimaryContainer else Color.White)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) SleekPrimary else SleekOutline,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { onSelectTheme(theme) }
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = theme.title,
                                color = if (isSelected) SleekPrimary else SleekOnBackground,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                for (t in sampleTiles) {
                                    Text(text = t.symbol, fontSize = 20.sp)
                                }
                            }
                        }

                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(SleekPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Active",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "BOARD BACKGROUNDS",
                color = SleekPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                for (bg in BoardBackground.values()) {
                    val isSelected = bg.id == activeBackground.id

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(if (isSelected) 4.dp else 1.dp, RoundedCornerShape(16.dp), ambientColor = Color(0x111D1B20))
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) SleekPrimaryContainer else Color.White)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) SleekPrimary else SleekOutline,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { onSelectBackground(bg) }
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        brush = Brush.verticalGradient(
                                            listOf(Color(bg.topGradientColor), Color(bg.bottomGradientColor))
                                        )
                                    )
                                    .border(1.dp, SleekOutline, RoundedCornerShape(10.dp))
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = bg.title,
                                color = if (isSelected) SleekPrimary else SleekOnBackground,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(SleekPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Active",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

