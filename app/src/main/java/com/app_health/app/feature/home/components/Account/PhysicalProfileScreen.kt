package com.app_health.app.feature.home.components.Account

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app_health.app.core.utils.WeightManager
import com.app_health.app.feature.home.components.LoginLogout.CurrentUserManager
import com.app_health.app.feature.home.components.LoginLogout.DataAuthManager
import com.app_health.app.feature.home.components.model.HealthViewModel
import com.app_health.app.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhysicalProfileScreen(
    viewModel: HealthViewModel, // ĐÃ THÊM: Truyền ViewModel
    onBackClick: () -> Unit = {},
    onNavigateToGoalSetup: () -> Unit = {}
) {
    val context = LocalContext.current
    val currentUserManager = remember { CurrentUserManager(context) }
    val currentEmail by currentUserManager.currentEmailFlow.collectAsState(initial = null)

    // Giữ Manager để lấy các thông số phụ (tên, số đo vòng eo...)
    val authManager = remember(currentEmail) { DataAuthManager(context, currentEmail) }
    val weightManager = remember(currentEmail) { WeightManager(context, currentEmail) }

    // ==========================================================
    // LẤY DỮ LIỆU CỐT LÕI TỪ VIEWMODEL CHUNG
    // ==========================================================
    val uiState by viewModel.homeUiState.collectAsState()
    val profile = uiState.profile
    val nutritionGoals = uiState.nutritionGoals // Lấy thẳng Calo mục tiêu đã tính

    // Các thông số phụ lấy từ DataStore
    val name by authManager.savedName.collectAsState(initial = "Người dùng")
    val goalType by authManager.savedGoalType.collectAsState(initial = "")
    val targetWeight by weightManager.targetWeightFlow.collectAsState(initial = 0f)

    val waist by authManager.savedWaist.collectAsState(initial = 0f)
    val chest by authManager.savedChest.collectAsState(initial = 0f)
    val hips by authManager.savedHips.collectAsState(initial = 0f)

    // Xác định phân loại BMI nhanh (dựa trên BMI đã tính sẵn trong ViewModel)
    val bmiStatus = when {
        profile.bmi == 0f -> "Chưa có đủ dữ liệu"
        profile.bmi < 18.5f -> "Thiếu cân"
        profile.bmi < 25f -> "Bình thường (Khỏe mạnh)"
        profile.bmi < 30f -> "Thừa cân"
        else -> "Béo phì"
    }

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            TopAppBar(
                title = { Text("Hồ sơ thể chất", fontWeight = FontWeight.Bold, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBackground)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // --- 1. Thông tin cơ bản ---
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Thông tin cơ bản", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextSecondary, modifier = Modifier.size(16.dp))
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = AppSurface,
                    shadowElevation = 1.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("NICKNAME", fontSize = 11.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(name ?: "Người dùng", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = TextPrimary)

                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = TextSecondary.copy(alpha = 0.1f))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            InfoColumn(label = "GIỚI TÍNH", value = profile.gender)
                            InfoColumn(label = "TUỔI", value = if (profile.age > 0) "${profile.age}" else "---")
                            InfoColumn(label = "CHIỀU CAO", value = if (profile.height > 0f) "${profile.height.toInt()} cm" else "---")
                        }
                    }
                }
            }

            // --- 2. Số đo cơ thể ---
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Số đo cơ thể", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = AppSurface,
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        InfoColumn(label = "VÒNG EO", value = if (waist > 0f) "${waist.toInt()} cm" else "---")
                        InfoColumn(label = "VÒNG NGỰC", value = if (chest > 0f) "${chest.toInt()} cm" else "---")
                        InfoColumn(label = "VÒNG MÔNG", value = if (hips > 0f) "${hips.toInt()} cm" else "---")
                    }
                }
            }

            // --- 3. Mục tiêu cân nặng & Chỉ số ---
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Mục tiêu cân nặng & Chỉ số", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.Info, contentDescription = "Info", tint = TextSecondary, modifier = Modifier.size(16.dp))
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = AppSurface,
                    shadowElevation = 1.dp
                ) {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(if (goalType.isNotBlank()) goalType else "Chưa thiết lập", fontSize = 15.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(if (profile.weight > 0f) "${profile.weight} kg" else "---", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "To", tint = TextSecondary, modifier = Modifier.padding(horizontal = 8.dp).size(16.dp))
                                Text(if (targetWeight > 0f) "$targetWeight kg" else "---", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                        }

                        HorizontalDivider(color = TextSecondary.copy(alpha = 0.1f))
                        GoalRowStatic(label = "Cường độ vận động", value = if (profile.activityLevel.isNotBlank()) profile.activityLevel else "Chưa thiết lập")

                        HorizontalDivider(color = TextSecondary.copy(alpha = 0.1f), modifier = Modifier.padding(horizontal = 16.dp))
                        GoalRowStatic(label = "Chỉ số BMI của bạn", value = if (profile.bmi > 0f) String.format(Locale.getDefault(), "%.1f (%s)", profile.bmi, bmiStatus) else "Chưa đủ dữ liệu")

                        HorizontalDivider(color = TextSecondary.copy(alpha = 0.1f), modifier = Modifier.padding(horizontal = 16.dp))
                        GoalRowStatic(
                            label = "Calo mục tiêu hàng ngày",
                            value = if (profile.isProfileUpdated && profile.activityLevel.isNotBlank()) "${nutritionGoals.targetCalories.toInt()} kcal" else "---"
                        )
                    }
                }
            }

            // --- 4. Nút Thiết lập mục tiêu mới ---
            Button(
                onClick = onNavigateToGoalSetup,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppPrimaryLight),
                shape = RoundedCornerShape(25.dp)
            ) {
                Icon(Icons.Default.Sync, contentDescription = "Reset", tint = AppPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Thiết lập mục tiêu mới", color = AppPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ==========================================
// CÁC HÀM THÀNH PHẦN (COMPOSABLES PHỤ TRỢ)
// ==========================================

@Composable
fun InfoColumn(label: String, value: String) {
    Column {
        Text(label, fontSize = 11.sp, color = TextSecondary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
    }
}

@Composable
fun GoalRowStatic(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 14.sp, color = TextSecondary)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
    }
}