package com.app_health.app.feature.home.components.Account

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app_health.app.core.utils.WeightManager
import com.app_health.app.feature.home.components.LoginLogout.DataAuthManager
import com.app_health.app.feature.home.components.model.HealthViewModel
import com.app_health.app.ui.theme.*
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalSetupViewModel(
    viewModel: HealthViewModel,
    authManager: DataAuthManager,
    onBackToProfile: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Sắp xếp lại thứ tự 3 dòng này như sau:
    val currentUserManager = remember { com.app_health.app.feature.home.components.LoginLogout.CurrentUserManager(context) }
    val currentEmail by currentUserManager.currentEmailFlow.collectAsState(initial = null)

    if (currentEmail == null) {
        return // Không làm gì cả, chờ đến khi có email
    }
    // Gắn email vào WeightManager
    val weightManager = remember(currentEmail) { WeightManager(context, currentEmail) }

    var isSummaryScreen by remember { mutableStateOf(false) }

    val uiState by viewModel.homeUiState.collectAsState()
    val profile = uiState.profile

    val savedGoal by authManager.savedGoalType.collectAsState(initial = "Giảm cân")
    val savedTargetW by weightManager.targetWeightFlow.collectAsState(initial = 0f)

    var goalType by remember { mutableStateOf("Giảm cân") }
    var currentWeight by remember { mutableStateOf("") }
    var targetWeight by remember { mutableStateOf("") }
    var activityLevel by remember { mutableStateOf("Vận động nhẹ nhàng") }
    var weeklyKg by remember { mutableStateOf(0.5f) }

    LaunchedEffect(profile.weight) {
        if (profile.weight > 0f && currentWeight.isEmpty()) {
            currentWeight = profile.weight.toString()
        }
    }

    LaunchedEffect(savedTargetW) {
        if (savedTargetW > 0f && targetWeight.isEmpty()) {
            targetWeight = savedTargetW.toString()
        }
    }

    LaunchedEffect(savedGoal, profile.activityLevel) {
        if (savedGoal.isNotBlank()) goalType = savedGoal
        if (profile.activityLevel.isNotBlank()) activityLevel = profile.activityLevel
    }

    val curW = currentWeight.toFloatOrNull() ?: profile.weight
    val tarW = targetWeight.toFloatOrNull() ?: savedTargetW

    val availableOptions = remember(curW, tarW) {
        when {
            curW > 0f && tarW > 0f -> {
                when {
                    curW > tarW -> listOf("Giảm cân")
                    curW < tarW -> listOf("Tăng cân")
                    else -> listOf("Duy trì cân nặng")
                }
            }
            else -> listOf("Giảm cân", "Tăng cân", "Duy trì cân nặng")
        }
    }

    LaunchedEffect(availableOptions) {
        if (!availableOptions.contains(goalType)) {
            goalType = availableOptions.first()
        }
    }

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isSummaryScreen) "Tổng hợp mục tiêu" else "Thiết lập mục tiêu",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isSummaryScreen) {
                            isSummaryScreen = false
                        } else {
                            onBackToProfile()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBackground)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                if (!isSummaryScreen) {
                    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {

                        // 1. Nhập thông tin cân nặng hiện tại & mong muốn
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("1. Nhập thông tin cân nặng của bạn", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            OutlinedTextField(
                                value = currentWeight,
                                onValueChange = { currentWeight = it },
                                label = { Text("Cân nặng hiện tại (kg)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = targetWeight,
                                onValueChange = { targetWeight = it },
                                label = { Text("Cân nặng mong muốn (kg)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        // 2. Chọn mục tiêu
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("2. Bạn muốn đạt mục tiêu gì?", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                availableOptions.forEach { option ->
                                    val isSelected = goalType == option
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { goalType = option },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) AppPrimaryLight else AppSurface,
                                        shadowElevation = 1.dp
                                    ) {
                                        Box(modifier = Modifier.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                                            Text(
                                                text = option,
                                                fontSize = 13.sp,
                                                color = if (isSelected) AppPrimary else TextPrimary,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 3. Cường độ tập luyện
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("3. Cường độ tập luyện trong tuần", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            val levels = listOf("Không tập luyện", "Ít vận động", "Vận động nhẹ nhàng", "Chăm chỉ tập luyện", "Rất năng động")
                            levels.forEach { level ->
                                val isSelected = activityLevel == level
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { activityLevel = level },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) AppPrimaryLight else AppSurface
                                ) {
                                    Text(
                                        text = level,
                                        modifier = Modifier.padding(14.dp),
                                        fontSize = 14.sp,
                                        color = if (isSelected) AppPrimary else TextPrimary,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }

                        // 4. Mục tiêu hàng tuần
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("4. Mục tiêu thay đổi mỗi tuần", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.align(Alignment.Start))

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(String.format(Locale.getDefault(), "%.1f kg", weeklyKg), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = AppPrimary)

                            val isUnsafe = weeklyKg > 1.0f
                            Text(
                                text = if (isUnsafe) "Không khuyến nghị (Vượt mức an toàn)" else "Mức khuyến nghị an toàn",
                                color = if (isUnsafe) Color.Red else Color(0xFF4CAF50),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Slider(
                                value = weeklyKg,
                                onValueChange = { weeklyKg = it },
                                valueRange = 0.1f..2.0f
                            )
                        }
                    }
                } else {
                    // --- TRANG 2: MÀN HÌNH TỔNG HỢP ---
                    val finalCurW = currentWeight.toFloatOrNull() ?: profile.weight
                    val finalTarW = targetWeight.toFloatOrNull() ?: savedTargetW
                    val heightM = profile.height / 100f
                    val bmi = if (heightM > 0f) finalCurW / (heightM * heightM) else 0f

                    val (bmiStatus, bmiDescription) = when {
                        bmi < 18.5f -> "Thiếu cân" to "Cân nặng thấp hơn mức tiêu chuẩn."
                        bmi < 25f -> "Bình thường" to "Cân nặng tuyệt vời, hãy tiếp tục duy trì!"
                        bmi < 30f -> "Thừa cân" to "Cân nặng vượt ngưỡng an toàn, nên điều chỉnh chế độ ăn."
                        bmi < 35f -> "Béo phì độ I" to "Cân nặng vượt ngưỡng an toàn, tăng nguy cơ tim mạch."
                        else -> "Béo phì độ II" to "Cần tham khảo ý kiến chuyên gia y tế."
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Wao tổng hợp lại thông tin giúp bạn nha ✨", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = TextPrimary)

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = AppSurface,
                            shadowElevation = 1.dp
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                SummaryRow("🎯 MỤC TIÊU", goalType)
                                HorizontalDivider(color = TextSecondary.copy(alpha = 0.1f))
                                SummaryRow("📍 CÂN NẶNG HIỆN TẠI", "$finalCurW kg")
                                HorizontalDivider(color = TextSecondary.copy(alpha = 0.1f))
                                SummaryRow("📍 CÂN NẶNG MỤC TIÊU", "$finalTarW kg")
                                HorizontalDivider(color = TextSecondary.copy(alpha = 0.1f))
                                SummaryRow("🔥 CƯỜNG ĐỘ TẬP LUYỆN", activityLevel)
                            }
                        }

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = AppSurface,
                            shadowElevation = 1.dp
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("BMI của bạn: ${String.format(Locale.getDefault(), "%.1f", bmi)}", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                                    Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFFF7043)) {
                                        Text(bmiStatus, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Text(bmiDescription, fontSize = 13.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (!isSummaryScreen) {
                        isSummaryScreen = true
                    } else {
                        coroutineScope.launch {
                            val finalCurW = currentWeight.toFloatOrNull() ?: profile.weight
                            val finalTarW = targetWeight.toFloatOrNull() ?: savedTargetW

                            weightManager.setupNewGoal(initialWeight = finalCurW, targetWeight = finalTarW)
                            authManager.savePhysicalGoals(
                                goalType = goalType,
                                targetWeight = finalTarW,
                                activityLevel = activityLevel
                            )

                            // TẢI LẠI DỮ LIỆU ĐỂ ĐỒNG BỘ MÀN HÌNH
                            viewModel.loadDataForUser(currentEmail ?: "")

                            onBackToProfile()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppPrimary),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text(
                    text = if (!isSummaryScreen) "Tiếp tục" else "Hoàn tất",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
    }
}