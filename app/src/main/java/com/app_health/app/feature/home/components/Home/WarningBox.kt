package com.app_health.app.feature.home.components.Home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app_health.app.ui.theme.WarningBg
import com.app_health.app.ui.theme.WarningText

@Composable
fun WarningBox(isOver: Boolean = false) {
    // Đổi màu nền, màu chữ và nội dung dựa theo trạng thái dư thừa
    val bgColor = if (isOver) Color(0xFF2C1A1D) else WarningBg // Nền đỏ đen nếu dư thừa
    val textColor = if (isOver) Color(0xFFEF4444) else WarningText
    val warningText = if (isOver) "Bạn đã nạp quá số calo mục tiêu trong ngày!" else "Mục tiêu đang dưới mức BMR."

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
            .background(bgColor, RoundedCornerShape(16.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = textColor, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Cảnh báo", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = textColor)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.Default.Warning, contentDescription = null, tint = textColor, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(warningText, style = MaterialTheme.typography.labelSmall, color = textColor.copy(alpha = 0.8f))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WarningBoxPreview(){
    WarningBox()
}