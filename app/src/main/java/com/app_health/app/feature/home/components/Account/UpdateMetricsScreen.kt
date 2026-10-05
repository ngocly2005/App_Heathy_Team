package com.app_health.app.feature.home.components.Account

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app_health.app.core.utils.WeightManager
import com.app_health.app.feature.home.components.LoginLogout.CurrentUserManager
import com.app_health.app.feature.home.components.LoginLogout.DataAuthManager
import com.app_health.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateMetricsScreen(
    onBackClick: () -> Unit = {},
    onSaveClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val currentUserManager = remember { CurrentUserManager(context) }
    val currentEmail by currentUserManager.currentEmailFlow.collectAsState(initial = null)

    val authManager = remember(currentEmail) { DataAuthManager(context, currentEmail) }
    val weightManager = remember { WeightManager(context) }
    val coroutineScope = rememberCoroutineScope()

    val currentAge by authManager.savedAge.collectAsState(initial = 0)
    val currentHeight by authManager.savedHeight.collectAsState(initial = 0f)
    val currentWeight by weightManager.currentWeightFlow.collectAsState(initial = 0f)
    val currentGender by authManager.savedGender.collectAsState(initial = "Nam")
    val currentWaist by authManager.savedWaist.collectAsState(initial = 0f)
    val currentChest by authManager.savedChest.collectAsState(initial = 0f)
    val currentHips by authManager.savedHips.collectAsState(initial = 0f)
    val currentActivity by authManager.savedActivityLevel.collectAsState(initial = "Vận động nhẹ")

    var age by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf("Nam") }
    var waist by remember { mutableStateOf("") }
    var chest by remember { mutableStateOf("") }
    var hips by remember { mutableStateOf("") }

    LaunchedEffect(currentAge, currentHeight, currentWeight, currentGender, currentWaist, currentChest, currentHips) {
        if (age.isEmpty() && currentAge > 0) age = currentAge.toString()
        if (height.isEmpty() && currentHeight > 0f) height = currentHeight.toString()
        if (weight.isEmpty() && currentWeight > 0f) weight = currentWeight.toString()
        if (currentGender.isNotBlank()) selectedGender = currentGender
        if (waist.isEmpty() && currentWaist > 0f) waist = currentWaist.toString()
        if (chest.isEmpty() && currentChest > 0f) chest = currentChest.toString()
        if (hips.isEmpty() && currentHips > 0f) hips = currentHips.toString()
    }

    val genders = listOf("Nam", "Nữ", "Khác")

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            TopAppBar(
                title = { Text("Cập nhật chỉ số", fontWeight = FontWeight.Bold, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBackground)
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = AppSurface,
                shadowElevation = 8.dp
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    Button(
                        onClick = {
                            val parsedAge = age.toIntOrNull() ?: 0
                            val parsedHeight = height.toFloatOrNull() ?: 0f
                            val parsedWeight = weight.toFloatOrNull() ?: 0f
                            val parsedWaist = waist.toFloatOrNull() ?: 0f
                            val parsedChest = chest.toFloatOrNull() ?: 0f
                            val parsedHips = hips.toFloatOrNull() ?: 0f

                            coroutineScope.launch {
                                authManager.saveFullMetrics(
                                    age = parsedAge,
                                    height = parsedHeight,
                                    weight = parsedWeight,
                                    gender = selectedGender,
                                    waist = parsedWaist,
                                    chest = parsedChest,
                                    hips = parsedHips,
                                    activityLevel = currentActivity
                                )

                                if (parsedWeight > 0f) {
                                    weightManager.saveWeightMetrics(parsedWeight)
                                }

                                // Reset toàn bộ Calo và Macro về 0 để hệ thống tự động tính lại theo công thức chuẩn mới
                                authManager.saveCustomNutritionGoals(0f, 0f, 0f, 0f)

                                onSaveClick()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Lưu thay đổi", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle(icon = "👤", title = "Thông tin cơ bản")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = age,
                        onValueChange = { age = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("Tuổi", fontSize = 12.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppPrimary,
                            unfocusedBorderColor = TextSecondary.copy(alpha = 0.2f),
                            focusedContainerColor = AppSurface,
                            unfocusedContainerColor = AppSurface
                        ),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = height,
                        onValueChange = { height = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("Chiều cao", fontSize = 12.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppPrimary,
                            unfocusedBorderColor = TextSecondary.copy(alpha = 0.2f),
                            focusedContainerColor = AppSurface,
                            unfocusedContainerColor = AppSurface
                        ),
                        trailingIcon = { Text("cm", fontSize = 12.sp, color = TextSecondary, modifier = Modifier.padding(end = 8.dp)) },
                        singleLine = true
                    )
                }

                Text("Giới tính", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    genders.forEach { gender ->
                        val isSelected = selectedGender == gender
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedGender = gender }
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) AppPrimary else TextSecondary.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(10.dp)
                                ),
                            color = if (isSelected) AppPrimaryLight.copy(alpha = 0.4f) else AppSurface
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = gender,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) AppPrimary else TextPrimary,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle(icon = "⚖️", title = "Cân nặng hiện tại")
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppPrimary,
                        unfocusedBorderColor = TextSecondary.copy(alpha = 0.2f),
                        focusedContainerColor = AppSurface,
                        unfocusedContainerColor = AppSurface
                    ),
                    trailingIcon = { Text("kg", color = TextSecondary, modifier = Modifier.padding(end = 16.dp)) },
                    singleLine = true
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle(icon = "📏", title = "Số đo cơ thể (Tùy chọn)")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MeasurementInput(modifier = Modifier.weight(1f), label = "Vòng eo", value = waist, onValueChange = { waist = it })
                    MeasurementInput(modifier = Modifier.weight(1f), label = "Vòng ngực", value = chest, onValueChange = { chest = it })
                    MeasurementInput(modifier = Modifier.weight(1f), label = "Vòng mông", value = hips, onValueChange = { hips = it })
                }
            }
        }
    }
}

// ==========================================
// CÁC HÀM PHỤ TRỢ (ĐẶT Ở CUỐI TỆP)
// ==========================================

@Composable
fun SectionTitle(icon: String, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(icon, fontSize = 20.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
    }
}

@Composable
fun MeasurementInput(
    modifier: Modifier,
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = { Text(label, fontSize = 12.sp, color = TextSecondary) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AppPrimary,
            unfocusedBorderColor = TextSecondary.copy(alpha = 0.2f),
            focusedContainerColor = AppSurface,
            unfocusedContainerColor = AppSurface
        ),
        trailingIcon = { Text("cm", fontSize = 12.sp, color = TextSecondary, modifier = Modifier.padding(end = 8.dp)) },
        singleLine = true
    )
}