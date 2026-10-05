package com.app_health.app.feature.home.components.Home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app_health.app.ui.theme.TextPrimary
import com.app_health.app.ui.theme.TextSecondary

@Composable
fun CalorieTargetHeader() {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text("Mục tiêu calo", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
    }
}

@Composable
fun CalorieRing(
    consumed: Int,
    target: Int = 2000,
    modifier: Modifier = Modifier
) {
    val isOver = consumed > target
    val displayValue = if (isOver) consumed - target else (target - consumed).coerceAtLeast(0)

    val ringColor = if (isOver) Color(0xFFEF4444) else Color(0xFF10B981)

    val progress = if (target > 0) {
        if (isOver) 1f else (consumed.toFloat() / target.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(240.dp)
    ) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxSize(),
            color = ringColor,
            strokeWidth = 18.dp,
            strokeCap = StrokeCap.Round,
            trackColor = Color.Gray.copy(alpha = 0.2f)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = displayValue.toString(),
                color = TextPrimary,
                fontSize = 52.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = if (isOver) "Calo dư thừa" else "Calo còn lại",
                color = TextSecondary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}