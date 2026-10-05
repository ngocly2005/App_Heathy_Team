package com.app_health.app.feature.home.components.Home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app_health.app.core.designsystem.components.AppBottomNavigation
import com.app_health.app.core.designsystem.components.BottomNavRoute
import com.app_health.app.core.utils.NutritionCalculator
import com.app_health.app.core.utils.WeightManager
import com.app_health.app.feature.home.components.Home.Activities.ActivityDetailScreen
import com.app_health.app.feature.home.components.Home.Activities.TrainingActivites
import com.app_health.app.feature.home.components.Home.Water.WaterCalculator
import com.app_health.app.feature.home.components.Home.Water.WaterIntakeCard
import com.app_health.app.feature.home.components.Home.Water.WaterTargetScreen
import com.app_health.app.feature.home.components.LoginLogout.DataAuthManager
import com.app_health.app.feature.home.components.LoginLogout.CurrentUserManager
import com.app_health.app.feature.home.components.model.ActivityRecord
import java.time.LocalDate
import java.time.YearMonth

import com.app_health.app.feature.home.components.model.HealthViewModel
import com.app_health.app.ui.theme.*

@Composable
fun HomeTabContent(
    viewModel: HealthViewModel,
    currentRoute: BottomNavRoute = BottomNavRoute.HOME,
    onNavigate: (BottomNavRoute) -> Unit = {},
    onNavigateToSelectFood: () -> Unit = {},
    onNavigateToCaptureFood: () -> Unit = {}
) {
    val context = LocalContext.current

    val currentUserManager = remember { CurrentUserManager(context) }
    val currentEmail by currentUserManager.currentEmailFlow.collectAsState(initial = null)
    val authManager = remember(currentEmail) { DataAuthManager(context, currentEmail) }
    val weightManager = remember(currentEmail) { WeightManager(context) }
    val streakManager = remember(currentEmail) { StreakManager(context, currentEmail) }
    val streakCount by streakManager.streakCountFlow.collectAsState(initial = 0)
    LaunchedEffect(currentEmail) {
        if (currentEmail != null) {
            streakManager.checkAndUpdateStreak()
        }
    }
    val userName by authManager.savedName.collectAsState(initial = "Người dùng mới")
    val userAge by authManager.savedAge.collectAsState(initial = 0)
    val userHeight by authManager.savedHeight.collectAsState(initial = 0f)
    val userGender by authManager.savedGender.collectAsState(initial = "")
    val userActivityLevel by authManager.savedActivityLevel.collectAsState(initial = "")
    val userWeight by weightManager.currentWeightFlow.collectAsState(initial = 0f)

    // Đọc thông số tùy chỉnh Calo & Macro từ DataStore
    val customCal by authManager.savedCustomCalories.collectAsState(initial = 0f)
    val customProtein by authManager.savedProteinPercent.collectAsState(initial = 0f)
    val customCarbs by authManager.savedCarbsPercent.collectAsState(initial = 0f)
    val customFat by authManager.savedFatPercent.collectAsState(initial = 0f)

    // Kiểm tra xem hồ sơ đã được cập nhật tối thiểu (cân nặng, chiều cao, tuổi) chưa
    val isProfileUpdated = userWeight > 0f && userHeight > 0f && userAge > 0

    // Tính toán dinh dưỡng động CHỈ KHI ĐÃ CẬP NHẬT HỒ SƠ
    val nutritionGoals = remember(
        isProfileUpdated, userWeight, userHeight, userAge, userGender, userActivityLevel,
        customCal, customProtein, customCarbs, customFat
    ) {
        if (isProfileUpdated) {
            NutritionCalculator.calculate(
                weight = userWeight,
                height = userHeight,
                age = userAge,
                gender = userGender,
                activityLevel = userActivityLevel,
                customCalories = customCal,
                proteinPct = if(customProtein > 0) customProtein else 25f,
                carbsPct = if(customCarbs > 0) customCarbs else 50f,
                fatPct = if(customFat > 0) customFat else 25f
            )
        } else {
            com.app_health.app.core.utils.NutritionGoals(
                bmr = 0f,
                tdee = 0f,
                targetCalories = 0f,
                proteinPercent = 0f,
                carbsPercent = 0f,
                fatPercent = 0f,
                proteinGrams = 0,
                carbsGrams = 0,
                fatGrams = 0
            )
        }
    }

    val targetCalories = if (nutritionGoals.targetCalories > 0f) nutritionGoals.targetCalories.toInt() else 0

    val loggedFoodMap by viewModel.loggedFoodMap.collectAsState()
    val loggedActivityMap by viewModel.loggedActivityMap.collectAsState()

    // Đọc dữ liệu nước uống vĩnh viễn từ ViewModel theo ngày được chọn
    val loggedWaterMap by viewModel.loggedWaterMap.collectAsState()

    val scrollState = rememberScrollState()
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showMonthCalendar by remember { mutableStateOf(false) }

    var selectedActivity by remember { mutableStateOf<String?>(null) }

    // Lấy lượng nước theo ngày từ ViewModel, mặc định là 0 nếu chưa có
    val currentWaterMl = loggedWaterMap["$selectedDate"] ?: 0
    var waterStepVolume by remember { mutableIntStateOf(200) }
    var showWaterWarning by remember { mutableStateOf(false) }

    // Tính toán mục tiêu nước động (tính bằng ml) CHỈ KHI ĐÃ CẬP NHẬT HỒ SƠ
    val calculatedWaterGoalLiters = remember(isProfileUpdated, userWeight, userActivityLevel) {
        if (isProfileUpdated) {
            WaterCalculator.calculateTargetLiters(userWeight, userActivityLevel)
        } else {
            0f
        }
    }
    val targetWaterMl = (calculatedWaterGoalLiters * 1000).toInt()

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            AppPrimary.copy(alpha = 0.15f),
            AppBackground
        ),
        startY = 0f,
        endY = 1200f
    )

    val timeSlots = (0..23).map { String.format("%02d:00", it) }

    val dailyFoods = remember(selectedDate, loggedFoodMap) {
        timeSlots.flatMap { time ->
            loggedFoodMap["${selectedDate}_$time"] ?: emptyList()
        }
    }

    val dailyConsumedCalories = dailyFoods.sumOf { it.calories }
    val dailyProtein = dailyFoods.sumOf { it.protein }
    val dailyCarbs = dailyFoods.sumOf { it.carbs }
    val dailyFat = dailyFoods.sumOf { it.fat }

    // Tính tổng calo tiêu hao do tập luyện trong ngày được chọn
    val dailyActivities = loggedActivityMap["$selectedDate"] ?: emptyList()
    val dailyBurnedCalories = dailyActivities.sumOf { it.calories }

    val isOverCalories = targetCalories > 0 && dailyConsumedCalories > targetCalories

    if (selectedActivity == "WaterSettings") {
        WaterTargetScreen(
            currentGoalLiters = calculatedWaterGoalLiters,
            currentVolumeOption = waterStepVolume,
            onBack = { selectedActivity = null },
            onSave = { _: Float, volumeStep: Int ->
                waterStepVolume = volumeStep
                selectedActivity = null
            }
        )
    } else if (selectedActivity == "List") {
        ActivitySelectScreen(
            onBack = { selectedActivity = null },
            onActivitySelected = { activityName ->
                selectedActivity = activityName
            }
        )
    } else if (selectedActivity != null) {
        ActivityDetailScreen(
            activityName = selectedActivity!!,
            onBack = {
                selectedActivity = null
            },
            onSave = { param, time, calories ->
                val burnedCal = calories.toIntOrNull() ?: 0
                if (burnedCal > 0) {
                    val record = ActivityRecord(
                        name = selectedActivity!!,
                        param = param,
                        time = time,
                        calories = burnedCal
                    )
                    viewModel.addActivityToDate("$selectedDate", record)
                }
                selectedActivity = null
            }
        )
    } else {
        Scaffold(
            bottomBar = {
                AppBottomNavigation(
                    currentRoute = currentRoute,
                    onRouteSelected = { route -> onNavigate(route) },
                    onNavigateToSelectFood = onNavigateToSelectFood,
                    onNavigateToCaptureFood = onNavigateToCaptureFood
                )
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(brush = backgroundGradient)
                    .padding(bottom = innerPadding.calculateBottomPadding())
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp)
                    .padding(top = 20.dp, bottom = 20.dp)
            ) {
                HomeHeader(
                    streakCount = streakCount,
                    userName = userName ?: "Người dùng mới",
                    onCalendarClick = { showMonthCalendar = true }
                )
                Spacer(modifier = Modifier.height(28.dp))

                WeeklyCalendarSwipeable(
                    selectedDate = selectedDate,
                    onDateSelected = { selectedDate = it }
                )
                Spacer(modifier = Modifier.height(32.dp))

                // NẾU CHƯA CẬP NHẬT HỒ SƠ -> HIỂN THỊ BANNER NHẮC NHỞ
                if (!isProfileUpdated) {
                    UpdateProfileBanner(onClick = { onNavigate(BottomNavRoute.ACCOUNT) })
                    Spacer(modifier = Modifier.height(24.dp))
                } else {
                    CalorieTargetHeader()
                    Spacer(modifier = Modifier.height(24.dp))

                    CalorieRing(
                        consumed = dailyConsumedCalories,
                        target = targetCalories,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(28.dp))

                    WarningBox(isOver = isOverCalories)
                    Spacer(modifier = Modifier.height(20.dp))
                }

                MacrosCard(
                    protein = dailyProtein,
                    carbs = dailyCarbs,
                    fat = dailyFat,
                    goals = nutritionGoals // nutritionGoals sẽ là 0 nếu chưa cập nhật
                )
                Spacer(modifier = Modifier.height(25.dp))

                RecentDiary(
                    foods = dailyFoods.take(2),
                    onTitleClick = { onNavigate(BottomNavRoute.DIARY) }
                )
                Spacer(modifier = Modifier.height(25.dp))

                StatsRow(
                    consumed = dailyConsumedCalories,
                    target = targetCalories,
                    burned = dailyBurnedCalories
                )
                Spacer(modifier = Modifier.height(25.dp))

                TrainingActivites(
                    onActivityClick = { activityName ->
                        if (activityName == "Hoạt động khác") {
                            selectedActivity = "List"
                        } else {
                            selectedActivity = when (activityName) {
                                "Chạy bộ" -> "Chạy bộ (Running)"
                                "Bơi lội" -> "Bơi lội (Swimming)"
                                "Cầu lông" -> "Cầu lông (Badminton)"
                                "Gym" -> "Gym (Thể hình)"
                                else -> activityName
                            }
                        }
                    }
                )
                Spacer(modifier = Modifier.height(25.dp))

                WaterIntakeCard(
                    currentWaterMl = currentWaterMl,
                    targetWaterMl = if(isProfileUpdated) targetWaterMl else 2000, // Hiển thị 2000ml tạm thời nếu chưa có hồ sơ
                    onIncrease = {
                        val newWater = currentWaterMl + waterStepVolume
                        viewModel.updateWaterForDate("$selectedDate", newWater)
                        if (isProfileUpdated && newWater > targetWaterMl) {
                            showWaterWarning = true
                        }
                    },
                    onDecrease = {
                        val newWater = if (currentWaterMl >= waterStepVolume) currentWaterMl - waterStepVolume else 0
                        viewModel.updateWaterForDate("$selectedDate", newWater)
                    },
                    onCardClick = {
                        selectedActivity = "WaterSettings"
                    }
                )
                Spacer(modifier = Modifier.height(25.dp))
            }
        }
    }

    if (showWaterWarning) {
        AlertDialog(
            onDismissRequest = { showWaterWarning = false },
            title = { Text("Lưu ý", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Bạn đã uống vượt quá số lượng nước mục tiêu (${targetWaterMl}ml)...", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = { showWaterWarning = false }) {
                    Text("Đã hiểu", color = AppPrimary, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = AppSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showMonthCalendar) {
        MonthCalendarDialog(
            initialMonth = YearMonth.from(selectedDate),
            selectedDate = selectedDate,
            onDateSelected = { date ->
                selectedDate = date
                showMonthCalendar = false
            },
            onDismiss = { showMonthCalendar = false }
        )
    }
}

// Component nhắc nhở cập nhật hồ sơ
@Composable
fun UpdateProfileBanner(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        color = Color(0xFFFFF3C4), // Màu nền vàng nhạt cảnh báo
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Cập nhật hồ sơ!",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE65100),
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Vui lòng cập nhật chiều cao, cân nặng và thông tin để tính toán mục tiêu Calo của bạn.",
                    fontSize = 13.sp,
                    color = Color(0xFFE65100)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE65100).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Đi tới Account",
                    tint = Color(0xFFE65100),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun HomeHeader(streakCount: Int, userName: String, onCalendarClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = userName,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .background(AppSurface, RoundedCornerShape(24.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .shadow(elevation = 1.dp, shape = RoundedCornerShape(24.dp))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = "Streak",
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$streakCount",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            }

            VerticalDivider(
                modifier = Modifier.height(16.dp).width(1.dp),
                color = TextSecondary.copy(alpha = 0.2f)
            )

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable { onCalendarClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Calendar",
                    tint = AppPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}