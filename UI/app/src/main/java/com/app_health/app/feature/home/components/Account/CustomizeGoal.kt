package com.app_health.app.feature.home.components.Account

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app_health.app.feature.home.components.LoginLogout.CurrentUserManager
import com.app_health.app.feature.home.components.LoginLogout.DataAuthManager
import com.app_health.app.feature.home.components.model.HealthViewModel
import com.app_health.app.ui.theme.*
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomizeGoal(
    viewModel: HealthViewModel, // Truyền ViewModel từ AppNavigation vào đây
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val currentUserManager = remember { CurrentUserManager(context) }
    val currentEmail by currentUserManager.currentEmailFlow.collectAsState(initial = null)
    val authManager = remember(currentEmail) { DataAuthManager(context, currentEmail) }
    val coroutineScope = rememberCoroutineScope()

    // LẤY TOÀN BỘ DỮ LIỆU TỪ UI STATE ĐỂ KHÔNG PHẢI TÍNH LẠI
    val uiState by viewModel.homeUiState.collectAsState()
    val tdee = uiState.nutritionGoals.tdee // Lấy TDEE (tổng năng lượng tiêu hao) chuẩn

    val currentCustomCal by authManager.savedCustomCalories.collectAsState(initial = 0f)
    val currentProteinPct by authManager.savedProteinPercent.collectAsState(initial = 0f)
    val currentCarbsPct by authManager.savedCarbsPercent.collectAsState(initial = 0f)
    val currentFatPct by authManager.savedFatPercent.collectAsState(initial = 0f)

    var calorieInput by remember { mutableStateOf("") }
    var proteinPct by remember { mutableFloatStateOf(25f) }
    var carbsPct by remember { mutableFloatStateOf(50f) }
    var fatPct by remember { mutableFloatStateOf(25f) }
    var isMacroInitialized by remember { mutableStateOf(false) }

    // Đồng bộ thông minh
    LaunchedEffect(currentCustomCal, tdee) {
        val targetCalToDisplay = if (currentCustomCal > 0f) currentCustomCal else tdee
        calorieInput = targetCalToDisplay.toInt().toString()
    }

    LaunchedEffect(currentProteinPct, currentCarbsPct, currentFatPct) {
        if (!isMacroInitialized) {
            proteinPct = if (currentProteinPct > 0f) currentProteinPct else 25f
            carbsPct = if (currentCarbsPct > 0f) currentCarbsPct else 50f
            fatPct = if (currentFatPct > 0f) currentFatPct else 25f
            isMacroInitialized = true
        }
    }

    val totalPct = proteinPct + carbsPct + fatPct
    val isPctValid = kotlin.math.abs(totalPct - 100f) < 0.1f

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            TopAppBar(
                title = { Text("Tuỳ chỉnh mục tiêu", fontWeight = FontWeight.Bold, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBackground)
            )
        },
        bottomBar = {
            Surface(modifier = Modifier.fillMaxWidth(), color = AppSurface, shadowElevation = 8.dp) {
                Box(modifier = Modifier.padding(16.dp)) {
                    Button(
                        onClick = {
                            val parsedCal = calorieInput.toFloatOrNull() ?: tdee
                            coroutineScope.launch {
                                authManager.saveCustomNutritionGoals(
                                    calories = parsedCal,
                                    proteinPct = proteinPct,
                                    carbsPct = carbsPct,
                                    fatPct = fatPct
                                )
                                viewModel.loadDataForUser(currentEmail ?: "")

                                onBackClick()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppPrimary),
                        shape = RoundedCornerShape(12.dp),
                        enabled = isPctValid
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("🔥 Calo mục tiêu hằng ngày", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                OutlinedTextField(
                    value = calorieInput,
                    onValueChange = { calorieInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Calo (kcal)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            HorizontalDivider(color = TextSecondary.copy(alpha = 0.2f))

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("⚖ Tỷ lệ Đa lượng (Macro)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(
                        text = "Tổng: ${String.format(Locale.getDefault(), "%.0f", totalPct)}%",
                        color = if (isPctValid) Color(0xFF4CAF50) else Color.Red,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                if (!isPctValid) {
                    Text(
                        text = "⚠ Tổng tỷ lệ phần trăm các chất phải bằng đúng 100%.",
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                }

                MacroSlider(label = "Chất đạm (Protein)", percent = proteinPct, color = ProteinColor, onValueChange = { proteinPct = it })
                MacroSlider(label = "Đường bột (Carbs)", percent = carbsPct, color = CarbsColor, onValueChange = { carbsPct = it })
                MacroSlider(label = "Chất béo (Fat)", percent = fatPct, color = FatColor, onValueChange = { fatPct = it })
            }
        }
    }
}

@Composable
fun MacroSlider(
    label: String,
    percent: Float,
    color: Color,
    onValueChange: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
            Text("${percent.toInt()}%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Slider(
            value = percent,
            onValueChange = onValueChange,
            valueRange = 0f..100f,
            steps = 99
        )
    }
}