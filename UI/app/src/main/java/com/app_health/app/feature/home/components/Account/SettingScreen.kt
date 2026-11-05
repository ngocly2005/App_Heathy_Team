package com.app_health.app.feature.home.components.Account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app_health.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    name: String,
    email: String,
    age: Int,
    height: Float,
    weight: Float,
    gender: String,
    activityLevel: String,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    // 1. Tính toán BMI
    val bmi = if (height > 0) weight / ((height / 100) * (height / 100)) else 0f
    val bmiFormatted = String.format("%.1f", bmi)

    // 2. Tính toán BMR
    val bmr = if (gender.equals("Nam", ignoreCase = true)) {
        (10 * weight) + (6.25f * height) - (5 * age) + 5
    } else {
        (10 * weight) + (6.25f * height) - (5 * age) - 161
    }
    val bmrFormatted = String.format("%.0f", bmr)

    // 3. Tính toán TDEE dựa trên hệ số vận động
    val activityMultiplier = when (activityLevel) {
        "Ít vận động" -> 1.2f
        "Vận động nhẹ nhàng" -> 1.375f
        "Vận động vừa phải" -> 1.55f
        "Chăm chỉ tập luyện" -> 1.725f
        "Tập luyện cường độ cao" -> 1.9f
        else -> 1.2f
    }
    val tdee = bmr * activityMultiplier
    val tdeeFormatted = String.format("%.0f", tdee)

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Cài đặt tài khoản",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Quay lại", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppBackground,
                    scrolledContainerColor = AppBackground
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {

                // --- 1. Phần Avatar ---
                val firstLetter = if (name.isNotBlank()) name.take(1).uppercase() else "N"
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(AppPrimaryLight.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = firstLetter,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(text = email, fontSize = 14.sp, color = TextSecondary)
                }

                // --- 2. Thẻ Thông tin cá nhân cơ bản ---
                SettingSectionCard(title = "Thông tin chung") {
                    SettingInfoRow(label = "Họ và tên", value = name)
                    HorizontalDivider(color = TextSecondary.copy(alpha = 0.1f))
                    SettingInfoRow(label = "Email", value = email)
                }

                // --- 3. Thẻ Chỉ số cơ thể (Đã thêm BMR, TDEE, BMI) ---
                SettingSectionCard(title = "Chỉ số cơ thể") {
                    SettingInfoRow(label = "Giới tính", value = gender)
                    HorizontalDivider(color = TextSecondary.copy(alpha = 0.1f))

                    SettingInfoRow(label = "Độ tuổi", value = "$age tuổi")
                    HorizontalDivider(color = TextSecondary.copy(alpha = 0.1f))

                    SettingInfoRow(label = "Chiều cao", value = "${height.toInt()} cm")
                    HorizontalDivider(color = TextSecondary.copy(alpha = 0.1f))

                    SettingInfoRow(label = "Cân nặng", value = "$weight kg")
                    HorizontalDivider(color = TextSecondary.copy(alpha = 0.1f))

                    SettingInfoRow(label = "Vận động", value = activityLevel)
                    HorizontalDivider(color = TextSecondary.copy(alpha = 0.1f))

                    SettingInfoRow(label = "BMI (Chỉ số khối)", value = bmiFormatted)
                    HorizontalDivider(color = TextSecondary.copy(alpha = 0.1f))

                    SettingInfoRow(label = "BMR (Trao đổi chất)", value = "$bmrFormatted kcal")
                    HorizontalDivider(color = TextSecondary.copy(alpha = 0.1f))

                    SettingInfoRow(label = "TDEE (Năng lượng)", value = "$tdeeFormatted kcal")
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            // --- Nút Đăng xuất ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Button(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4B4B).copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp)
                ) {
                    Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Đăng xuất", tint = Color(0xFFFF4B4B))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Đăng xuất tài khoản", color = Color(0xFFFF4B4B), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Khối Card chứa các thông tin
@Composable
fun SettingSectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary,
            modifier = Modifier.padding(start = 8.dp)
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = AppSurface,
            shadowElevation = 1.dp
        ) {
            Column(
                modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content
            )
        }
    }
}

@Composable
fun SettingInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 15.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
        Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
    }
}