package com.app_health.app.feature.home.components.Home

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app_health.app.core.utils.NutritionGoals
import com.app_health.app.ui.theme.AppBackground
import com.app_health.app.ui.theme.AppSurface
import com.app_health.app.ui.theme.CarbsColor
import com.app_health.app.ui.theme.FatColor
import com.app_health.app.ui.theme.ProteinColor
import com.app_health.app.ui.theme.TextPrimary
import com.app_health.app.ui.theme.TextSecondary

@Composable
fun MacrosCard(
    protein: Int,
    carbs: Int,
    fat: Int,
    goals: NutritionGoals?, // Nhận đối tượng mục tiêu dinh dưỡng đồng bộ
    fiber: Int = 0,
    sugar: Int = 0,
    salt: Int = 0,
    modifier: Modifier = Modifier
) {
    // Lấy giá trị mục tiêu từ tính toán động, nếu chưa có thì dùng mặc định an toàn
    val targetProtein = goals?.proteinGrams ?: 64
    val targetCarbs = goals?.carbsGrams ?: 160
    val targetFat = goals?.fatGrams ?: 43

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 3.dp, shape = RoundedCornerShape(20.dp))
            .background(AppSurface, RoundedCornerShape(20.dp))
            .padding(vertical = 20.dp, horizontal = 12.dp)
    ) {
        // Dùng Row kết hợp weight(1f) để các mục tự động co giãn và chia đều khoảng cách
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MacroItem(
                label = "Chất đạm",
                consumed = protein,
                target = targetProtein,
                color = ProteinColor,
                modifier = Modifier.weight(1f)
            )
            MacroItem(
                label = "Đường bột",
                consumed = carbs,
                target = targetCarbs,
                color = CarbsColor,
                modifier = Modifier.weight(1f)
            )
            MacroItem(
                label = "Chất béo",
                consumed = fat,
                target = targetFat,
                color = FatColor,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun MacroItem(
    label: String,
    consumed: Int,
    target: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    val progress = if (target > 0) {
        (consumed.toFloat() / target.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val isOver = consumed > target
    val activeColor = if (isOver) Color(0xFFEF4444) else color
    val textColor = if (isOver) Color(0xFFEF4444) else TextSecondary

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Bolt, contentDescription = null, tint = activeColor, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
        }
        Spacer(modifier = Modifier.height(10.dp))

        // Thanh tiến trình tự động lấp đầy phần trăm không gian của mỗi cột chia đều
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = activeColor,
            trackColor = AppBackground,
            strokeCap = StrokeCap.Round
        )

        Spacer(modifier = Modifier.height(10.dp))
        Text("$consumed / ${target}g", style = MaterialTheme.typography.labelSmall, color = textColor)
    }
}

@Preview(showBackground = true)
@Composable
fun MacrosCardPreview() {
    MacrosCard(
        protein = 45,
        carbs = 180,
        fat = 20,
        goals = null,
        fiber = 10,
        sugar = 15,
        salt = 2,
        modifier = Modifier.padding(16.dp)
    )
}