package com.app_health.app.feature.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app_health.app.feature.home.components.Diary.CreateCustomFoodScreen
import com.app_health.app.feature.home.model.FoodItem
import com.app_health.app.feature.home.model.FoodDatabase
import com.app_health.app.feature.home.components.model.HealthViewModel
import com.app_health.app.ui.theme.*
import java.time.LocalDate

// 1. Stateful Composable
@Composable
fun FoodSelectScreen(
    viewModel: HealthViewModel,
    timeSlot: String,
    onBack: () -> Unit
) {
    val recentFoods by viewModel.recentFoods.collectAsState()

    FoodSelectContent(
        foodList = FoodDatabase.allFoods,
        customFoods = viewModel.customFoods,
        recentFoods = recentFoods,
        onFoodClick = { food ->
            val today = LocalDate.now()
            val dailyKey = "${today}_$timeSlot"
            viewModel.addFoodToTimeSlot(dailyKey, food)
            onBack()
        },
        onSaveCustomFood = { newFood ->
            viewModel.addCustomFood(newFood)
        },
        onBack = onBack
    )
}

// 2. Stateless Composable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodSelectContent(
    foodList: List<FoodItem>,
    customFoods: List<FoodItem>,
    recentFoods: List<FoodItem>,
    onSaveCustomFood: (FoodItem) -> Unit,
    onFoodClick: (FoodItem) -> Unit,
    onBack: () -> Unit
) {
    // Biến trạng thái quản lý việc hiển thị màn hình
    var selectedFood by remember { mutableStateOf<FoodItem?>(null) }
    var showCreateFoodScreen by remember { mutableStateOf(false) }

    // [QUAN TRỌNG] Gộp danh sách: Món Gần Đây + Món Tự Tạo + Tất cả CSDL.
    // Lệnh distinctBy { it.name } giúp loại bỏ những món bị trùng lặp, đảm bảo
    // món nào vừa tương tác sẽ ngoi lên đầu, các món mặc định khác xếp phía sau.
    val combinedList = remember(recentFoods, customFoods, foodList) {
        (recentFoods + customFoods + foodList).distinctBy { it.name }
    }

    // XỬ LÝ ĐIỀU HƯỚNG CÁC MÀN HÌNH CHỒNG LÊN NHAU
    if (showCreateFoodScreen) {
        CreateCustomFoodScreen(
            onBack = { showCreateFoodScreen = false },
            onSaveClick = { newFood ->
                onSaveCustomFood(newFood)
                showCreateFoodScreen = false
            }
        )
    } else if (selectedFood != null) {
        FoodDetailScreen(
            food = selectedFood!!,
            onBack = { selectedFood = null },
            onAdd = { adjustedFood ->
                onFoodClick(adjustedFood)
                selectedFood = null
            }
        )
    } else {
        // GIAO DIỆN CHÍNH: THANH TÌM KIẾM VÀ CÁC TAB
        var searchQuery by remember { mutableStateOf("") }
        var selectedTab by remember { mutableStateOf(0) }
        val tabs = listOf("Gần đây", "Tạo bởi tôi", "Yêu thích")

        // Lọc danh sách dựa trên danh sách ĐÃ GỘP (combinedList)
        val filteredFoods = remember(searchQuery, combinedList) {
            if (searchQuery.isBlank()) {
                combinedList
            } else {
                combinedList.filter { food ->
                    food.name.contains(searchQuery, ignoreCase = true)
                }
            }
        }

        Scaffold(
            topBar = {
                Column(modifier = Modifier.background(AppBackground)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Hôm nay • 12:00",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown",
                                tint = TextPrimary
                            )
                        }
                        IconButton(onClick = { /* Thông tin */ }) {
                            Icon(Icons.Default.Info, contentDescription = "Info", tint = TextPrimary)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .shadow(2.dp, RoundedCornerShape(25.dp)),
                            placeholder = { Text("Tìm thực phẩm hoặc món ăn", color = TextSecondary) },
                            leadingIcon = { Icon(Icons.Default.Search, tint = TextSecondary, contentDescription = null) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = AppSurface,
                                unfocusedContainerColor = AppSurface,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(25.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }

                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = AppBackground,
                        contentColor = AppPrimary,
                        indicator = { tabPositions ->
                            TabRowDefaults.Indicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = AppPrimary
                            )
                        }
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = {
                                    Text(
                                        text = title,
                                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedTab == index) AppPrimary else TextSecondary
                                    )
                                }
                            )
                        }
                    }
                }
            },
            containerColor = AppBackground
        ) { paddingValues ->

            // XỬ LÝ HIỂN THỊ NỘI DUNG DỰA TRÊN TAB ĐANG CHỌN
            if (searchQuery.isNotBlank()) {
                // ĐANG TÌM KIẾM: Hiển thị kết quả tìm kiếm chung
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    if (filteredFoods.isEmpty()) {
                        item {
                            Text(
                                text = "Không tìm thấy món ăn nào",
                                color = TextSecondary,
                                modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        items(filteredFoods) { food ->
                            FoodItemCard(
                                food = food,
                                onItemClick = { selectedFood = food },
                                onAddClick = { onFoodClick(food) }
                            )
                        }
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }

            } else if (selectedTab == 1) {
                // TAB "TẠO BỞI TÔI"
                if (customFoods.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Bạn chưa tạo món ăn cá nhân nào.",
                            color = TextSecondary,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showCreateFoodScreen = true },
                            colors = ButtonDefaults.buttonColors(containerColor = AppPrimary),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Tạo món ăn mới", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { showCreateFoodScreen = true },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = AppPrimary)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Tạo thêm món ăn khác", fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        items(customFoods) { food ->
                            FoodItemCard(
                                food = food,
                                onItemClick = { selectedFood = food },
                                onAddClick = { onFoodClick(food) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }

            } else {
                // TAB "GẦN ĐÂY" VÀ "YÊU THÍCH"
                // Luôn hiển thị danh sách đầy đủ (có Gần Đây/Tạo Mới trên cùng, Default ở dưới)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    if (filteredFoods.isEmpty()) {
                        item {
                            Text(
                                text = "Không tìm thấy món ăn nào",
                                color = TextSecondary,
                                modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        items(filteredFoods) { food ->
                            FoodItemCard(
                                food = food,
                                onItemClick = { selectedFood = food },
                                onAddClick = { onFoodClick(food) }
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
fun FoodItemCard(
    food: FoodItem,
    onItemClick: () -> Unit,
    onAddClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
            .clickable { onItemClick() },
        color = AppSurface,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AppBackground),
                contentAlignment = Alignment.Center
            ) {
                Text("🍲", fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = food.name,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Verified",
                        tint = AppPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Khẩu phần • ${food.calories} cal",
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("⚡ ${food.protein}g", fontSize = 11.sp, color = ProteinColor)
                    Text("💧 ${food.carbs}g", fontSize = 11.sp, color = CarbsColor)
                    Text("🛢️ ${food.fat}g", fontSize = 11.sp, color = FatColor)
                }
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(AppPrimaryLight)
                    .clickable { onAddClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = AppPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FoodSelectScreenPreview() {
    FoodSelectContent(
        foodList = FoodDatabase.allFoods.take(5),
        customFoods = emptyList(),
        recentFoods = emptyList(),
        onSaveCustomFood = {},
        onFoodClick = {},
        onBack = {}
    )
}