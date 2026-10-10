package com.app_health.app.feature.home.components.Plan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app_health.app.core.designsystem.components.AppBottomNavigation
import com.app_health.app.core.designsystem.components.BottomNavRoute
import com.app_health.app.core.utils.NutritionCalculator
import com.app_health.app.core.utils.WeightManager
import com.app_health.app.feature.home.components.FoodSelectContent
import com.app_health.app.feature.home.model.FoodItem
import com.app_health.app.feature.home.model.FoodDatabase
import com.app_health.app.feature.home.components.model.HealthViewModel
import com.app_health.app.feature.home.components.LoginLogout.CurrentUserManager
import com.app_health.app.feature.home.components.LoginLogout.DataAuthManager
import com.app_health.app.ui.theme.*
import java.time.LocalDate

@Composable
fun MenuScreen(
    viewModel: HealthViewModel,
    currentRoute: BottomNavRoute = BottomNavRoute.PLAN,
    onNavigate: (BottomNavRoute) -> Unit = {},
    onNavigateToSelectFood: () -> Unit = {},
    onNavigateToCaptureFood: () -> Unit = {}
) {
    val context = LocalContext.current
    val currentUserManager = remember { CurrentUserManager(context) }
    val currentEmail by currentUserManager.currentEmailFlow.collectAsState(initial = null)
    val authManager = remember(currentEmail) { DataAuthManager(context, currentEmail) }
    val weightManager = remember { WeightManager(context) }

    val userAge by authManager.savedAge.collectAsState(initial = 22)
    val userHeight by authManager.savedHeight.collectAsState(initial = 166f)
    val userGender by authManager.savedGender.collectAsState(initial = "Nữ")
    val userActivityLevel by authManager.savedActivityLevel.collectAsState(initial = "Chăm chỉ tập luyện")
    val userWeight by weightManager.currentWeightFlow.collectAsState(initial = 89f)

    val customCal by authManager.savedCustomCalories.collectAsState(initial = 0f)
    val customProtein by authManager.savedProteinPercent.collectAsState(initial = 25f)
    val customCarbs by authManager.savedCarbsPercent.collectAsState(initial = 50f)
    val customFat by authManager.savedFatPercent.collectAsState(initial = 25f)

    val nutritionGoals = remember(
        userWeight, userHeight, userAge, userGender, userActivityLevel,
        customCal, customProtein, customCarbs, customFat
    ) {
        NutritionCalculator.calculate(
            weight = userWeight,
            height = userHeight,
            age = userAge,
            gender = userGender,
            activityLevel = userActivityLevel,
            customCalories = customCal,
            proteinPct = customProtein,
            carbsPct = customCarbs,
            fatPct = customFat
        )
    }

    val targetCalories = if (nutritionGoals.targetCalories > 0f) nutritionGoals.targetCalories.toInt() else 2000

    var mealToAddTo by remember { mutableStateOf<String?>(null) }

    // Xác định ngày hôm nay và thứ hiện tại để làm mặc định
    val today = remember { LocalDate.now() }
    val todayString = getDayOfWeekString(today)
    var currentDayForAdd by remember { mutableStateOf(todayString) }

    val plannedFoodsMap by viewModel.plannedFoodMap.collectAsState()

    // [ĐÃ SỬA] Lấy thêm danh sách recentFoods từ ViewModel
    val recentFoods by viewModel.recentFoods.collectAsState()

    // Kiểm tra xem ngày đang chọn có phải là ngày trong quá khứ so với hôm nay không
    val isPastDay = isSelectedDayInPast(currentDayForAdd, today)

    if (mealToAddTo != null) {
        FoodSelectContent(
            foodList = FoodDatabase.allFoods,
            customFoods = viewModel.customFoods,
            recentFoods = recentFoods, // [ĐÃ SỬA] Truyền tham số bị thiếu vào đây
            onSaveCustomFood = { newFood ->
                viewModel.addCustomFood(newFood)
            },
            onBack = { mealToAddTo = null },
            onFoodClick = { food ->
                val actualDateStr = getActualDateFromDayString(currentDayForAdd, today).toString()
                val key = "$actualDateStr-$mealToAddTo"

                viewModel.addFoodToPlan(key, food)
                mealToAddTo = null
            }
        )
    } else {
        Scaffold(
            containerColor = AppBackground,
            bottomBar = {
                AppBottomNavigation(
                    currentRoute = currentRoute,
                    onRouteSelected = { route -> onNavigate(route) },
                    onNavigateToSelectFood = onNavigateToSelectFood,
                    onNavigateToCaptureFood = onNavigateToCaptureFood
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Text(
                    text = "Kế hoạch Thực đơn",
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 8.dp)
                )

                WeeklyPlanHeader(
                    selectedDay = currentDayForAdd,
                    today = today,
                    onDaySelected = { currentDayForAdd = it },
                    plannedFoodsMap = plannedFoodsMap,
                    targetCalories = targetCalories,
                    isPastDay = isPastDay,
                    onAddFoodClick = { mealName ->
                        if (!isPastDay) {
                            mealToAddTo = mealName
                        }
                    }
                )
            }
        }
    }
}

