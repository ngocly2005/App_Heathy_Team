package com.app_health.app.feature.home.components.Home.Activities

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app_health.app.feature.home.components.Home.Calculator.ActivityCalculator
import com.app_health.app.feature.home.model.ActivityInputType
import com.app_health.app.feature.home.model.ExerciseDatabase
import com.app_health.app.ui.theme.AppBackground
import com.app_health.app.ui.theme.AppPrimary
import com.app_health.app.ui.theme.AppSurface
import com.app_health.app.ui.theme.TextPrimary
import com.app_health.app.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityDetailScreen(
    activityName: String = "Chạy bộ (Running)",
    onBack: () -> Unit = {},
    onSave: (levelOrDistance: String, time: String, calories: String) -> Unit = { _, _, _ -> }
) {
    // 1. Tìm thông tin chi tiết của môn thể thao trong Database
    val activityData = remember(activityName) {
        ExerciseDatabase.allActivities.find { it.name == activityName }
    }

    val inputType = activityData?.inputType ?: ActivityInputType.DISTANCE
    val levelOptions = activityData?.levelOptions ?: ExerciseDatabase.defaultLevels

    // Trạng thái lưu trữ dữ liệu
    var distance by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var selectedLevel by remember { mutableStateOf(levelOptions.firstOrNull() ?: "") }

    // Trạng thái cho Bottom Sheet chọn mức độ
    var showLevelSheet by remember { mutableStateOf(false) }

    // 2. TỰ ĐỘNG TÍNH CALO KHI CÁC CHỈ SỐ THAY ĐỔI
    LaunchedEffect(distance, time, selectedLevel) {
        val calculated = ActivityCalculator.calculateCaloriesBurned(activityName, distance, time, selectedLevel)
        if (calculated > 0) {
            calories = calculated.toString()
        } else if (distance.isEmpty() && time.isEmpty()) {
            calories = "" // Xóa ô calo nếu người dùng xóa hết khoảng cách/thời gian
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppBackground)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .statusBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }

                Text(
                    text = activityName,
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                TextButton(onClick = {
                    val finalParam = if (inputType == ActivityInputType.DISTANCE) distance else selectedLevel
                    onSave(finalParam, time, calories)
                }) {
                    Text("Lưu", color = TextSecondary, fontSize = 16.sp)
                }
            }
        },
        containerColor = AppBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Khối hình ảnh minh họa (Hero Image)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF2A2A3A)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsRun,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // 3. Logic Hiển thị Hàng nhập liệu động
            if (inputType == ActivityInputType.DISTANCE) {
                ActivityInputRow(label = "Khoảng cách", value = distance, onValueChange = { distance = it }, unit = "Km")
            } else {
                // Hàng chọn Mức độ
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showLevelSheet = true }
                        .padding(vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Mức độ", color = TextSecondary, fontSize = 16.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = selectedLevel, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                    }
                }
            }
            HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.5f), thickness = 1.dp)

            ActivityInputRow(label = "Thời gian", value = time, onValueChange = { time = it }, unit = "Phút")
            HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.5f), thickness = 1.dp)

            // Ô calo bây giờ sẽ tự động nhảy số theo LaunchedEffect
            ActivityInputRow(label = "Calo tiêu thụ", value = calories, onValueChange = { calories = it }, unit = "Cal")
            HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.5f), thickness = 1.dp)
        }
    }

    // 4. Modal Bottom Sheet cho việc chọn Mức độ (Nghiệp dư, Yoga...)
    if (showLevelSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLevelSheet = false },
            containerColor = Color(0xFF1E1E2C), // Màu nền tối cho BottomSheet
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                levelOptions.forEach { level ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedLevel = level
                                showLevelSheet = false
                            }
                            .padding(horizontal = 24.dp, vertical = 18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = level,
                            color = if (selectedLevel == level) AppPrimary else Color.White,
                            fontSize = 16.sp
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = if (selectedLevel == level) AppPrimary else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.3f), thickness = 1.dp)
                }
            }
        }
    }
}

@Composable
fun ActivityInputRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    unit: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextSecondary, fontSize = 16.sp)

        Row(verticalAlignment = Alignment.CenterVertically) {
            BasicTextField(
                value = value,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*\$"))) {
                        onValueChange(newValue)
                    }
                },
                textStyle = TextStyle(fontSize = 16.sp, color = TextPrimary, textAlign = TextAlign.End),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                cursorBrush = SolidColor(TextPrimary),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.widthIn(min = 40.dp), contentAlignment = Alignment.CenterEnd) {
                        if (value.isEmpty()) {
                            Text("0", color = TextSecondary.copy(alpha = 0.5f), fontSize = 16.sp)
                        }
                        innerTextField()
                    }
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = unit, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun ActivityDetailScreenRunningPreview() {
    ActivityDetailScreen(activityName = "Chạy bộ (Running)")
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun ActivityDetailScreenBadmintonPreview() {
    ActivityDetailScreen(activityName = "Cầu lông (Badminton)")
}