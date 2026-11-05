package com.app_health.app.feature.home.components.Home.Activities

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.app_health.app.ui.theme.AppBackground
import com.app_health.app.ui.theme.AppPrimary
import com.app_health.app.ui.theme.AppSurface
import com.app_health.app.ui.theme.TextPrimary

@Composable
fun TrainingActivites(onActivityClick: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionTitle("Hoạt động tập luyện")
        Box(modifier = Modifier.fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
            .background(AppSurface, RoundedCornerShape(16.dp))
            .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    ActivityItem(Icons.Default.DirectionsRun, "Chạy bộ", onClick = { onActivityClick("Chạy bộ (Running)") })
                    ActivityItem(Icons.Default.Directions, "Bơi lội", onClick = { onActivityClick("Bơi lội (Swimming)") })
                    ActivityItem(Icons.Default.Directions, "Cầu lông", onClick = { onActivityClick("Cầu lông (Badminton)") })
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ){
                    ActivityItem(Icons.Default.DirectionsRun, "Pickleball", onClick = { onActivityClick("Pickleball") })
                    ActivityItem(Icons.Default.Directions, "Gym", onClick = { onActivityClick("Gym") })
                    ActivityItem(Icons.Default.Directions, "Khác", onClick = { onActivityClick("Hoạt động khác") })
                }
            }
        }
    }
}

@Composable
fun ActivityItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(80.dp)) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(AppBackground)
                .clickable { onClick() }, // Gọi hàm onClick khi người dùng nhấn
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = AppPrimary)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextPrimary)
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = TextPrimary,
        modifier = Modifier.padding(bottom = 12.dp, top = 8.dp)
    )
}