// Hàm hỗ trợ chuyển LocalDate thành ký hiệu "T2", "T3",..."CN"
fun getDayOfWeekString(date: LocalDate): String {
    return when (date.dayOfWeek) {
        java.time.DayOfWeek.MONDAY -> "T2"
        java.time.DayOfWeek.TUESDAY -> "T3"
        java.time.DayOfWeek.WEDNESDAY -> "T4"
        java.time.DayOfWeek.THURSDAY -> "T5"
        java.time.DayOfWeek.FRIDAY -> "T6"
        java.time.DayOfWeek.SATURDAY -> "T7"
        java.time.DayOfWeek.SUNDAY -> "CN"
    }
}

// Hàm kiểm tra xem thứ được chọn có nằm trong quá khứ so với hôm nay không
fun isSelectedDayInPast(selectedDayStr: String, today: LocalDate): Boolean {
    val daysOfWeek = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
    val todayStr = getDayOfWeekString(today)
    val todayIndex = daysOfWeek.indexOf(todayStr)
    val selectedIndex = daysOfWeek.indexOf(selectedDayStr)

    return selectedIndex < todayIndex
}

// Hàm hỗ trợ quy đổi từ chữ "T2", "T3"... về ngày tháng năm cụ thể (LocalDate) trong tuần hiện tại
fun getActualDateFromDayString(dayStr: String, today: LocalDate): LocalDate {
    val daysOfWeek = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
    val todayStr = getDayOfWeekString(today)

    val todayIndex = daysOfWeek.indexOf(todayStr)
    val targetIndex = daysOfWeek.indexOf(dayStr)

    // Tính khoảng cách ngày giữa thứ được chọn và hôm nay
    val daysDifference = targetIndex - todayIndex

    // Cộng hoặc trừ số ngày tương ứng để ra đúng LocalDate của thứ đó trong tuần này
    return today.plusDays(daysDifference.toLong())
}

@Composable
fun WeeklyPlanHeader(
    selectedDay: String,
    today: LocalDate,
    onDaySelected: (String) -> Unit,
    plannedFoodsMap: Map<String, List<FoodItem>>,
    targetCalories: Int,
    isPastDay: Boolean,
    onAddFoodClick: (String) -> Unit
) {
    val daysOfWeek = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
    val meals = listOf("Bữa sáng", "Bữa trưa", "Bữa tối", "Bữa phụ")

    // Tính chuỗi ngày thực tế cho Tab thứ đang được chọn (VD: 2026-10-12)
    val actualDateStr = getActualDateFromDayString(selectedDay, today).toString()

    val totalConsumedCalories = meals.sumOf { mealName ->
        val key = "$actualDateStr-$mealName"
        val foods = plannedFoodsMap[key] ?: emptyList()
        foods.sumOf { it.calories }
    }

    val isOverLimit = totalConsumedCalories > targetCalories

    Column(modifier = Modifier.fillMaxSize()) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            items(daysOfWeek) { day ->
                val isSelected = day == selectedDay
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onDaySelected(day) }
                        .padding(horizontal = 8.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) AppPrimary else Color.Transparent)
                    ) {
                        Text(
                            text = day,
                            color = if (isSelected) Color.White else TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Kế hoạch $selectedDay", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)

            Text(
                text = "Đã nạp: $totalConsumedCalories / $targetCalories cal",
                color = if (isOverLimit) Color.Red else TextSecondary,
                fontSize = 14.sp,
                fontWeight = if (isOverLimit) FontWeight.Bold else FontWeight.Normal
            )
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(meals) { mealName ->
                val key = "$actualDateStr-$mealName"
                val foodsInMeal = plannedFoodsMap[key] ?: emptyList()

                MealPlanCard(
                    mealName = mealName,
                    foods = foodsInMeal,
                    isPastDay = isPastDay,
                    onAddClick = { onAddFoodClick(mealName) }
                )
            }
        }
    }
}

@Composable
fun MealPlanCard(
    mealName: String,
    foods: List<FoodItem>,
    isPastDay: Boolean,
    onAddClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AppSurface,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.RestaurantMenu, contentDescription = null, tint = AppPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = mealName,
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (!isPastDay) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(AppPrimaryLight)
                            .clickable { onAddClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = AppPrimary, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (foods.isEmpty()) {
                Text(
                    text = "Chưa có món ăn nào được lên lịch.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    foods.forEach { food ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = food.name, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Text(
                                    text = "${food.calories} cal • 1 phần",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        HorizontalDivider(color = AppBackground, thickness = 1.dp)
                    }
                }
            }
        }
    }
}