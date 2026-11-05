package com.app_health.app.feature.home.components.Home.Water

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app_health.app.ui.theme.AppBackground
import com.app_health.app.ui.theme.AppSurface
import com.app_health.app.ui.theme.TextPrimary
import com.app_health.app.ui.theme.TextSecondary
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterTargetScreen(
    currentGoalLiters: Float = 2.3f,
    currentVolumeOption: Int = 200,
    onBack: () -> Unit = {},
    onSave: (targetLiters: Float, volumeStep: Int) -> Unit = { _, _ -> }
) {
    var sliderValue by remember { mutableFloatStateOf(currentGoalLiters) }
    var selectedVolume by remember { mutableIntStateOf(currentVolumeOption) }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppBackground)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .statusBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Spacer(modifier = Modifier.weight(1f))
                Text("Mục tiêu nước uống", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1.2f))
            }
        },
        containerColor = AppBackground,
        bottomBar = {
            Box(modifier = Modifier.padding(16.dp).navigationBarsPadding()) {
                Button(
                    onClick = { onSave(sliderValue, selectedVolume) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0)),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Text("Cập nhật", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text("Lượng nước cần uống trong ngày", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "${(sliderValue * 10).roundToInt() / 10f} lít",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
            ) {
                IconButton(
                    onClick = { if (sliderValue > 1f) sliderValue -= 0.1f },
                    modifier = Modifier.background(AppSurface, RoundedCornerShape(12.dp)).size(36.dp)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = TextPrimary)
                }

                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    valueRange = 1f..4f,
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF9C27B0),
                        activeTrackColor = Color(0xFF9C27B0),
                        inactiveTrackColor = AppSurface
                    )
                )

                IconButton(
                    onClick = { if (sliderValue < 4f) sliderValue += 0.1f },
                    modifier = Modifier.background(AppSurface, RoundedCornerShape(12.dp)).size(36.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Increase", tint = TextPrimary)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 48.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("1 lít", color = TextSecondary, fontSize = 12.sp)
                Text("4 lít", color = TextSecondary, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text("Dung tích mỗi lần ghi lại", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                VolumeOptionItem(
                    icon = Icons.Default.LocalDrink,
                    title = "Ly nước",
                    volume = 200,
                    isSelected = selectedVolume == 200,
                    onClick = { selectedVolume = 200 }
                )

                Box(
                    modifier = Modifier
                        .height(80.dp)
                        .width(1.dp)
                        .background(TextSecondary.copy(alpha = 0.2f))
                )

                VolumeOptionItem(
                    icon = Icons.Default.WaterDrop,
                    title = "Chai nước",
                    volume = 500,
                    isSelected = selectedVolume == 500,
                    onClick = { selectedVolume = 500 }
                )
            }
        }
    }
}

@Composable
fun VolumeOptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    volume: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }.padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (isSelected) Color(0xFF3B82F6).copy(alpha = 0.2f) else AppSurface),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = Color(0xFF3B82F6), modifier = Modifier.size(32.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF9C27B0), unselectedColor = TextSecondary)
        )
        Text(text = title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Text(text = "${volume}ml", color = TextSecondary, fontSize = 12.sp)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun PreviewWaterTargetScreen() {
    WaterTargetScreen()
}