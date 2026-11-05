package com.app_health.app.feature.home.components.Account

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app_health.app.core.designsystem.components.AppBottomNavigation
import com.app_health.app.core.designsystem.components.BottomNavRoute
import com.app_health.app.core.utils.NutritionGoals
import com.app_health.app.core.utils.WeightManager
import com.app_health.app.feature.home.components.LoginLogout.CurrentUserManager
import com.app_health.app.feature.home.components.LoginLogout.DataAuthManager
import com.app_health.app.feature.home.components.model.HealthViewModel
import com.app_health.app.ui.theme.*
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun AccountScreen(
    viewModel: HealthViewModel,
    currentRoute: BottomNavRoute = BottomNavRoute.ACCOUNT,
    onNavigate: (BottomNavRoute) -> Unit = {},
    onNavigateToUpdateMetrics: () -> Unit = {},
    onNavigateToPhysicalProfile: () -> Unit = {},
    onNavigateToCustomGoal: () -> Unit = {},
    onLogout: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val currentUserManager = remember { CurrentUserManager(context) }
    val currentEmail by currentUserManager.currentEmailFlow.collectAsState(initial = null)

    val authManager = remember(currentEmail) { DataAuthManager(context, currentEmail) }
    val weightManager = remember(currentEmail) { WeightManager(context, currentEmail) }

    val userName by authManager.savedName.collectAsState(initial = "Đang tải...")

    val initialWeight by weightManager.initialWeightFlow.collectAsState(initial = 0f)
    val targetWeight by weightManager.targetWeightFlow.collectAsState(initial = 0f)
    val weightHistory by weightManager.weightHistoryFlow.collectAsState(initial = emptyList())

    var showHistoryDialog by remember { mutableStateOf(false) }
    var showQuickUpdateDialog by remember { mutableStateOf(false) }
    var showSettingsScreen by remember { mutableStateOf(false) }

    val uiState by viewModel.homeUiState.collectAsState()
    val profile = uiState.profile
    val nutritionGoals = uiState.nutritionGoals

    if (showSettingsScreen) {
        SettingScreen(
            name = userName ?: "Người dùng",
            email = currentEmail ?: "Chưa cập nhật email",
            age = profile.age,
            height = profile.height,
            weight = profile.weight,
            gender = profile.gender,
            activityLevel = profile.activityLevel,
            onBack = { showSettingsScreen = false },
            onLogout = {
                coroutineScope.launch {
                    currentUserManager.clearSession()
                    showSettingsScreen = false
                    onNavigateToLogin()
                }
            }
        )
    } else {
        Scaffold(
            containerColor = AppBackground,
            bottomBar = {
                AppBottomNavigation(
                    currentRoute = currentRoute,
                    onRouteSelected = { route -> onNavigate(route) },
                    onNavigateToSelectFood = {},
                    onNavigateToCaptureFood = {}
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item { ProfileHeader(name = userName ?: "Người dùng", onSettingsClick = { showSettingsScreen = true }) }
                item { PremiumBanner() }
                item { BasicStatsRow(age = profile.age, height = profile.height, weight = profile.weight, bmi = profile.bmi) }
                item { ActionButtonsRow(onUpdateMetricsClick = onNavigateToUpdateMetrics, onPhysicalProfileClick = onNavigateToPhysicalProfile) }

                item {
                    WeightJourneySection(
                        weight = profile.weight,
                        initialWeight = initialWeight,
                        targetWeight = targetWeight,
                        weightHistory = weightHistory,
                        onHistoryClick = { showHistoryDialog = true },
                        onQuickUpdateClick = { showQuickUpdateDialog = true }
                    )
                }

                item { NutritionGoalsSection(weight = profile.weight, goals = nutritionGoals, onCustomGoalClick = onNavigateToCustomGoal) }
            }
        }
    }

    if (showQuickUpdateDialog) {
        QuickUpdateWeightDialog(
            currentWeight = profile.weight,
            onDismiss = { showQuickUpdateDialog = false },
            onConfirm = { newWeight ->
                coroutineScope.launch {
                    weightManager.updateCurrentWeight(newWeight)
                    showQuickUpdateDialog = false
                }
            }
        )
    }

    if (showHistoryDialog) { WeightHistoryDialog(weightHistory = weightHistory, onDismiss = { showHistoryDialog = false }) }
}

@Composable
fun ProfileHeader(name: String, onSettingsClick: () -> Unit) {
    val firstLetter = if (name.isNotBlank()) name.take(1).uppercase() else "N"
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Hồ sơ cá nhân", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            IconButton(onClick = onSettingsClick) { Icon(Icons.Default.Settings, contentDescription = "Settings", tint = TextSecondary) }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier.size(90.dp).clip(CircleShape).background(AppPrimary),
                contentAlignment = Alignment.Center
            ) { Text(firstLetter, fontSize = 40.sp, color = Color.White, fontWeight = FontWeight.Bold) }
            Box(
                modifier = Modifier.size(28.dp).clip(CircleShape).background(Color.White).padding(2.dp).clip(CircleShape).background(TextPrimary),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.Add, contentDescription = "Add Avatar", tint = Color.White, modifier = Modifier.size(16.dp)) }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text("Đã tham gia ứng dụng", fontSize = 13.sp, color = TextSecondary)
    }
}

