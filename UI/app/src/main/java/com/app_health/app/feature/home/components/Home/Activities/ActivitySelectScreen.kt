package com.app_health.app.feature.home.components.Home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app_health.app.feature.home.model.ExerciseDatabase
import com.app_health.app.ui.theme.AppBackground
import com.app_health.app.ui.theme.AppPrimary
import com.app_health.app.ui.theme.AppSurface
import com.app_health.app.ui.theme.TextPrimary
import com.app_health.app.ui.theme.TextSecondary
import java.text.Normalizer

// --- HÀM HỖ TRỢ XÓA DẤU TIẾNG VIỆT ---
fun String.removeAccents(): String {
    val normalized = Normalizer.normalize(this, Normalizer.Form.NFD)
    val regex = "\\p{InCombiningDiacriticalMarks}+".toRegex()
    return regex.replace(normalized, "")
        .replace("đ", "d")
        .replace("Đ", "D")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivitySelectScreen(
    onBack: () -> Unit,
    onActivitySelected: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val allActivities = ExerciseDatabase.allActivities

    // === LOGIC TÌM KIẾM THÔNG MINH BỎ QUA DẤU TIẾNG VIỆT ===
    val filteredActivities = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            allActivities
        } else {
            val queryNormalized = searchQuery.removeAccents().lowercase()
            allActivities.filter { activity ->
                val nameNormalized = activity.name.removeAccents().lowercase()
                nameNormalized.contains(queryNormalized)
            }
        }
    }

    // Nhóm dữ liệu theo chữ cái đầu (category)
    val groupedActivities = filteredActivities.groupBy { it.category }
    val alphabets = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".map { it.toString() }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(AppBackground)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .statusBarsPadding(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                    Text(
                        text = "Thêm hoạt động",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { /* Thêm hoạt động mới */ }) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = TextPrimary)
                    }
                }

                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .shadow(2.dp, RoundedCornerShape(25.dp))
                            .border(1.dp, Color.DarkGray.copy(alpha = 0.3f), RoundedCornerShape(25.dp)),
                        placeholder = { Text("Tìm kiếm hoạt động", color = TextSecondary) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = TextSecondary) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = AppSurface,
                            unfocusedContainerColor = AppSurface,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = AppPrimary
                        ),
                        shape = RoundedCornerShape(25.dp)
                    )
                }
            }
        },
        containerColor = AppBackground
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            // Xử lý giao diện khi không tìm thấy kết quả
            if (filteredActivities.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Không tìm thấy hoạt động nào phù hợp",
                        color = TextSecondary,
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    groupedActivities.forEach { (category, activities) ->
                        item {
                            Text(
                                text = category,
                                color = TextSecondary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(activities) { activity ->
                            Column(modifier = Modifier.clickable { onActivitySelected(activity.name) }) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = activity.name,
                                        color = TextPrimary,
                                        fontSize = 16.sp
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = "Select",
                                        tint = TextSecondary
                                    )
                                }
                                HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.3f), thickness = 1.dp)
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                alphabets.forEach { letter ->
                    Text(
                        text = letter,
                        fontSize = 10.sp,
                        color = TextSecondary,
                        modifier = Modifier
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                            .clickable {}
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun ActivitySelectScreenPreview() {
    ActivitySelectScreen(onBack = {}, onActivitySelected = {})
}