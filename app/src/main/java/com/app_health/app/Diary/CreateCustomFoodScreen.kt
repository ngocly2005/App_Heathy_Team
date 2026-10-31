package com.app_health.app.feature.home.components.Diary

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app_health.app.feature.home.components.Home.MacrosCard
import com.app_health.app.feature.home.model.FoodItem
import com.app_health.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCustomFoodScreen(
    onBack: () -> Unit,
    onSaveClick: (FoodItem) -> Unit
) {
    // State lưu trữ dữ liệu
    var foodName by remember { mutableStateOf("") }
    var ingredients by remember { mutableStateOf("") } // Ô nhập thành phần (trứng, dầu ăn...)
    var quantityStr by remember { mutableStateOf("100") }

    // State cho Dropdown chọn Đơn vị
    var unitExpanded by remember { mutableStateOf(false) }
    var selectedUnit by remember { mutableStateOf("g") }
    val unitOptions = listOf("g", "ml", "phần", "bát")

    var caloriesStr by remember { mutableStateOf("0") }
    var proteinStr by remember { mutableStateOf("0") }
    var carbsStr by remember { mutableStateOf("0") }
    var fatStr by remember { mutableStateOf("0") }

    val quantity = quantityStr.toFloatOrNull() ?: 0f

    // Hiệu ứng tự động tính toán dinh dưỡng khi "Thành phần" hoặc "Số lượng" thay đổi
    LaunchedEffect(ingredients, quantity, selectedUnit) {
        val (cal, p, c, f) = autoCalculateNutrition(ingredients, quantity, selectedUnit)
        if (cal > 0 || ingredients.isNotBlank()) {
            caloriesStr = cal.toString()
            proteinStr = p.toString()
            carbsStr = c.toString()
            fatStr = f.toString()
        }
    }

    val calories = caloriesStr.toIntOrNull() ?: 0
    val protein = proteinStr.toIntOrNull() ?: 0
    val carbs = carbsStr.toIntOrNull() ?: 0
    val fat = fatStr.toIntOrNull() ?: 0

    val isSaveEnabled = foodName.isNotBlank() && caloriesStr.isNotBlank() && caloriesStr != "0"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tạo thực phẩm", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppBackground,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
                )
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppBackground)
                    .padding(16.dp)
            ) {
                Button(
                    onClick = {
                        val newFood = FoodItem(
                            name = foodName,
                            calories = calories,
                            protein = protein,
                            carbs = carbs,
                            fat = fat
                        )
                        onSaveClick(newFood)
                    },
                    enabled = isSaveEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppPrimary,
                        disabledContainerColor = Color.LightGray,
                        contentColor = Color.White,
                        disabledContentColor = Color.DarkGray
                    )
                ) {
                    Text(
                        text = "Lưu thực phẩm",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // 1. Tên món ăn
            Column {
                Text("Tên món ăn (*)", color = TextSecondary, fontSize = 14.sp)
                BasicTextField(
                    value = foodName,
                    onValueChange = { foodName = it },
                    textStyle = TextStyle(color = TextPrimary, fontSize = 16.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
            }

            // 2. Thành phần (Nhập tự do)
            Column {
                Text("Thành phần (tự động tính calo)", color = TextSecondary, fontSize = 14.sp)
                BasicTextField(
                    value = ingredients,
                    onValueChange = { ingredients = it },
                    textStyle = TextStyle(color = TextPrimary, fontSize = 16.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    decorationBox = { innerTextField ->
                        if (ingredients.isEmpty()) {
                            Text("Ví dụ: 2 trứng, 10g dầu ăn, cơm...", color = Color.Gray, fontSize = 16.sp)
                        }
                        innerTextField()
                    }
                )
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 3. Khối lượng & Đơn vị (Dropdown)
            Text("Giá trị dinh dưỡng được tính cho", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Khối lượng / Số lượng *", color = TextSecondary, fontSize = 15.sp)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    BasicTextField(
                        value = quantityStr,
                        onValueChange = { quantityStr = it },
                        textStyle = TextStyle(color = TextPrimary, fontSize = 16.sp, textAlign = TextAlign.End),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(60.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    // Dropdown Menu cho Đơn vị
                    Box {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = AppSurface,
                            modifier = Modifier
                                .clickable { unitExpanded = true }
                                .padding(vertical = 8.dp, horizontal = 12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(selectedUnit, color = TextPrimary, fontSize = 14.sp)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextPrimary)
                            }
                        }
                        DropdownMenu(
                            expanded = unitExpanded,
                            onDismissRequest = { unitExpanded = false },
                            modifier = Modifier.background(AppSurface)
                        ) {
                            unitOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option, color = TextPrimary) },
                                    onClick = {
                                        selectedUnit = option
                                        unitExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

            Spacer(modifier = Modifier.height(8.dp))
            Text("Giá trị dinh dưỡng", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)

            // 4. Các hàng nhập liệu Macros
            NutrientInputRow("Năng lượng *", caloriesStr, "cal") { caloriesStr = it }
            NutrientInputRow("Đường bột", carbsStr, "gram") { carbsStr = it }
            NutrientInputRow("Chất đạm", proteinStr, "gram") { proteinStr = it }
            NutrientInputRow("Chất béo", fatStr, "gram") { fatStr = it }

            Spacer(modifier = Modifier.height(16.dp))

        }
    }
}

// Composable dùng chung cho các hàng nhập liệu dinh dưỡng (Giống UI mẫu)
@Composable
fun NutrientInputRow(
    label: String,
    value: String,
    unit: String,
    onValueChange: (String) -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = TextSecondary, fontSize = 15.sp)

            Row(verticalAlignment = Alignment.CenterVertically) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = TextStyle(color = TextPrimary, fontSize = 16.sp, textAlign = TextAlign.End),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(60.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(unit, color = TextPrimary, fontSize = 14.sp, modifier = Modifier.width(40.dp), textAlign = TextAlign.End)
            }
        }
        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
    }
}

// Hàm mô phỏng logic bóc tách ngôn ngữ (NLP) để tự động tính Calo
// Trong thực tế, bạn cần gọi API (như Edamam, Nutritionix) hoặc dùng Database cục bộ
fun autoCalculateNutrition(ingredients: String, quantity: Float, unit: String): NutritionResult {
    var totalCal = 0f
    var totalPro = 0f
    var totalCarb = 0f
    var totalFat = 0f

    val text = ingredients.lowercase()

    // Một số dữ liệu giả lập (Tính trên 100g hoặc 1 phần chuẩn)
    if (text.contains("trứng")) {
        totalCal += 155f; totalPro += 13f; totalCarb += 1.1f; totalFat += 11f
    }
    if (text.contains("dầu ăn")) {
        totalCal += 884f; totalPro += 0f; totalCarb += 0f; totalFat += 100f
    }
    if (text.contains("cơm")) {
        totalCal += 130f; totalPro += 2.7f; totalCarb += 28f; totalFat += 0.3f
    }
    if (text.contains("thịt bò")) {
        totalCal += 250f; totalPro += 26f; totalCarb += 0f; totalFat += 15f
    }

    // Nếu không có thành phần nào khớp, trả về 0
    if (totalCal == 0f) return NutritionResult(0, 0, 0, 0)

    // Áp dụng hệ số tính toán theo Số lượng người dùng nhập
    val multiplier = if (unit == "g" || unit == "ml") quantity / 100f else quantity

    return NutritionResult(
        cal = (totalCal * multiplier).toInt(),
        pro = (totalPro * multiplier).toInt(),
        carb = (totalCarb * multiplier).toInt(),
        fat = (totalFat * multiplier).toInt()
    )
}

data class NutritionResult(val cal: Int, val pro: Int, val carb: Int, val fat: Int)