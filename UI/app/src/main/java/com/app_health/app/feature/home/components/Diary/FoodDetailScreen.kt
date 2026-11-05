package com.app_health.app.feature.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app_health.app.feature.home.model.FoodItem
import com.app_health.app.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun FoodDetailScreen(
    food: FoodItem, // Dữ liệu gốc lấy từ FoodDatabase
    onBack: () -> Unit,
    onAdd: (FoodItem) -> Unit
) {
    // 1. Trạng thái lưu số gram người dùng nhập (mặc định 100g)
    var gramInput by remember { mutableStateOf("100") }

    // 2. Logic tính toán tỷ lệ (Ratio)
    // Nếu người dùng xóa trắng ô nhập, mặc định là 0f để tránh lỗi crash
    val gram = gramInput.toFloatOrNull() ?: 0f
    val ratio = gram / 100f

    // 3. Nhân tỷ lệ với dữ liệu gốc của món ăn để ra chỉ số hiện tại
    val currentCal = (food.calories * ratio).roundToInt()
    val currentPro = (food.protein * ratio).roundToInt()
    val currentCarbs = (food.carbs * ratio).roundToInt()
    val currentFat = (food.fat * ratio).roundToInt()

    // 4. Tính toán phần trăm Macro cho biểu đồ
    val totalMacros = currentPro + currentCarbs + currentFat
    val proPercent = if (totalMacros > 0) (currentPro.toFloat() / totalMacros * 100).roundToInt() else 0
    val carbPercent = if (totalMacros > 0) (currentCarbs.toFloat() / totalMacros * 100).roundToInt() else 0
    val fatPercent = if (totalMacros > 0) (currentFat.toFloat() / totalMacros * 100).roundToInt() else 0

    // Các chỉ số phụ giả lập (tính theo tỷ lệ gram để giao diện đồng bộ)
    val fiber = (3.2f * ratio).roundToInt()
    val sugar = (1.5f * ratio).roundToInt()
    val sodium = (120 * ratio).roundToInt()
    val potassium = (250 * ratio).roundToInt()

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppBackground)
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Text(
                    text = "Chi tiết món ăn",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { /* Thêm vào yêu thích */ }) {
                    Icon(Icons.Default.FavoriteBorder, contentDescription = "Favorite", tint = TextPrimary)
                }
            }
        },
        bottomBar = {
            Surface(
                color = AppBackground,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        if (gram > 0) {
                            // Tạo một bản sao của FoodItem với các chỉ số đã tính toán lại theo gram
                            val adjustedFood = food.copy(
                                name = food.name,
                                calories = currentCal,
                                protein = currentPro,
                                carbs = currentCarbs,
                                fat = currentFat
                            )
                            onAdd(adjustedFood) // Trả dữ liệu đã tính toán về để lưu vào ViewModel
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppPrimary),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Thêm vào nhật ký", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        },
        containerColor = AppBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .shadow(4.dp, RoundedCornerShape(16.dp))
                    .background(AppSurface, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("🍲", fontSize = 60.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = food.name,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Hiển thị các chỉ số động
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(AppSurface),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { 1f },
                        color = AppPrimary,
                        modifier = Modifier.fillMaxSize(),
                        strokeWidth = 4.dp
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$currentCal", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        Text("Cal", fontSize = 11.sp, color = TextSecondary)
                    }
                }

                MacroDetailItem(value = "${currentPro} g", label = "⚡ CHẤT ĐẠM", percent = "$proPercent%", color = ProteinColor)
                MacroDetailItem(value = "${currentCarbs} g", label = "💧 ĐƯỜNG BỘT", percent = "$carbPercent%", color = CarbsColor)
                MacroDetailItem(value = "${currentFat} g", label = "🛢️ CHẤT BÉO", percent = "$fatPercent%", color = FatColor)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Khu vực nhập số Gram
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppSurface, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text("Khẩu phần tuỳ chỉnh", fontSize = 14.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppBackground, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BasicTextField(
                        value = gramInput,
                        onValueChange = { newValue ->
                            // Chỉ cho phép nhập số (bao gồm cả việc xóa trắng)
                            if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                                gramInput = newValue
                            }
                        },
                        textStyle = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        cursorBrush = SolidColor(AppPrimary),
                        modifier = Modifier.weight(1f)
                    )
                    Text("gram", fontSize = 16.sp, color = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Chi tiết dinh dưỡng phụ động theo số gram
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppSurface, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text("Chi tiết dinh dưỡng", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(12.dp))

                NutritionRow("Chất xơ", "${fiber}g")
                HorizontalDivider(color = AppBackground, thickness = 1.dp, modifier = Modifier.padding(vertical = 10.dp))
                NutritionRow("Đường", "${sugar}g")
                HorizontalDivider(color = AppBackground, thickness = 1.dp, modifier = Modifier.padding(vertical = 10.dp))
                NutritionRow("Natri", "${sodium}mg")
                HorizontalDivider(color = AppBackground, thickness = 1.dp, modifier = Modifier.padding(vertical = 10.dp))
                NutritionRow("Kali", "${potassium}mg")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = AppPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Thông tin từ nguồn Viện Dinh dưỡng Việt Nam",
                    fontSize = 12.sp,
                    color = AppPrimary,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun MacroDetailItem(value: String, label: String, percent: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .background(color.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(percent, fontSize = 10.sp, color = color, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(modifier = Modifier.height(2.dp))
        Text(label, fontSize = 10.sp, color = TextSecondary)
    }
}

@Composable
fun NutritionRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp, color = TextSecondary)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
    }
}

@Preview(showBackground = true)
@Composable
fun FoodDetailScreenPreview() {
    FoodDetailScreen(
        food = FoodItem("Bún, tươi", 112, 2, 26, 0),
        onBack = {},
        onAdd = {}
    )
}