@Composable
fun PremiumBanner() {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFFFF3C4)
    ) {}
}

@Composable
fun BasicStatsRow(age: Int, height: Float, weight: Float, bmi: Float) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = AppSurface,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItem(icon = "🧍", value = if (height > 0f) "${height.toInt()} cm" else "---")
            VerticalDivider(modifier = Modifier.height(24.dp), color = TextSecondary.copy(alpha = 0.2f))
            StatItem(icon = "⚖️", value = if (weight > 0f) "$weight kg" else "---")
            VerticalDivider(modifier = Modifier.height(24.dp), color = TextSecondary.copy(alpha = 0.2f))
            StatItem(icon = "📊", value = if (bmi > 0f) String.format(Locale.getDefault(), "BMI %.1f", bmi) else "BMI ---")
        }
    }
}

@Composable
fun StatItem(icon: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(icon, fontSize = 18.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Text(value, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
    }
}

@Composable
fun ActionButtonsRow(onUpdateMetricsClick: () -> Unit, onPhysicalProfileClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onUpdateMetricsClick,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = AppPrimaryLight),
            shape = RoundedCornerShape(12.dp)
        ) { Text("Cập nhật chỉ số", color = AppPrimary, fontWeight = FontWeight.Bold) }
        Button(
            onClick = onPhysicalProfileClick,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = AppPrimaryLight),
            shape = RoundedCornerShape(12.dp)
        ) { Text("Hồ sơ thể chất", color = AppPrimary, fontWeight = FontWeight.Bold) }
    }
}

// ==========================================
// ĐÃ SỬA: LOGIC CẢNH BÁO ĐI NGƯỢC HƯỚNG
// ==========================================
@Composable
fun WeightJourneySection(
    weight: Float,
    initialWeight: Float,
    targetWeight: Float,
    weightHistory: List<Float>,
    onHistoryClick: () -> Unit,
    onQuickUpdateClick: () -> Unit
) {
    val startW = if (initialWeight > 0f) initialWeight else (weightHistory.firstOrNull() ?: weight)
    val diff = weight - startW

    // Kiểm tra xem người dùng đang đi ngược hướng mục tiêu hay không
    val isLosingGoal = targetWeight > 0f && targetWeight < startW
    val isGainingGoal = targetWeight > 0f && targetWeight > startW

    val isWrongDirection = when {
        isLosingGoal && weight > startW -> true // Muốn giảm nhưng lại tăng
        isGainingGoal && weight < startW -> true // Muốn tăng nhưng lại giảm
        else -> false
    }

    val diffText = when {
        diff < 0 -> "Bạn đã giảm được ${String.format(Locale.getDefault(), "%.1f", -diff)} kg!" + if (!isWrongDirection) " 🎉" else ""
        diff > 0 -> "Bạn đã tăng ${String.format(Locale.getDefault(), "%.1f", diff)} kg." + if (!isWrongDirection) " 💪" else ""
        else -> "Bạn đang duy trì ổn định cân nặng."
    }

    // Nếu sai hướng, chuyển chữ thành Đỏ
    val statusColor = if (isWrongDirection) Color(0xFFD32F2F) else TextPrimary

    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(modifier = Modifier.clickable { onHistoryClick() }, verticalAlignment = Alignment.CenterVertically) {
                Text("Hành trình của bạn", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.width(4.dp))
                Text(">", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
            }
            TextButton(onClick = onQuickUpdateClick) { Text("+ Cập nhật", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppPrimary) }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = AppSurface,
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                // Đổi icon nếu đang đi lùi
                Text(if (isWrongDirection) "⚠️" else "🎯", fontSize = 32.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (weight > 0f) diffText else "Chưa có dữ liệu cân nặng",
                    fontWeight = FontWeight.Bold,
                    color = if (weight > 0f) statusColor else TextPrimary,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (weight > 0f) "Cân nặng hiện tại: $weight kg" else "Hãy cập nhật chỉ số để xem tiến trình",
                    color = TextSecondary,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                LinearWeightProgressBar(
                    initialWeight = startW,
                    currentWeight = weight,
                    targetWeight = targetWeight,
                    isWrongDirection = isWrongDirection // Truyền trạng thái xuống thanh Bar
                )
            }
        }
    }
}

