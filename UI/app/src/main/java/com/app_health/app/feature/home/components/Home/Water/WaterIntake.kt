package com.app_health.app.feature.home.components.Home.Water

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app_health.app.ui.theme.AppBackground
import com.app_health.app.ui.theme.AppSurface
import com.app_health.app.ui.theme.TextPrimary
import com.app_health.app.ui.theme.TextSecondary
import java.text.DecimalFormat

@Composable
fun WaterIntakeCard(
    currentWaterMl: Int,
    targetWaterMl: Int,
    onIncrease: () -> Unit = {},
    onDecrease: () -> Unit = {},
    onCardClick: () -> Unit = {}
) {
    // Ép kiểu sang Float (chia 1000) và format để xóa số 0 thừa ở đuôi
    // Ví dụ: 3500 -> 3.5 | 2544 -> 2.544
    val df = DecimalFormat("#.###")
    val currentLiters = df.format(currentWaterMl / 1000f)
    val targetLiters = df.format(targetWaterMl / 1000f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
            .background(AppSurface, RoundedCornerShape(16.dp))
            .clickable { onCardClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = "Water",
                    tint = Color(0xFF3B82F6),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Uống nước",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    // CẬP NHẬT: Hiển thị theo đơn vị Lít
                    Text(
                        text = "$currentLiters / $targetLiters Lít",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                }
            }

            // Nút điều khiển - và +
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(AppBackground, RoundedCornerShape(20.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Minus",
                    tint = TextSecondary,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onDecrease() }
                )
                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .height(16.dp)
                        .width(1.dp)
                        .background(TextSecondary.copy(alpha = 0.3f))
                )

                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Plus",
                    tint = Color(0xFF3B82F6),
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onIncrease() }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewWaterIntakeCard() {
    WaterIntakeCard(
        currentWaterMl = 3500,
        targetWaterMl = 2544
    )
}