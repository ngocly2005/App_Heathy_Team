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
import com.app_health.app.feature.home.components.Home.Activities.ActivityDetailScreen
import com.app_health.app.feature.home.components.Home.Activities.TrainingActivites
import com.app_health.app.feature.home.components.Home.Water.WaterIntakeCard
import com.app_health.app.feature.home.components.Home.Water.WaterTargetScreen
import com.app_health.app.feature.home.components.LoginLogout.DataAuthManager
import com.app_health.app.feature.home.components.LoginLogout.CurrentUserManager
import com.app_health.app.feature.home.components.model.ActivityRecord
import com.app_health.app.feature.home.components.model.HealthViewModel
import com.app_health.app.ui.theme.*
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun HomeTabContent(
    viewModel: HealthViewModel,
    currentRoute: BottomNavRoute = BottomNavRoute.HOME,
    onNavigate: (BottomNavRoute) -> Unit = {},
    onNavigateToSelectFood: () -> Unit = {},
    onNavigateToCaptureFood: () -> Unit = {}
) {
    val context = LocalContext.current

    // Quản lý phiên đăng nhập và tên người dùng
    val currentUserManager = remember { CurrentUserManager(context) }
    val currentEmail by currentUserManager.currentEmailFlow.collectAsState(initial = null)
    val authManager = remember(currentEmail) { DataAuthManager(context, currentEmail) }
    val userName by authManager.savedName.collectAsState(initial = "Người dùng mới")

    val streakManager = remember(currentEmail) { StreakManager(context, currentEmail) }
    val streakCount by streakManager.streakCountFlow.collectAsState(initial = 0)

    LaunchedEffect(currentEmail) {
        if (currentEmail != null) {
            streakManager.checkAndUpdateStreak()
        }
    }

    // ==========================================================
    // "TRÁI TIM" CỦA GIAO DIỆN: Lấy toàn bộ dữ liệu từ ViewModel
    // ==========================================================
    val uiState by viewModel.homeUiState.collectAsState()

    // Cần giữ biến ngày này để điều khiển UI của các pop-up lịch
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    // Các biến phụ trợ cho hiển thị UI
    val scrollState = rememberScrollState()
    var showMonthCalendar by remember { mutableStateOf(false) }
    var selectedActivity by remember { mutableStateOf<String?>(null) }
    var waterStepVolume by remember { mutableIntStateOf(200) }
    var showWaterWarningUI by remember { mutableStateOf(false) }

    // Đồng bộ cảnh báo nước từ ViewModel
    LaunchedEffect(uiState.showWaterWarning) {
        if (uiState.showWaterWarning) {
            showWaterWarningUI = true
        }
    }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            AppPrimary.copy(alpha = 0.15f),
            AppBackground
        ),
        startY = 0f,
        endY = 1200f
    )

    // ==========================================================
    // ĐIỀU HƯỚNG CÁC POP-UP VÀ MÀN HÌNH CHI TIẾT
    // ==========================================================
    if (selectedActivity == "WaterSettings") {
        WaterTargetScreen(
            currentGoalLiters = uiState.targetWaterMl / 1000f,
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

        // ==========================================================
        // GIAO DIỆN CHÍNH CỦA TRANG CHỦ
        // ==========================================================
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

                // Thanh Lịch Vuốt Ngang
                WeeklyCalendarSwipeable(
                    selectedDate = selectedDate,
                    onDateSelected = {
                        selectedDate = it // Cập nhật UI
                        viewModel.updateSelectedDate(it) // Báo cho ViewModel tính toán lại
                    }
                )
                Spacer(modifier = Modifier.height(32.dp))

                // HIỂN THỊ BANNER NHẮC NHỞ NẾU CHƯA CÓ HỒ SƠ
                if (!uiState.profile.isProfileUpdated) {
                    UpdateProfileBanner(onClick = { onNavigate(BottomNavRoute.ACCOUNT) })
                    Spacer(modifier = Modifier.height(24.dp))
                } else {
                    // NẾU CÓ HỒ SƠ THÌ HIỆN BIỂU ĐỒ CALO
                    CalorieTargetHeader()
                    Spacer(modifier = Modifier.height(24.dp))

                    CalorieRing(
                        consumed = uiState.dailyConsumedCalories,
                        target = uiState.targetCalories,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(28.dp))

                    WarningBox(isOver = uiState.isOverCalories)
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // CÁC THẺ THÔNG TIN TRUYỀN DỮ LIỆU TỪ uiState XUỐNG
                MacrosCard(
                    protein = uiState.dailyProtein,
                    carbs = uiState.dailyCarbs,
                    fat = uiState.dailyFat,
                    goals = uiState.nutritionGoals
                )
                Spacer(modifier = Modifier.height(25.dp))

                RecentDiary(
                    foods = uiState.consumedFoods.reversed().take(2),
                    onTitleClick = { onNavigate(BottomNavRoute.DIARY) }
                )
                Spacer(modifier = Modifier.height(25.dp))

                StatsRow(
                    consumed = uiState.dailyConsumedCalories,
                    target = uiState.targetCalories,
                    burned = uiState.dailyBurnedCalories
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
                    currentWaterMl = uiState.currentWaterMl,
                    targetWaterMl = if(uiState.profile.isProfileUpdated) uiState.targetWaterMl else 2000,
                    onIncrease = {
                        val newWater = uiState.currentWaterMl + waterStepVolume
                        viewModel.updateWaterForDate("$selectedDate", newWater)
                    },
                    onDecrease = {
                        val newWater = if (uiState.currentWaterMl >= waterStepVolume) uiState.currentWaterMl - waterStepVolume else 0
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

    // Hiển thị Cảnh báo nước
    if (showWaterWarningUI) {
        AlertDialog(
            onDismissRequest = { showWaterWarningUI = false },
            title = { Text("Lưu ý", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Bạn đã uống vượt quá số lượng nước mục tiêu (${uiState.targetWaterMl}ml)...", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = { showWaterWarningUI = false }) {
                    Text("Đã hiểu", color = AppPrimary, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = AppSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Hiển thị Lịch popup chọn tháng
    if (showMonthCalendar) {
        MonthCalendarDialog(
            initialMonth = YearMonth.from(selectedDate),
            selectedDate = selectedDate,
            onDateSelected = { date ->
                selectedDate = date // Cập nhật UI
                viewModel.updateSelectedDate(date) // Gọi ViewModel
                showMonthCalendar = false
            },
            onDismiss = { showMonthCalendar = false }
        )
    }
}

// ==========================================================
// CÁC COMPONENT PHỤ TRỢ BÊN DƯỚI GIỮ NGUYÊN
// ==========================================================

@Composable
fun UpdateProfileBanner(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        color = Color(0xFFFFF3C4),
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