// ==========================================
// ĐÃ SỬA: BIỂU ĐỒ CHUYỂN ĐỎ VÀ VỀ 0 NẾU ĐI LÙI
// ==========================================
@Composable
fun LinearWeightProgressBar(
    initialWeight: Float,
    currentWeight: Float,
    targetWeight: Float,
    isWrongDirection: Boolean
) {
    val startW = if (initialWeight > 0f) initialWeight else currentWeight
    val targetW = if (targetWeight > 0f) targetWeight else startW

    val isLosingGoal = targetW < startW
    val totalDistance = kotlin.math.abs(targetW - startW)

    // Nếu đi sai hướng, cho vạch tiến trình về 0. Nếu đi đúng, tính toán chính xác phần trăm.
    val currentProgress = if (isWrongDirection) {
        0f
    } else if (totalDistance > 0f) {
        val traveled = if (isLosingGoal) startW - currentWeight else currentWeight - startW
        (traveled / totalDistance).coerceIn(0f, 1f)
    } else {
        0f
    }

    // Đổi màu thanh tiến trình
    val barColor = if (isWrongDirection) Color(0xFFD32F2F) else AppPrimary
    val bgColor = if (isWrongDirection) Color(0xFFFFEBEE) else AppPrimaryLight.copy(alpha = 0.3f)

    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)).background(bgColor)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(currentProgress).fillMaxHeight().clip(RoundedCornerShape(6.dp)).background(barColor)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "$startW kg", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
            Text(text = if (targetWeight > 0f) "$targetWeight kg" else "Mục tiêu", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
        }
    }
}

@Composable
fun NutritionGoalsSection(weight: Float, goals: NutritionGoals, onCustomGoalClick: () -> Unit) {
    val hasData = weight > 0f
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
        Text("Mục tiêu dinh dưỡng & đa lượng", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = AppSurface,
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Chỉ số BMR (Trao đổi chất cơ bản):", fontSize = 13.sp, color = TextSecondary)
                    Text(
                        text = if (hasData) "${goals.bmr.toInt()} kcal" else "---",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppPrimary
                    )
                }

                HorizontalDivider(color = TextSecondary.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(100.dp), contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidth = 12.dp.toPx()
                            if (hasData) {
                                drawArc(color = CarbsColor, startAngle = -90f, sweepAngle = 180f, useCenter = false, style = Stroke(strokeWidth))
                                drawArc(color = ProteinColor, startAngle = 90f, sweepAngle = 90f, useCenter = false, style = Stroke(strokeWidth))
                                drawArc(color = FatColor, startAngle = 162f, sweepAngle = 108f, useCenter = false, style = Stroke(strokeWidth))
                            } else {
                                drawArc(color = TextSecondary.copy(alpha = 0.2f), startAngle = 0f, sweepAngle = 360f, useCenter = false, style = Stroke(strokeWidth))
                            }
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🔥", fontSize = 14.sp)
                            Text(if (hasData) "${goals.targetCalories.toInt()}" else "0", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("kcal", fontSize = 10.sp, color = TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.width(24.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        MacroRow(icon = "⚡", label = "Chất đạm", value = if (hasData) "${goals.proteinPercent.toInt()}% (${goals.proteinGrams}g)" else "0g", color = ProteinColor)
                        MacroRow(icon = "💧", label = "Đường bột", value = if (hasData) "${goals.carbsPercent.toInt()}% (${goals.carbsGrams}g)" else "0g", color = CarbsColor)
                        MacroRow(icon = "🛢️", label = "Chất béo", value = if (hasData) "${goals.fatPercent.toInt()}% (${goals.fatGrams}g)" else "0g", color = FatColor)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onCustomGoalClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AppBackground),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Tuỳ chỉnh mục tiêu", color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun MacroRow(icon: String, label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, fontSize = 14.sp, color = TextSecondary)
        }
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
    }
}