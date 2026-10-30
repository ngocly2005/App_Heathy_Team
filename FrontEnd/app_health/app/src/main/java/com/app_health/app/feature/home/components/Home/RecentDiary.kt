package com.app_health.app.feature.home.components.Home

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app_health.app.ui.theme.AppSurface
import com.app_health.app.ui.theme.TextPrimary
import com.app_health.app.ui.theme.TextSecondary
import com.app_health.app.ui.theme.AppBackground

// Import Model FoodItem
import com.app_health.app.feature.home.model.FoodItem

@Composable
fun RecentDiary(
    foods: List<FoodItem>,
    onTitleClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Bọc tiêu đề để tăng vùng bấm và tạo hiệu ứng ripple đẹp hơn
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { onTitleClick() }
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Nhật ký gần đây",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (foods.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
                    .background(AppSurface, RoundedCornerShape(16.dp))
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Assignment,
                        contentDescription = null,
                        tint = TextSecondary.copy(alpha = 0.5f),
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Chưa có dữ liệu",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        } else {
            // Giao diện hiển thị danh sách các món ăn vừa chọn
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                foods.forEach { food ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
                            .background(AppSurface, RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Icon món ăn nhỏ bên trái
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AppBackground),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🍲", fontSize = 20.sp)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Sử dụng Modifier.weight(1f) để tránh chèn ép nội dung phía sau
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = food.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "P: ${food.protein}g | C: ${food.carbs}g | F: ${food.fat}g",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = "${food.calories} kcal",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RecentDiaryPreview() {
    RecentDiary(
        foods = listOf(
            FoodItem(
                name = "Bánh xếp kiểu Hàn Quốc 365 nhân thịt và rau củ Bibigo",
                calories = 459,
                protein = 21,
                carbs = 66,
                fat = 9
            )
        ),
        onTitleClick = {}
    )
}