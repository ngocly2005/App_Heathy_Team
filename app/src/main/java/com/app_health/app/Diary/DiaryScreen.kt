package com.app_health.app.feature.home.components.Diary

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app_health.app.core.designsystem.components.AppBottomNavigation
import com.app_health.app.core.designsystem.components.BottomNavRoute
import com.app_health.app.feature.home.components.Home.MacrosCard
import com.app_health.app.ui.theme.*
import java.time.LocalDate

// Import Utils và Model
import com.app_health.app.core.utils.toFriendlyString
import com.app_health.app.feature.home.model.FoodItem
import com.app_health.app.feature.home.model.FoodDatabase
import com.app_health.app.feature.home.components.FoodSelectContent
import com.app_health.app.feature.home.components.model.HealthViewModel

@Composable
fun DiaryScreen(
    viewModel: HealthViewModel,
    currentRoute: BottomNavRoute = BottomNavRoute.DIARY,
    onNavigate: (BottomNavRoute) -> Unit = {}
) {
    // 1. TÁI SỬ DỤNG STATE TỪ VIEWMODEL (Không cần tự tính toán nữa)
    val uiState by viewModel.homeUiState.collectAsState()
    val nutritionGoals = uiState.nutritionGoals

    val timeSlots = (0..23).map { hour ->
        String.format("%02d:00", hour)
    }

    val today = remember { LocalDate.now() }
    var selectedDate by remember { mutableStateOf(today) }

    val dateText = selectedDate.toFriendlyString()
    val isPastDate = selectedDate.isBefore(today)

    var selectedTimeSlotForAdd by remember { mutableStateOf<String?>(null) }

    val loggedFoodMap by viewModel.loggedFoodMap.collectAsState()
    val recentFoods by viewModel.recentFoods.collectAsState()

    if (selectedTimeSlotForAdd != null) {
        FoodSelectContent(
            foodList = FoodDatabase.allFoods,
            customFoods = viewModel.customFoods,
            recentFoods = recentFoods,
            favoriteFoods = viewModel.favoriteFoods,
            onToggleFavorite = { viewModel.toggleFavorite(it) },
            onSaveCustomFood = { newFood ->
                viewModel.addCustomFood(newFood)
            },
            onBack = { selectedTimeSlotForAdd = null },
            onFoodClick = { food ->
                val dailyKey = "${selectedDate}_${selectedTimeSlotForAdd!!}"
                viewModel.addFoodToTimeSlot(dailyKey, food)
                selectedTimeSlotForAdd = null
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
            },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Thanh chọn ngày
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { selectedDate = selectedDate.minusDays(1) },
                            enabled = true,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Previous", tint = TextPrimary)
                        }

                        Text(
                            text = dateText,
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.widthIn(min = 110.dp),
                            textAlign = TextAlign.Center
                        )

                        IconButton(
                            onClick = { selectedDate = selectedDate.plusDays(1) },
                            enabled = selectedDate.isBefore(today),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Next Day",
                                tint = if (selectedDate.isBefore(today)) TextPrimary else TextSecondary.copy(alpha = 0.5f)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    val dailyFoods = timeSlots.flatMap { time ->
                        loggedFoodMap["${selectedDate}_$time"] ?: emptyList()
                    }

                    val totalPro = dailyFoods.sumOf { it.protein }
                    val totalCarbs = dailyFoods.sumOf { it.carbs }
                    val totalFat = dailyFoods.sumOf { it.fat }

                    MacrosCard(
                        protein = totalPro,
                        carbs = totalCarbs,
                        fat = totalFat,
                        goals = nutritionGoals // Truyền thẳng dữ liệu đã tính từ ViewModel
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(timeSlots) { time ->
                        val dailyKey = "${selectedDate}_$time"
                        val foodsInThisSlot = loggedFoodMap[dailyKey] ?: emptyList()

                        TimeSlotRow(
                            time = time,
                            foods = foodsInThisSlot,
                            isPastDate = isPastDate,
                            onAddClicked = { selectedTimeSlotForAdd = time },
                            onDeleteClicked = { food ->
                                viewModel.removeFoodFromTimeSlot(dailyKey, food)
                            }
                        )
                    }
                }
            }
        }
    }
}

// CÁC HÀM COMPOSABLE Ở DƯỚI (TimeSlotRow, LoggedFoodCard) GIỮ NGUYÊN NHƯ CODE CŨ CỦA BẠN...
@Composable
fun TimeSlotRow(
    time: String,
    foods: List<FoodItem>,
    isPastDate: Boolean,
    onAddClicked: () -> Unit,
    onDeleteClicked: (FoodItem) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = AppSurface,
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = time,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (foods.isNotEmpty()) {
                        val totalCal = foods.sumOf { it.calories }
                        val totalProtein = foods.sumOf { it.protein }
                        val totalCarbs = foods.sumOf { it.carbs }
                        val totalFat = foods.sumOf { it.fat }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("🔥 $totalCal cal", fontSize = 12.sp, color = TextSecondary)
                            Text("⚡ ${totalProtein}g", fontSize = 12.sp, color = ProteinColor)
                            Text("💧 ${totalCarbs}g", fontSize = 12.sp, color = CarbsColor)
                            Text("🛢️ ${totalFat}g", fontSize = 12.sp, color = FatColor)
                        }
                    }
                }

                if (!isPastDate) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(AppPrimaryLight)
                            .clickable { onAddClicked() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Item",
                            tint = AppPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        foods.forEach { food ->
            LoggedFoodCard(
                food = food,
                isPastDate = isPastDate,
                onDelete = { onDeleteClicked(food) }
            )
        }
    }
}

@Composable
fun LoggedFoodCard(food: FoodItem, isPastDate: Boolean, onDelete: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp),
        color = AppSurface,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AppBackground),
                contentAlignment = Alignment.Center
            ) {
                Text("🍲", fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = food.name,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Khẩu phần • ${food.calories} cal",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("⚡ ${food.protein}g", fontSize = 11.sp, color = ProteinColor)
                    Text("💧 ${food.carbs}g", fontSize = 11.sp, color = CarbsColor)
                    Text("🛢️ ${food.fat}g", fontSize = 11.sp, color = FatColor)
                }
            }

            if (!isPastDate) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Delete Food",
                        tint = TextSecondary.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}