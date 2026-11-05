package com.app_health.app.feature.home.components.Home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.app_health.app.ui.theme.AppPrimary
import com.app_health.app.ui.theme.AppSurface
import com.app_health.app.ui.theme.ProteinColor
import com.app_health.app.ui.theme.TextPrimary
import com.app_health.app.ui.theme.TextSecondary

@Composable
fun StatsRow(
    consumed: Int,
    target: Int = 1276,
    burned: Int = 0
) {
    val isOver = consumed > target
    // Đổi màu số lượng "Đã nạp" sang đỏ nếu vượt mục tiêu
    val consumedColor = if (isOver) Color(0xFFEF4444) else AppPrimary

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StatItem(target.toString(), "Mục tiêu", TextSecondary, TextPrimary)
        StatItem(consumed.toString(), "Đã nạp", consumedColor, consumedColor)
        StatItem(burned.toString(), "Tập luyện", ProteinColor, TextPrimary)
    }
}

@Composable
fun StatItem(value: String, label: String, iconColor: Color, valueColor: Color = TextPrimary) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
            .background(AppSurface, RoundedCornerShape(16.dp))
            .padding(vertical = 12.dp)
            .width(100.dp)
    ) {
        Text(value, style = MaterialTheme.typography.titleMedium, color = valueColor) // Nhận màu động
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).background(iconColor, CircleShape))
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
    }
}